package fr.axephir.tnd.narrative;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.progression.PlayerProgression;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Evidence of placement, never a guess based on generated loot or opening a chest. Server only. */
public final class PlayerBase {
    public static final int CAPACITY=4096, MIN_UTILITIES=10, SPAWN_RADIUS=64;
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<List<Long>>> POSITIONS=PlayerProgression.TYPES.register("base_utilities",()->AttachmentType.<List<Long>>builder(()->List.of())
            .serialize(Codec.LONG.listOf(0,CAPACITY).fieldOf("positions")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<String>> PLACER=PlayerProgression.TYPES.register("utility_placer",()->AttachmentType.builder(()->"")
            .serialize(Codec.STRING.fieldOf("owner")).build());
    public static void bootstrap(){}
    private static boolean utility(ServerLevel level,BlockPos at,BlockEntity be){
        return be!=null&&(be instanceof Container||level.getCapability(Capabilities.Item.BLOCK,at,null)!=null
                ||level.getCapability(Capabilities.Energy.BLOCK,at,null)!=null||level.getCapability(Capabilities.Fluid.BLOCK,at,null)!=null);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void placed(BlockEvent.EntityPlaceEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer player)||player instanceof net.neoforged.neoforge.common.util.FakePlayer||!player.level().dimension().equals(Level.OVERWORLD))return;
        if(event instanceof BlockEvent.EntityMultiPlaceEvent multi){for(var snapshot:multi.getReplacedBlockSnapshots())record(player,snapshot.getPos());}
        else record(player,event.getPos());
    }
    private static void record(ServerPlayer player,BlockPos at){
        var level=player.level();var be=level.getBlockEntity(at);if(!utility(level,at,be))return;
        be.setData(PLACER,player.getUUID().toString());var positions=new LinkedHashSet<>(player.getData(POSITIONS));
        positions.remove(at.asLong());positions.add(at.asLong());while(positions.size()>CAPACITY)positions.remove(positions.iterator().next());
        player.setData(POSITIONS,List.copyOf(positions));
    }
    public record Site(BlockPos spawn,BlockPos focus,List<BlockPos> targets){public Site{spawn=spawn.immutable();focus=focus.immutable();targets=targets.stream().map(BlockPos::immutable).toList();}}
    private record Evidence(BlockPos at,int importance){}

    /** Checks at most one indexed chunk per tick, including an unloaded base. Never searches generated terrain for containers. */
    public static final class Search {
        private final ServerPlayer player;private final ServerLevel level;private final int radius;private final BlockPos spawn;
        private final Map<Long,List<BlockPos>> chunks=new LinkedHashMap<>();private final List<Evidence> evidence=new ArrayList<>();
        private Iterator<List<BlockPos>> pending;private int checked;private boolean finished;private Site result;
        public Search(ServerPlayer player,int radius){
            this.player=player;this.level=player.level();this.radius=radius;var respawn=player.getRespawnConfig();
            if(!level.dimension().equals(Level.OVERWORLD)||respawn==null||respawn.forced()||!respawn.respawnData().dimension().equals(level.dimension())){spawn=null;finished=true;return;}
            spawn=respawn.respawnData().pos().immutable();
            var near=player.getData(POSITIONS).stream().distinct().map(BlockPos::of)
                    .filter(p->Math.abs(p.getX()-spawn.getX())<=SPAWN_RADIUS&&Math.abs(p.getZ()-spawn.getZ())<=SPAWN_RADIUS&&Math.abs(p.getY()-spawn.getY())<=48)
                    .sorted(Comparator.comparingDouble((BlockPos p)->p.distSqr(spawn)).thenComparingLong(BlockPos::asLong)).toList();
            if(near.size()<MIN_UTILITIES){finished=true;return;}
            for(var at:near)chunks.computeIfAbsent(net.minecraft.world.level.ChunkPos.pack(at),k->new ArrayList<>()).add(at);
            pending=chunks.values().iterator();
        }
        public void step(){
            if(finished)return;
            if(checked==0){
                level.getChunkAt(spawn);
                if(!(level.getBlockState(spawn).getBlock() instanceof BedBlock)||player.getRespawnConfig()==null
                        ||!player.getRespawnConfig().respawnData().pos().equals(spawn)){finished=true;return;}
            }
            var positions=pending.next();level.getChunkAt(positions.getFirst());
            for(var at:positions){
                var be=level.getBlockEntity(at);
                if(be==null||!be.hasData(PLACER)||!be.getData(PLACER).equals(player.getUUID().toString())||!utility(level,at,be))continue;
                boolean unopened=be instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity loot&&loot.getLootTable()!=null;
                int weight=!unopened&&be instanceof Container inventory&&!inventory.isEmpty()?4:be instanceof Container?1:3;
                evidence.add(new Evidence(at,weight));
            }
            checked++;
            if(!pending.hasNext()){choose();finished=true;}
        }
        private void choose(){
            List<Evidence> best=List.of();int bestWeight=-1;double bestDistance=Double.MAX_VALUE;
            // A compact three-dimensional cluster may cross chunk borders; ten scattered outposts are not a base.
            for(var seed:evidence){
                var cluster=evidence.stream().filter(e->Math.abs(e.at.getX()-seed.at.getX())<=24&&Math.abs(e.at.getZ()-seed.at.getZ())<=24
                        &&Math.abs(e.at.getY()-seed.at.getY())<=12&&inFootprint(e.at)).toList();
                if(cluster.size()<MIN_UTILITIES)continue;
                int weight=cluster.stream().mapToInt(Evidence::importance).sum();double distance=seed.at.distSqr(spawn);
                if(weight>bestWeight||weight==bestWeight&&distance<bestDistance){best=cluster;bestWeight=weight;bestDistance=distance;}
            }
            if(best.isEmpty())return;
            var focus=new BlockPos((int)Math.round(best.stream().mapToInt(e->e.at.getX()).average().orElseThrow()),
                    (int)Math.round(best.stream().mapToInt(e->e.at.getY()).average().orElseThrow()),
                    (int)Math.round(best.stream().mapToInt(e->e.at.getZ()).average().orElseThrow()));
            // Eight spaced impacts fit the eight-second shot and favour occupied storage over empty decoys.
            var ordered=new ArrayList<Evidence>();var remaining=new ArrayList<>(best);
            remaining.sort(Comparator.comparingInt(Evidence::importance).reversed().thenComparingDouble(e->e.at.distSqr(focus)).thenComparingLong(e->e.at.asLong()));
            ordered.add(remaining.removeFirst());
            while(!remaining.isEmpty()&&ordered.size()<8){
                var next=remaining.stream().max(Comparator.comparingDouble((Evidence e)->ordered.stream().mapToDouble(p->p.at.distSqr(e.at)).min().orElse(0))
                        .thenComparingInt(Evidence::importance).thenComparingLong(e->e.at.asLong())).orElseThrow();
                ordered.add(next);remaining.remove(next);
            }
            result=new Site(spawn,focus,ordered.stream().map(Evidence::at).toList());
        }
        private boolean inFootprint(BlockPos at){return Math.abs((at.getX()>>4)-(spawn.getX()>>4))<=radius&&Math.abs((at.getZ()>>4)-(spawn.getZ()>>4))<=radius;}
        public int progress(){return finished?1000:checked*1000/Math.max(1,chunks.size());}
        public boolean finished(){return finished;}
        public Optional<Site> result(){if(!finished)throw new IllegalStateException("Base search is not complete");return Optional.ofNullable(result);}
    }
    public PlayerBase(){}
}
