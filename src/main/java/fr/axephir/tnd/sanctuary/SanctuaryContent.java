package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import fr.axephir.tnd.entities.VestigeEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import java.util.*;

public final class SanctuaryContent {
    public static final Map<String,DeferredBlock<Block>> STONE=new LinkedHashMap<>();
    static{for(String name:List.of("obsidian_metal","obsidian_tiles","obsidian_pillar","obsidian_relief","obsidian_ribbed","obsidian_grate","luminous_script","guiding_inlay","amber_seal","memory_lattice","silent_conduit","observatory_brazier")){
        int light=switch(name){case "luminous_script"->13;case "guiding_inlay"->12;case "amber_seal"->12;case "observatory_brazier"->15;case "memory_lattice"->7;case "silent_conduit"->5;default->0;};
        STONE.put(name,ModContent.BLOCKS.registerSimpleBlock(name,p->p.strength(6,30).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).lightLevel(s->light)));
    }}
    public static final DeferredBlock<StairBlock> STAIRS=ModContent.BLOCKS.registerBlock("obsidian_stairs",p->new StairBlock(block("obsidian_tiles").defaultBlockState(),p),p->p.strength(6,30).requiresCorrectToolForDrops());
    public static final DeferredBlock<SlabBlock> SLAB=ModContent.BLOCKS.registerBlock("obsidian_slab",SlabBlock::new,p->p.strength(6,30).requiresCorrectToolForDrops());
    public static final DeferredBlock<SanctuaryNodeBlock> NODE=ModContent.BLOCKS.registerBlock("observatory_station",SanctuaryNodeBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->10).sound(SoundType.METAL));
    public static final DeferredBlock<SanctuaryDialBlock> DIAL=ModContent.BLOCKS.registerBlock("observatory_dial",SanctuaryDialBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->9).sound(SoundType.METAL));
    public static final DeferredBlock<SanctuaryRelayBlock> RELAY=ModContent.BLOCKS.registerBlock("observatory_relay",SanctuaryRelayBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)?12:3));
    public static final DeferredBlock<SanctuaryTraceBlock> TRACE=ModContent.BLOCKS.registerBlock("observatory_trace",SanctuaryTraceBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->12));
    public static final DeferredBlock<SanctuaryGateBlock> GATE=ModContent.BLOCKS.registerBlock("observatory_gate",SanctuaryGateBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->8).sound(SoundType.METAL));
    public static final DeferredBlock<WitnessFilamentBlock> FILAMENT=ModContent.BLOCKS.registerBlock("observatory_filament",WitnessFilamentBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->s.getValue(WitnessFilamentBlock.POWER)==0?1:12+s.getValue(WitnessFilamentBlock.POWER)));
    public static final DeferredBlock<WitnessDeviceBlock> COUPLER=ModContent.BLOCKS.registerBlock("observatory_coupler",p->new WitnessDeviceBlock(p,WitnessDeviceBlock.Kind.COUPLER),p->p.strength(-1,3600000).noLootTable().noOcclusion().lightLevel(s->s.getValue(WitnessDeviceBlock.LIT)?12:3));
    public static final DeferredBlock<WitnessDeviceBlock> RELIQUARY=ModContent.BLOCKS.registerBlock("observatory_reliquary",p->new WitnessDeviceBlock(p,WitnessDeviceBlock.Kind.RELIQUARY),p->p.strength(-1,3600000).noLootTable().noOcclusion().lightLevel(s->s.getValue(WitnessDeviceBlock.LIT)?12:2));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SanctuaryNodeEntity>> NODE_ENTITY=MachineRegistry.ENTITIES.register("observatory_station",()->new BlockEntityType<>(SanctuaryNodeEntity::new,NODE.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<SanctuaryMenu>> MENU=ModContent.MENUS.register("observatory",()->new MenuType<>(SanctuaryMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<SanctuaryCompassItem> COMPASS=ModContent.ITEMS.registerItem("observatory_compass",SanctuaryCompassItem::new,p->p.stacksTo(1));
    public static final DeferredItem<Item> SHARD=ModContent.ITEMS.registerSimpleItem("mnemonic_shard"),CORE=ModContent.ITEMS.registerSimpleItem("axiomatic_core"),INGOT=ModContent.ITEMS.registerSimpleItem("nocturne_ingot"),CIRCUIT=ModContent.ITEMS.registerSimpleItem("mnemonic_circuit"),DUST=ModContent.ITEMS.registerSimpleItem("purified_auralite"),MEMORY=ModContent.ITEMS.registerSimpleItem("observatory_memory");
    public static final ToolMaterial TOOLS=new ToolMaterial(net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL,2800,10,4.5f,22,net.minecraft.tags.TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("tnd","nocturne_materials")));
    public static final DeferredItem<Item> BLADE=ModContent.ITEMS.registerSimpleItem("nocturne_blade",p->p.sword(TOOLS,4,-2.5f));
    public static final DeferredItem<Item> PICKAXE=ModContent.ITEMS.registerSimpleItem("nocturne_pickaxe",p->p.pickaxe(TOOLS,2,-2.8f));
    private static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,"tnd");
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.biome.BiomeSource>> BIOMES=DeferredRegister.create(Registries.BIOME_SOURCE,"tnd");
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.DensityFunction>> DENSITIES=DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE,"tnd");
    static{FEATURES.register("black_observatory",SanctuaryFeature::new);BIOMES.register("black_observatory",()->SanctuaryBiomeSource.CODEC);DENSITIES.register("observatory_blend",()->SanctuaryBlend.CODEC);}
    public static Block block(String name){return STONE.get(name).get();}
    public static void register(IEventBus bus){
        STONE.forEach((n,b)->ModContent.ITEMS.registerSimpleBlockItem(n,b));ModContent.ITEMS.registerSimpleBlockItem("obsidian_stairs",STAIRS);ModContent.ITEMS.registerSimpleBlockItem("obsidian_slab",SLAB);
        ModContent.ITEMS.registerSimpleBlockItem("observatory_relay",RELAY);ModContent.ITEMS.registerSimpleBlockItem("observatory_trace",TRACE);ModContent.ITEMS.registerSimpleBlockItem("observatory_station",NODE);ModContent.ITEMS.registerSimpleBlockItem("observatory_dial",DIAL);ModContent.ITEMS.registerSimpleBlockItem("observatory_gate",GATE);
        ModContent.ITEMS.registerSimpleBlockItem("observatory_filament",FILAMENT);ModContent.ITEMS.registerSimpleBlockItem("observatory_coupler",COUPLER);ModContent.ITEMS.registerSimpleBlockItem("observatory_reliquary",RELIQUARY);
        FEATURES.register(bus);BIOMES.register(bus);DENSITIES.register(bus);SanctuaryEntities.register(bus);SanctuaryRegions.GENERATED.getId();
        for(String n:List.of("observatory_drone","observatory_threshold","observatory_relay","observatory_warning","observatory_impact","observatory_victory","observatory_core","witness_current","witness_bridge","witness_reliquary","witness_awaken"))ModContent.ATMOSPHERE_SOUNDS.put(n,ModContent.SOUNDS.register(n,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("tnd",n))));
    }
    private SanctuaryContent(){}
}
