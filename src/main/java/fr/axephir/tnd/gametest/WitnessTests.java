package fr.axephir.tnd.gametest;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.sanctuary.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.function.Consumer;

final class WitnessTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> f){
        f.register("witness_circuit",()->WitnessTests::circuit);f.register("witness_discoveries",()->WitnessTests::discoveries);f.register("witness_bridge",()->WitnessTests::bridge);f.register("witness_gates",()->WitnessTests::gates);f.register("witness_boss",()->WitnessTests::boss);f.register("witness_restore",()->WitnessTests::restore);f.register("witness_generation",()->WitnessTests::generation);
    }
    private static void check(GameTestHelper h,boolean b,String message){h.assertTrue(b,Component.literal(message));}
    private static final class Fixture implements AutoCloseable {
        final ServerLevel w;final SanctuaryRoute.Site site;final int index;SanctuaryNodeEntity n;
        final List<BlockPos> platforms=new ArrayList<>();final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int cell,int index){w=FieldworkTests.vestige(h);site=SanctuaryRoute.nearest(cell*6144,cell*6144);this.index=index;platform(SanctuaryRegions.station(site,index));node();for(int i=0;i<index;i++)SanctuaryLedger.get(w.getServer()).finish(site.key(),i);}
        void platform(BlockPos p){FieldworkTests.platform(w,p);platforms.add(p);}
        void node(){var p=SanctuaryRegions.station(site,index);w.getChunkAt(p).setData(SanctuaryRegions.GENERATED,true);w.setBlock(p,SanctuaryContent.NODE.get().defaultBlockState(),3);n=(SanctuaryNodeEntity)w.getBlockEntity(p);n.initializeWitness(site,index);}
        BlockPos at(WitnessAct.Point p){return n.local(p.x(),p.y(),p.z());}
        ServerPlayer player(GameTestHelper h,BlockPos p){var player=FieldworkTests.player(h,w,p);players.add(player);return player;}
        public void close(){for(var e:w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(n.getBlockPos()).inflate(64)))e.discard();players.forEach(GameplayTests::cleanup);platforms.forEach(p->FieldworkTests.release(w,p));}
    }
    static void circuit(GameTestHelper h){try(var f=new Fixture(h,90,5)){
        var p=f.player(h,f.n.getBlockPos().east(2));
        for(int[] d:new int[][]{{-24,8},{24,8},{0,-30}}){var at=f.n.local(d[0],1,d[1]);f.platform(at);f.w.setBlock(at,SanctuaryContent.TRACE.get().defaultBlockState(),3);p.setPos(Vec3.atBottomCenterOf(at.south(2)));f.n.trace(p,at);}
        check(h,f.n.traces()==7&&!SanctuaryRoute.done(f.n.mask(),5),"Three echoes cannot skip the physical power circuit in the enhanced act");
        for(int i=0;i<3;i++){var at=f.at(WitnessAct.coupler(i));f.platform(at);f.w.setBlock(at,SanctuaryContent.COUPLER.get().defaultBlockState(),3);}
        for(int i=0;i<3;i++){var at=f.at(WitnessAct.coupler(i));p.setPos(Vec3.atBottomCenterOf(at.south(2)));for(int turn=0;turn<WitnessAct.facing(i);turn++)check(h,WitnessScene.use(f.n,p,at,WitnessDeviceBlock.Kind.COUPLER),"A nearby native coupler accepts one real turn");}
        check(h,WitnessScene.circuit(f.n)==7&&SanctuaryRoute.done(f.n.mask(),5),"A real complete circuit plus the three echoes advances exactly this chamber");
        var last=f.at(WitnessAct.coupler(2));check(h,!WitnessScene.use(f.n,p,last,WitnessDeviceBlock.Kind.COUPLER)&&WitnessScene.circuit(f.n)==7,"A latched circuit stays stable after completion");
        check(h,p.getInventory().countItem(SanctuaryContent.CORE.get())==0,"A mechanism cannot award a boss core");
    }h.succeed();}
    static void discoveries(GameTestHelper h){try(var f=new Fixture(h,92,0)){
        var at=f.at(WitnessAct.reliquary(0));f.platform(at);f.w.setBlock(at,SanctuaryContent.RELIQUARY.get().defaultBlockState(),3);var p=f.player(h,at.south(2));
        check(h,!WitnessScene.use(f.n,p,at,WitnessDeviceBlock.Kind.RELIQUARY)&&f.n.discoveries(p)==0,"The unearned receptacle stays sealed");SanctuaryLedger.get(f.w.getServer()).finish(f.site.key(),0);
        f.w.setBlock(at.south(),Blocks.STONE.defaultBlockState(),3);f.w.setBlock(at.south().above(),Blocks.STONE.defaultBlockState(),3);
        check(h,!WitnessScene.use(f.n,p,at,WitnessDeviceBlock.Kind.RELIQUARY)&&f.n.discoveries(p)==0,"An interaction through an occluding wall cannot collect a secret");f.w.setBlock(at.south(),Blocks.AIR.defaultBlockState(),3);f.w.setBlock(at.south().above(),Blocks.AIR.defaultBlockState(),3);
        check(h,WitnessScene.use(f.n,p,at,WitnessDeviceBlock.Kind.RELIQUARY)&&p.getInventory().countItem(SanctuaryContent.SHARD.get())==4,"The first native interaction grants exactly four physical shards");
        check(h,!WitnessScene.use(f.n,p,at,WitnessDeviceBlock.Kind.RELIQUARY)&&p.getInventory().countItem(SanctuaryContent.SHARD.get())==4,"A personal reward cannot be repeated");
        var other=f.player(h,at.south(2));check(h,WitnessScene.use(f.n,other,at,WitnessDeviceBlock.Kind.RELIQUARY)&&other.getInventory().countItem(SanctuaryContent.SHARD.get())==4,"A second player has an independent personal reward");
        var ledger=SanctuaryLedger.get(f.w.getServer());var data=SanctuaryLedger.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,ledger).result().orElseThrow();var loaded=SanctuaryLedger.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,data).result().orElseThrow();check(h,loaded.discoveries(f.site.key(),p.getUUID())==1&&!loaded.discover(f.site.key(),p.getUUID(),0),"Discovery identities persist through the real saved-data codec");
    }h.succeed();}
    static void bridge(GameTestHelper h){var f=new Fixture(h,94,4);int cx=f.n.local(0,0,0).getX(),cz=f.n.local(0,0,0).getZ();
        for(int x=cx-48;x<=cx+48;x+=16)for(int z=cz-16;z<=cz;z+=16)new SanctuaryFeature().generate(f.w,new BlockPos(x,0,z));
        var device=f.n.local(-42,11,0);var p=f.player(h,device.south(2));var obstruction=f.n.local(37,10,0);f.w.setBlock(obstruction,Blocks.GOLD_BLOCK.defaultBlockState(),3);
        check(h,WitnessScene.use(f.n,p,device,WitnessDeviceBlock.Kind.COUPLER),"The real gallery control starts the bridge mechanism");
        h.runAfterDelay(45,()->{check(h,new SanctuaryMenu(1,p.getInventory(),f.n).value(19)==0&&f.w.getBlockState(obstruction).is(Blocks.GOLD_BLOCK),"An unexpected solid block pauses construction without being overwritten");f.w.setBlock(obstruction,Blocks.AIR.defaultBlockState(),3);});
        h.runAfterDelay(180,()->{try{
            check(h,new SanctuaryMenu(1,p.getInventory(),f.n).value(19)==10,"The persistent animation reaches its final bridge stage");
            for(int x=-37;x<=37;x++)check(h,f.w.getBlockState(f.n.local(x,10,0)).is(SanctuaryContent.block("obsidian_grate")),"Every centre-lane bridge column has a real solid floor");
            check(h,f.w.getBlockState(f.n.local(0,11,0)).isAir()&&f.w.getBlockState(f.n.local(0,12,0)).isAir(),"The completed central crossing has two blocks of headroom");
            check(h,!SanctuaryRoute.done(f.n.mask(),4),"Building the optional crossing cannot skip the four physical resonances");h.succeed();
        }finally{f.close();}});
    }
    static void gates(GameTestHelper h){try(var f=new Fixture(h,96,1)){
        var low=f.n.local(-48,1,0);var high=f.n.local(-48,8,0);f.w.getChunkAt(low);for(int y=1;y<=8;y++)f.w.setBlock(f.n.local(-48,y,0),SanctuaryContent.GATE.get().defaultBlockState(),3);
        f.n.tick();check(h,f.w.getBlockState(low).isAir()&&f.w.getBlockState(high).is(SanctuaryContent.GATE.get()),"A newly earned monumental gate starts lifting one row at a time");
        for(int i=0;i<7;i++)f.n.tick();check(h,f.w.getBlockState(high).isAir(),"The animated gate eventually clears its full original opening");
    }h.succeed();}
    static void boss(GameTestHelper h){var f=new Fixture(h,98,7);var center=f.n.local(0,1,0);for(int x:new int[]{-16,0,16})for(int z:new int[]{-16,0,16})f.platform(center.offset(x,0,z));f.node();var p=f.player(h,f.n.getBlockPos().east(2));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE,1200,5));
        check(h,f.n.action(p,0),"A legitimate survival player starts the enhanced guardian");f.n.tick();
        h.runAfterDelay(2,()->{
        var e=f.w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(center).inflate(40),v->v.kind()==SanctuaryConstruct.Kind.COLOSSUS).stream().findFirst().orElseThrow(()->new IllegalStateException("Guardian did not spawn: active="+f.n.active()+", spawned="+f.n.spawned()+", natural="+f.n.natural()+", player="+p.position()));
        check(h,e.ritual()&&e.awakening()>=155&&e.awakening()<=160&&!e.hurtServer(f.w,p.damageSources().playerAttack(p),50),"The eight-second ceremonial awakening protects the emerging guardian");
        var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,f.w.registryAccess());e.saveWithoutId(saved);var restored=(SanctuaryConstruct)EntityType.create(SanctuaryEntities.TYPES.get(e.kind()).get(),net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,f.w.registryAccess(),saved.buildResult()),f.w,EntitySpawnReason.LOAD).orElseThrow(()->new IllegalStateException("Native guardian reload failed"));check(h,restored.ritual()&&restored.awakening()==e.awakening(),"Native entity serialization preserves the ritual and remaining emergence time");restored.discard();
        h.runAfterDelay(180,()->{check(h,e.isAlive()&&e.awakening()==0,"The real server completes the emergence");p.setPos(e.position().add(0,0,10));e.hurtServer(f.w,p.damageSources().playerAttack(p),100000);e.setHealth(500);});
        h.runAfterDelay(266,()->{check(h,e.phase()==2&&e.beginAttack(p)&&e.pattern()==4,"The final phase can prepare the new triple-impact pattern");check(h,e.threatens(p.position())&&!e.threatens(p.position().add(0,0,6)),"The three marked impact circles leave a real safe route");});
        h.runAfterDelay(286,()->{try{check(h,!SanctuaryRoute.done(f.n.mask(),7),"A changed phase or reloaded guardian cannot fabricate a victory");h.succeed();}finally{f.close();}});
        });
    }
    static void restore(GameTestHelper h){try(var f=new Fixture(h,100,4)){
        var tag=f.n.saveWithFullMetadata(f.w.registryAccess());tag.putBoolean("bridgeWanted",true);tag.putInt("bridgeStage",6);var loaded=(SanctuaryNodeEntity)BlockEntity.loadStatic(f.n.getBlockPos(),f.n.getBlockState(),tag,f.w.registryAccess());f.n.setRemoved();loaded.setLevel(f.w);f.w.setBlockEntity(loaded);f.n=loaded;
        var p=f.player(h,f.n.getBlockPos().east(2));var menu=new SanctuaryMenu(2,p.getInventory(),f.n);check(h,f.n.enhanced()&&menu.value(19)==6&&menu.value(17)==1,"An unfinished mechanism retains its edition and exact deployment stage after native reload");
        tag.remove("witnessAct");var legacy=(SanctuaryNodeEntity)BlockEntity.loadStatic(f.n.getBlockPos(),f.n.getBlockState(),tag,f.w.registryAccess());check(h,!legacy.enhanced(),"A pre-update station stays on its existing objective rules");
    }h.succeed();}
    static void generation(GameTestHelper h){try(var f=new Fixture(h,102,0)){
        var cache=f.at(WitnessAct.reliquary(0));new SanctuaryFeature().generate(f.w,cache);check(h,f.w.getBlockState(cache).is(SanctuaryContent.RELIQUARY.get())&&!f.w.getBlockState(cache.below()).isAir(),"A naturally generated receptacle rests on the bottom of the carved court");
        var engraving=f.n.local(8,0,-36);new SanctuaryFeature().generate(f.w,engraving);check(h,f.w.getBlockState(engraving).is(SanctuaryContent.FILAMENT.get())&&f.w.getBlockState(engraving).getValue(WitnessFilamentBlock.POWER)==0,"New authored circuits begin dormant in the real foundation");
        new SanctuaryFeature().generate(f.w,f.n.getBlockPos());f.n=(SanctuaryNodeEntity)f.w.getBlockEntity(f.n.getBlockPos());check(h,f.n.enhanced()&&f.n.natural(),"Only natural new stations receive the enhanced first-act rules");
    }h.succeed();}
}
