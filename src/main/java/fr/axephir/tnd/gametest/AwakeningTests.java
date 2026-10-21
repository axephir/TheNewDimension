package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.*;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.technology.*;
import fr.axephir.tnd.world.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;

/** Native actions, elapsed processing, real hopper IO, physical attack damage and authored paths. */
final class AwakeningTests {
    private static void check(GameTestHelper h,boolean b,String message){h.assertTrue(b,Component.literal(message));}
    private static AwakenedMob mob(GameTestHelper h,BlockPos at,boolean heavy){var m=(heavy?VestigeEntities.LITHOGUARD:VestigeEntities.STALKER).get().create(h.getLevel(),EntitySpawnReason.EVENT);m.setPos(Vec3.atBottomCenterOf(at));h.getLevel().addFreshEntity(m);return m;}
    static void materials(GameTestHelper h){
        var w=h.getLevel();var at=h.absolutePos(new BlockPos(2,1,2));w.setBlock(at,Blocks.FURNACE.defaultBlockState(),3);var f=(FurnaceBlockEntity)w.getBlockEntity(at);
        f.setItem(0,new ItemStack(AwakeningContent.RAW_AURALITE.get()));f.setItem(1,new ItemStack(Items.COAL));
        var ore=AwakeningContent.AURALITE_ORE.get().defaultBlockState();
        check(h,!new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(ore)&&new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore),"Both minerals require native iron mining tiers");
        check(h,fr.axephir.tnd.progression.ProgressionEvents.isOre(ore),"Common ore tags include new minerals in mining mastery and placed-block tracking");
        h.runAfterDelay(225,()->{check(h,f.getItem(2).is(AwakeningContent.AURALITE_INGOT.get())&&f.getItem(2).getCount()==1,"A real vanilla furnace smelts the registered raw mineral into its actual ingot");h.succeed();});
    }
    static void ores(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var at=FieldworkTests.center(h,17);FieldworkTests.platform(w,at);int x=at.getX()&~15,z=at.getZ()&~15;
        try{for(var name:java.util.List.of("silent_expanse","glass_woodland")){
            for(var p:BlockPos.betweenClosed(new BlockPos(x,119,z),new BlockPos(x+15,119,z+15)))w.setBlock(p,ModContent.ASHSTONE.get().defaultBlockState(),2);
            var biome=w.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("tnd",name)));
            net.minecraft.server.commands.FillBiomeCommand.fill(w,new BlockPos(x,116,z),new BlockPos(x+15,124,z+15),biome);
            net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(w.getChunk(x>>4,z>>4),java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
            int made=0;for(int seed=0;seed<10&&made==0;seed++)made+=AwakeningOres.generate(w,new BlockPos(x,120,z),net.minecraft.util.RandomSource.create(seed));
            check(h,made>0,"Surface vein generation finds valid native habitat: "+name);
            var expected=name.equals("silent_expanse")?AwakeningContent.AURALITE_ORE:AwakeningContent.NOCTILITE_ORE;
            int found=0;for(var p:BlockPos.betweenClosed(new BlockPos(x,119,z),new BlockPos(x+15,119,z+15)))if(w.getBlockState(p).is(expected.get())){found++;check(h,w.isEmptyBlock(p.above()),"Every produced mineral remains exposed at the terrain surface");}
            check(h,found==made,"A vein uses the correct habitat-specific mineral without hidden replacements");
        }}finally{FieldworkTests.release(w,at);}h.succeed();
    }
    static void lithoHit(GameTestHelper h){combat(h,true,false);}
    static void lithoDodge(GameTestHelper h){combat(h,true,true);}
    static void stalkerHit(GameTestHelper h){combat(h,false,false);}
    private static void combat(GameTestHelper h,boolean heavy,boolean dodge){
        var at=h.absolutePos(new BlockPos(16,1,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(4)));var m=mob(h,at,heavy);
        m.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),1);check(h,m.beginAttack(p),"Actual attack starts with a snapshotted warning");float hp=p.getHealth();
        check(h,m.charge()==(heavy?40:32)&&m.marks().size()==(heavy?1:3),"Each creature announces its own timing and impact pattern");
        if(dodge)p.setPos(Vec3.atBottomCenterOf(at.west(5)));
        h.runAfterDelay(heavy?45:44,()->{try{check(h,dodge?p.getHealth()==hp:p.getHealth()<=hp-(heavy?7.9f:3.9f),"Leaving a marked zone avoids damage; staying is actually hit");check(h,m.charge()==0&&m.recovery()>0,"Every attack grants a recovery window");}finally{m.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);}h.succeed();});
    }
    static void lithoPatterns(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,1,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(4)));var m=mob(h,at,true);m.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),1);check(h,m.beginAttack(p),"First attack starts");
        // AI navigation after the first knockback is intentionally not scheduled to
        // an exact tick. Observe the actual next wind-up, without starting it by hand.
        m.setTarget(p);
        Runnable cleanup=()->{m.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);};
        h.onEachTick(()->{if(m.charge()>0&&m.marks().size()==3){cleanup.run();h.succeed();}});
        h.runAtTickTime(190,()->{try{check(h,false,"Native AI must announce its alternating three-fracture attack within 190 ticks; charge="+m.charge()+", recovery="+m.recovery()+", marks="+m.marks().size());}finally{cleanup.run();}});
    }
    static void stalkerInterrupt(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,1,16));var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.east(4)));var m=mob(h,at,false);m.setNoAi(true);
        try{check(h,m.beginAttack(p),"Stalker begins a salvo");check(h,m.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),4),"A native player hit reaches the creature");check(h,m.charge()==0&&m.marks().isEmpty()&&m.recovery()>0&&!m.beginAttack(p),"A sufficiently strong hit removes the warning and prevents immediate recharging");}finally{m.discard();GameplayTests.cleanup(p);}h.succeed();
    }
    static void loot(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,1,16));var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at.south(3)));
        try{for(boolean heavy:new boolean[]{true,false}){var m=mob(h,at,heavy);m.setNoAi(true);m.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),10000);}
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(4));
            check(h,drops.stream().anyMatch(d->d.getItem().is(AwakeningContent.LITHIC_HEART.get()))&&drops.stream().anyMatch(d->d.getItem().is(AwakeningContent.GLASS_FILAMENT.get())),"Confirmed native player kills distribute each creature's own real loot table");
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    private static ResonanceForgeEntity separator(GameTestHelper h,BlockPos relative){var at=h.absolutePos(relative);h.getLevel().setBlock(at,AwakeningContent.SEPARATOR.get().defaultBlockState(),3);return (ResonanceForgeEntity)h.getLevel().getBlockEntity(at);}
    static void separatorProcessing(GameTestHelper h){
        var f=separator(h,new BlockPos(2,1,2));f.setItem(0,new ItemStack(AwakeningContent.RAW_AURALITE.get(),2));f.setItem(1,new ItemStack(ModContent.ECHO_DUST.get()));
        check(h,ResonanceForgeEntity.result(new ItemStack(Items.RAW_IRON),true).isEmpty()&&ResonanceForgeEntity.result(new ItemStack(AwakeningContent.AURALITE_GRIT.get()),true).isEmpty(),"Separator outputs cannot feed back into an infinite multiplication loop");
        h.runAfterDelay(275,()->{check(h,f.getItem(2).is(AwakeningContent.AURALITE_GRIT.get())&&f.getItem(2).getCount()==2&&f.getItem(0).getCount()==1&&f.getItem(1).isEmpty(),"Twelve seconds consume exactly one raw mineral and one dust for two grit");
            f.setItem(1,new ItemStack(ModContent.ECHO_DUST.get()));f.setItem(2,new ItemStack(AwakeningContent.AURALITE_GRIT.get(),63));check(h,f.status()==2,"A pair of grit requires room for both outputs");
            h.getLevel().setBlock(f.getBlockPos().east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);check(h,f.status()==3,"A real neighbouring redstone signal pauses the separator");h.succeed();});
    }
    static void separatorHoppers(GameTestHelper h){
        var f=separator(h,new BlockPos(3,2,3));var w=h.getLevel();var at=f.getBlockPos();
        w.setBlock(at.above(),Blocks.HOPPER.defaultBlockState(),3);w.setBlock(at.west(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.EAST),3);w.setBlock(at.below(),Blocks.HOPPER.defaultBlockState(),3);
        var input=(HopperBlockEntity)w.getBlockEntity(at.above());var fuel=(HopperBlockEntity)w.getBlockEntity(at.west());var output=(HopperBlockEntity)w.getBlockEntity(at.below());
        input.setItem(0,new ItemStack(AwakeningContent.RAW_NOCTILITE.get()));fuel.setItem(0,new ItemStack(ModContent.ECHO_DUST.get()));
        h.runAfterDelay(350,()->{check(h,input.isEmpty()&&fuel.isEmpty()&&output.countItem(AwakeningContent.NOCTILITE_GRIT.get())==2,"Real vanilla hoppers insert raw mineral/fuel on their designated sides and extract only processed grit below");h.succeed();});
    }
    static void reaperNight(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var at=FieldworkTests.center(h,20);FieldworkTests.platform(w,at);w.setBlock(at,AwakeningContent.REAPER.get().defaultBlockState(),3);var f=(EchoReaperEntity)w.getBlockEntity(at);var plant=at.east(2);
        w.setBlock(plant,ModContent.ECHO_BLOOM.get().defaultBlockState().setValue(EchoBloomBlock.CHARGE,3),3);f.setItem(1,new ItemStack(Items.COAL));var clock=w.dimensionType().defaultClock().orElseThrow();long old=w.getServer().clockManager().getTotalTicks(clock);
        h.onEachTick(()->w.getServer().clockManager().setTotalTicks(clock,30000));
        h.runAfterDelay(225,()->{try{check(h,f.getItem(2).is(ModContent.ECHO_DUST.get())&&f.getItem(2).getCount()==1&&f.getItem(1).isEmpty(),"Native block ticking harvests a mature neighbouring aggregate with finite fuel");check(h,w.getBlockState(plant).getValue(EchoBloomBlock.CHARGE)==0,"The real aggregate is emptied rather than copied or destroyed");var saved=f.saveWithFullMetadata(w.registryAccess());check(h,saved.getIntOr("FuelUses",0)==7,"One coal stores exactly seven remaining harvest charges after the first");}finally{w.getServer().clockManager().setTotalTicks(clock,old);w.removeBlockEntity(at);FieldworkTests.release(w,at);}h.succeed();});
    }
    static void reaperGuards(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var at=FieldworkTests.center(h,22);FieldworkTests.platform(w,at);w.setBlock(at,AwakeningContent.REAPER.get().defaultBlockState(),3);var f=(EchoReaperEntity)w.getBlockEntity(at);var plant=at.east();w.setBlock(plant,ModContent.ECHO_BLOOM.get().defaultBlockState().setValue(EchoBloomBlock.CHARGE,3),3);f.setItem(1,new ItemStack(Items.CHARCOAL));var clock=w.dimensionType().defaultClock().orElseThrow();long old=w.getServer().clockManager().getTotalTicks(clock);
        try{w.getServer().clockManager().setTotalTicks(clock,3000);check(h,f.status()==6,"Daylight is reported explicitly");for(int i=0;i<20;i++)f.work();check(h,f.getItem(1).getCount()==1&&f.getItem(2).isEmpty(),"Daytime never consumes fuel or invents harvests");
            w.getServer().clockManager().setTotalTicks(clock,30000);f.setItem(2,new ItemStack(ModContent.ECHO_DUST.get(),64));check(h,f.status()==2,"A full output protects the mature aggregate");f.work();check(h,w.getBlockState(plant).getValue(EchoBloomBlock.CHARGE)==3,"Blocked output leaves actual plants unchanged");
            f.setItem(2,ItemStack.EMPTY);w.setBlock(at.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);check(h,f.status()==3,"Redstone pauses a real reaper");f.work();check(h,f.getItem(1).getCount()==1,"Pause consumes no fuel");
            var atOther=h.absolutePos(new BlockPos(2,1,2));h.getLevel().setBlock(atOther,AwakeningContent.REAPER.get().defaultBlockState(),3);check(h,((EchoReaperEntity)h.getLevel().getBlockEntity(atOther)).status()==5,"The same machine rejects operation outside Vestige");
        }finally{w.getServer().clockManager().setTotalTicks(clock,old);w.removeBlockEntity(at);FieldworkTests.release(w,at);}h.succeed();
    }
    static void reaperSave(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(2,1,2));var w=h.getLevel();w.setBlock(at,AwakeningContent.REAPER.get().defaultBlockState(),3);var f=(EchoReaperEntity)w.getBlockEntity(at);f.setItem(1,new ItemStack(Items.COAL,3));f.setItem(2,new ItemStack(ModContent.ECHO_DUST.get(),12));var tag=f.saveWithFullMetadata(w.registryAccess());tag.putInt("FuelUses",5);tag.putInt("Work",180);
        var restored=(EchoReaperEntity)BlockEntity.loadStatic(at,f.getBlockState(),tag,w.registryAccess());restored.setLevel(w);check(h,restored.getItem(1).getCount()==3&&restored.getItem(2).getCount()==12,"Native block serialization retains exact physical inventories");var again=restored.saveWithFullMetadata(w.registryAccess());check(h,again.getIntOr("FuelUses",0)==5&&again.getIntOr("Work",0)==180,"Stored fuel and work survive reload without fabricating elapsed harvests");h.succeed();
    }
    static void equipment(GameTestHelper h){
        var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,1,2))));
        p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(AwakeningContent.UMBRA_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(AwakeningContent.UMBRA_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(AwakeningContent.UMBRA_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(AwakeningContent.UMBRA_BOOTS.get()));
        h.onEachTick(p::doTick);
        h.runAfterDelay(3,()->{try{
            check(h,p.getArmorValue()==20&&p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)>=7.9,"Native equipment ticks apply four real pieces: armor="+p.getArmorValue()+", toughness="+p.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            check(h,new ItemStack(AwakeningContent.AURALITE_PICKAXE.get()).getMaxDamage()==1250&&new ItemStack(AwakeningContent.UMBRA_PICKAXE.get()).getMaxDamage()==2250,"The two tool materials have different registered durability");
        }finally{GameplayTests.cleanup(p);}h.succeed();});
    }
    static void hammer(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,1,16));FieldworkTests.platform(h.getLevel(),at);var p=GameplayTests.player(h);p.setPos(Vec3.atBottomCenterOf(at));p.setYRot(0);p.setXRot(0);var front=mob(h,at.south(4),false);var behind=mob(h,at.north(4),false);front.setNoAi(true);behind.setNoAi(true);float hp=front.getHealth(),backHp=behind.getHealth();var stack=new ItemStack(AwakeningContent.HAMMER.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);h.onEachTick(p::doTick);
        h.runAfterDelay(2,()->{check(h,p.onGround(),"Native player physics places the user on solid ground before charging");AwakeningContent.HAMMER.get().use(h.getLevel(),p,InteractionHand.MAIN_HAND);});
        h.runAfterDelay(27,()->{try{check(h,front.getHealth()<hp&&behind.getHealth()==backHp,"The real twenty-tick item use damages a hostile in front and leaves the rear safe");check(h,stack.getDamageValue()==3&&p.getCooldowns().isOnCooldown(stack),"Successful use pays exact durability and applies its native cooldown");}finally{front.discard();behind.discard();GameplayTests.cleanup(p);FieldworkTests.release(h.getLevel(),at);}h.succeed();});
    }
    static void citadel(GameTestHelper h){building(h,"aureate_citadel",new BlockPos(47,1,7),new BlockPos(31,11,36));}
    static void conservatory(GameTestHelper h){building(h,"echo_conservatory",new BlockPos(39,1,7),new BlockPos(25,10,31));}
    private static void building(GameTestHelper h,String name,BlockPos from,BlockPos to){
        var w=h.getLevel();var template=w.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd",name)).orElseThrow();var origin=h.absolutePos(new BlockPos(2,1,2));
        check(h,template.placeInWorld(w,origin,origin,new StructurePlaceSettings(),w.getRandom(),2),"The complete authored structure places in the native engine");
        var walker=net.minecraft.world.entity.EntityTypes.ZOMBIE.create(w,EntitySpawnReason.EVENT);walker.setNoAi(true);walker.setPos(Vec3.atBottomCenterOf(origin.offset(from)));walker.setOnGround(true);walker.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(128);w.addFreshEntity(walker);var nav=(net.minecraft.world.entity.ai.navigation.GroundPathNavigation)walker.getNavigation();nav.setCanOpenDoors(true);nav.setRequiredPathLength(160);nav.setMaxVisitedNodesMultiplier(32);
        h.runAfterDelay(2,()->{try{
            if(name.equals("aureate_citadel")){
                var upper=origin.offset(39,11,65);var ascent=nav.createPath(upper,0);
                check(h,ascent!=null&&ascent.canReach(),"Native route connects the entrance to the upper stair landing");
                // An explicit landing waypoint avoids A* preferring the much closer floor below the final target.
                walker.setPos(Vec3.atBottomCenterOf(upper));walker.setOnGround(true);
            }
            var path=nav.createPath(origin.offset(to),0);check(h,path!=null&&path.canReach(),"Native walking path reaches the upper gallery: "+name+", grounded="+walker.onGround()+", position="+walker.position()+", end="+(path==null?"null":path.getEndNode()));}finally{walker.discard();}h.succeed();});
    }
}
