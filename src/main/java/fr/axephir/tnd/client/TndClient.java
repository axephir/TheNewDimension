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
        bus.addListener(UmbraArmorModels::register);
        bus.addListener(VestigeHud::register);
        bus.addListener(FamiliarHud::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(VestigeHud::tick);
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions e)->{RelicRenderer.layers(e);CustodianRenderer.layers(e);AwakenedRenderer.layers(e);CityRenderer.layers(e);});
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->{RelicRenderer.renderers(e);CustodianRenderer.renderers(e);AwakenedRenderer.renderers(e);CityRenderer.renderers(e);e.registerBlockEntityRenderer(MachineRegistry.PORTAL_ENTITY.get(),c->new GateRenderer());});
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(InventoryMasteries::init);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(InventoryMasteries::position);
    }
    private static void screens(RegisterMenuScreensEvent event) {
        event.register(fr.axephir.tnd.familiar.FamiliarContent.MENU.get(),FamiliarScreen::new);
        event.register(fr.axephir.tnd.city.CityContent.MONITOR_MENU.get(),PortalMonitorScreen::new);
        event.register(fr.axephir.tnd.city.CityContent.CODEX_MENU.get(),CodexScreen::new);
        event.register(ModContent.MASTERY_MENU.get(), MasteryScreen::new);
        event.register(ModContent.EXPEDITION_MENU.get(),ExpeditionScreen::new);
        event.register(MachineRegistry.EXTRACTOR_MENU.get(), ExtractorScreen::new);
        event.register(MachineRegistry.FORGE_MENU.get(),ResonanceForgeScreen::new);
        event.register(MachineRegistry.SEPARATOR_MENU.get(),ResonanceForgeScreen::new);
        event.register(MachineRegistry.REAPER_MENU.get(),ResonanceForgeScreen::new);
        event.register(fr.axephir.tnd.technology.Industry.MENU.get(),IndustrialScreen::new);
    }
}
