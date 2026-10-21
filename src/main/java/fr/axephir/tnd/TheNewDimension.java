package fr.axephir.tnd;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.core.TndConfig;
import fr.axephir.tnd.progression.ProgressionEvents;
import fr.axephir.tnd.progression.PlayerProgression;
import fr.axephir.tnd.world.DevelopmentCommands;
import fr.axephir.tnd.technology.MachineRegistry;
import fr.axephir.tnd.world.StarterOutpost;
import fr.axephir.tnd.gametest.ReadinessTests;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(TheNewDimension.ID)
public final class TheNewDimension {
    public static final String ID = "tnd";

    public TheNewDimension(IEventBus bus, ModContainer container) {
        MachineRegistry.register(bus);
        fr.axephir.tnd.entities.VestigeEntities.register(bus);
        ReadinessTests.FUNCTIONS.register(bus);
        ModContent.register(bus);
        fr.axephir.tnd.world.HabitatOreFeature.FEATURES.register(bus);
        PlayerProgression.TYPES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, TndConfig.SERVER);
        NeoForge.EVENT_BUS.register(new ProgressionEvents());
        NeoForge.EVENT_BUS.register(new fr.axephir.tnd.progression.ExpandedMasteries());
        NeoForge.EVENT_BUS.register(new StarterOutpost());
        NeoForge.EVENT_BUS.register(new fr.axephir.tnd.entities.CompanionTravel());
        NeoForge.EVENT_BUS.addListener(DevelopmentCommands::register);
    }
}
