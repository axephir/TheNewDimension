package fr.axephir.tnd.gametest;

import fr.axephir.tnd.assault.*;
import fr.axephir.tnd.city.*;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

final class AssaultTests {
    static void register(DeferredRegister<Consumer<GameTestHelper>> f){
        f.register("assault_equipment",()->AssaultTests::equipment);f.register("assault_repair",()->AssaultTests::repair);f.register("assault_repair_guards",()->AssaultTests::repairGuards);
        f.register("assault_node",()->AssaultTests::node);f.register("assault_boss",()->AssaultTests::boss);f.register("assault_loot",()->AssaultTests::loot);f.register("assault_templates",()->AssaultTests::templates);f.register("assault_blackout",()->AssaultTests::blackout);f.register("assault_familiar",()->AssaultTests::familiar);
    }
    private static void check(GameTestHelper h,boolean ok,String why){h.assertTrue(ok,Component.literal(why));}
    private static ItemStack piece(AssaultContent.Set set){return new ItemStack(AssaultContent.ARMOR.get(set).get(EquipmentSlot.CHEST).get());}
    private static void equipment(GameTestHelper h){var p=GameplayTests.player(h);
        try{
            for(var set:AssaultContent.Set.values()){
                for(var e:AssaultContent.ARMOR.get(set).entrySet()){var stack=new ItemStack(e.getValue().get());check(h,stack.getMaxDamage()==500&&stack.isEnchantable(),"All twelve pieces have exactly 500 durability and native enchantability");
                    var protection=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);check(h,protection.value().canEnchant(stack),"Vanilla protection supports each new armor slot");check(h,p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING).value().canEnchant(stack),"Ordinary Mending remains compatible with tower equipment");p.setItemSlot(e.getKey(),stack);}
                p.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);p.removeAllEffects();p.tickCount=20;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));check(h,AssaultEquipment.effects(set).stream().noneMatch(p::hasEffect),"Three matching pieces grant no full-set potion effect");
                p.setItemSlot(EquipmentSlot.FEET,new ItemStack(AssaultContent.ARMOR.get(set).get(EquipmentSlot.FEET).get()));p.tickCount=40;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));check(h,AssaultEquipment.effects(set).stream().allMatch(p::hasEffect),"A complete matching set applies both real native potion effects");
            }
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    private static IndustrialEntity station(GameTestHelper h,BlockPos at){var w=h.getLevel();FieldworkTests.platform(w,at);w.setBlock(at,Industry.BLOCKS.get(Industry.Kind.REPAIR).get().defaultBlockState(),3);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)w.setBlock(at.offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),3);return (IndustrialEntity)w.getBlockEntity(at);}
    private static void repair(GameTestHelper h){var at=new BlockPos(250000,120,250000);var e=station(h,at);var w=h.getLevel();var gear=piece(AssaultContent.Set.WARD);gear.setDamageValue(380);gear.set(DataComponents.CUSTOM_NAME,Component.literal("Dernier rempart"));gear.enchant(w.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),3);var expected=gear.copy();expected.setDamageValue(280);e.setItem(0,gear);e.setItem(1,new ItemStack(ModContent.BLUE_SHARD.get(),3));e.energy.set(6000);
        h.runAfterDelay(100,()->{
            check(h,ItemStack.isSameItemSameComponents(e.getItem(2),expected)&&e.getItem(0).isEmpty()&&e.getItem(1).getCount()==2&&e.energy.getAmountAsInt()==4000,"A real timed cycle repairs exactly 100, preserves names/enchantments and pays one crystal plus 2000 FE");
            var restored=(IndustrialEntity)BlockEntity.loadStatic(at,e.getBlockState(),e.saveWithFullMetadata(w.registryAccess()),w.registryAccess());check(h,restored.energy.getAmountAsInt()==4000&&ItemStack.isSameItemSameComponents(restored.getItem(2),expected),"Native machine serialization retains the unique repaired equipment and exact energy");
            w.setBlock(at.below(),Blocks.HOPPER.defaultBlockState(),3);w.setBlock(at.below(2),Blocks.CHEST.defaultBlockState(),3);
            h.runAfterDelay(40,()->{try{check(h,((ChestBlockEntity)w.getBlockEntity(at.below(2))).countItem(expected.getItem())==1&&e.getItem(2).isEmpty(),"A real bottom hopper exports the repaired unique item");}finally{FieldworkTests.release(w,at);}h.succeed();});
        });
    }
    private static void repairGuards(GameTestHelper h){var at=new BlockPos(252000,120,250000);var e=station(h,at);var w=h.getLevel();var gear=piece(AssaultContent.Set.SHADE);gear.setDamageValue(300);e.setItem(0,gear);e.setItem(1,new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(),2));e.setItem(2,piece(AssaultContent.Set.CHOIR));e.energy.set(4000);
        h.runAfterDelay(100,()->{check(h,e.getItem(0).getDamageValue()==300&&e.getItem(1).getCount()==2&&e.energy.getAmountAsInt()==4000&&e.status()==2,"Blocked single output never spends gear, crystal or power, and cannot spill into hidden output slots");e.setItem(2,ItemStack.EMPTY);w.setBlock(at.west().below(),Blocks.AIR.defaultBlockState(),3);
            h.runAfterDelay(100,()->{try{check(h,e.status()==6&&e.getItem(0).getDamageValue()==300&&e.getItem(1).getCount()==2,"Missing physical foundation stops repair without consumption");check(h,!e.canPlaceItem(0,new ItemStack(Items.DIAMOND_CHESTPLATE))&&!e.canPlaceItem(1,new ItemStack(Items.DIAMOND)),"Ordinary gear and arbitrary fuel cannot bypass the tower repair contract");}finally{FieldworkTests.release(w,at);}h.succeed();});});
    }
    private static AssaultNodeEntity relay(ServerLevel w,BlockPos at,boolean roof){w.setBlock(at,AssaultContent.NODE.get().defaultBlockState().setValue(AssaultNodeBlock.SUMMIT,roof),3);return (AssaultNodeEntity)w.getBlockEntity(at);}
    private static void node(GameTestHelper h){var w=FieldworkTests.vestige(h);var at=new BlockPos(254000,120,250000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at.south(8));p.getAbilities().invulnerable=true;var e=relay(w,at,false);e.tick();check(h,e.spawned()==0,"An unbound relay outside generated city chunks cannot create mobs");w.getChunkAt(at).setData(CityRegions.GENERATED,true);
        h.runAfterDelay(110,()->{try{
            check(h,e.spawned()==4,"A real generated floor releases its four finite guards");var copy=(AssaultNodeEntity)BlockEntity.loadStatic(at,e.getBlockState(),e.saveWithFullMetadata(w.registryAccess()),w.registryAccess());copy.setLevel(w);copy.tick();check(h,copy.spawned()==4,"Reloading an exhausted floor cannot replenish its guards");
        }finally{for(var mob:w.getEntitiesOfClass(CityMob.class,new AABB(at).inflate(40)))mob.discard();GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();});
    }
    private static void boss(GameTestHelper h){var w=h.getLevel();var at=new BlockPos(256000,120,250000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at.south(8));p.getAbilities().invulnerable=true;
        var surgeon=CityMobs.TYPES.get(CityMob.Kind.SURGEON).get().create(w,EntitySpawnReason.EVENT);surgeon.setPos(Vec3.atBottomCenterOf(at));w.addFreshEntity(surgeon);var guard=CityMobs.TYPES.get(CityMob.Kind.ENFORCER).get().create(w,EntitySpawnReason.EVENT);guard.setPos(Vec3.atBottomCenterOf(at.east(3)));guard.setNoAi(true);guard.setHealth(10);w.addFreshEntity(guard);check(h,surgeon.beginAttack(p)&&surgeon.charge()==44,"The new healing boss announces a full native channel before striking");
        var prowler=CityMobs.TYPES.get(CityMob.Kind.PROWLER).get().create(w,EntitySpawnReason.EVENT);prowler.setPos(Vec3.atBottomCenterOf(at.south(4)));w.addFreshEntity(prowler);check(h,prowler.beginAttack(p),"The new flanking boss accepts a visible survival target");prowler.hurtServer(w,p.damageSources().playerAttack(p),6);check(h,prowler.charge()==0&&prowler.recovery()>0,"A real heavy hit interrupts the agile boss's announced attack");prowler.discard();
        h.runAfterDelay(48,()->{try{check(h,guard.getHealth()>=16&&surgeon.recovery()>0,"The completed channel heals a real nearby guard and enters its counterattack window");}finally{surgeon.discard();guard.discard();GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();});
    }
    private static void loot(GameTestHelper h){var w=h.getLevel();var p=GameplayTests.player(h);try{
        for(var kind:List.of(CityMob.Kind.MARSHAL,CityMob.Kind.PROWLER,CityMob.Kind.SURGEON)){
            var mob=CityMobs.TYPES.get(kind).get().create(w,EntitySpawnReason.EVENT);var table=w.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("tnd","entities/"+kind.id)));
            var builder=new LootParams.Builder(w).withParameter(LootContextParams.THIS_ENTITY,mob).withParameter(LootContextParams.ORIGIN,p.position()).withParameter(LootContextParams.DAMAGE_SOURCE,p.damageSources().playerAttack(p));var params=builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER,p).create(LootContextParamSets.ENTITY);
            int drops=0;var pieces=new HashMap<Item,Integer>();var random=net.minecraft.util.RandomSource.create(14014);
            for(int n=0;n<5000;n++){var rolled=table.getRandomItems(params,random);var armor=rolled.stream().filter(AssaultContent::gear).toList();check(h,armor.size()<=1,"Each victory can drop at most one special piece");if(!armor.isEmpty()){drops++;pieces.merge(armor.getFirst().getItem(),1,Integer::sum);}}
            org.slf4j.LoggerFactory.getLogger("tnd/assault-tests").info("{}: {} equipment drops / 5000 native loot rolls; slots {}",kind.id,drops,pieces.values());
            check(h,drops>=850&&drops<=1150&&pieces.size()==4&&pieces.values().stream().allMatch(v->v>=170&&v<=330),"Native seeded loot approaches 20 percent with four uniformly distributed parts: "+drops+" "+pieces.values());
        }
    }finally{GameplayTests.cleanup(p);}h.succeed();}
    private static void templates(GameTestHelper h){var w=h.getLevel();var t=w.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","neon_district")).orElseThrow();var at=new BlockPos(258000,120,250000);
        var positions=List.of(new BlockPos(23,81,23),new BlockPos(103,65,25),new BlockPos(63,81,103));int lane=0;
        for(var target:positions){var origin=at.east(lane++*160);var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(origin.getX()+target.getX()-1,origin.getY()+target.getY()-1,origin.getZ()+target.getZ()-1,origin.getX()+target.getX()+1,origin.getY()+target.getY()+4,origin.getZ()+target.getZ()+1);
            t.placeInWorld(w,origin,origin,new StructurePlaceSettings().setBoundingBox(clip).setIgnoreEntities(true),w.getRandom(),2);var state=w.getBlockState(origin.offset(target));check(h,state.is(AssaultContent.NODE.get())&&state.getValue(AssaultNodeBlock.SUMMIT),"All three authored roofs contain the right finite encounter node: "+target);}
        h.succeed();}
    private static void blackout(GameTestHelper h){var w=FieldworkTests.vestige(h);var at=new BlockPos(260000,120,250000);FieldworkTests.platform(w,at);var p=FieldworkTests.player(h,w,at.south(8));p.getAbilities().invulnerable=true;w.getChunkAt(at).setData(CityRegions.GENERATED,true);var lamp=at.above(6);w.setBlock(lamp,AssaultContent.LAMP.get().defaultBlockState(),3);var e=relay(w,at,true);
        h.runAfterDelay(30,()->{check(h,e.spawned()==1&&!w.getBlockState(lamp).getValue(AssaultLampBlock.LIT),"Entering the roof triggers one boss and a real temporary local light failure");w.removeBlockEntity(at);});
        h.runAfterDelay(210,()->{try{check(h,e.spawned()==1&&w.getBlockState(lamp).getValue(AssaultLampBlock.LIT),"The lamp restores itself even without the relay ticking; a used roof has released only one boss");}finally{for(var mob:w.getEntitiesOfClass(CityMob.class,new AABB(at).inflate(40)))mob.discard();GameplayTests.cleanup(p);FieldworkTests.release(w,at);}h.succeed();});
    }
    private static void familiar(GameTestHelper h){var w=h.getLevel();var p=GameplayTests.player(h);var other=GameplayTests.player(h);var pet=fr.axephir.tnd.entities.VestigeEntities.SHARDLING.get().create(w,EntitySpawnReason.EVENT);pet.setPos(p.position());w.addFreshEntity(pet);pet.tame(p);
        try{
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(AssaultContent.MEMORIES.get(AssaultContent.Set.WARD).get(),2));pet.mobInteract(p,net.minecraft.world.InteractionHand.MAIN_HAND);
            check(h,pet.protocols()==1&&pet.getMaxHealth()==30&&p.getMainHandItem().getCount()==1,"One genuine tower memory installs a persistent health protocol on the owner's familiar");pet.mobInteract(p,net.minecraft.world.InteractionHand.MAIN_HAND);check(h,p.getMainHandItem().getCount()==1,"Repeated installation never consumes a duplicate memory");
            other.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(AssaultContent.MEMORIES.get(AssaultContent.Set.SHADE).get()));pet.mobInteract(other,net.minecraft.world.InteractionHand.MAIN_HAND);check(h,pet.protocols()==1&&other.getMainHandItem().getCount()==1,"A foreign player cannot alter another familiar or lose their item");
            var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess());pet.saveWithoutId(out);var clone=fr.axephir.tnd.entities.VestigeEntities.SHARDLING.get().create(w,EntitySpawnReason.EVENT);clone.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,w.registryAccess(),out.buildResult()));check(h,clone.protocols()==1&&clone.getMaxHealth()==30,"Native familiar serialization retains the upgrade and derived health");
            var menu=new fr.axephir.tnd.familiar.FamiliarMenu(70,p.getInventory(),p);check(h,menu.value(18)==1,"The owner-only familiar dossier reports installed tower protocols");
        }finally{pet.discard();GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();
    }

}
