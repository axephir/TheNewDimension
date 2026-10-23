package fr.axephir.tnd.gametest;

import fr.axephir.tnd.sanctuary.*;
import fr.axephir.tnd.domain.SanctuaryRoute;
import fr.axephir.tnd.technology.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.function.Consumer;

final class ObservatoryTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> f){
        f.register("observatory_interior_routes",()->ObservatoryTests::interiorRoutes);
        f.register("observatory_interior_foundations",()->ObservatoryTests::interiorFoundations);
        f.register("observatory_trace_gate",()->ObservatoryTests::traceGate);
        f.register("observatory_duo_rewards",()->ObservatoryTests::duoRewards);
        f.register("observatory_linked_dials",()->ObservatoryTests::linkedDials);
        f.register("observatory_live_ai",()->ObservatoryTests::liveAi);
        f.register("observatory_core_layers",()->ObservatoryTests::coreLayers);f.register("observatory_sorter",()->ObservatoryTests::sorter);f.register("observatory_pads",()->ObservatoryTests::pads);f.register("observatory_relays",()->ObservatoryTests::relays);
        f.register("observatory_route",()->ObservatoryTests::route);f.register("observatory_ledger",()->ObservatoryTests::ledger);f.register("observatory_palette",()->ObservatoryTests::palette);f.register("observatory_codec",()->ObservatoryTests::codec);f.register("observatory_mechanics",()->ObservatoryTests::mechanics);f.register("observatory_industry",()->ObservatoryTests::industry);f.register("observatory_industry_guards",()->ObservatoryTests::guards);f.register("observatory_construct",()->ObservatoryTests::construct);f.register("observatory_attacks",()->ObservatoryTests::attacks);f.register("observatory_familiar",()->ObservatoryTests::familiar);f.register("observatory_generation",()->ObservatoryTests::generation);f.register("observatory_protection",()->ObservatoryTests::protection);
    }

    /** Check traversability using native collision shapes, including both gallery heights.
     * Goals are the actual station, devices, chest approaches and four resonance locations. */
    static void interiorRoutes(GameTestHelper h){
        var shapeCache=new HashMap<net.minecraft.world.level.block.state.BlockState,Boolean>();
        for(int index=0;index<48;index++){
            int height=SanctuaryRoute.boss(index)?62:32+(index%3)*7;
            int[][] floors=new int[95][95];
            for(int x=-47;x<=47;x++)for(int z=-47;z<=47;z++){
                boolean[] solid=new boolean[20];
                for(int y=-6;y<=13;y++){
                    var state=SanctuaryInterior.sample(x,y,z,index,height);
                    solid[y+6]=shapeCache.computeIfAbsent(state,s->!s.getCollisionShape(h.getLevel(),BlockPos.ZERO).isEmpty());
                }
                for(int feet=-4;feet<=11;feet++)if(solid[feet+5]&&!solid[feet+6]&&!solid[feet+7])floors[x+47][z+47]|=1<<(feet+4);
            }
            var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();
            var start=new BlockPos(0,1,20);seen.add(start);queue.add(start);
            while(!queue.isEmpty()){
                var p=queue.removeFirst();
                for(var direction:Direction.Plane.HORIZONTAL)for(int step=-1;step<=1;step++){
                    var q=p.relative(direction).above(step);int x=q.getX(),z=q.getZ(),y=q.getY();
                    if(Math.abs(x)>47||Math.abs(z)>47||y< -4||y>11)continue;
                    if((floors[x+47][z+47]&(1<<(y+4)))!=0&&seen.add(q))queue.addLast(q);
                }
            }
            for(var target:new BlockPos[]{new BlockPos(-47,1,0),new BlockPos(47,1,0),new BlockPos(0,1,-47),new BlockPos(0,1,47),new BlockPos(-35,1,33),new BlockPos(35,1,33),new BlockPos(-42,11,-12),new BlockPos(42,11,12),new BlockPos(0,1,-20),new BlockPos(0,1,28)})
                check(h,seen.contains(target),"Room "+index+" retains a walk/jump route to "+target);
            if(index%8!=7&&index%8!=3)check(h,seen.contains(new BlockPos(22,-3,-26)),"The lowest court has a connected return route in room "+index);
            if(index%8==0||index%8==5)for(var target:new BlockPos[]{new BlockPos(-24,1,10),new BlockPos(24,1,10),new BlockPos(0,1,-28)})check(h,seen.contains(target),"Stele approaches remain connected in room "+index);
            if(index%8==2)for(int n=0;n<4;n++)check(h,seen.contains(new BlockPos(-12+n*8,1,-12)),"Each dial is reachable in room "+index);
            if(index%8==3)for(int x:new int[]{-24,24})for(int z:new int[]{-24,24})check(h,seen.contains(new BlockPos(x,1,z+Integer.signum(z)*2)),"Relay approaches remain at ground level in room "+index);
            if(index%8==7)for(int x=-19;x<=19;x++)for(int z=-7;z<=11;z++)for(int y=1;y<=12;y++)check(h,SanctuaryInterior.sample(x,y,z,index,height).getCollisionShape(h.getLevel(),BlockPos.ZERO).isEmpty(),"Large boss spawn and central combat envelopes remain clear in room "+index);
        }
        h.succeed();
    }

    static void interiorFoundations(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(78*6144,78*6144);
        int cx=site.x()+SanctuaryRoute.x(0),cz=site.z()+SanctuaryRoute.z(0);
        var bottom=new BlockPos(cx+22,SanctuaryRoute.FLOOR-4,cz-24);
        new SanctuaryFeature().generate(w,bottom);
        check(h,!w.getBlockState(bottom).isAir(),"The naturally carved court has a supported mineral floor four blocks down");
        for(int y=1;y<=4;y++)check(h,w.getBlockState(bottom.above(y)).isAir(),"The court really cuts through the foundation instead of retaining the original flat floor");
        var step=new BlockPos(cx+12,SanctuaryRoute.FLOOR,cz-24);new SanctuaryFeature().generate(w,step);
        check(h,w.getBlockState(step).is(SanctuaryContent.STAIRS.get()),"A real native stair connects the first court terrace to the hall");
        check(h,!w.getBlockState(bottom.below(4)).isAir(),"Excavation preserves the load-bearing foundation below the court");
        h.succeed();
    }
    private static void check(GameTestHelper h,boolean ok,String why){h.assertTrue(ok,Component.literal(why));}
    static void route(GameTestHelper h){long mask=0;for(int i=0;i<48;i++){check(h,SanctuaryRoute.available(mask,i),"Next authored station is reachable");check(h,i==47||!SanctuaryRoute.available(mask,i+1),"The route cannot skip an objective");if(i>0)check(h,Math.abs(SanctuaryRoute.x(i)-SanctuaryRoute.x(i-1))+Math.abs(SanctuaryRoute.z(i)-SanctuaryRoute.z(i-1))==128,"Adjacent route chambers share one straight corridor");mask=SanctuaryRoute.complete(mask,i);}check(h,mask==SanctuaryRoute.FULL&&SanctuaryRoute.reached(mask)==48,"All six acts have a persistent unbroken route");h.succeed();}
    static void ledger(GameTestHelper h){var store=SanctuaryLedger.get(h.getLevel().getServer());long key=UUID.randomUUID().getLeastSignificantBits();var p=UUID.randomUUID();check(h,!store.finish(key,7)&&!store.claim(key,p,0),"Cannot skip to a boss or claim an unavailable reward");for(int i=0;i<8;i++)check(h,store.finish(key,i),"Real objective completes once");check(h,!store.finish(key,7)&&!store.claim(key,p,0),"Completion does not imply reward participation");store.eligible(key,p,0);check(h,store.claim(key,p,0)&&!store.claim(key,p,0),"Eligible reward is granted once");var json=SanctuaryLedger.CODEC.encodeStart(JsonOps.INSTANCE,store).getOrThrow();var copy=SanctuaryLedger.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();check(h,copy.mask(key)==255&&copy.rewards(key,p)==1&&!copy.claim(key,p,0),"The native SavedData codec retains progress, eligibility and spent rewards");h.succeed();}
    static void palette(GameTestHelper h){for(String name:List.of("guiding_inlay","luminous_script","observatory_brazier"))check(h,SanctuaryContent.block(name).defaultBlockState().getLightEmission()>=12,"Main navigation materials emit real light");check(h,SanctuaryContent.NODE.get().defaultBlockState().getDestroySpeed(h.getLevel(),BlockPos.ZERO)<0,"A required natural station cannot be mined away");for(var k:SanctuaryConstruct.Kind.values())check(h,SanctuaryEntities.TYPES.get(k).get().getDimensions().height()>=2.5f,"Construct geometry matches an explicitly registered large hitbox");h.succeed();}
    static void codec(GameTestHelper h){var w=h.getLevel();try(var reader=w.getServer().getResourceManager().getResourceOrThrow(Identifier.fromNamespaceAndPath("tnd","dimension/vestige.json")).openAsReader()){
        var stem=LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,w.registryAccess()),com.google.gson.JsonParser.parseReader(reader)).getOrThrow();var gen=(net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)stem.generator();var random=RandomState.create(gen.generatorSettings().value(),w.registryAccess().lookupOrThrow(Registries.NOISE),1234);var s=SanctuaryRoute.nearest(3000,3000);
        for(int dx:new int[]{-500,0,500}){check(h,gen.getBiomeSource().getNoiseBiome((s.x()+dx)>>2,28,s.z()>>2,random.sampler()).unwrapKey().orElseThrow().identifier().getPath().equals("black_observatory"),"New giant region decodes and selects its own biome");int y=gen.getBaseHeight(s.x()+dx,s.z(),Heightmap.Types.WORLD_SURFACE_WG,net.minecraft.world.level.LevelHeightAccessor.create(0,256),random);check(h,y>=SanctuaryRoute.FLOOR-1&&y<=SanctuaryRoute.FLOOR+2,"Native terrain supports the sanctuary floor without floating buildings");}
    }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}h.succeed();}
    static void mechanics(GameTestHelper h){var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(60*6144,60*6144);var at=SanctuaryRegions.station(site,0);FieldworkTests.platform(w,at);w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(site,0);var p=FieldworkTests.player(h,w,at.east(2));
        try{check(h,node.natural(),"A station requires its exact authored coordinate and generated chunk");check(h,!node.action(p,0)||node.mask()==0,"Opening or examining a station cannot complete unread traces");var copy=(SanctuaryNodeEntity)BlockEntity.loadStatic(at,node.getBlockState(),node.saveWithFullMetadata(w.registryAccess()),w.registryAccess());copy.setLevel(w);check(h,!copy.natural()&&copy.index()==0,"Serialized metadata survives but a detached block entity has no live objective authority");w.setBlock(at.east(3),SanctuaryContent.DIAL.get().defaultBlockState(),3);var dial=at.east(3);w.getBlockState(dial).useWithoutItem(w,p,new BlockHitResult(Vec3.atCenterOf(dial),Direction.UP,dial,false));check(h,w.getBlockState(dial).getValue(SanctuaryDialBlock.TONE)==1,"A real block interaction rotates a persistent dial");
        }finally{GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();}
    private static IndustrialEntity machine(GameTestHelper h,BlockPos at,Industry.Kind kind){var w=h.getLevel();FieldworkTests.platform(w,at);w.setBlock(at,Industry.BLOCKS.get(kind).get().defaultBlockState(),3);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)w.setBlock(at.offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),3);return (IndustrialEntity)w.getBlockEntity(at);}
    static void industry(GameTestHelper h){var w=h.getLevel();var stations=new ArrayList<IndustrialEntity>();int n=0;for(var k:List.of(Industry.Kind.REFINERY,Industry.Kind.CRUCIBLE,Industry.Kind.ASSEMBLER)){var at=new BlockPos(280000+n++*64,120,280000);var e=machine(h,at,k);var r=ObservatoryProcessing.recipe(k);e.setItem(0,new ItemStack(r.first(),r.firstCount()));e.setItem(1,new ItemStack(r.second(),r.secondCount()));e.energy.set(r.energy());stations.add(e);}
        h.runAfterDelay(270,()->{try{for(var e:stations){var r=ObservatoryProcessing.recipe(e.kind);check(h,e.getItem(0).isEmpty()&&e.getItem(1).isEmpty()&&e.countItem(r.output())==r.count()&&e.energy.getAmountAsInt()==0,"A real advanced cycle commits both inputs, exact output and exact FE");}}finally{stations.forEach(e->FieldworkTests.release(w,e.getBlockPos()));}h.succeed();});}
    static void guards(GameTestHelper h){var at=new BlockPos(280256,120,280000);var w=h.getLevel();var e=machine(h,at,Industry.Kind.ASSEMBLER);var r=ObservatoryProcessing.recipe(e.kind);e.setItem(0,new ItemStack(r.first(),1));e.setItem(1,new ItemStack(r.second()));e.energy.set(r.energy());h.runAfterDelay(260,()->{
        check(h,e.getItem(0).getCount()==1&&e.getItem(1).getCount()==1&&e.energy.getAmountAsInt()==r.energy(),"Partial batch never consumes inputs or FE");e.setItem(0,new ItemStack(r.first(),2));w.setBlock(at.west().below(),Blocks.AIR.defaultBlockState(),3);
        h.runAfterDelay(260,()->{try{check(h,e.status()==6&&e.getItem(0).getCount()==2&&e.energy.getAmountAsInt()==r.energy(),"Removing the multiblock foundation prevents advanced production without consumption");}finally{FieldworkTests.release(w,at);}h.succeed();});});}
    static void construct(GameTestHelper h){var w=h.getLevel();for(var k:SanctuaryConstruct.Kind.values()){var e=SanctuaryEntities.TYPES.get(k).get().create(w,EntitySpawnReason.EVENT);e.bind(new BlockPos(0,120,0),"fixture",3,0);check(h,e.maxIntegrity()==(float)(k.hp*(k.boss()?2.3:1)),"Boss health scales once for a three-player encounter");check(h,e.getAttributeValue(Attributes.ARMOR)>=8,"Constructs have real native armour");}h.succeed();}
    static void attacks(GameTestHelper h){var w=h.getLevel();var at=new BlockPos(280384,120,280000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at.south(8));var mob=SanctuaryEntities.TYPES.get(SanctuaryConstruct.Kind.COLOSSUS).get().create(w,EntitySpawnReason.EVENT);mob.setPos(Vec3.atBottomCenterOf(at));w.addFreshEntity(mob);
        try{check(h,mob.beginAttack(p)&&mob.charge()>=36,"A giant attack has a long visible warning");var initial=p.position();p.setPos(initial.add(8,0,0));check(h,mob.threatens(initial)&&!mob.threatens(p.position()),"Moving away dodges a snapshotted impact; it never tracks the player");check(h,!mob.beginAttack(p),"Preparing an attack cannot stack another attack");mob.hurtServer(w,p.damageSources().playerAttack(p),300);check(h,mob.charge()==0&&mob.exposed()>0,"A sufficiently strong real hit interrupts and exposes the core");}finally{mob.discard();GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();}
    static void familiar(GameTestHelper h){var w=h.getLevel();var p=GameplayTests.player(h);var pet=fr.axephir.tnd.entities.VestigeEntities.SHARDLING.get().create(w,EntitySpawnReason.EVENT);try{pet.tame(p);for(int n=0;n<300&&pet.familiarLevel()<20;n++)pet.awardExperience(100);float max=pet.getMaxHealth();double damage=pet.getAttributeValue(Attributes.ATTACK_DAMAGE);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SanctuaryContent.MEMORY.get(),2));pet.mobInteract(p,InteractionHand.MAIN_HAND);check(h,(pet.protocols()&8)!=0&&pet.getMaxHealth()==max+8&&pet.getAttributeValue(Attributes.ATTACK_DAMAGE)==damage+2,"The owner's new memory grants its real permanent bonuses");pet.mobInteract(p,InteractionHand.MAIN_HAND);check(h,p.getMainHandItem().getCount()==1,"Installing the same memory twice never consumes or stacks it");var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess());pet.saveWithoutId(out);var restored=(fr.axephir.tnd.entities.Shardling)EntityType.create(fr.axephir.tnd.entities.VestigeEntities.SHARDLING.get(),net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess(),out.buildResult()),w,EntitySpawnReason.LOAD).orElseThrow();check(h,(restored.protocols()&8)!=0&&restored.getMaxHealth()==max+8,"The fourth protocol survives native entity serialization");check(h,pet.protocols()==8,"Old three-protocol identities remain distinct from the new bit");}finally{pet.discard();GameplayTests.cleanup(p);}h.succeed();}
    static void generation(GameTestHelper h){var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(62*6144,62*6144);var at=SanctuaryRegions.station(site,0);int x0=at.getX()&~15,z0=at.getZ()&~15;var outside=new BlockPos(x0-1,SanctuaryRoute.FLOOR+2,z0);w.setBlock(outside,Blocks.GOLD_BLOCK.defaultBlockState(),2);var feature=new SanctuaryFeature();check(h,feature.generate(w,new BlockPos(x0,0,z0)),"Natural sanctuary feature decorates its assigned chunk");check(h,w.getBlockEntity(at) instanceof SanctuaryNodeEntity node&&node.natural()&&node.index()==0,"A real generated station retains authored route metadata");check(h,w.getBlockState(outside).is(Blocks.GOLD_BLOCK),"Generation never writes the neighbouring chunk");check(h,w.getBlockState(at.below(5)).is(SanctuaryContent.block("obsidian_metal")),"The structure has continuous solid foundations");h.succeed();}
    static void protection(GameTestHelper h){var w=FieldworkTests.vestige(h);var s=SanctuaryRoute.nearest(64*6144,64*6144);var at=SanctuaryRegions.station(s,0);FieldworkTests.platform(w,at);try{w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,false);check(h,!SanctuaryRegions.protectedAt(w,at),"An old player structure under the coordinate grid stays editable");w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);check(h,SanctuaryRegions.protectedAt(w,at),"Protection applies only after natural generation has marked the chunk");}finally{FieldworkTests.release(w,at);}h.succeed();}
    static void coreLayers(GameTestHelper h){var w=h.getLevel();var p=GameplayTests.player(h);var e=SanctuaryEntities.TYPES.get(SanctuaryConstruct.Kind.ARCHON).get().create(w,EntitySpawnReason.EVENT);try{
        check(h,e.getMaxHealth()<=1024&&e.maxIntegrity()==5600,"Giant integrity respects Minecraft's native max-health range");float before=e.integrity();e.hurtServer(w,p.damageSources().playerAttack(p),100000);check(h,e.isAlive()&&e.integrity()<before&&e.integrity()>1024,"A broken core layer lowers total integrity without a false boss death");var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess());e.saveWithoutId(out);var restored=(SanctuaryConstruct)EntityType.create(SanctuaryEntities.TYPES.get(SanctuaryConstruct.Kind.ARCHON).get(),net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess(),out.buildResult()),w,EntitySpawnReason.LOAD).orElseThrow();check(h,restored.integrity()==e.integrity()&&restored.maxIntegrity()==5600,"Native serialization preserves remaining core layers without healing");restored.discard();
    }finally{e.discard();GameplayTests.cleanup(p);}h.succeed();}
    static void sorter(GameTestHelper h){var at=new BlockPos(280512,120,280000);var w=h.getLevel();var e=machine(h,at,Industry.Kind.SORTER);var filter=new ItemStack(Items.DIAMOND);filter.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Accord"));e.setItem(1,filter);e.setItem(0,new ItemStack(Items.DIAMOND));e.energy.set(360);
        h.runAfterDelay(100,()->{check(h,e.getItem(0).getCount()==1&&e.energy.getAmountAsInt()==360,"Unmatched components cannot pass an exact sorter filter");e.setItem(0,filter.copyWithCount(3));h.succeedWhen(()->{check(h,e.getItem(0).isEmpty()&&e.countItem(Items.DIAMOND)==4&&e.energy.getAmountAsInt()==0,"Three exact native transfers preserve the filter and cost 360 FE");check(h,java.util.Arrays.stream(e.getSlotsForFace(Direction.UP)).noneMatch(i->i==1)&&e.canTakeItemThroughFace(2,e.getItem(2),Direction.DOWN),"Hoppers cannot replace the filter and can export sorted output");FieldworkTests.release(w,at);});});}
    static void pads(GameTestHelper h){var w=FieldworkTests.vestige(h);var s=SanctuaryRoute.nearest(66*6144,66*6144);var at=SanctuaryRegions.station(s,4);FieldworkTests.platform(w,at);w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(s,4);var ledger=SanctuaryLedger.get(w.getServer());for(int i=0;i<4;i++)ledger.finish(s.key(),i);var p=FieldworkTests.player(h,w,at.east(2));var points=new ArrayList<BlockPos>();
        try{check(h,node.action(p,0),"A reached lens route starts from its real station");for(int i=0;i<4;i++){var point=SanctuaryNodeEntity.pad(s,4,i);points.add(point);w.getChunkAt(point);w.setBlock(point.below(),Blocks.STONE.defaultBlockState(),2);w.setBlock(point,Blocks.AIR.defaultBlockState(),2);w.setBlock(point.above(),Blocks.AIR.defaultBlockState(),2);p.setPos(Vec3.atBottomCenterOf(point));node.tick();check(h,node.sequence()==i+1,"Walking onto the next authored floor or gallery pad advances exactly one resonance");}check(h,SanctuaryRoute.done(node.mask(),4),"All four physical resonances complete the objective");}finally{GameplayTests.cleanup(p);FieldworkTests.release(w,at);points.forEach(point->FieldworkTests.release(w,point));}h.succeed();}
    static void relays(GameTestHelper h){var w=FieldworkTests.vestige(h);var s=SanctuaryRoute.nearest(68*6144,68*6144);var at=SanctuaryRegions.station(s,3);FieldworkTests.platform(w,at);w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(s,3);var ledger=SanctuaryLedger.get(w.getServer());for(int i=0;i<3;i++)ledger.finish(s.key(),i);var p=FieldworkTests.player(h,w,at.east(2));var points=new ArrayList<BlockPos>();
        try{int[][] offsets={{-24,-24},{24,-24},{24,24},{-24,24}};for(var d:offsets){var point=new BlockPos(s.x()+SanctuaryRoute.x(3)+d[0],SanctuaryRoute.FLOOR+2,s.z()+SanctuaryRoute.z(3)+d[1]);points.add(point);FieldworkTests.platform(w,point);w.setBlock(point,SanctuaryContent.RELAY.get().defaultBlockState(),3);p.setPos(Vec3.atBottomCenterOf(point.south(2)));check(h,node.disarm(p,point)&&!w.getBlockState(point).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT),"A real authored relay is disabled by local interaction");}check(h,node.relays()==15&&!SanctuaryRoute.done(node.mask(),3),"Disarming surveillance does not fabricate the required guard victories");}finally{GameplayTests.cleanup(p);FieldworkTests.release(w,at);points.forEach(point->FieldworkTests.release(w,point));}h.succeed();}

    static void liveAi(GameTestHelper h) {
        var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(76*6144,76*6144);int index=7;
        var at=SanctuaryRegions.station(site,index);
        var center=new BlockPos(site.x()+SanctuaryRoute.x(index),SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(index));
        var platforms=new ArrayList<BlockPos>();
        for(int dx:new int[]{-16,0,16})for(int dz:new int[]{-16,0,16}){var p=center.offset(dx,0,dz);platforms.add(p);FieldworkTests.platform(w,p);}
        for(var p:BlockPos.betweenClosed(center.offset(-28,0,-28),center.offset(28,11,28)))w.setBlock(p,Blocks.AIR.defaultBlockState(),2);
        w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);
        var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(site,index);
        var ledger=SanctuaryLedger.get(w.getServer());for(int i=0;i<index;i++)ledger.finish(site.key(),i);
        var player=FieldworkTests.player(h,w,at.east(2));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE,1600,5));
        check(h,node.action(player,0),"The natural encounter starts for an ordinary survival player");node.tick();
        h.succeedWhen(()->{
            var enemies=w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(center).inflate(40),e->e.kind()==SanctuaryConstruct.Kind.COLOSSUS);
            check(h,enemies.stream().anyMatch(e->e.exposed()>0),"After awakening, native AI must navigate, target the player, prepare and resolve an attack");
            enemies.forEach(Entity::discard);GameplayTests.cleanup(player);platforms.forEach(p->FieldworkTests.release(w,p));
        });
    }

    static void traceGate(GameTestHelper h) {
        var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(70*6144,70*6144);
        var at=SanctuaryRegions.station(site,0);FieldworkTests.platform(w,at);
        w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);
        w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);
        var first=(SanctuaryNodeEntity)w.getBlockEntity(at);first.initialize(site,0);
        var nextAt=SanctuaryRegions.station(site,1);FieldworkTests.platform(w,nextAt);
        w.getChunkAt(nextAt).setData(SanctuaryRegions.GENERATED,true);
        w.setBlock(nextAt,SanctuaryContent.NODE.get().defaultBlockState(),3);
        var next=(SanctuaryNodeEntity)w.getBlockEntity(nextAt);next.initialize(site,1);
        var gate=new BlockPos(site.x()+SanctuaryRoute.x(1)-48,SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(1));
        w.setBlock(gate,SanctuaryContent.GATE.get().defaultBlockState(),3);
        var p=FieldworkTests.player(h,w,at.east(2));
        try {
            next.tick();check(h,w.getBlockState(gate).is(SanctuaryContent.GATE.get()),"An unearned threshold remains solid");
            for(var offset:new int[][]{{-24,8},{24,8},{0,-30}}) {
                var trace=new BlockPos(site.x()+SanctuaryRoute.x(0)+offset[0],SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(0)+offset[1]);
                w.setBlock(trace,SanctuaryContent.TRACE.get().defaultBlockState(),3);
                p.setPos(Vec3.atBottomCenterOf(trace.south(2)));
                w.getBlockState(trace).useWithoutItem(w,p,new BlockHitResult(Vec3.atCenterOf(trace),Direction.UP,trace,false));
            }
            check(h,first.traces()==7&&first.mask()==1,"Three real stele interactions complete only the first station");
            next.tick();check(h,w.getBlockState(gate).isAir(),"The next live station opens its native threshold after actual completion");
            check(h,!first.claim(p)&&p.getInventory().countItem(SanctuaryContent.CORE.get())==0,"A narrative room cannot fabricate a boss reward");
        } finally {GameplayTests.cleanup(p);FieldworkTests.release(w,at);FieldworkTests.release(w,nextAt);}
        h.succeed();
    }

    static void duoRewards(GameTestHelper h) {
        var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(72*6144,72*6144);int index=39;
        var at=SanctuaryRegions.station(site,index);FieldworkTests.platform(w,at);
        var center=new BlockPos(site.x()+SanctuaryRoute.x(index),SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(index));
        for(int dx:new int[]{-14,14}) {
            var spawn=center.offset(dx,0,-2);FieldworkTests.platform(w,spawn);
            for(var pos:BlockPos.betweenClosed(spawn.offset(-3,0,-3),spawn.offset(3,11,3)))w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        }
        w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);
        var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(site,index);
        var ledger=SanctuaryLedger.get(w.getServer());for(int i=0;i<index;i++)ledger.finish(site.key(),i);
        var p=FieldworkTests.player(h,w,at.east(2));var made=new ArrayList<SanctuaryConstruct>();
        check(h,node.action(p,0),"A local endgame station awakens the paired encounter");node.tick();node.tick();
        h.runAfterDelay(20,()->{try {
            made.addAll(w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(center).inflate(30),e->e.kind().pair()));
            check(h,made.size()==2&&node.spawned()==2,"Both guardians spawn: found="+made.size()+", spawned="+node.spawned()+", active="+node.active()+", players="+w.players().size()+", difficulty="+w.getDifficulty()+", pos="+p.position());
            made.get(0).hurtServer(w,w.damageSources().genericKill(),100000);
            check(h,node.killed()==1&&!SanctuaryRoute.done(node.mask(),index)&&p.getInventory().countItem(SanctuaryContent.CORE.get())==0,"One guardian's real death cannot complete or reward the duo");
            made.get(1).hurtServer(w,w.damageSources().genericKill(),100000);
            check(h,node.killed()==2&&SanctuaryRoute.done(node.mask(),index)&&p.getInventory().countItem(SanctuaryContent.CORE.get())==1,"Both actual deaths complete the encounter and give one eligible reward");
            check(h,!node.claim(p)&&!node.action(p,0),"A completed duo cannot respawn or duplicate its core");
        } finally {made.forEach(Entity::discard);GameplayTests.cleanup(p);FieldworkTests.release(w,at);for(int dx:new int[]{-14,14})FieldworkTests.release(w,center.offset(dx,0,-2));}
        h.succeed();});
    }

    static void linkedDials(GameTestHelper h) {
        var w=FieldworkTests.vestige(h);var site=SanctuaryRoute.nearest(74*6144,74*6144);int index=10;
        var at=SanctuaryRegions.station(site,index);FieldworkTests.platform(w,at);
        w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);
        var node=(SanctuaryNodeEntity)w.getBlockEntity(at);node.initialize(site,index);
        var ledger=SanctuaryLedger.get(w.getServer());for(int i=0;i<index;i++)ledger.finish(site.key(),i);
        var p=FieldworkTests.player(h,w,at.east(2));var dials=new ArrayList<BlockPos>();
        try {
            for(int n=0;n<4;n++){var pos=new BlockPos(site.x()+SanctuaryRoute.x(index)-12+n*8,SanctuaryRoute.FLOOR+2,site.z()+SanctuaryRoute.z(index)-14);dials.add(pos);w.setBlock(pos,SanctuaryContent.DIAL.get().defaultBlockState(),3);}
            var first=dials.get(0);p.setPos(Vec3.atBottomCenterOf(first.south(2)));
            w.getBlockState(first).useWithoutItem(w,p,new BlockHitResult(Vec3.atCenterOf(first),Direction.UP,first,false));
            check(h,w.getBlockState(first).getValue(SanctuaryDialBlock.TONE)==1&&w.getBlockState(dials.get(1)).getValue(SanctuaryDialBlock.TONE)==1&&w.getBlockState(dials.get(2)).getValue(SanctuaryDialBlock.TONE)==0,"The second act's real dial drives its successor and leaves the others alone");
            var menu=new SanctuaryMenu(3,p.getInventory(),node);check(h,menu.value(13)==2&&menu.value(14)==2&&menu.value(15)==1,"The station menu exposes actual coupled dial states without rotating them");
            p.setPos(Vec3.atBottomCenterOf(at.east(2)));check(h,node.action(p,4),"The local terminal can reset a coupled puzzle");
            for(var pos:dials)check(h,w.getBlockState(pos).getValue(SanctuaryDialBlock.TONE)==0,"Reset clears every physical dial");
            int[] clicks={2,2,1,1};
            for(int n=0;n<4;n++)for(int turn=0;turn<clicks[n];turn++){var pos=dials.get(n);p.setPos(Vec3.atBottomCenterOf(pos.south(2)));w.getBlockState(pos).useWithoutItem(w,p,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
            p.setPos(Vec3.atBottomCenterOf(at.east(2)));check(h,node.action(p,0)&&SanctuaryRoute.done(node.mask(),index),"Solving native coupled blocks and validating locally completes the puzzle");
        } finally {GameplayTests.cleanup(p);FieldworkTests.release(w,at);}
        h.succeed();
    }

}
