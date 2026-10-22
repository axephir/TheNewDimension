package fr.axephir.tnd.client;
import fr.axephir.tnd.domain.Mastery;
import fr.axephir.tnd.progression.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
/** A discreet personal signature. No chat decoration, potion-side buttons or combat statistics are changed. */
public final class PrestigeHud {
    public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(Identifier.fromNamespaceAndPath("tnd","prestige"),(g,dt)->{var mc=Minecraft.getInstance();if(mc.player==null||mc.gui.hud.isHidden()||mc.gui.screen()!=null)return;int title=mc.player.getData(Prestige.SELECTION);if(title<0||title>=60)return;var m=Mastery.values()[title/4];if(mc.player.getData(PlayerProgression.PROFILE).get(m).level()<Prestige.LEVELS[title%4])return;
        var line=Component.translatable("prestige.tnd.title",Component.translatable("prestige.tnd.rank."+(title%4)),Component.translatable("mastery.tnd."+m.id()));int width=mc.font.width(line);int y=g.guiHeight()-18;g.fill(5,y-3,15+width,y+11,0x95101920);g.fill(5,y-3,7,y+11,new int[]{0xff95cfbe,0xffaad5e8,0xffcdb3e8,0xfff4d991}[title%4]);g.text(mc.font,line,11,y,0xffd6e6df,false);});}
    private PrestigeHud(){}
}
