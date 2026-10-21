package fr.axephir.tnd.core;

import fr.axephir.tnd.entities.VestigeEntities;
import fr.axephir.tnd.technology.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.registries.*;
import java.util.*;

/** Stable 0.9 registry identifiers. Initialized before the shared deferred registers are attached. */
public final class AwakeningContent {
    public static final List<String> SOUND_IDS=List.of("lithoguard_wake","lithoguard_charge","lithoguard_impact","lithoguard_step","lithoguard_fall","stalker_charge","stalker_shot","stalker_hurt","stalker_fall","separator_complete","reaper_harvest","hammer_charge","hammer_impact","ruin_chime","expedition_complete","anchor_depart");
    static{for(var name:SOUND_IDS)ModContent.ATMOSPHERE_SOUNDS.put(name,ModContent.SOUNDS.register(name,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id(name))));}
    public static final DeferredBlock<Block> AURALITE_ORE=block("auralite_ore",3);
    public static final DeferredBlock<Block> NOCTILITE_ORE=block("noctilite_ore",4);
    public static final Map<String,DeferredBlock<Block>> DECOR=new LinkedHashMap<>();
    static{for(var name:List.of("citadel_bricks","carved_citadel_bricks","cracked_citadel_bricks","gilded_pillar","gilded_trim","auralite_block","umbra_block","umbra_tiles","etched_umbra","crystal_mosaic","conservatory_tiles","verdigris_panel","latticed_alloy","runic_lamp","amber_lamp","violet_lamp","lithic_trophy"))DECOR.put(name,block(name,name.endsWith("lamp")?15:name.endsWith("trophy")?7:0));
        for(var base:List.of("citadel_bricks","umbra_tiles","conservatory_tiles")){
            var stone=DECOR.get(base);var stair=ModContent.BLOCKS.registerBlock(base+"_stairs",p->new StairBlock(stone.get().defaultBlockState(),p),p->p.strength(4,10).requiresCorrectToolForDrops());ModContent.ITEMS.registerSimpleBlockItem(base+"_stairs",stair);
            var slab=ModContent.BLOCKS.registerBlock(base+"_slab",SlabBlock::new,p->p.strength(4,10).requiresCorrectToolForDrops());ModContent.ITEMS.registerSimpleBlockItem(base+"_slab",slab);
        }
    }
    public static final DeferredBlock<fr.axephir.tnd.world.HarmonicChimeBlock> CHIME=ModContent.BLOCKS.registerBlock("harmonic_chime",fr.axephir.tnd.world.HarmonicChimeBlock::new,p->p.strength(3,8).requiresCorrectToolForDrops().noOcclusion().lightLevel(s->8).sound(SoundType.AMETHYST));
    public static final DeferredBlock<ResonanceForgeBlock> SEPARATOR=ModContent.BLOCKS.registerBlock("ore_separator",p->new ResonanceForgeBlock(p,true),p->p.strength(5,12).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->s.getValue(ResonanceForgeBlock.LIT)?9:0));
    public static final DeferredBlock<EchoReaperBlock> REAPER=ModContent.BLOCKS.registerBlock("echo_reaper",EchoReaperBlock::new,p->p.strength(5,12).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->s.getValue(ResonanceForgeBlock.LIT)?9:0));
    static{ModContent.ITEMS.registerSimpleBlockItem("harmonic_chime",CHIME);ModContent.ITEMS.registerSimpleBlockItem("ore_separator",SEPARATOR);ModContent.ITEMS.registerSimpleBlockItem("echo_reaper",REAPER);}
    public static final DeferredItem<Item> RAW_AURALITE=item("raw_auralite"),RAW_NOCTILITE=item("raw_noctilite"),AURALITE_GRIT=item("auralite_grit"),NOCTILITE_GRIT=item("noctilite_grit"),AURALITE_INGOT=item("auralite_ingot"),NOCTILITE_CRYSTAL=item("noctilite_crystal"),UMBRA_ALLOY=item("umbra_alloy"),LITHIC_HEART=item("lithic_heart"),GLASS_FILAMENT=item("glass_filament");
    public static final ToolMaterial AURIC_TOOLS=new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,1250,10,3,20,tag("auric_repairs"));
    public static final ToolMaterial UMBRA_TOOLS=new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,2250,9,4,14,tag("umbra_repairs"));
    public static final ArmorMaterial UMBRA_ARMOR=new ArmorMaterial(33,Map.of(ArmorType.HELMET,3,ArmorType.CHESTPLATE,8,ArmorType.LEGGINGS,6,ArmorType.BOOTS,3),14,Holder.direct(SoundEvents.ARMOR_EQUIP_NETHERITE.value()),2,.05f,tag("umbra_repairs"),ResourceKey.create(EquipmentAssets.ROOT_ID,id("umbra")));
    public static final DeferredItem<Item> AURALITE_PICKAXE=ModContent.ITEMS.registerSimpleItem("auralite_pickaxe",p->p.pickaxe(AURIC_TOOLS,1,-2.8f));
    public static final DeferredItem<Item> AURALITE_BLADE=ModContent.ITEMS.registerSimpleItem("auralite_blade",p->p.sword(AURIC_TOOLS,3,-2.4f));
    public static final DeferredItem<Item> UMBRA_PICKAXE=ModContent.ITEMS.registerSimpleItem("umbra_pickaxe",p->p.pickaxe(UMBRA_TOOLS,1,-2.8f));
    public static final DeferredItem<Item> UMBRA_BLADE=ModContent.ITEMS.registerSimpleItem("umbra_blade",p->p.sword(UMBRA_TOOLS,3,-2.4f));
    public static final DeferredItem<fr.axephir.tnd.entities.GravitasHammerItem> HAMMER=ModContent.ITEMS.registerItem("gravitas_hammer",fr.axephir.tnd.entities.GravitasHammerItem::new,p->p.sword(UMBRA_TOOLS,5,-3.2f));
    public static final DeferredItem<Item> UMBRA_HELMET=armor("umbra_helmet",ArmorType.HELMET),UMBRA_CHESTPLATE=armor("umbra_chestplate",ArmorType.CHESTPLATE),UMBRA_LEGGINGS=armor("umbra_leggings",ArmorType.LEGGINGS),UMBRA_BOOTS=armor("umbra_boots",ArmorType.BOOTS);
    public static final DeferredItem<SpawnEggItem> LITHOGUARD_EGG=ModContent.ITEMS.registerItem("lithoguard_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(VestigeEntities.LITHOGUARD.get()));
    public static final DeferredItem<SpawnEggItem> STALKER_EGG=ModContent.ITEMS.registerItem("glass_stalker_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(VestigeEntities.STALKER.get()));
    private static DeferredItem<Item> armor(String name,ArmorType type){return ModContent.ITEMS.registerSimpleItem(name,p->p.humanoidArmor(UMBRA_ARMOR,type));}
    private static DeferredItem<Item> item(String name){return ModContent.ITEMS.registerSimpleItem(name);}
    private static DeferredBlock<Block> block(String name,int light){var b=ModContent.BLOCKS.registerSimpleBlock(name,p->p.strength(4,10).requiresCorrectToolForDrops().lightLevel(s->light).sound(name.contains("lamp")?SoundType.GLASS:SoundType.DEEPSLATE));ModContent.ITEMS.registerSimpleBlockItem(name,b);return b;}
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath("tnd",name);}
    private static TagKey<Item> tag(String name){return TagKey.create(Registries.ITEM,id(name));}
    public static void bootstrap(){}
    private AwakeningContent(){}
}
