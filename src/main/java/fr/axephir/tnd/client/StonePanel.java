package fr.axephir.tnd.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Pixel bevels and inset panels follow the visual language of vanilla containers. */
final class StonePanel {
    static void frame(GuiGraphicsExtractor g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xff191c18);
        g.fill(x+1,y+1,x+w-1,y+h-1,0xff666a5c);
        g.fill(x+2,y+2,x+w-2,y+h-2,0xffd0cebb);
        g.fill(x+3,y+3,x+w-3,y+h-3,0xff999e8c);
        g.fill(x+4,y+4,x+w-4,y+h-4,0xffb4b7a3);
        g.fill(x+4,y+h-5,x+w-4,y+h-4,0xff737b68);
        for(int dx:new int[]{5,w-7}) for(int dy:new int[]{5,h-7}) {
            g.fill(x+dx,y+dy,x+dx+2,y+dy+2,0xff69715e);
            g.fill(x+dx,y+dy,x+dx+1,y+dy+1,0xffe4d9aa);
        }
    }
    static void inset(GuiGraphicsExtractor g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xff59604f);
        g.fill(x+1,y+1,x+w,y+h,0xffd2d4bd);
        g.fill(x+1,y+1,x+w-1,y+h-1,0xff303c33);
    }
    static void slot(GuiGraphicsExtractor g,int x,int y) {
        g.fill(x-1,y-1,x+17,y+17,0xff626657);
        g.fill(x,y,x+17,y+17,0xffe2e1cd);
        g.fill(x,y,x+16,y+16,0xff8b917e);
    }
    private StonePanel() {}
}
