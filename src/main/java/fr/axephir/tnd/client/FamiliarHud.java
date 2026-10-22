package fr.axephir.tnd.client;

import fr.axephir.tnd.entities.Shardling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** A three-second in-world bonding vignette. The player keeps camera/control; reduced motion omits the vignette. */
final class FamiliarHud {
    static void register(RegisterGuiLayersEvent e){e.registerAboveAll(Identifier.fromNamespaceAndPath("tnd","symbiosis"),FamiliarHud::render);}
    private static void render(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker dt){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.gui.screen()!=null||mc.gui.hud.isHidden())return;
        for(var pet:mc.level.getEntitiesOfClass(Shardling.class,mc.player.getBoundingBox().inflate(12),p->p.getOwnerReference()!=null&&p.getOwnerReference().matches(mc.player)&&p.bondTicks()>0)){
            int t=pet.bondTicks();var line=Component.translatable(t>30?"familiar.tnd.scene.0":"familiar.tnd.scene.1",pet.getName());int width=g.guiWidth(),height=g.guiHeight();
            if(AtmosphereOptions.ANIMATIONS.get()){int alpha=Math.min(190,Math.min(t,60-t)*15);g.fill(0,0,width,14,alpha<<24);g.fill(0,height-35,width,height,alpha<<24);}
            g.text(mc.font,line,(width-mc.font.width(line))/2,height-24,0xffd2f3ed,true);return;
        }
    }
    private FamiliarHud(){}
}
