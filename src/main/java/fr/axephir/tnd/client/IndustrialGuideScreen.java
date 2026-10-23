package fr.axephir.tnd.client;

import fr.axephir.tnd.technology.Industry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class IndustrialGuideScreen extends Screen {
    private final Screen parent;private Industry.Kind kind;private int x,y,page;
    public IndustrialGuideScreen(Screen parent,Industry.Kind kind){super(Component.translatable("industry.tnd.guide"));this.parent=parent;this.kind=kind;this.page=kind.ordinal()/8;}
    @Override protected void init(){x=(width-332)/2;y=(height-232)/2;
        for(var k:Industry.Kind.values()){if(k.ordinal()/8!=page)continue;var b=AstralButton.create(Component.translatable("block.tnd."+k.id),v->kind=k).bounds(x+8,y+31+(k.ordinal()%8)*19,121,18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("block.tnd."+k.id))).build();b.setOverrideRenderHighlightedSprite(()->kind==k);addRenderableWidget(b);}
        addRenderableWidget(AstralButton.create(Component.literal("‹"),b->{page=Math.max(0,page-1);rebuildWidgets();}).bounds(x+8,y+190,26,18).build()).active=page>0;
        addRenderableWidget(AstralButton.create(Component.literal("›"),b->{page=Math.min((Industry.Kind.values().length-1)/8,page+1);rebuildWidgets();}).bounds(x+103,y+190,26,18).build()).active=(page+1)*8<Industry.Kind.values().length;
        addRenderableWidget(AstralButton.create(Component.literal("×"),b->onClose()).bounds(x+305,y+6,18,18).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
        AstralPanel.frame(g,x,y,332,232);g.text(font,title,x+10,y+11,ConstellationStyle.TEXT,false);AstralPanel.inset(g,x+136,y+30,188,194);
        g.text(font,(page+1)+" / "+((Industry.Kind.values().length+7)/8),x+55,y+196,ConstellationStyle.MUTED,false);
        g.item(Industry.BLOCKS.get(kind).get().asItem().getDefaultInstance(),x+144,y+37);
        int yy=y+61;for(int row=0;row<3;row++){var lines=font.split(Component.translatable("industry.tnd.help."+kind.id+"."+row),169);for(var line:lines){g.text(font,line,x+144,yy,ConstellationStyle.TEXT,false);yy+=10;}yy+=9;}
        super.extractRenderState(g,mx,my,partial);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}@Override public boolean isPauseScreen(){return false;}
}
