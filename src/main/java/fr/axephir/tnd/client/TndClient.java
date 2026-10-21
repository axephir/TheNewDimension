package fr.axephir.tnd.client;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TheNewDimension.ID, dist = Dist.CLIENT)
public final class TndClient {
    public TndClient(IEventBus bus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,AtmosphereOptions.SPEC);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtmosphereDirector::tick);
        bus.addListener(TndClient::screens);
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e)->RelicRenderer.layers(e));
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->RelicRenderer.renderers(e));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(InventoryMasteries::init);
    }
    private static void screens(RegisterMenuScreensEvent event) {
        event.register(ModContent.MASTERY_MENU.get(), MasteryScreen::new);
        event.register(MachineRegistry.EXTRACTOR_MENU.get(), ExtractorScreen::new);
    }
}
