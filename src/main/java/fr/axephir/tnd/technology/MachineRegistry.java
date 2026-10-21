package fr.axephir.tnd.technology;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.PortalControllerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class MachineRegistry {
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheNewDimension.ID);
    public static final DeferredBlock<ExtractorBlock> EXTRACTOR = ModContent.BLOCKS.registerBlock("extractor", ExtractorBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY).sound(net.minecraft.world.level.block.SoundType.METAL).strength(5, 12).requiresCorrectToolForDrops());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_ENTITY = ENTITIES.register("extractor",
            () -> new BlockEntityType<>(ExtractorBlockEntity::new, EXTRACTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalControllerEntity>> PORTAL_ENTITY = ENTITIES.register("portal_controller",
            () -> new BlockEntityType<>(PortalControllerEntity::new, ModContent.PORTAL_CONTROLLER.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<fr.axephir.tnd.dungeon.VaultCoreEntity>> VAULT_ENTITY=ENTITIES.register("vault_core",
            ()->new BlockEntityType<>(fr.axephir.tnd.dungeon.VaultCoreEntity::new,ModContent.VAULT_CORE.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorMenu>> EXTRACTOR_MENU = ModContent.MENUS.register("extractor",
            () -> new MenuType<>(ExtractorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredBlock<ResonanceForgeBlock> FORGE=ModContent.BLOCKS.registerBlock("resonance_forge",ResonanceForgeBlock::new,
            p->p.mapColor(MapColor.COLOR_GRAY).sound(net.minecraft.world.level.block.SoundType.METAL).strength(5,12).requiresCorrectToolForDrops().lightLevel(s->s.getValue(ResonanceForgeBlock.LIT)?10:0));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ResonanceForgeEntity>> FORGE_ENTITY=ENTITIES.register("resonance_forge",()->new BlockEntityType<>(ResonanceForgeEntity::new,FORGE.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<ResonanceForgeMenu>> FORGE_MENU=ModContent.MENUS.register("resonance_forge",()->new MenuType<>(ResonanceForgeMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<fr.axephir.tnd.expedition.ReturnAnchorEntity>> ANCHOR_ENTITY=ENTITIES.register("return_anchor",()->new BlockEntityType<>(fr.axephir.tnd.expedition.ReturnAnchorEntity::new,ModContent.RETURN_ANCHOR.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<fr.axephir.tnd.expedition.SurveyTerminalEntity>> SURVEY_ENTITY=ENTITIES.register("survey_terminal",()->new BlockEntityType<>(fr.axephir.tnd.expedition.SurveyTerminalEntity::new,ModContent.SURVEY_TERMINAL.get()));
    public static void register(IEventBus bus) {
        ModContent.ITEMS.registerSimpleBlockItem("extractor", EXTRACTOR);
        ModContent.ITEMS.registerSimpleBlockItem("resonance_forge",FORGE);
        ENTITIES.register(bus);
    }
    private MachineRegistry() {}
}
