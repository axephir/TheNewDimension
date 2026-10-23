package fr.axephir.tnd.gametest;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.sanctuary.*;
import fr.axephir.tnd.technology.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.*;
import java.util.function.Consumer;

final class FoundryTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> f){
        f.register("foundry_circuit",()->FoundryTests::circuit);f.register("foundry_generation",()->FoundryTests::generation);f.register("foundry_route",()->FoundryTests::route);f.register("foundry_restore",()->FoundryTests::restore);f.register("foundry_bosses",()->FoundryTests::bosses);f.register("foundry_patterns",()->FoundryTests::patterns);
        f.register("foundry_shortcut",()->FoundryTests::shortcut);
        f.register("tempering_process",()->FoundryTests::process);f.register("tempering_guards",()->FoundryTests::guards);f.register("tempering_restore",()->FoundryTests::machineRestore);f.register("tempering_energy",()->FoundryTests::energy);
    }
    private static void check(GameTestHelper h,boolean b,String m){h.assertTrue(b,Component.literal(m));}
    private static final class Fixture implements AutoCloseable {
        final ServerLevel w;final SanctuaryRoute.Site site;SanctuaryNodeEntity n;
        final List<BlockPos> platforms=new ArrayList<>();final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int cell,int index){w=FieldworkTests.vestige(h);site=SanctuaryRoute.nearest(cell*6144,cell*6144);var at=SanctuaryRegions.station(site,index);platform(at);w.getChunkAt(at).setData(SanctuaryRegions.GENERATED,true);w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);n=(SanctuaryNodeEntity)w.getBlockEntity(at);n.initializeWitness(site,index);for(int i=0;i<index;i++)SanctuaryLedger.get(w.getServer()).finish(site.key(),i);}
        void node(){var at=SanctuaryRegions.station(site,n.index());w.setBlock(at,SanctuaryContent.NODE.get().defaultBlockState(),3);var fresh=(SanctuaryNodeEntity)w.getBlockEntity(at);fresh.initializeWitness(site,n.index());n=fresh;}
        void platform(BlockPos p){FieldworkTests.platform(w,p);platforms.add(p);}
        ServerPlayer player(GameTestHelper h,BlockPos at){var p=FieldworkTests.player(h,w,at);players.add(p);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE,1200,5));return p;}
        void valves(){for(int i=0;i<FoundryAct.valves(n.index());i++){var p=FoundryScene.point(n,FoundryAct.valve(n.index(),i));platform(p);w.setBlock(p,SanctuaryContent.FOUNDRY_VALVE.get().defaultBlockState(),3);}}
        public void close(){w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(n.getBlockPos()).inflate(70)).forEach(e->e.discard());players.forEach(GameplayTests::cleanup);platforms.forEach(p->FieldworkTests.release(w,p));}
    }
    static void circuit(GameTestHelper h){try(var f=new Fixture(h,110,8)){
        var p=f.player(h,f.n.getBlockPos().east(2));
        for(int[] d:new int[][]{{-24,8},{24,8},{0,-30}}){var at=f.n.local(d[0],1,d[1]);f.platform(at);f.w.setBlock(at,SanctuaryContent.TRACE.get().defaultBlockState(),3);p.setPos(Vec3.atBottomCenterOf(at.south(2)));f.n.trace(p,at);}
        check(h,f.n.traces()==7&&!SanctuaryRoute.done(f.n.mask(),8),"The new vestibule cannot skip its physical three-valve circuit");f.valves();
        var first=FoundryScene.point(f.n,FoundryAct.valve(8,0));p.setPos(Vec3.atBottomCenterOf(first.south(2)));f.w.setBlock(first.south().above(),Blocks.STONE.defaultBlockState(),3);
        check(h,!FoundryScene.use(f.n,p,first),"An occluded valve cannot be operated through a wall");f.w.setBlock(first.south().above(),Blocks.AIR.defaultBlockState(),3);
        for(int i=0;i<3;i++){var at=FoundryScene.point(f.n,FoundryAct.valve(8,i));p.setPos(Vec3.atBottomCenterOf(at.south(2)));for(int t=0;t<2;t++)check(h,FoundryScene.use(f.n,p,at),"A real nearby valve turns exactly one setting");}
        check(h,FoundryScene.circuit(f.n)==7&&SanctuaryRoute.done(f.n.mask(),8),"Actual alignment and three traces complete only the earned chamber");
        check(h,!FoundryScene.use(f.n,p,FoundryScene.point(f.n,FoundryAct.valve(8,2))),"The earned circuit remains latched");
    }h.succeed();}
    static void generation(GameTestHelper h){try(var f=new Fixture(h,112,12)){
        var floor=f.n.local(12,-27,-10);new SanctuaryFeature().generate(f.w,floor);check(h,f.w.getBlockState(floor).is(SanctuaryContent.block("amber_seal")),"The real well has a grounded luminous lower foundation");
        var ladder=f.n.local(24,-8,-18);new SanctuaryFeature().generate(f.w,ladder);check(h,f.w.getBlockState(ladder).is(Blocks.LADDER)&&!f.w.getBlockState(ladder.east()).isAir(),"The deep well has a real supported escape ladder");
        var valve=FoundryScene.point(f.n,FoundryAct.valve(12,0));new SanctuaryFeature().generate(f.w,valve);check(h,f.w.getBlockState(valve).is(SanctuaryContent.FOUNDRY_VALVE.get())&&!f.w.getBlockState(valve.below()).isAir(),"Elevated circuit controls stand on an accessible gallery");
        new SanctuaryFeature().generate(f.w,f.n.getBlockPos());f.n=(SanctuaryNodeEntity)f.w.getBlockEntity(f.n.getBlockPos());check(h,f.n.foundry()&&f.n.natural(),"Only newly authored natural stations use the foundry objectives");
    }h.succeed();}
    private static net.minecraft.world.level.block.state.BlockState sample(int x,int y,int z){return SanctuaryInterior.sample(x,y,z,12,FoundryAct.height(12));}
    static void route(GameTestHelper h){
        for(int index:new int[]{8,12,13,15})for(int i=0;i<FoundryAct.valves(index);i++){
            var p=FoundryAct.valve(index,i);int height=FoundryAct.height(index);
            check(h,!SanctuaryInterior.sample(p.x(),p.y()-1,p.z()+2,index,height).isAir(),"Valve standing surface in room "+index+", device "+i);
            for(int y=p.y();y<=p.y()+1;y++)check(h,SanctuaryInterior.sample(p.x(),y,p.z()+2,index,height).isAir(),"Valve approach is obstructed in room "+index+", device "+i+", height "+y);
        }

        for(int side:new int[]{-1,1}){
            for(int y=-26;y<=0;y++)check(h,sample(side*24,y,-18).is(Blocks.LADDER),"Each emergency ladder extends continuously through the entire wall");
            for(int y=1;y<=2;y++)check(h,sample(side*24,y,-18).isAir()&&sample(side*25,y,-18).isAir(),"No turbine footing caps the ladder or its upper dismount");
            check(h,!sample(side*25,0,-18).isAir(),"Each emergency ascent ends at a solid upper landing");
        }

        for(int side:new int[]{-1,1})for(int step=0;step<10;step++){
            int z=-31+2*step,y=1+step;check(h,sample(side*42,y,z).is(SanctuaryContent.STAIRS.get())&&sample(side*42,y+1,z).isAir()&&sample(side*42,y+2,z).isAir(),"Every lower flight tread has full walking headroom");
            int x=-31+2*step,yy=11+step;check(h,sample(x,yy,side*42).is(SanctuaryContent.STAIRS.get())&&sample(x,yy+1,side*42).isAir()&&sample(x,yy+2,side*42).isAir(),"Every second-gallery tread has full walking headroom");
        }
        for(int i=0;i<4;i++){var at=FoundryAct.resonance(i);check(h,!sample(at.x(),at.y()-1,at.z()).isAir()&&sample(at.x(),at.y(),at.z()).isAir()&&sample(at.x(),at.y()+1,at.z()).isAir(),"Every route resonance has a grounded unobstructed landing");}
        try(var f=new Fixture(h,114,12)){
            f.valves();var p=f.player(h,f.n.getBlockPos().east(2));check(h,f.n.action(p,0),"The gallery route can be started before adjusting the valves along the route");
            for(int i=0;i<4;i++){var at=f.n.resonance();f.platform(at);p.setPos(Vec3.atBottomCenterOf(at));f.n.tick();}
            check(h,f.n.sequence()==4&&!SanctuaryRoute.done(f.n.mask(),12),"Four resonances alone cannot skip the three physical valves");f.n.tick();check(h,f.n.sequence()==4,"A waiting circuit never advances the route counter beyond four");
            f.valves();for(int i=0;i<3;i++){var at=FoundryScene.point(f.n,FoundryAct.valve(12,i));p.setPos(Vec3.atBottomCenterOf(at.south(2)));for(int t=0;t<2;t++)check(h,FoundryScene.use(f.n,p,at),"Route valves can be adjusted from their real landings");}f.n.tick();
            check(h,SanctuaryRoute.done(f.n.mask(),12),"Only the combined actual walk and restored circuit complete the well");
        }h.succeed();
    }
    static void restore(GameTestHelper h){try(var f=new Fixture(h,116,13)){
        var tag=f.n.saveWithFullMetadata(f.w.registryAccess());tag.putBoolean("foundryLatched",true);tag.putInt("foundryCooling",120);tag.putInt("foundrySpan",6);var loaded=(SanctuaryNodeEntity)BlockEntity.loadStatic(f.n.getBlockPos(),f.n.getBlockState(),tag,f.w.registryAccess());f.n.setRemoved();loaded.setLevel(f.w);f.w.setBlockEntity(loaded);f.n=loaded;var p=f.player(h,f.n.getBlockPos().east(2));var menu=new SanctuaryMenu(1,p.getInventory(),loaded);
        check(h,loaded.foundry()&&menu.value(24)==1&&menu.value(25)==6&&menu.value(26)==6,"Native serialization retains the edition, latched circuit and vent cooldown");
        tag.remove("foundryAct");var legacy=(SanctuaryNodeEntity)BlockEntity.loadStatic(f.n.getBlockPos(),f.n.getBlockState(),tag,f.w.registryAccess());check(h,!legacy.foundry(),"An old archive station is not assigned missing new mechanisms");
    }h.succeed();}
    static void bosses(GameTestHelper h){var f=new Fixture(h,118,15);for(int x:new int[]{-16,0,16})for(int z:new int[]{-16,0,16})f.platform(f.n.local(x,1,z));f.node();f.valves();var p=f.player(h,f.n.getBlockPos().east(2));check(h,f.n.action(p,0),"A survival player deliberately wakes the duo");f.n.tick();f.n.tick();
        h.runAfterDelay(3,()->{
            var made=f.w.getEntitiesOfClass(SanctuaryConstruct.class,new AABB(f.n.getBlockPos()).inflate(60),e->e.kind().foundryPair());check(h,made.size()==2&&made.get(0).kind()!=made.get(1).kind(),"The real new station owns two distinct finite bosses");check(h,made.stream().allMatch(e->e.awakening()>0),"Both guardians emerge before they can attack");
            h.runAfterDelay(115,()->{
                var valve=FoundryScene.point(f.n,FoundryAct.valve(15,0));p.setPos(Vec3.atBottomCenterOf(valve.south(2)));check(h,FoundryScene.use(f.n,p,valve)&&made.stream().allMatch(e->e.exposed()>=95),"The actual cooling valve opens both living boss cores");check(h,!FoundryScene.use(f.n,p,valve),"The shared pressure cooldown prevents valve spam");
                made.get(0).hurtServer(f.w,f.w.damageSources().genericKill(),100000);check(h,f.n.killed()==1&&!SanctuaryRoute.done(f.n.mask(),15)&&p.getInventory().countItem(SanctuaryContent.FOUNDRY_HEART.get())==0,"Defeating only one partner cannot grant the unique foundry heart");
                h.runAfterDelay(2,()->{try{check(h,made.get(1).phase()==2,"The survivor enters its changed final phase");made.get(1).hurtServer(f.w,f.w.damageSources().genericKill(),100000);check(h,SanctuaryRoute.done(f.n.mask(),15)&&p.getInventory().countItem(SanctuaryContent.FOUNDRY_HEART.get())==1&&!f.n.claim(p),"Both real defeats grant exactly one personal heart and cannot repeat the reward");h.succeed();}finally{f.close();}});
            });
        });
    }
    static void patterns(GameTestHelper h){
        var w=h.getLevel();var start=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(8,4,5)));var p=GameplayTests.player(h);p.setPos(start.add(0,0,15));p.setNoGravity(true);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE,300,5));
        var founder=SanctuaryEntities.TYPES.get(SanctuaryConstruct.Kind.FOUNDER).get().create(w,EntitySpawnReason.EVENT);var regulator=SanctuaryEntities.TYPES.get(SanctuaryConstruct.Kind.REGULATOR).get().create(w,EntitySpawnReason.EVENT);founder.setPos(start);regulator.setPos(start.add(3,0,15));founder.setNoGravity(true);regulator.setNoGravity(true);w.addFreshEntity(founder);w.addFreshEntity(regulator);
        h.runAfterDelay(2,()->{check(h,founder.beginAttack(p)&&founder.pattern()==5&&founder.threatens(p.position())&&!founder.threatens(p.position().add(6,0,0)),"The founder snapshots a double-ring hammer mark with a real escape route");check(h,!regulator.beginAttack(p),"A partner cannot overlap a new wind-up with the founder's current attack");float before=regulator.integrity();
            h.runAfterDelay(65,()->{try{check(h,regulator.exposed()>0&&regulator.integrity()<before,"Luring the partner into the founder's actual impact exposes and damages it");h.succeed();}finally{founder.discard();regulator.discard();GameplayTests.cleanup(p);}});
        });
    }
    static void shortcut(GameTestHelper h){var f=new Fixture(h,120,12);
        for(int x=-32;x<=32;x+=16)new SanctuaryFeature().generate(f.w,f.n.local(x,20,-23));
        for(int x:new int[]{-29,29})new SanctuaryFeature().generate(f.w,f.n.local(x,20,-23));
        var p=f.player(h,f.n.getBlockPos().east(2));var obstruction=f.n.local(27,20,-23);f.w.setBlock(obstruction,Blocks.GOLD_BLOCK.defaultBlockState(),3);SanctuaryLedger.get(f.w.getServer()).finish(f.site.key(),12);
        h.runAfterDelay(45,()->{check(h,new SanctuaryMenu(1,p.getInventory(),f.n).value(26)==0&&f.w.getBlockState(obstruction).is(Blocks.GOLD_BLOCK),"The upper shortcut pauses before a foreign block without destroying it");f.w.setBlock(obstruction,Blocks.AIR.defaultBlockState(),3);});
        h.runAfterDelay(180,()->{try{
            check(h,new SanctuaryMenu(1,p.getInventory(),f.n).value(26)==10,"An earned real shortcut deploys all ten saved stages");
            for(int x=-27;x<=27;x++)check(h,f.w.getBlockState(f.n.local(x,20,-23)).is(SanctuaryContent.block("obsidian_grate"))&&f.w.getBlockState(f.n.local(x,21,-23)).isAir()&&f.w.getBlockState(f.n.local(x,22,-23)).isAir(),"Every shortcut column has a solid floor and actual walking headroom");
            check(h,f.w.getBlockState(f.n.local(-29,21,-23)).isAir()&&f.w.getBlockState(f.n.local(29,23,-23)).isAir(),"Both authored upper seals clear after deployment");h.succeed();
        }finally{f.close();}});
    }
    private static TemperingForgeEntity forge(GameTestHelper h){var at=h.absolutePos(new BlockPos(8,3,8));h.getLevel().setBlock(at,TemperingContent.BLOCK.get().defaultBlockState(),3);return (TemperingForgeEntity)h.getLevel().getBlockEntity(at);}
    private static void build(GameTestHelper h,TemperingForgeEntity e){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)h.getLevel().setBlock(e.getBlockPos().offset(x,-1,z),SanctuaryContent.block("obsidian_tiles").defaultBlockState(),3);for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){h.getLevel().setBlock(e.getBlockPos().offset(x,0,z),SanctuaryContent.block("obsidian_pillar").defaultBlockState(),3);h.getLevel().setBlock(e.getBlockPos().offset(x,1,z),SanctuaryContent.block("amber_seal").defaultBlockState(),3);}}
    private static ItemStack weapon(GameTestHelper h){var s=new ItemStack(Items.NETHERITE_SWORD);s.set(DataComponents.CUSTOM_NAME,Component.literal("Mémoire de contrôle"));s.setDamageValue(147);s.enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),3);return s;}
    private static void feed(TemperingForgeEntity e,ItemStack weapon){e.setItem(0,weapon);e.setItem(1,new ItemStack(SanctuaryContent.SHARD.get(),8));e.setItem(2,new ItemStack(SanctuaryContent.DUST.get(),2));e.energy.set(100000);}
    static void process(GameTestHelper h){var e=forge(h);build(h,e);var original=weapon(h);feed(e,original.copy());var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(e.getBlockPos().north(2)));check(h,e.action(p,0)&&e.action(p,3),"A complete powered forge accepts a deliberate exclusive temper");
        h.runAfterDelay(230,()->{try{var out=e.getItem(3);check(h,!out.isEmpty()&&out.getDamageValue()==147&&Objects.equals(out.get(DataComponents.CUSTOM_NAME),original.get(DataComponents.CUSTOM_NAME))&&Objects.equals(out.get(DataComponents.ENCHANTMENTS),original.get(DataComponents.ENCHANTMENTS)),"The actual processing cycle retains the item name, real enchantment and exact wear");check(h,e.energy.getAmountAsInt()==20000&&e.getItem(0).isEmpty()&&e.getItem(1).isEmpty()&&e.getItem(2).isEmpty(),"Exactly 80000 FE, eight shards and two purified auralite are consumed once");check(h,e.status()==6,"A completed item tells the player to collect the output before requesting new inputs");check(h,!TemperingForgeEntity.eligible(out)&&TemperingForgeEntity.tempered(out,1).isEmpty(),"An already tempered item cannot stack a second or different exclusive bonus");check(h,out.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(m->m.modifier().is(TemperingForgeEntity.MODIFIER)&&m.slot()==EquipmentSlotGroup.MAINHAND&&m.modifier().amount()==.12),"The output carries a real native main-hand damage modifier");h.succeed();}finally{GameplayTests.cleanup(p);}});
    }
    static void guards(GameTestHelper h){var e=forge(h);feed(e,weapon(h));var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(e.getBlockPos().north(2)));check(h,e.action(p,0)&&!e.action(p,3)&&e.status()==1,"An incomplete multiblock cannot consume a weapon or FE");build(h,e);e.energy.set(79999);check(h,!e.action(p,3)&&e.status()==7,"One missing FE prevents irreversible work");e.energy.set(100000);e.setItem(3,new ItemStack(Items.STONE));check(h,!e.action(p,3)&&e.status()==6,"A blocked output cannot be overwritten");e.removeItemNoUpdate(3);check(h,e.action(p,3),"Clearing the true output permits work");e.removeItemNoUpdate(0);check(h,!e.action(p,3)&&e.getItem(3).isEmpty()&&e.energy.getAmountAsInt()==100000,"Removing the input cancels its pending work without fabricated equipment");check(h,!TemperingForgeEntity.eligible(new ItemStack(Items.STONE))&&!TemperingForgeEntity.eligible(new ItemStack(Items.ELYTRA)),"Only durable combat weapons and tools with main-hand attacks can be tempered");p.setPos(Vec3.atCenterOf(e.getBlockPos().north(20)));check(h,!e.action(p,1),"Distant forged menu actions are rejected by the server");GameplayTests.cleanup(p);h.succeed();}
    static void machineRestore(GameTestHelper h){var e=forge(h);build(h,e);feed(e,weapon(h));var p=GameplayTests.player(h);p.setPos(Vec3.atCenterOf(e.getBlockPos().north(2)));e.action(p,2);e.action(p,3);
        h.runAfterDelay(55,()->{var tag=e.saveWithFullMetadata(h.getLevel().registryAccess());int progress=new TemperingMenu(1,p.getInventory(),e,e.data).progress();var loaded=(TemperingForgeEntity)BlockEntity.loadStatic(e.getBlockPos(),e.getBlockState(),tag,h.getLevel().registryAccess());e.setRemoved();loaded.setLevel(h.getLevel());h.getLevel().setBlockEntity(loaded);check(h,loaded.data.get(0)==3&&loaded.data.get(1)==progress&&loaded.energy.getAmountAsInt()==100000,"Native save and reload retain choice, loaded work and unspent FE");
            h.runAfterDelay(220,()->{try{check(h,!loaded.getItem(3).isEmpty()&&loaded.energy.getAmountAsInt()==20000&&!TemperingForgeEntity.eligible(loaded.getItem(3)),"A resumed real cycle produces exactly one exclusive tempered item");h.succeed();}finally{GameplayTests.cleanup(p);}});
        });
    }
    static void energy(GameTestHelper h){var e=forge(h);build(h,e);var w=h.getLevel();var port=w.getCapability(Capabilities.Energy.BLOCK,e.getBlockPos(),Direction.NORTH);check(h,port!=null,"The new forge exposes native transactional FE");try(var tx=Transaction.openRoot()){check(h,port.insert(5000,tx)==5000,"The FE port accepts a bounded transfer");}check(h,e.energy.getAmountAsInt()==0,"Aborting a transfer leaves no phantom FE");
        var batteryAt=e.getBlockPos().north(4);w.setBlock(batteryAt,Industry.BLOCKS.get(Industry.Kind.BATTERY).get().defaultBlockState(),3);var battery=(IndustrialEntity)w.getBlockEntity(batteryAt);battery.energy.set(20000);for(int d=1;d<4;d++)w.setBlock(e.getBlockPos().north(d),Industry.CABLE.get().defaultBlockState(),3);
        var bottom=e.getBlockPos().below();w.setBlock(bottom,Blocks.HOPPER.defaultBlockState(),3);e.setItem(3,new ItemStack(Items.NETHERITE_SWORD));
        h.runAfterDelay(50,()->{check(h,e.built()&&e.getItem(3).isEmpty()&&((HopperBlockEntity)w.getBlockEntity(bottom)).countItem(Items.NETHERITE_SWORD)==1,"The multiblock leaves a real hopper outlet under its centre");check(h,e.energy.getAmountAsInt()>0&&battery.energy.getAmountAsInt()+e.energy.getAmountAsInt()==20000,"Real cables deliver exactly conserved FE to the new forge");h.succeed();});
    }
}
