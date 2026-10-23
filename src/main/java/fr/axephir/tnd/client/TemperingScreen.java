package fr.axephir.tnd.client;

import fr.axephir.tnd.sanctuary.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Same quiet astral visual language as masteries, with an explicit irreversible choice and preview. */
public final class TemperingScreen extends AbstractContainerScreen<TemperingMenu> {
    private final Button[] choices=new Button[3];private Button start,cancel;
    private final PanelMotion motion=new PanelMotion();
    public TemperingScreen(TemperingMenu menu,Inventory inv,Component title){super(menu,inv,title,342,234);}
    private static Component tr(String key,Object...args){return Component.translatable("gui.tnd.tempering."+key,args);}
    protected void init(){super.init();
        for(int i=0;i<3;i++){final int n=i;choices[i]=addRenderableWidget(AstralButton.create(tr("choice."+i),b->action(n)).bounds(leftPos+192,topPos+47+i*25,136,20).tooltip(Tooltip.create(tr("effect."+i))).build());}
        start=addRenderableWidget(AstralButton.create(tr("start"),b->action(3)).bounds(leftPos+192,topPos+166,136,20).build());
        cancel=addRenderableWidget(AstralButton.create(tr("cancel"),b->action(4)).bounds(leftPos+192,topPos+194,136,20).build());
        addRenderableWidget(AstralButton.create(Component.literal("?"),b->{}).bounds(leftPos+312,topPos+7,18,18).tooltip(Tooltip.create(tr("structure_help"))).build());
    }
    private void action(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    protected void containerTick(){super.containerTick();for(int i=0;i<3;i++){choices[i].active=!menu.armed();choices[i].setMessage(Component.literal(menu.choice()==i?"◆ ":"◇ ").append(tr("choice."+i)));}start.active=menu.status()==8;cancel.active=menu.armed();}
    protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,imageWidth,imageHeight);AstralPanel.heading(g,font,title,x+12,y+12,291,mx,my);g.text(font,tr("cost"),x+12,y+29,ConstellationStyle.MUTED,false);
        AstralPanel.inset(g,x+12,y+44,166,66);g.text(font,tr("equipment"),x+48,y+58,ConstellationStyle.TEXT,false);g.text(font,tr("result"),x+132,y+53,ConstellationStyle.TEXT,false);
        for(int[] p:new int[][]{{24,54},{24,88},{60,88},{150,70}})AstralPanel.slot(g,x+p[0],y+p[1]);
        g.fill(x+103,y+76,x+138,y+83,0xff202c45);g.fill(x+104,y+77,x+104+motion.value(0,menu.progress()*1000/TemperingForgeEntity.PERIOD)*33/1000,y+82,ConstellationStyle.GOLD);
        var preview=menu.preview();if(!preview.isEmpty()&&menu.getSlot(3).getItem().isEmpty()){g.item(preview,x+150,y+70);if(mx>=x+149&&mx<x+168&&my>=y+69&&my<y+89&&menu.getSlot(3).getItem().isEmpty())g.setTooltipForNextFrame(font,preview,mx,my);}
        g.fill(x+12,y+117,x+178,y+121,0xff202c45);g.fill(x+12,y+117,x+12+Math.clamp(menu.energy()*166/TemperingForgeEntity.CAPACITY,0,166),y+121,ConstellationStyle.CYAN);
        g.text(font,tr("energy",menu.energy()),x+192,y+126,ConstellationStyle.CYAN,false);
        AstralPanel.heading(g,font,tr("status."+menu.status()),x+12,y+126,166,mx,my);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)AstralPanel.slot(g,x+12+c*18,y+141+r*18);for(int c=0;c<9;c++)AstralPanel.slot(g,x+12+c*18,y+199);
        AstralPanel.heading(g,font,tr("permanent"),x+192,y+145,136,mx,my);
        g.text(font,tr("one_choice"),x+12,y+222,ConstellationStyle.MUTED,false);
    }
}
