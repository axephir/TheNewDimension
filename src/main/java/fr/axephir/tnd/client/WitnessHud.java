package fr.axephir.tnd.client;
import fr.axephir.tnd.domain.SanctuaryRoute;
import fr.axephir.tnd.sanctuary.*;
import fr.axephir.tnd.world.DevelopmentCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
/** Compact context help reads only the block already under the crosshair. */
final class WitnessHud {
    static void render(GuiGraphicsExtractor g){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(DevelopmentCommands.DIMENSION)||!(mc.hitResult instanceof BlockHitResult hit)||mc.player.distanceToSqr(hit.getLocation())>36||!mc.level.hasChunkAt(hit.getBlockPos()))return;
        var at=hit.getBlockPos();var site=SanctuaryRoute.nearest(at.getX(),at.getZ());int index=SanctuaryRoute.indexAt(at.getX()-site.x(),at.getZ()-site.z());if(index<0||index>=16)return;
        var state=mc.level.getBlockState(at);String key=state.is(SanctuaryContent.FOUNDRY_VALVE.get())?(index==15?"vent":"valve"):state.is(SanctuaryContent.COUPLER.get())?(index==4?"bridge":"coupler"):state.is(SanctuaryContent.RELIQUARY.get())?"reliquary":state.is(SanctuaryContent.DIAL.get())?"prism":null;if(key==null)return;
        var text=Component.translatable((index>=8?"observatory.tnd.foundry.hint.":"observatory.tnd.witness.hint.")+key);int width=mc.font.width(text)+18,x=(g.guiWidth()-width)/2,y=g.guiHeight()-100;
        AstralPanel.inset(g,x,y,width,20);g.text(mc.font,text,x+9,y+6,ConstellationStyle.CYAN,false);
    }
    private WitnessHud(){}
}
