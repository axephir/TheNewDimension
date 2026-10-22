package fr.axephir.tnd.gametest;
import fr.axephir.tnd.city.*;
import fr.axephir.tnd.core.*;
import fr.axephir.tnd.technology.*;
import fr.axephir.tnd.world.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.function.Consumer;

final class CityTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> f){
        f.register("gate_monitor_guards",()->CityTests::guards);f.register("gate_monitor_sequence",()->CityTests::sequence);f.register("gate_monitor_cancel",()->CityTests::cancel);f.register("gate_monitor_storage",()->CityTests::storage);
        f.register("city_attacks",()->CityTests::attacks);f.register("city_drone_wall",()->CityTests::droneWall);f.register("city_reaver_interrupt",()->CityTests::interrupt);f.register("city_archives",()->CityTests::archives);f.register("city_template",()->CityTests::building);f.register("city_climate",()->CityTests::climate);f.register("city_ecology",()->CityTests::ecology);f.register("city_tiles",()->CityTests::tiles);
    }
    private static void check(GameTestHelper h,boolean b,String s){h.assertTrue(b,Component.literal(s));}
    static PortalMonitorEntity monitor(ServerLevel w,BlockPos at,boolean built){
        w.setBlock(at,CityContent.MONITOR.get().defaultBlockState(),3);
        if(built){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)w.setBlock(at.offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),3);for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})w.setBlock(at.offset(x,0,z),CityContent.DECOR.get("relay_coil").get().defaultBlockState(),3);}
        return (PortalMonitorEntity)w.getBlockEntity(at);
    }
    private static PortalControllerEntity portal(GameTestHelper h){var at=h.absolutePos(new BlockPos(16,2,16));h.getLevel().setBlock(at,ModContent.PORTAL_CONTROLLER.get().defaultBlockState(),3);return (PortalControllerEntity)h.getLevel().getBlockEntity(at);}
    static void guards(GameTestHelper h){
        var g=portal(h);var p=GameplayTests.player(h);var m=monitor(h.getLevel(),g.getBlockPos().west(5),false);p.setPos(Vec3.atCenterOf(m.getBlockPos().above()));var crystals=new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(),2);
        try{check(h,m.status()==1&&!g.activateTo(h.getLevel().getServer().getLevel(Level.END),crystals),"A monitor without frames cannot bypass assembly");
            monitor(h.getLevel(),m.getBlockPos(),true);m=(PortalMonitorEntity)h.getLevel().getBlockEntity(m.getBlockPos());m.setItem(0,crystals);check(h,m.status()==5&&!m.start(p),"Insufficient power refuses charging");
            m.energy.set(16000);check(h,m.status()==6,"An Overworld-only installation becomes ready at exactly 16000 FE");
            p.setPos(Vec3.atCenterOf(m.getBlockPos().east(15)));check(h,!m.start(p)&&!new PortalMonitorMenu(11,p.getInventory(),m,m.data).clickMenuButton(p,0),"Distant forged buttons cannot start the gate");
            check(h,m.getItem(0).getCount()==2&&m.energy.getAmountAsInt()==16000,"Rejected actions preserve physical crystals and power");
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    static void sequence(GameTestHelper h){
        var target=FieldworkTests.vestige(h);var g=portal(h);var m=monitor(h.getLevel(),g.getBlockPos().west(5),true);m.energy.set(20000);m.setItem(0,new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(),2));var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(m.getBlockPos().above()));
        check(h,m.start(p),"Nearby player starts the real monitor sequence");
        h.runAfterDelay(50,()->check(h,m.charging()&&g.destination()==null&&m.energy.getAmountAsInt()==20000&&m.getItem(0).getCount()==2,"Halfway animation has not created a link or charged its cost"));
        h.runAfterDelay(125,()->{try{check(h,g.destination()!=null&&g.destination().dimension().equals(DevelopmentCommands.DIMENSION)&&!m.charging(),"120 loaded ticks establish the actual Vestige pair");check(h,m.getItem(0).getCount()==1&&m.energy.getAmountAsInt()==4000,"Only successful completion spends exactly one crystal and 16000 FE");
            var partner=(PortalControllerEntity)target.getBlockEntity(g.destination().pos());check(h,g.travel(p),"Built source monitor permits actual travel");
            h.getLevel().destroyBlock(m.getBlockPos(),false);check(h,partner.travel(p)&&p.level()==h.getLevel(),"Return remains possible after the Overworld monitor is destroyed");check(h,!g.travel(p),"Destroyed monitor blocks new departures without stranding a returning player");
        }finally{GameplayTests.cleanup(p);}h.succeed();});
    }
    static void cancel(GameTestHelper h){
        FieldworkTests.vestige(h);var g=portal(h);var m=monitor(h.getLevel(),g.getBlockPos().west(5),true);m.energy.set(16000);m.setItem(0,new ItemStack(ModContent.COMPOSITE_CRYSTAL.get()));var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(m.getBlockPos().above()));check(h,m.start(p),"Sequence starts before deliberate disassembly");
        h.runAfterDelay(35,()->h.getLevel().destroyBlock(m.getBlockPos().offset(1,0,1),false));
        h.runAfterDelay(135,()->{try{check(h,!m.charging()&&g.destination()==null&&m.energy.getAmountAsInt()==16000&&m.getItem(0).getCount()==1,"Removing a live relay cancels without resource loss or partial linkage");check(h,!g.getBlockState().getValue(PortalControllerBlock.CHARGING),"Cancelled gate closes its animated membrane");}finally{GameplayTests.cleanup(p);}h.succeed();});
    }
    static void storage(GameTestHelper h){
        var g=portal(h);var m=monitor(h.getLevel(),g.getBlockPos().west(5),true);var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(m.getBlockPos().above()));var port=h.getLevel().getCapability(Capabilities.Energy.BLOCK,m.getBlockPos(),Direction.NORTH);
        try{try(var tx=Transaction.openRoot()){check(h,port.insert(20000,tx)==8000,"FE port caps each transfer");}check(h,m.energy.getAmountAsInt()==0,"Aborted FE transaction leaves no charge");
            m.energy.set(19000);m.setItem(0,new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(),3));m.start(p);
            var copy=(PortalMonitorEntity)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),m.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            check(h,copy.energy.getAmountAsInt()==19000&&copy.getItem(0).getCount()==3&&!copy.charging()&&copy.progress()==0,"Native save/reload preserves resources and never resumes unobserved charging");
            m.cancel();h.getLevel().destroyBlock(m.getBlockPos(),false);int dropped=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(m.getBlockPos()).inflate(3)).stream().filter(e->e.getItem().is(ModContent.COMPOSITE_CRYSTAL.get())).mapToInt(e->e.getItem().getCount()).sum();check(h,dropped==3,"Breaking the monitor drops exactly its real crystal inventory");
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    private static CityMob mob(GameTestHelper h,BlockPos at,CityMob.Kind k){var m=CityMobs.TYPES.get(k).get().create(h.getLevel(),EntitySpawnReason.EVENT);m.setPos(Vec3.atBottomCenterOf(at));h.getLevel().addFreshEntity(m);return m;}
    static void attacks(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,2,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(5)));var m=mob(h,at,CityMob.Kind.ENFORCER);float hp=p.getHealth();check(h,m.beginAttack(p)&&m.charge()==28,"Armoured guard announces its attack before any damage");
        h.runAfterDelay(40,()->{check(h,p.getHealth()<=hp-5&&m.recovery()>0,"Guard physically rushes to its locked mark and damages a stationary player");p.setHealth(20);p.invulnerableTime=0;m.discard();var r=mob(h,at,CityMob.Kind.REAVER);check(h,r.beginAttack(p),"Reaver prepares a distinct attack");p.setPos(Vec3.atBottomCenterOf(at.west(6)));
            h.runAfterDelay(40,()->{try{check(h,p.getHealth()==20&&r.recovery()>0,"Moving out of the marked rush avoids the Reaver; recovery grants counterplay");}finally{r.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);}h.succeed();});});
    }
    static void droneWall(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,2,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(7)));var d=mob(h,at.above(2),CityMob.Kind.DRONE);check(h,d.beginAttack(p),"Drone locks visible target");float hp=p.getHealth();
        for(var q:BlockPos.betweenClosed(at.offset(3,0,-3),at.offset(3,5,3)))h.getLevel().setBlock(q,Blocks.OBSIDIAN.defaultBlockState(),3);
        h.runAfterDelay(60,()->{check(h,p.getHealth()==hp,"A solid wall blocks the complete announced beam burst; HP="+p.getHealth()+" expected="+hp+" drone="+d.position()+" last attacker="+p.getLastHurtByMob());for(var q:BlockPos.betweenClosed(at.offset(3,0,-3),at.offset(3,5,3)))h.getLevel().setBlock(q,Blocks.AIR.defaultBlockState(),3);
            d.discard();var firing=mob(h,at.above(2),CityMob.Kind.DRONE);check(h,firing.beginAttack(p),"A fresh drone can aim without a wall");h.runAfterDelay(60,()->{try{check(h,p.getHealth()<hp,"Unblocked actual laser damages a stationary player");}finally{firing.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);}h.succeed();});});
    }
    static void interrupt(GameTestHelper h){var at=h.absolutePos(new BlockPos(16,2,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(5)));var m=mob(h,at,CityMob.Kind.REAVER);
        try{check(h,m.beginAttack(p),"Reaver wind-up begins");m.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),6);check(h,m.charge()==0&&m.recovery()==40,"Strong actual player hit interrupts wind-up and exposes recovery");}finally{m.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);}h.succeed();}
    static void archives(GameTestHelper h){var p=GameplayTests.player(h);var other=GameplayTests.player(h);
        try{p.setData(Codex.NOTES,0);other.setData(Codex.NOTES,0);long xp=p.getData(PlayerProgression.PROFILE).get(Mastery.ANCIENT_TECH).experience();for(int i=0;i<8;i++){Codex.collect(p,i);Codex.collect(p,i);}check(h,p.getData(Codex.NOTES)==255&&p.getData(PlayerProgression.PROFILE).get(Mastery.ANCIENT_TECH).experience()==xp+96,"Eight first readings award exactly 96 XP; repeats award nothing");
            var menu=new CodexMenu(13,p.getInventory(),p,7);check(h,!menu.clickMenuButton(other,100)&&!menu.clickMenuButton(p,99),"Forged owner and invalid archive indices are rejected");check(h,menu.clickMenuButton(p,100)&&!menu.clickMenuButton(p,100)&&p.getInventory().countItem(CityContent.DECOR.get("nacre_memorial").get().asItem())==1,"Memorial is granted once through the real server menu");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());p.saveWithoutId(out);var clone=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),p.getGameProfile(),p.clientInformation());clone.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));check(h,clone.getData(Codex.NOTES)==511,"Native serialization preserves all records and the claimed trophy");other.restoreFrom(p,false);check(h,other.getData(Codex.NOTES)==511,"Native death restoration preserves discoveries and claim state");
        }finally{GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();}
    static void building(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var origin=new BlockPos(30000,80,30000);var template=w.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","neon_district")).orElseThrow();check(h,template.getSize().equals(new Vec3i(127,111,127)),"Real city template is loaded at full skyline scale");
        for(int x=origin.getX()>>4;x<=(origin.getX()+126)>>4;x++)for(int z=origin.getZ()>>4;z<=(origin.getZ()+126)>>4;z++){w.getChunk(x,z);w.setChunkForced(x,z,true);}
        check(h,template.placeInWorld(w,origin,origin,new StructurePlaceSettings(),w.getRandom(),2),"Complete city places with native block entities");
        var walker=EntityTypes.ZOMBIE.create(w,EntitySpawnReason.EVENT);walker.setNoAi(true);walker.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(256);w.addFreshEntity(walker);var nav=(net.minecraft.world.entity.ai.navigation.GroundPathNavigation)walker.getNavigation();nav.setRequiredPathLength(256);nav.setMaxVisitedNodesMultiplier(64);
        try{
            for(int floor=0;floor<12;floor++){
                walker.setPos(Vec3.atBottomCenterOf(origin.offset(47,floor*8+1,13)));walker.setOnGround(true);
                var path=nav.createPath(origin.offset(47,(floor+1)*8+1,13),0);check(h,path!=null&&path.canReach(),"Native walking route climbs central tower floor "+floor+"; end="+(path==null?"null":path.getEndNode()));
            }
            check(h,w.getBlockState(origin.offset(57,97,11)).is(CityContent.ARCHIVE.get())&&w.getBlockState(origin.offset(57,97,11)).getValue(ArchiveTerminalBlock.ENTRY)==7,"Final record stands on the accessible central roof");
            check(h,w.getBlockEntity(origin.offset(76,97,35)) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"Roof treasure remains a functional native chest");
        }finally{walker.discard();for(int x=origin.getX()>>4;x<=(origin.getX()+126)>>4;x++)for(int z=origin.getZ()>>4;z<=(origin.getZ()+126)>>4;z++)w.setChunkForced(x,z,false);}h.succeed();
    }
    static void tiles(GameTestHelper h){
        var w=FieldworkTests.vestige(h);int lane=0;
        for(var rotation:Rotation.values()){
            var centre=new BlockPos(34000+lane++*400,100,34000);var piece=new CrownedSitePiece(w.getStructureManager(),Identifier.fromNamespaceAndPath("tnd","neon_district"),centre,52,1,rotation);var origin=piece.buildingOrigin();var pivot=new BlockPos(63,0,63);
            var terminal=StructureTemplate.transform(new BlockPos(57,97,11),Mirror.NONE,rotation,pivot).offset(origin);
            var chest=StructureTemplate.transform(new BlockPos(76,97,35),Mirror.NONE,rotation,pivot).offset(origin);
            var marker=terminal.offset(40,0,40);w.setBlock(marker,Blocks.GOLD_BLOCK.defaultBlockState(),2);
            for(var target:List.of(terminal,chest)){
                var chunk=new ChunkPos(target.getX()>>4,target.getZ()>>4);w.getChunk(chunk.x(),chunk.z());var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(chunk.getMinBlockX(),1,chunk.getMinBlockZ(),chunk.getMaxBlockX(),254,chunk.getMaxBlockZ());
                piece.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),w.getRandom(),clip,chunk,centre);
            }
            check(h,w.getBlockState(terminal).is(CityContent.ARCHIVE.get())&&w.getBlockState(terminal).getValue(ArchiveTerminalBlock.ENTRY)==7,"Clipped city tiles preserve the final record under rotation "+rotation);
            check(h,w.getBlockEntity(chest) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"Clipped city tiles preserve native roof chest NBT under rotation "+rotation);
            check(h,w.getBlockState(marker).is(Blocks.GOLD_BLOCK),"City generation writes only into its current chunk");
        }h.succeed();
    }
    static void ecology(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var at=FieldworkTests.center(h,27);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at.east(6));w.waitForEntities(new ChunkPos(at.getX()>>4,at.getZ()>>4),0);var made=new ArrayList<CityMob>();
        var clock=w.dimensionType().defaultClock().orElseThrow();long time=w.getServer().clockManager().getTotalTicks(clock);
        try{
            var biome=w.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("tnd","neon_city")));
            net.minecraft.server.commands.FillBiomeCommand.fill(w,at.offset(-8,-3,-8),at.offset(8,6,8),biome);
            w.setBlock(at.below(),CityContent.DECOR.get("city_tiles").get().defaultBlockState(),3);w.setBlock(at.above(3),CityContent.DECOR.get("neon_cyan").get().defaultBlockState(),3);
            var type=CityMobs.TYPES.get(CityMob.Kind.ENFORCER).get();
            for(long t:new long[]{2000,30000}){w.getServer().clockManager().setTotalTicks(clock,t);check(h,CityMobs.spawn(type,w,EntitySpawnReason.NATURAL,at,w.getRandom()),"City patrols can spawn by neon in daylight and at night");}
            check(h,!CityMobs.spawn(type,h.getLevel(),EntitySpawnReason.NATURAL,h.absolutePos(new BlockPos(8,2,8)),w.getRandom()),"City patrols cannot naturally invade the Overworld");
            for(var kind:CityMob.Kind.values())for(int i=0;i<2;i++){var m=CityMobs.TYPES.get(kind).get().create(w,EntitySpawnReason.EVENT);m.setNoAi(true);m.setPos(Vec3.atBottomCenterOf(at.offset(i*2,0,kind.ordinal()*2)));w.addFreshEntity(m);made.add(m);}
            check(h,!CityMobs.spawn(type,w,EntitySpawnReason.NATURAL,at,w.getRandom()),"Six nearby security mobs enforce the local population cap");
            for(int i=0;i<made.size();i++)made.get(i).hurtServer(w,i%2==0?p.damageSources().playerAttack(p):w.damageSources().genericKill(),1000);
            int processors=0,filaments=0;for(var item:w.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(12))){if(item.getItem().is(CityContent.PROCESSOR.get()))processors+=item.getItem().getCount();if(item.getItem().is(CityContent.FILAMENT.get()))filaments+=item.getItem().getCount();}
            check(h,processors>=2&&processors<=4&&filaments>=1&&filaments<=2,"Actual native player kills yield useful salvage; environmental kills do not duplicate it");
        }finally{for(var m:made)m.discard();GameplayTests.cleanup(p);w.getServer().clockManager().setTotalTicks(clock,time);FieldworkTests.release(w,at);}h.succeed();
    }
    static void climate(GameTestHelper h){
        var w=h.getLevel();try(var reader=w.getServer().getResourceManager().getResourceOrThrow(Identifier.fromNamespaceAndPath("tnd","dimension/vestige.json")).openAsReader()){
            var stem=net.minecraft.world.level.dimension.LevelStem.CODEC.parse(RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,w.registryAccess()),com.google.gson.JsonParser.parseReader(reader)).getOrThrow();var gen=stem.generator();var random=RandomState.create(w.registryAccess(),ResourceKey.create(Registries.NOISE_SETTINGS,Identifier.fromNamespaceAndPath("tnd","vestige")),987654);var bounds=LevelHeightAccessor.create(0,256);boolean found=false;
            var holder=w.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("tnd","neon_district")));
            for(int x=-4096;x<=4096&&!found;x+=128)for(int z=-4096;z<=4096&&!found;z+=128){
                if(!gen.getBiomeSource().getNoiseBiome(x>>2,24,z>>2,random.sampler()).unwrapKey().orElseThrow().identifier().getPath().equals("neon_city"))continue;
                var start=holder.value().generate(holder,DevelopmentCommands.DIMENSION,w.registryAccess(),gen,gen.getBiomeSource(),random,w.getServer().getStructureManager(),987654,new ChunkPos(x>>4,z>>4),0,bounds,b->b.unwrapKey().orElseThrow().identifier().getPath().equals("neon_city"));
                if(start.isValid()){check(h,start.getBoundingBox().maxY()<254&&start.getBoundingBox().minY()>0,"Full skyscrapers plus mineral crown fit actual authored terrain bounds");found=true;}
            }check(h,found,"A real urban climate region admits a naturally generated crowned city");
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}h.succeed();
    }
}
