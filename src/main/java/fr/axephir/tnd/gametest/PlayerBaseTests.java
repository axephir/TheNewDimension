package fr.axephir.tnd.gametest;

import fr.axephir.tnd.city.Codex;
import fr.axephir.tnd.narrative.*;
import fr.axephir.tnd.technology.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Actual BlockItem placement, saved ownership, native personal respawn and a base far from the reactor. */
final class PlayerBaseTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> functions){
        functions.register("base_provenance",()->PlayerBaseTests::provenance);
        functions.register("base_selection",()->PlayerBaseTests::selection);
        functions.register("base_fallbacks",()->PlayerBaseTests::fallbacks);
        functions.register("base_scene",()->h->scene(h,0));
        functions.register("base_interruption",()->h->scene(h,1));
        functions.register("base_skip",()->h->scene(h,2));
    }
    private static void check(GameTestHelper h,boolean ok,String reason){h.assertTrue(ok,Component.literal(reason));}
    private static void bed(ServerPlayer p,BlockPos at){
        var w=p.level();var state=Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING,Direction.EAST);
        w.setBlock(at.west(),state.setValue(BedBlock.PART,BedPart.FOOT),2);w.setBlock(at,state.setValue(BedBlock.PART,BedPart.HEAD),2);
        p.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(w.dimension(),at,0,0),false),false);
    }
    private static void place(GameTestHelper h,ServerPlayer p,BlockPos at,Block block){
        p.level().setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);p.level().setBlock(at,Blocks.AIR.defaultBlockState(),3);
        p.setPos(Vec3.atBottomCenterOf(at.south(2)));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(block));
        var floor=at.below();p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
        check(h,p.level().getBlockState(at).is(block)&&p.getMainHandItem().isEmpty(),"A real survival BlockItem is placed and consumed: "+block+" at "+at);
    }
    private static List<BlockPos> cluster(GameTestHelper h,ServerPlayer p,BlockPos origin,int count){
        var out=new ArrayList<BlockPos>();
        for(int i=0;i<count;i++){
            var at=origin.offset(-5+(i%5)*2,0,3+(i/5)*2);var block=i%3==0?Blocks.FURNACE:i%3==1?Blocks.BARREL:Blocks.CHEST;
            place(h,p,at,block);out.add(at);
        }return out;
    }
    private static Optional<PlayerBase.Site> find(ServerPlayer p,int radius){var search=new PlayerBase.Search(p,radius);for(int n=0;!search.finished()&&n<82;n++)search.step();if(!search.finished())throw new IllegalStateException("Base lookup exceeded its bounded chunk budget");return search.result();}
    private static void provenance(GameTestHelper h){
        var w=h.getLevel();var at=new BlockPos(232000,120,232000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at);var other=GameplayTests.player(h);
        try{
            var chest=at.south(3);place(h,p,chest,Blocks.CHEST);var be=(ChestBlockEntity)w.getBlockEntity(chest);
            check(h,be.getData(PlayerBase.PLACER).equals(p.getUUID().toString())&&p.getData(PlayerBase.POSITIONS).equals(List.of(chest.asLong())),"Only the actual placer is recorded by the real NeoForge placement event");
            var saved=be.saveWithFullMetadata(w.registryAccess());var loaded=new ChestBlockEntity(chest,be.getBlockState());loaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,w.registryAccess(),saved));
            check(h,loaded.getData(PlayerBase.PLACER).equals(p.getUUID().toString()),"Block entity ownership survives native NBT serialization");
            var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,w.registryAccess());p.saveWithoutId(output);
            var clone=new ServerPlayer(w.getServer(),w,p.getGameProfile(),p.clientInformation());clone.load(TagValueInput.create(ProblemReporter.DISCARDING,w.registryAccess(),output.buildResult()));
            check(h,clone.getData(PlayerBase.POSITIONS).equals(p.getData(PlayerBase.POSITIONS)),"The owner's bounded utility index survives player serialization");
            var generated=at.east(3);w.setBlock(generated,Blocks.CHEST.defaultBlockState(),3);check(h,!w.getBlockEntity(generated).hasData(PlayerBase.PLACER),"Generated containers are not attributed to the nearest player");
            var snapshot=BlockSnapshot.create(w.dimension(),w,generated);var canceled=new BlockEvent.EntityPlaceEvent(snapshot,Blocks.CHEST.defaultBlockState(),p);canceled.setCanceled(true);NeoForge.EVENT_BUS.post(canceled);
            check(h,!w.getBlockEntity(generated).hasData(PlayerBase.PLACER)&&p.getData(PlayerBase.POSITIONS).size()==1,"Canceled placement never grants base ownership");
            place(h,other,chest,Blocks.CHEST);check(h,w.getBlockEntity(chest).getData(PlayerBase.PLACER).equals(other.getUUID().toString()),"Breaking and replacing transfers evidence to the actual new placer");
        }finally{GameplayTests.cleanup(p);GameplayTests.cleanup(other);FieldworkTests.release(w,at);}h.succeed();
    }
    private static void selection(GameTestHelper h){
        var w=h.getLevel();var at=new BlockPos(234000,120,232000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at);var other=FieldworkTests.player(h,w,at);
        try{
            bed(p,at);var own=cluster(h,p,at,10);var stocked=(FurnaceBlockEntity)w.getBlockEntity(own.getFirst());stocked.setItem(0,new ItemStack(Items.DIAMOND,32));
            var unopened=(ChestBlockEntity)w.getBlockEntity(own.get(5));unopened.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.withDefaultNamespace("chests/simple_dungeon")));var unopenedBefore=unopened.saveWithFullMetadata(w.registryAccess());
            cluster(h,other,at.north(10),15);
            var site=find(p,2).orElseThrow();check(h,site.spawn().equals(at)&&site.targets().stream().allMatch(own::contains)&&site.targets().contains(own.getFirst()),"Ten placed utilities across a chunk boundary qualify; occupied storage is framed and foreign equipment is excluded");
            check(h,unopened.saveWithFullMetadata(w.registryAccess()).equals(unopenedBefore),"Scoring an owned container never unpacks a loot table or changes source NBT");
            var search=new PlayerBase.Search(p,2);check(h,!search.finished()&&search.progress()==0,"An eligible base is examined incrementally");search.step();check(h,!search.finished()&&search.progress()>0,"Cross-chunk evidence is not all loaded in one server tick");
            for(var target:own)w.setBlock(target,Blocks.AIR.defaultBlockState(),3);
            check(h,find(p,2).isEmpty(),"Broken or replaced indexed blocks never remain false evidence of a base");
        }finally{GameplayTests.cleanup(p);GameplayTests.cleanup(other);FieldworkTests.release(w,at);}h.succeed();
    }
    private static void fallbacks(GameTestHelper h){
        var w=h.getLevel();var at=new BlockPos(236000,120,232000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at);
        try{
            bed(p,at);var own=cluster(h,p,at,9);w.setBlock(at.east(8),Blocks.CHEST.defaultBlockState(),3);
            check(h,find(p,2).isEmpty(),"Nine placed containers plus generated loot cannot pass the ten-utility threshold");
            var extra=at.offset(5,0,7);place(h,p,extra,Industry.BLOCKS.get(Industry.Kind.BATTERY).get());check(h,find(p,2).isPresent(),"A tenth genuine machine placement activates personal-base targeting");
            p.setRespawnPosition(null,false);check(h,find(p,2).isEmpty(),"World spawn never substitutes for a personal respawn point");
            bed(p,at);w.setBlock(at,Blocks.AIR.defaultBlockState(),3);check(h,find(p,2).isEmpty(),"A destroyed bed invalidates base detection");
            bed(p,at.east(128));check(h,find(p,2).isEmpty(),"A temporary distant bed cannot select a remote warehouse");
            bed(p,at);for(var target:own)w.setBlock(target,Blocks.AIR.defaultBlockState(),3);w.setBlock(extra,Blocks.AIR.defaultBlockState(),3);
            for(int i=0;i<10;i++)place(h,p,at.offset((i-5)*12,0,12),Blocks.BARREL);
            check(h,find(p,4).isEmpty(),"Ten widely scattered owned containers cannot impersonate a compact base");
        }finally{GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();
    }
    private static void assembly(ServerLevel w,BlockPos at,boolean reactor){int r=reactor?2:1;
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(reactor?Math.max(Math.abs(x),Math.abs(z))==r:x!=0||z!=0)w.setBlock(at.offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),2);
        for(int x:new int[]{-r,r})for(int z:new int[]{-r,r}){if(reactor)for(int y=0;y<3;y++)w.setBlock(at.offset(x,y,z),fr.axephir.tnd.city.CityContent.DECOR.get("carbon_panel").get().defaultBlockState(),2);w.setBlock(at.offset(x,reactor?3:0,z),fr.axephir.tnd.city.CityContent.DECOR.get("relay_coil").get().defaultBlockState(),2);}
    }
    private static void scene(GameTestHelper h,int lane){
        boolean interrupt=lane==1;var w=h.getLevel();ConvergenceTests.echo(h);var home=new BlockPos(238000+lane*2048,120,232000);var reactor=home.east(512);var core=reactor.east(8);
        FieldworkTests.platform(w,home);FieldworkTests.platform(w,reactor);var p=FieldworkTests.player(h,w,home);bed(p,home);var utilities=cluster(h,p,home,10);
        var important=utilities.get(2);var chest=(ChestBlockEntity)w.getBlockEntity(important);chest.setItem(7,new ItemStack(Items.DIAMOND,47));var savedChest=chest.saveWithFullMetadata(w.registryAccess());
        for(var roof:BlockPos.betweenClosed(home.offset(-8,9,-1),home.offset(8,9,9)))w.setBlock(roof,Blocks.OAK_PLANKS.defaultBlockState(),2);
        w.setBlock(core,ConvergenceContent.RESTORER.get().defaultBlockState(),3);assembly(w,core,false);w.setBlock(reactor,ConvergenceContent.REACTOR.get().defaultBlockState(),3);assembly(w,reactor,true);
        p.setPos(Vec3.atBottomCenterOf(reactor.south(3)));var returnPosition=p.position();p.setData(Codex.NOTES,255);p.getInventory().setItem(0,new ItemStack(Items.NETHERITE_INGOT,3));
        boolean[] started={false},entered={false},finished={false};BlockPos[] mappedCore={null};
        h.onEachTick(()->{
            if(finished[0])return;
            if(!started[0]){started[0]=Convergence.begin(p,reactor,1);return;}
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            if(!Convergence.inEcho(p))return;
            if(!entered[0]){
                entered[0]=true;mappedCore[0]=Convergence.echoMachine(p);int dx=mappedCore[0].getX()-home.getX(),dz=mappedCore[0].getZ()-home.getZ();
                check(h,p.level().getBlockState(mappedCore[0]).is(ConvergenceContent.RESTORER.get()),"The usable personal core is inside the selected base, despite a reactor 512 blocks away");
                var visual=p.level().getBlockEntity(important.offset(dx,0,dz));check(h,visual instanceof ChestBlockEntity c&&c.isEmpty(),"The pre-impact base includes recognisable real storage but no copied valuables");
                check(h,p.getCamera()!=p&&p.getCamera().position().distanceToSqr(Vec3.atCenterOf(important.offset(dx,0,dz)))<10000,"The native camera orbits the important player base, not the remote reactor");
                var viewTarget=Vec3.atCenterOf(home.offset(-1,2,4).offset(dx,0,dz));var visibility=p.level().clip(new ClipContext(viewTarget,p.getCamera().getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
                check(h,visibility.getType()==HitResult.Type.MISS&&p.getCamera().getEyeY()<home.getY()+9,"The initial camera stays below the real copied roof and can see the important storage");
                if(interrupt){NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));check(h,Convergence.echoMachine(p).equals(mappedCore[0]),"A persisted personal signal retains the relocated core without a live session");NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));finished[0]=true;}
                if(lane==2)Convergence.finishCinema(p);
            }
            if(!interrupt&&p.getData(Convergence.PHASE)==3){
                int dx=mappedCore[0].getX()-home.getX(),dz=mappedCore[0].getZ()-home.getZ();
                long destroyed=utilities.stream().filter(at->p.level().getBlockState(at.offset(dx,0,dz)).isAir()).count();
                check(h,destroyed>=8,"Actual cinematic impacts destroy the dense player storage cluster: "+destroyed+"/10");
                check(h,p.level().getBlockState(mappedCore[0]).is(ConvergenceContent.RESTORER.get()),"The private signal remains usable after all eight impacts");
                var at=mappedCore[0];p.setPos(at.getX()+.5,at.getY(),at.getZ()+3.5);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(at));
                check(h,Convergence.inspect(p)&&p.containerMenu instanceof ConvergenceMenu menu&&menu.select(p,2),"The real owner ray opens the relocated core, and refusing leaves exploration available");
                check(h,Convergence.recover(p),"Recovery succeeds from a scene centred away from the original machines");finished[0]=true;
            }
        });
        h.succeedWhen(()->{
            check(h,finished[0]&&!Convergence.inEcho(p)&&p.getData(Convergence.PHASE)==6,"The personal-base sequence completes or recovers after interruption");
            try{
                check(h,p.position().distanceToSqr(returnPosition)<9&&!p.isSpectator()&&p.getInventory().countItem(Items.NETHERITE_INGOT)==3,"The exact original reactor return and personal inventory remain independent of scene targeting");
                check(h,chest.saveWithFullMetadata(w.registryAccess()).equals(savedChest)&&w.getBlockState(home).is(Blocks.BED.red()),"Source storage NBT, valuables and personal bed remain intact");
            }finally{GameplayTests.cleanup(p);FieldworkTests.release(w,home);FieldworkTests.release(w,reactor);}
        });
    }
    private PlayerBaseTests(){}
}
