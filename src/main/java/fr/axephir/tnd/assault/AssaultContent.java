package fr.axephir.tnd.assault;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;

public final class AssaultContent {
    public enum Set { WARD("ward"), SHADE("shade"), CHOIR("choir");public final String id;Set(String id){this.id=id;} }
    public static final Map<Set,DeferredItem<Item>> MEMORIES=new EnumMap<>(Set.class);
    public static final Map<Set,Map<EquipmentSlot,DeferredItem<Item>>> ARMOR=new EnumMap<>(Set.class);
    static {
        for(var set:Set.values()){
            MEMORIES.put(set,ModContent.ITEMS.registerSimpleItem(set.id+"_memory"));
            var material=new ArmorMaterial(30,Map.of(ArmorType.HELMET,3,ArmorType.CHESTPLATE,8,ArmorType.LEGGINGS,6,ArmorType.BOOTS,3),16,
                    Holder.direct(SoundEvents.ARMOR_EQUIP_NETHERITE.value()),2,0,TagKey.create(Registries.ITEM,id("assault_repairs")),ResourceKey.create(EquipmentAssets.ROOT_ID,id(set.id)));
            var pieces=new EnumMap<EquipmentSlot,DeferredItem<Item>>(EquipmentSlot.class);
            for(var type:List.of(ArmorType.HELMET,ArmorType.CHESTPLATE,ArmorType.LEGGINGS,ArmorType.BOOTS)){
                var slot=switch(type){case HELMET->EquipmentSlot.HEAD;case CHESTPLATE->EquipmentSlot.CHEST;case LEGGINGS->EquipmentSlot.LEGS;default->EquipmentSlot.FEET;};
                pieces.put(slot,ModContent.ITEMS.registerSimpleItem(set.id+"_"+type.getName(),p->p.humanoidArmor(material,type).durability(500)));
            }ARMOR.put(set,pieces);
        }
    }
    public static final DeferredBlock<AssaultNodeBlock> NODE=ModContent.BLOCKS.registerBlock("assault_node",AssaultNodeBlock::new,p->p.strength(-1,3600000).noLootTable().sound(SoundType.METAL).lightLevel(s->s.getValue(AssaultNodeBlock.LIT)?9:2));
    public static final DeferredBlock<AssaultLampBlock> LAMP=ModContent.BLOCKS.registerBlock("assault_lamp",AssaultLampBlock::new,p->p.strength(4,10).requiresCorrectToolForDrops().sound(SoundType.GLASS).lightLevel(s->s.getValue(AssaultLampBlock.LIT)?13:0));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AssaultNodeEntity>> NODE_ENTITY=MachineRegistry.ENTITIES.register("assault_node",()->new BlockEntityType<>(AssaultNodeEntity::new,NODE.get()));
    public static void bootstrap(){ModContent.ITEMS.registerSimpleBlockItem("assault_lamp",LAMP);}
    public static Set set(ItemStack stack){for(var e:ARMOR.entrySet())for(var item:e.getValue().values())if(stack.is(item.get()))return e.getKey();return null;}
    public static boolean gear(ItemStack stack){return set(stack)!=null;}
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    private AssaultContent(){}
}
