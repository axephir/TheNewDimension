package fr.axephir.tnd.client;
import fr.axephir.tnd.city.CityContent;
import fr.axephir.tnd.technology.Industry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GateGuideScreen extends Screen {
    private final Screen parent;private int page,x,y;
    public GateGuideScreen(Screen parent){super(Component.translatable("gate.tnd.guide"));this.parent=parent;}
    @Override protected void init(){x=(width-304)/2;y=(height-226)/2;
        for(int i=0;i<3;i++){final int p=i;var tab=addRenderableWidget(AstralButton.create(Component.translatable("gate.tnd.guide.tab."+i),b->page=p).bounds(x+8+i*96,y+30,94,20).build());tab.setOverrideRenderHighlightedSprite(()->page==p);}
        addRenderableWidget(AstralButton.create(Component.literal("×"),b->onClose()).bounds(x+278,y+6,18,18).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){AstralPanel.frame(g,x,y,304,226);g.text(font,title,x+10,y+12,ConstellationStyle.TEXT,false);AstralPanel.inset(g,x+8,y+55,288,163);
        int yy=y+64;
        if(page==0){for(int row=0;row<3;row++)for(int col=0;col<3;col++){
            var block=row==1&&col==1?CityContent.MONITOR.get():row!=1&&col!=1?CityContent.DECOR.get("relay_coil").get():Industry.FRAME.get();
            g.item(block.asItem().getDefaultInstance(),x+17+col*20,yy+row*20);
        }var lines=font.split(Component.translatable("gate.tnd.guide.diagram"),197);for(int i=0;i<lines.size();i++)g.text(font,lines.get(i),x+86,yy+i*10,ConstellationStyle.TEXT,false);yy+=68;}
        var lines=font.split(Component.translatable("gate.tnd.guide.text."+page),269);for(int i=0;i<Math.min(page==0?8:15,lines.size());i++)g.text(font,lines.get(i),x+17,yy+i*10,ConstellationStyle.TEXT,false);
        super.extractRenderState(g,mx,my,partial);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}@Override public boolean isPauseScreen(){return false;}
}
