package fr.axephir.tnd.gametest;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.*;
import fr.axephir.tnd.world.DevelopmentCommands;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Run with runGameTestServer in a disposable run-gametest directory. */
public final class ReadinessTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(
            BuiltInRegistries.TEST_FUNCTION, TheNewDimension.ID);
    static {
        SymbiosisTests.register(FUNCTIONS);
        FUNCTIONS.register("industry_energy_guards",()->IndustryTests::energyGuards);
        FUNCTIONS.register("industry_farm_guards",()->IndustryTests::farmGuards);
        FUNCTIONS.register("industry_ranch_cap",()->IndustryTests::ranchCap);
        FUNCTIONS.register("industry_milking",()->IndustryTests::milking);
        FUNCTIONS.register("industry_energy",()->IndustryTests::energy);
        FUNCTIONS.register("industry_generation",()->IndustryTests::generation);
        FUNCTIONS.register("industry_processing",()->IndustryTests::processing);
        FUNCTIONS.register("industry_hoppers",()->IndustryTests::hoppers);
        FUNCTIONS.register("industry_quarry_hopper",()->IndustryTests::quarryHopper);
        FUNCTIONS.register("industry_quarry",()->IndustryTests::quarry);
        FUNCTIONS.register("industry_quarry_guards",()->IndustryTests::quarryGuards);
        FUNCTIONS.register("industry_farming",()->IndustryTests::farming);
        FUNCTIONS.register("industry_ranching",()->IndustryTests::ranching);
        FUNCTIONS.register("industry_pasture",()->IndustryTests::pasture);
        FUNCTIONS.register("industry_recovery",()->IndustryTests::recovery);
        FUNCTIONS.register("industry_solar",()->IndustryTests::solar);
        FUNCTIONS.register("crowned_generation",()->CrownedSiteTests::generation);
        FUNCTIONS.register("crowned_mountain",()->CrownedSiteTests::mountain);
        FUNCTIONS.register("crowned_starter",()->CrownedSiteTests::starter);
        FUNCTIONS.register("awakening_materials",()->AwakeningTests::materials);
        FUNCTIONS.register("awakening_ores",()->AwakeningTests::ores);
        FUNCTIONS.register("lithoguard_hit",()->AwakeningTests::lithoHit);
        FUNCTIONS.register("lithoguard_dodge",()->AwakeningTests::lithoDodge);
        FUNCTIONS.register("lithoguard_patterns",()->AwakeningTests::lithoPatterns);
        FUNCTIONS.register("stalker_hit",()->AwakeningTests::stalkerHit);
        FUNCTIONS.register("stalker_interrupt",()->AwakeningTests::stalkerInterrupt);
        FUNCTIONS.register("awakened_loot",()->AwakeningTests::loot);
        FUNCTIONS.register("separator_processing",()->AwakeningTests::separatorProcessing);
        FUNCTIONS.register("separator_hoppers",()->AwakeningTests::separatorHoppers);
        FUNCTIONS.register("reaper_night",()->AwakeningTests::reaperNight);
        FUNCTIONS.register("reaper_guards",()->AwakeningTests::reaperGuards);
        FUNCTIONS.register("reaper_save",()->AwakeningTests::reaperSave);
        FUNCTIONS.register("awakening_equipment",()->AwakeningTests::equipment);
        FUNCTIONS.register("gravitas_wave",()->AwakeningTests::hammer);
        FUNCTIONS.register("citadel_layout",()->AwakeningTests::citadel);
        FUNCTIONS.register("conservatory_layout",()->AwakeningTests::conservatory);

        FUNCTIONS.register("field_objectives",()->FieldworkTests::objectives);
        FUNCTIONS.register("field_actions",()->FieldworkTests::actions);
        FUNCTIONS.register("field_completion",()->FieldworkTests::completion);
        FUNCTIONS.register("recall_success",()->FieldworkTests::recallSuccess);
        FUNCTIONS.register("recall_movement",()->FieldworkTests::recallMovement);
        FUNCTIONS.register("recall_damage",()->FieldworkTests::recallDamage);
        FUNCTIONS.register("recall_blocked",()->FieldworkTests::recallBlocked);
        FUNCTIONS.register("recall_replaced",()->FieldworkTests::recallReplaced);
        FUNCTIONS.register("recall_guards",()->FieldworkTests::recallGuards);
        FUNCTIONS.register("observatory",()->FieldworkTests::observatory);
        FUNCTIONS.register("survey_authority",()->FieldworkTests::surveyAuthority);
        FUNCTIONS.register("custodian_phase",()->ExpeditionTests::phase);
        FUNCTIONS.register("custodian_dodge",()->ExpeditionTests::dodge);
        FUNCTIONS.register("custodian_hit",()->ExpeditionTests::hit);
        FUNCTIONS.register("custodian_crown",()->ExpeditionTests::crown);
        FUNCTIONS.register("custodian_stagger",()->ExpeditionTests::stagger);
        FUNCTIONS.register("custodian_progress",()->ExpeditionTests::bossProgress);
        FUNCTIONS.register("custodian_recovery",()->ExpeditionTests::bossRecovery);
        FUNCTIONS.register("forge_processing",()->ExpeditionTests::forgeProcessing);
        FUNCTIONS.register("forge_recovery",()->ExpeditionTests::forgeRecovery);
        FUNCTIONS.register("forge_hoppers",()->ExpeditionTests::forgeHoppers);
        FUNCTIONS.register("vault_progress",()->DungeonTests::progress);
        FUNCTIONS.register("vault_recovery",()->DungeonTests::recovery);
        FUNCTIONS.register("vault_safety",()->DungeonTests::safety);
        FUNCTIONS.register("vault_rotations",()->DungeonTests::rotations);
        FUNCTIONS.register("resonant_aegis",()->DungeonTests::aegis);
        FUNCTIONS.register("biome_palette",()->AtmosphereTests::biomes);
        FUNCTIONS.register("vestige_landscapes",()->AtmosphereTests::landscapes);
        FUNCTIONS.register("mastery_celebration",()->AtmosphereTests::celebration);
        FUNCTIONS.register("dimension", () -> ReadinessTests::dimension);
        FUNCTIONS.register("extractor", () -> ReadinessTests::extractor);
        FUNCTIONS.register("portal_resistance", () -> ReadinessTests::portalResistance);
        PortalTests.register(FUNCTIONS);CityTests.register(FUNCTIONS);
        FUNCTIONS.register("companion_bond",()->EncounterTests::bond);
        FUNCTIONS.register("companion_follow",()->EncounterTests::follow);
        FUNCTIONS.register("companion_travel",()->EncounterTests::travel);
        FUNCTIONS.register("companion_cold_arrival",()->EncounterTests::coldArrival);
        FUNCTIONS.register("sentinel_dodge",()->EncounterTests::pulseDodge);
        FUNCTIONS.register("sentinel_hit",()->EncounterTests::pulseHit);
        FUNCTIONS.register("sentinel_interrupt",()->EncounterTests::pulseInterrupt);
        FUNCTIONS.register("echo_lens",()->EncounterTests::lens);
        FUNCTIONS.register("echo_deposits",()->EncounterTests::deposits);
        FUNCTIONS.register("mastery_actions", () -> GameplayTests::masteries);
        FUNCTIONS.register("console", () -> GameplayTests::console);
        FUNCTIONS.register("extraction_modes", () -> GameplayTests::extractionModes);
        FUNCTIONS.register("talent_menus", () -> GameplayTests::talentMenus);
        FUNCTIONS.register("talent_effects", () -> GameplayTests::talentEffects);
        FUNCTIONS.register("habitat_ores", () -> WorldExperienceTests::habitatOres);
        FUNCTIONS.register("buildings", () -> WorldExperienceTests::buildings);
        FUNCTIONS.register("outpost_terrain", () -> WorldExperienceTests::outpostTerrain);
    }
    private static void dimension(GameTestHelper helper) {
        // GameTestServer intentionally bakes only its flat preset, excluding datapack
        // dimensions. Parse our real LevelStem with runtime registries and exercise
        // its actual generator. A normal dedicated server separately checks level creation.
        var registries = helper.getLevel().registryAccess();
        LevelStem stem;
        try (var reader = helper.getLevel().getServer().getResourceManager().getResourceOrThrow(
                Identifier.fromNamespaceAndPath("tnd", "dimension/vestige.json")).openAsReader()) {
            stem = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), JsonParser.parseReader(reader)).getOrThrow();
        } catch (java.io.IOException e) { throw new IllegalStateException("Cannot read dimension definition", e); }
        helper.assertTrue(stem.type().value().defaultClock().isPresent(), Component.literal("Dimension needs its own clock"));
        var random = RandomState.create(registries, ResourceKey.create(Registries.NOISE_SETTINGS,
                Identifier.fromNamespaceAndPath("tnd", "vestige")), 987654L);
        var bounds = LevelHeightAccessor.create(0, 256);
        int height = stem.generator().getBaseHeight(48, 48, Heightmap.Types.WORLD_SURFACE, bounds, random);
        helper.assertTrue(height > 0 && height <= 256, Component.literal("Terrain must generate within dimension bounds"));
        boolean ashstone = false;
        var column = stem.generator().getBaseColumn(48, 48, bounds, random);
        for (int y = 1; y < height; y++) if (column.getBlock(y).is(ModContent.ASHSTONE.get())) ashstone = true;
        helper.assertTrue(ashstone, Component.literal("Dimension terrain must use original ashstone"));
        for(int x:new int[]{0,16,96,192}) {
            int peak=stem.generator().getBaseHeight(x,-x,Heightmap.Types.WORLD_SURFACE,bounds,random);
            helper.assertTrue(peak<=208,Component.literal("Terrain must leave enough headroom for the largest structures"));
        }
        helper.succeed();
    }
    private static void extractor(GameTestHelper helper) {
        BlockPos relative = new BlockPos(2, 1, 2);
        helper.setBlock(relative, MachineRegistry.EXTRACTOR.get());
        var machine = (ExtractorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        helper.runAfterDelay(21, () -> {
            helper.assertTrue(machine.getItem(2).isEmpty(), Component.literal("No fuel must produce no output"));
            machine.setItem(0, new ItemStack(Items.COAL, 4));
            machine.setItem(1, new ItemStack(ModContent.EFFICIENCY_MODULE.get()));
            int period = machine.periodTicks();
            helper.runAfterDelay(period + 40, () -> {
                helper.assertTrue(machine.getItem(2).is(Items.RAW_IRON), Component.literal("Loaded extractor must produce raw iron"));
                helper.assertTrue(machine.getItem(2).getCount() == 1, Component.literal("Exactly one cycle must finish"));
                helper.assertTrue(machine.getItem(0).getCount() == 3, Component.literal("One cycle consumes one coal"));
                var saved = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
                helper.assertTrue(saved.contains("items") && saved.contains("lastTick"), Component.literal("Inventory and production clock must be serialized"));
                helper.succeed();
            });
        });
    }
    private static void portalResistance(GameTestHelper helper) {
        BlockPos relative = new BlockPos(2, 1, 2);
        helper.setBlock(relative, ModContent.PORTAL_FRAME.get());
        var absolute = helper.absolutePos(relative);
        helper.assertTrue(helper.getLevel().getBlockState(absolute).getDestroySpeed(helper.getLevel(), absolute) < 0,
                Component.literal("Portal frame must be unbreakable by survival mining"));
        helper.getLevel().explode(null, null, null, Vec3.atCenterOf(absolute), 4, false, Level.ExplosionInteraction.BLOCK);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(absolute).is(ModContent.PORTAL_FRAME.get()),
                    Component.literal("Portal frame must survive explosions"));
            helper.succeed();
        });
    }
    private ReadinessTests() {}
}
