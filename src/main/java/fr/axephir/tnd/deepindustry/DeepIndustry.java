package fr.axephir.tnd.deepindustry;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.*;

/** Three independent material paths and a maintained, transactional industrial tier. */
public final class DeepIndustry {
    public enum Metal {FERRONYTE("ferronyte",2100,8.5f,3.5f,30,2.5f,.06f),THALASSITE("thalassite",2500,10,4,33,3,.08f),ASTERIUM("asterium",3600,12,5,42,4,.12f);
        public final String id;final int durability,armor;final float speed,damage,toughness,knockback;
        Metal(String id,int durability,float speed,float damage,int armor,float toughness,float knockback){this.id=id;this.durability=durability;this.speed=speed;this.damage=damage;this.armor=armor;this.toughness=toughness;this.knockback=knockback;}}
    public enum Kind {CENTRIFUGE("hydrocentrifuge",128000),KILN("pressure_kiln",256000),DREDGER("abyssal_dredger",256000),BORE("abyssal_bore",512000),REPLICATOR("asterium_replicator",512000),TURBINE("closed_cycle_turbine",2000000),ASSEMBLER("precision_assembler",256000);
        public final String id;public final int capacity;Kind(String id,int capacity){this.id=id;this.capacity=capacity;}}
    public static final Map<String,DeferredItem<Item>> ITEMS=new LinkedHashMap<>();
    public static final Map<Metal,Map<String,DeferredItem<Item>>> GEAR=new EnumMap<>(Metal.class);
    public static final Map<String,DeferredBlock<Block>> ORES=new LinkedHashMap<>();
    public static final Map<Kind,DeferredBlock<DeepMachineBlock>> MACHINES=new EnumMap<>(Kind.class);
    static {
        for(String n:List.of("raw_ferronyte","raw_thalassite","raw_asterium","ferronyte_grit","thalassite_grit","ferronyte_ingot","thalassite_ingot","asterium_ingot","pressure_circuit","mineral_matrix","maintenance_kit","resonant_fuel_cell","sundered_drive"))ITEMS.put(n,ModContent.ITEMS.registerSimpleItem(n));
        ITEMS.put("hardened_drill_bit",ModContent.ITEMS.registerSimpleItem("hardened_drill_bit",p->p.durability(512)));
        for(String name:List.of("ferronyte_ore","deepslate_ferronyte_ore","thalassite_nodule")){var block=ModContent.BLOCKS.registerSimpleBlock(name,p->p.strength(4.5f,12).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE).lightLevel(s->name.contains("thalassite")?3:0));ORES.put(name,block);ModContent.ITEMS.registerSimpleBlockItem(name,block);}
        for(var metal:Metal.values()){
            var tag=TagKey.create(Registries.ITEM,id(metal.id+"_repairs"));var tool=new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,metal.durability,metal.speed,metal.damage,metal==Metal.ASTERIUM?24:18,tag);
            var armor=new ArmorMaterial(metal.armor,Map.of(ArmorType.HELMET,3,ArmorType.CHESTPLATE,8,ArmorType.LEGGINGS,6,ArmorType.BOOTS,3),metal==Metal.ASTERIUM?24:18,Holder.direct(SoundEvents.ARMOR_EQUIP_NETHERITE.value()),metal.toughness,metal.knockback,tag,ResourceKey.create(EquipmentAssets.ROOT_ID,id(metal.id)));
            var pieces=new LinkedHashMap<String,DeferredItem<Item>>();
            pieces.put("sword",ModContent.ITEMS.registerSimpleItem(metal.id+"_sword",p->p.sword(tool,3,-2.4f)));
            pieces.put("pickaxe",ModContent.ITEMS.registerSimpleItem(metal.id+"_pickaxe",p->p.pickaxe(tool,1,-2.8f)));
            pieces.put("axe",ModContent.ITEMS.registerSimpleItem(metal.id+"_axe",p->p.axe(tool,5,-3)));
            pieces.put("shovel",ModContent.ITEMS.registerSimpleItem(metal.id+"_shovel",p->p.shovel(tool,1.5f,-3)));
            pieces.put("hoe",ModContent.ITEMS.registerSimpleItem(metal.id+"_hoe",p->p.hoe(tool,-4,0)));
            for(var type:List.of(ArmorType.HELMET,ArmorType.CHESTPLATE,ArmorType.LEGGINGS,ArmorType.BOOTS))pieces.put(type.getName(),ModContent.ITEMS.registerSimpleItem(metal.id+"_"+type.getName(),p->p.humanoidArmor(armor,type)));
            GEAR.put(metal,pieces);
        }
        for(var kind:Kind.values()){var b=ModContent.BLOCKS.registerBlock(kind.id,p->new DeepMachineBlock(p,kind),p->p.strength(6,18).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).lightLevel(s->s.getValue(DeepMachineBlock.LIT)?12:2));MACHINES.put(kind,b);ModContent.ITEMS.registerSimpleBlockItem(kind.id,b);}
    }
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DeepMachineEntity>> ENTITY=MachineRegistry.ENTITIES.register("deep_machine",()->new BlockEntityType<>(DeepMachineEntity::new,MACHINES.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<MenuType<?>,MenuType<DeepMachineMenu>> MENU=ModContent.MENUS.register("deep_machine",()->new MenuType<>(DeepMachineMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static Item item(String n){return ITEMS.get(n).get();}
    public static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    public static void register(IEventBus bus){bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Energy.BLOCK,ENTITY.get(),(be,side)->be.port));}
    private DeepIndustry(){}
}
