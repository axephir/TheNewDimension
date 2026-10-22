package fr.axephir.tnd.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import java.util.function.Supplier;

/** Mod-local controls only: native focus/narration and packets, with one visual and sound language. */
class AstralButton extends Button {
    private Supplier<Boolean> selected=()->false;
    private Tooltip explicitTooltip;
    AstralButton(int x,int y,int w,int h,Component message,OnPress press,CreateNarration narration){super(x,y,w,h,message,press,narration);}
    static Builder create(Component message,OnPress press){return new Builder(message,press);}
    @Override public void setOverrideRenderHighlightedSprite(Supplier<Boolean> selected){super.setOverrideRenderHighlightedSprite(selected);this.selected=selected;}
    @Override public void setTooltip(Tooltip tooltip){super.setTooltip(tooltip);explicitTooltip=tooltip;}
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){
        boolean on=selected.get(),hot=isHoveredOrFocused();
        ConstellationStyle.plate(g,getX(),getY(),getWidth(),getHeight(),active?(on||hot?ConstellationStyle.CYAN:0xff405574):0xff263149,active?(on?0xff213a47:hot?0xff254051:0xff17273c):0xff101b2d);
        if(on)g.fill(getX()+1,getY()+5,getX()+3,getY()+getHeight()-5,active?ConstellationStyle.CYAN:ConstellationStyle.DIM);
        var font=Minecraft.getInstance().font;String value=getMessage().getString();int width=getWidth()-(getWidth()<25?4:8);
        boolean clipped=font.width(value)>width;
        if(clipped)value=font.plainSubstrByWidth(value,Math.max(0,width-10))+"…";
        g.text(font,value,getX()+(getWidth()-font.width(value))/2,getY()+(getHeight()-8)/2,active?ConstellationStyle.TEXT:ConstellationStyle.DIM,false);
        if(clipped&&hot&&explicitTooltip==null)g.setTooltipForNextFrame(getMessage(),mx,my);
    }
    @Override public void playDownSound(SoundManager manager){manager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(),1.3f,.18f));}
    static final class Builder {
        private final Component message;private final OnPress press;private int x,y,width=150,height=20;private Tooltip tooltip;
        Builder(Component message,OnPress press){this.message=message;this.press=press;}
        Builder bounds(int x,int y,int width,int height){this.x=x;this.y=y;this.width=width;this.height=height;return this;}
        Builder tooltip(Tooltip tooltip){this.tooltip=tooltip;return this;}
        AstralButton build(){var button=new AstralButton(x,y,width,height,message,press,n->n.get());button.setTooltip(tooltip);return button;}
    }
}
