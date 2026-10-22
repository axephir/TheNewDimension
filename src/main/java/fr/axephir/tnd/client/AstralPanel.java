package fr.axephir.tnd.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/** Shared Constellations surfaces. Cards stay opaque and slot coordinates stay native. */
final class AstralPanel {
    static final int TEXT=ConstellationStyle.TEXT,MUTED=ConstellationStyle.MUTED,CYAN=ConstellationStyle.CYAN,GOLD=ConstellationStyle.GOLD;
    static void frame(GuiGraphicsExtractor g,int x,int y,int w,int h){
        if(h<=28){ConstellationStyle.plate(g,x,y,w,h,0xff405574,0xff17273c);return;}
        ConstellationStyle.panel(g,x,y,w,h);
        g.enableScissor(x+2,y+26,x+w-2,y+h-3);
        ConstellationStyle.space(g,x+2,y+26,w-4,h-29,AtmosphereOptions.ANIMATIONS.get()?Util.getMillis()/1000.0:0);
        g.disableScissor();
    }
    static void inset(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(x,y,x+w,y+h,0xff2b3a54);g.fill(x+1,y+1,x+w-1,y+h-1,0xff111c2f);}
    static void slot(GuiGraphicsExtractor g,int x,int y){
        g.fill(x-1,y-1,x+17,y+17,0xff405574);g.fill(x,y,x+16,y+16,0xff0b1220);
        g.fill(x+1,y+1,x+16,y+16,0xff152137);g.fill(x,y+15,x+16,y+16,0xff293a55);
    }
    static void heading(GuiGraphicsExtractor g,Font font,Component text,int x,int y,int width,int mx,int my){
        String value=text.getString();boolean clipped=font.width(value)>width;
        if(clipped)value=font.plainSubstrByWidth(value,Math.max(0,width-10))+"…";
        g.text(font,value,x,y,TEXT,false);
        if(clipped&&mx>=x&&mx<x+width&&my>=y&&my<y+11)g.setTooltipForNextFrame(text,mx,my);
    }
    private AstralPanel(){}
}
