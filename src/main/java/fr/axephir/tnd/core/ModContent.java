package fr.axephir.tnd.core;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.progression.MasteryMenu;
import fr.axephir.tnd.world.FieldJournalItem;
import fr.axephir.tnd.world.DetectorItem;
import fr.axephir.tnd.world.AncientConsoleBlock;
import fr.axephir.tnd.world.InscriptionBlock;
import fr.axephir.tnd.world.PortalControllerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheNewDimension.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheNewDimension.ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheNewDimension.ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TheNewDimension.ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TheNewDimension.ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> LOW_SIGNAL = SOUNDS.register("low_signal",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TheNewDimension.ID, "low_signal")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VESTIGE_WIND = SOUNDS.register("vestige_wind",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TheNewDimension.ID, "vestige_wind")));
    public static final DeferredHolder<SoundEvent, SoundEvent> METAL_ECHO = SOUNDS.register("metal_echo",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TheNewDimension.ID, "metal_echo")));

    public static final java.util.Map<String,DeferredHolder<SoundEvent,SoundEvent>> ATMOSPHERE_SOUNDS=new java.util.LinkedHashMap<>();
    static{for(String name:java.util.List.of("fracture_drone","hollow_resonance","glass_whispers","basin_lament","dune_rumble","night_pressure","enclosed_hum","distant_pulse","glass_tinkle","stone_shift","submerged_groan","dust_scrape","reverse_breath","spatial_chime","mastery_ascend"))
        ATMOSPHERE_SOUNDS.put(name,SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TheNewDimension.ID,name))));}
    public static SoundEvent atmosphereSound(String name){
        if(name.equals("low_signal"))return LOW_SIGNAL.get();if(name.equals("metal_echo"))return METAL_ECHO.get();
        return ATMOSPHERE_SOUNDS.get(name).get();
    }
    public static final DeferredBlock<Block> VITREOUS_STONE=stone("vitreous_stone",3.5f,8,0);
    public static final DeferredBlock<Block> VITRIFIED_ASH=stone("vitrified_ash",2.5f,6,0);
    public static final DeferredBlock<Block> ASHEN_SILT=stone("ashen_silt",1.5f,4,0);
    public static final DeferredBlock<Block> FOSSIL_COLUMN=stone("fossil_column",3,6,0);
    public static final DeferredBlock<Block> PALE_CRYSTAL=BLOCKS.registerSimpleBlock("pale_crystal",p->p.strength(3,6).requiresCorrectToolForDrops().lightLevel(state->3).sound(net.minecraft.world.level.block.SoundType.AMETHYST));
    public static final DeferredBlock<Block> ASHSTONE = stone("ashstone", 3.5f, 6, 0);
    public static final DeferredBlock<Block> ANCIENT_ALLOY = stone("ancient_alloy", 5, 12, 0);
    public static final DeferredBlock<Block> RESONANT_ORE = stone("resonant_ore", 5, 8, 4);
    public static final DeferredBlock<Block> CONDUIT = stone("conduit", 4, 10, 8);
    public static final DeferredBlock<Block> BLUE_ORE = stone("blue_ore", 4, 6, 3);
    public static final DeferredBlock<Block> RED_ORE = stone("red_ore", 4, 6, 3);
    public static final DeferredBlock<Block> GREEN_ORE = stone("green_ore", 4, 6, 3);
    public static final DeferredBlock<Block> PORTAL_FRAME = BLOCKS.registerSimpleBlock("portal_frame",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1, 3_600_000).noLootTable());
    public static final DeferredBlock<PortalControllerBlock> PORTAL_CONTROLLER = BLOCKS.registerBlock("portal_controller", PortalControllerBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1, 3_600_000).noLootTable()
                    .lightLevel(state -> state.getValue(PortalControllerBlock.ACTIVE) ? 11 : 1));
    public static final DeferredBlock<InscriptionBlock> MURAL_OCEAN = BLOCKS.registerBlock("mural_ocean", p -> new InscriptionBlock(p,"ocean"), p -> p.strength(4,10).lightLevel(state -> 2));
    public static final DeferredBlock<InscriptionBlock> MURAL_LAVA = BLOCKS.registerBlock("mural_lava", p -> new InscriptionBlock(p,"lava"), p -> p.strength(4,10).lightLevel(state -> 2));
    public static final DeferredBlock<InscriptionBlock> MURAL_FOREST = BLOCKS.registerBlock("mural_forest", p -> new InscriptionBlock(p,"forest"), p -> p.strength(4,10).lightLevel(state -> 2));
    public static final DeferredBlock<InscriptionBlock> MURAL_FUSION = BLOCKS.registerBlock("mural_fusion", p -> new InscriptionBlock(p,"fusion"), p -> p.strength(4,10).lightLevel(state -> 2));
    public static final DeferredBlock<Block> ASHSTONE_BRICKS = stone("ashstone_bricks", 3.5f, 8, 0);
    public static final DeferredBlock<Block> CRACKED_ASHSTONE_BRICKS = stone("cracked_ashstone_bricks", 3.5f, 8, 0);
    public static final DeferredBlock<Block> MOSSY_ASHSTONE_BRICKS = stone("mossy_ashstone_bricks", 3.5f, 8, 0);
    public static final DeferredBlock<Block> POLISHED_ASHSTONE = stone("polished_ashstone", 3.5f, 8, 0);
    public static final DeferredBlock<Block> ASHSTONE_TILES = stone("ashstone_tiles", 3.5f, 8, 0);
    public static final DeferredBlock<Block> CHISELED_ASHSTONE = stone("chiseled_ashstone", 3.5f, 8, 0);
    public static final DeferredBlock<Block> ANCIENT_PILLAR = stone("ancient_pillar", 3.5f, 8, 0);
    public static final DeferredBlock<Block> RIVETED_ALLOY = stone("riveted_alloy", 3.5f, 8, 0);
    public static final DeferredBlock<Block> CUT_ALLOY = stone("cut_alloy", 3.5f, 8, 0);
    public static final DeferredBlock<Block> OXIDIZED_ALLOY = stone("oxidized_alloy", 3.5f, 8, 0);
    public static final DeferredBlock<Block> ANCIENT_GRATE = stone("ancient_grate", 3.5f, 8, 0);
    public static final DeferredBlock<Block> DORMANT_CONDUIT = stone("dormant_conduit", 3.5f, 8, 0);
    public static final DeferredBlock<Block> RESONANT_LAMP = stone("resonant_lamp", 3.5f, 8, 15);
    public static final DeferredBlock<Block> BLUE_LAMP = stone("blue_lamp", 3.5f, 8, 15);
    public static final DeferredBlock<Block> RED_LAMP = stone("red_lamp", 3.5f, 8, 15);
    public static final DeferredBlock<Block> GREEN_LAMP = stone("green_lamp", 3.5f, 8, 15);
    public static final DeferredBlock<Block> ARCHIVE_SHELF = stone("archive_shelf", 3.5f, 8, 0);
    public static final DeferredBlock<AncientConsoleBlock> ANCIENT_CONSOLE = BLOCKS.registerBlock("ancient_console", AncientConsoleBlock::new, p -> p.strength(3.5f,8).sound(net.minecraft.world.level.block.SoundType.METAL).lightLevel(state -> state.getValue(AncientConsoleBlock.LIT)?12:1));
    public static final DeferredBlock<Block> ALLOY_VENT = stone("alloy_vent", 3.5f, 8, 0);
    public static final DeferredBlock<StairBlock> ASHSTONE_BRICKS_STAIRS = BLOCKS.registerBlock("ashstone_bricks_stairs", p -> new StairBlock(ASHSTONE_BRICKS.get().defaultBlockState(), p), p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<SlabBlock> ASHSTONE_BRICKS_SLAB = BLOCKS.registerBlock("ashstone_bricks_slab", SlabBlock::new, p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<StairBlock> POLISHED_ASHSTONE_STAIRS = BLOCKS.registerBlock("polished_ashstone_stairs", p -> new StairBlock(POLISHED_ASHSTONE.get().defaultBlockState(), p), p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<SlabBlock> POLISHED_ASHSTONE_SLAB = BLOCKS.registerBlock("polished_ashstone_slab", SlabBlock::new, p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<StairBlock> CUT_ALLOY_STAIRS = BLOCKS.registerBlock("cut_alloy_stairs", p -> new StairBlock(CUT_ALLOY.get().defaultBlockState(), p), p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<SlabBlock> CUT_ALLOY_SLAB = BLOCKS.registerBlock("cut_alloy_slab", SlabBlock::new, p -> p.strength(3.5f, 8).requiresCorrectToolForDrops());
    public static final DeferredBlock<fr.axephir.tnd.world.EchoBloomBlock> ECHO_BLOOM=BLOCKS.registerBlock("echo_bloom",fr.axephir.tnd.world.EchoBloomBlock::new,
            p->p.strength(2.5f,6).requiresCorrectToolForDrops().noOcclusion().randomTicks().lightLevel(state->2+state.getValue(fr.axephir.tnd.world.EchoBloomBlock.CHARGE)*3).sound(net.minecraft.world.level.block.SoundType.AMETHYST));
    public static final DeferredItem<Item> ECHO_DUST=ITEMS.registerSimpleItem("echo_dust");
    public static final DeferredItem<fr.axephir.tnd.world.EchoLensItem> ECHO_LENS=ITEMS.registerItem("echo_lens",fr.axephir.tnd.world.EchoLensItem::new,p->p.stacksTo(1));
    public static final DeferredItem<SpawnEggItem> SHARDLING_EGG=ITEMS.registerItem("shardling_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(fr.axephir.tnd.entities.VestigeEntities.SHARDLING.get()));
    public static final DeferredItem<SpawnEggItem> SENTINEL_EGG=ITEMS.registerItem("rift_sentinel_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(fr.axephir.tnd.entities.VestigeEntities.SENTINEL.get()));
    public static final DeferredBlock<fr.axephir.tnd.dungeon.VaultCoreBlock> VAULT_CORE=BLOCKS.registerBlock("vault_core",fr.axephir.tnd.dungeon.VaultCoreBlock::new,p->p.strength(5,12).requiresCorrectToolForDrops().lightLevel(state->6));
    public static final DeferredBlock<fr.axephir.tnd.dungeon.VaultBlock> VAULT_SEAL=BLOCKS.registerBlock("vault_seal",p->new fr.axephir.tnd.dungeon.VaultBlock(p,false),p->p.strength(5,12).requiresCorrectToolForDrops().lightLevel(state->state.getValue(fr.axephir.tnd.dungeon.VaultBlock.LIT)?12:2));
    public static final DeferredBlock<InscriptionBlock> MURAL_VAULT=BLOCKS.registerBlock("mural_vault",p->new InscriptionBlock(p,"vault"),p->p.strength(4,10).lightLevel(state->2));
    public static final DeferredItem<Item> RESONANCE_HEART=ITEMS.registerSimpleItem("resonance_heart");
    public static final DeferredItem<fr.axephir.tnd.dungeon.ResonantAegisItem> RESONANT_AEGIS=ITEMS.registerItem("resonant_aegis",fr.axephir.tnd.dungeon.ResonantAegisItem::new,p->p.stacksTo(1));
    public static final DeferredItem<Item> RELIC_FRAGMENT = ITEMS.registerSimpleItem("relic_fragment");
    public static final DeferredItem<Item> STABILIZER_MODULE = ITEMS.registerSimpleItem("stabilizer_module", p -> p.stacksTo(1));
    public static final DeferredItem<Item> BLUE_SHARD = ITEMS.registerSimpleItem("blue_shard");
    public static final DeferredItem<Item> RED_SHARD = ITEMS.registerSimpleItem("red_shard");
    public static final DeferredItem<Item> GREEN_SHARD = ITEMS.registerSimpleItem("green_shard");
    public static final DeferredItem<Item> RESONANT_SHARD = ITEMS.registerSimpleItem("resonant_shard");
    public static final DeferredItem<Item> COMPOSITE_CRYSTAL = ITEMS.registerSimpleItem("composite_crystal");
    public static final DeferredItem<Item> LOCATOR_CORE = ITEMS.registerSimpleItem("locator_core", p -> p.stacksTo(1));
    public static final DeferredItem<Item> EFFICIENCY_MODULE = ITEMS.registerSimpleItem("efficiency_module", p -> p.stacksTo(1));
    public static final DeferredItem<FieldJournalItem> FIELD_JOURNAL = ITEMS.registerItem("field_journal", FieldJournalItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<DetectorItem> DETECTOR = ITEMS.registerItem("detector", DetectorItem::new, p -> p.stacksTo(1));
    public static final DeferredHolder<MenuType<?>, MenuType<MasteryMenu>> MASTERY_MENU = MENUS.register("masteries",
            () -> new MenuType<>(MasteryMenu::new, FeatureFlags.DEFAULT_FLAGS));

    static {
        BLOCKS.getEntries().forEach(block -> ITEMS.registerSimpleBlockItem(block.getId().getPath(), block));
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tnd"))
            .icon(() -> COMPOSITE_CRYSTAL.get().getDefaultInstance())
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private static DeferredBlock<Block> stone(String name, float hardness, float resistance, int light) {
        boolean metal = name.contains("alloy") || name.contains("conduit") || name.contains("grate")
                || name.contains("vent") || name.contains("console");
        return BLOCKS.registerSimpleBlock(name, p -> p.mapColor(MapColor.COLOR_GRAY)
                .strength(hardness, resistance).requiresCorrectToolForDrops().lightLevel(state -> light)
                .sound(name.equals("ashen_silt")?net.minecraft.world.level.block.SoundType.MUD:
                        name.equals("vitrified_ash")?net.minecraft.world.level.block.SoundType.SAND:
                        name.equals("vitreous_stone")?net.minecraft.world.level.block.SoundType.DEEPSLATE:
                        name.equals("fossil_column")?net.minecraft.world.level.block.SoundType.CALCITE:
                        metal ? net.minecraft.world.level.block.SoundType.METAL : name.endsWith("_lamp")
                        ? net.minecraft.world.level.block.SoundType.GLASS : net.minecraft.world.level.block.SoundType.STONE));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); TABS.register(bus); MENUS.register(bus); SOUNDS.register(bus);
    }
    private ModContent() {}
}
