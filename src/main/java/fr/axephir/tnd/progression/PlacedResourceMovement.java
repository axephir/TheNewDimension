package fr.axephir.tnd.progression;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.level.PistonEvent;
import java.util.*;

/** Observe real moved block entities: a retracting piston still has its head during Pre, so its ordinary resolver is incomplete. */
public final class PlacedResourceMovement {
    private record Key(ServerLevel level,BlockPos piston){}
    private record Move(long time,Direction facing,Direction delta,boolean extending,Map<BlockPos,BlockState> marked){}
    private final Map<Key,Move> pending=new HashMap<>();
    @SubscribeEvent(priority=EventPriority.LOWEST) public void before(PistonEvent.Pre e){
        if(!(e.getLevel() instanceof ServerLevel w))return;long now=w.getGameTime();pending.entrySet().removeIf(p->p.getKey().level()==w&&now-p.getValue().time()>2);if(pending.size()>1024)pending.clear();
        var at=e.getPos();boolean extending=e.getPistonMoveType()==PistonEvent.PistonMoveType.EXTEND;var delta=extending?e.getDirection():e.getDirection().getOpposite();var marked=new HashMap<BlockPos,BlockState>();
        // At most nine already loaded chunks. Native push depth is twelve; fourteen also covers pull origins and sticky side branches.
        for(int x=(at.getX()-14)>>4;x<=(at.getX()+14)>>4;x++)for(int z=(at.getZ()-14)>>4;z<=(at.getZ()+14)>>4;z++){
            if(!w.hasChunk(x,z))continue;var chunk=w.getChunk(x,z);if(!chunk.hasData(ProgressionEvents.PLACED))continue;
            for(long packed:chunk.getData(ProgressionEvents.PLACED)){var source=BlockPos.of(packed);if(Math.abs(source.getX()-at.getX())>14||Math.abs(source.getY()-at.getY())>14||Math.abs(source.getZ()-at.getZ())>14)continue;
                var state=w.getBlockState(source);var target=source.relative(delta);
                // An existing moving entity belongs to an earlier event, never to this move.
                if(!state.isAir()&&w.hasChunkAt(target)&&!(w.getBlockEntity(target) instanceof PistonMovingBlockEntity))marked.put(source,state);
            }
        }
        pending.put(new Key(w,at.immutable()),new Move(now,e.getDirection(),delta,extending,Map.copyOf(marked)));
    }
    @SubscribeEvent public void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){pending.clear();}
    @SubscribeEvent public void after(PistonEvent.Post e){
        if(!(e.getLevel() instanceof ServerLevel w))return;var move=pending.remove(new Key(w,e.getPos()));if(move==null||w.getGameTime()-move.time()>2)return;
        var moved=new HashMap<BlockPos,BlockPos>();for(var entry:move.marked().entrySet()){
            var target=entry.getKey().relative(move.delta());if(w.getBlockEntity(target) instanceof PistonMovingBlockEntity actual&&!actual.isSourcePiston()&&actual.getDirection()==move.facing()&&actual.isExtending()==move.extending()&&actual.getMovedState().is(entry.getValue().getBlock()))moved.put(entry.getKey(),target);
        }
        Map<net.minecraft.world.level.chunk.LevelChunk,Set<Long>> edits=new HashMap<>();java.util.function.Function<BlockPos,Set<Long>> positions=p->edits.computeIfAbsent(w.getChunkAt(p),c->new HashSet<>(c.getData(ProgressionEvents.PLACED)));
        for(var source:moved.keySet())positions.apply(source).remove(source.asLong());for(var target:moved.values())positions.apply(target).add(target.asLong());
        edits.forEach((chunk,set)->chunk.setData(ProgressionEvents.PLACED,List.copyOf(set)));
    }
}
