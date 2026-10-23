package fr.axephir.tnd.client;

import fr.axephir.tnd.sanctuary.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;

/** Remappable R activation; a small owned-only HUD shows the single shared cooldown. */
final class RelicControls {
    private static KeyMapping key;
    static Component binding(){return key==null?Component.literal("R"):key.getTranslatedKeyMessage();}
    static void register(IEventBus bus){bus.addListener((RegisterKeyMappingsEvent e)->{var category=new KeyMapping.Category(Identifier.fromNamespaceAndPath("tnd","relics"));e.registerCategory(category);key=new KeyMapping("key.tnd.archive_relic",org.lwjgl.glfw.GLFW.GLFW_KEY_R,category);e.register(key);});
        bus.addListener((RegisterGuiLayersEvent e)->e.registerAboveAll(Identifier.fromNamespaceAndPath("tnd","archive_relic"),(g,dt)->{
            var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden()||mc.gui.screen()!=null||!mc.player.getData(ArchiveContent.UNLOCKED))return;
            int choice=mc.player.getData(ArchiveContent.SELECTION);long left=Math.max(0,(mc.player.getData(ArchiveContent.READY)-mc.level.getGameTime()+19)/20);int x=g.guiWidth()/2+99,y=g.guiHeight()-24;
            AstralPanel.inset(g,x,y,56,21);g.item(RelicScreen.icon(choice),x+3,y+2);g.text(mc.font,left>0?Long.toString(left):key.getTranslatedKeyMessage().getString(),x+23,y+7,left>0?ConstellationStyle.MUTED:ConstellationStyle.GOLD,false);
        }));net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{var mc=Minecraft.getInstance();if(key==null)return;while(key.consumeClick())if(mc.player!=null&&mc.gui.screen()==null)mc.player.connection.send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new RelicPowers.Activate()));});
    }
    private RelicControls(){}
}
