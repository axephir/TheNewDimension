package fr.axephir.tnd.client;

import fr.axephir.tnd.technology.Industry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class IndustrialGuideScreen extends Screen {
    private final Screen parent;private Industry.Kind kind;private int x,y;
    public IndustrialGuideScreen(Screen parent,Industry.Kind kind){super(Component.translatable("industry.tnd.guide"));this.parent=parent;this.kind=kind;}
    @Override protected void init(){x=(width-332)/2;y=(height-232)/2;
        for(var k:Industry.Kind.values()){var b=Button.builder(Component.translatable("block.tnd."+k.id),v->kind=k).bounds(x+8,y+31+k.ordinal()*22,121,20).build();b.setOverrideRenderHighlightedSprite(()->kind==k);addRenderableWidget(b);}
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(x+305,y+6,18,18).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
        StonePanel.frame(g,x,y,332,232);g.text(font,title,x+10,y+11,0xff29382e,false);StonePanel.inset(g,x+136,y+30,188,194);
        g.item(Industry.BLOCKS.get(kind).get().asItem().getDefaultInstance(),x+144,y+37);
        int yy=y+61;for(int row=0;row<3;row++){var lines=font.split(Component.translatable("industry.tnd.help."+kind.id+"."+row),169);for(var line:lines){g.text(font,line,x+144,yy,0xffd7ddc8,false);yy+=10;}yy+=9;}
        super.extractRenderState(g,mx,my,partial);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}@Override public boolean isPauseScreen(){return false;}
}
