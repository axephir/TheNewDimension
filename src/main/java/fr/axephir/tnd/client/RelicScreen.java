package fr.axephir.tnd.client;

import fr.axephir.tnd.sanctuary.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

/** Three calm, illustrated choices feed one visibly exclusive socket. */
public final class RelicScreen extends AbstractContainerScreen<RelicMenu> {
    private final Button[] choices=new Button[3];private Button activate;
    public RelicScreen(RelicMenu menu,Inventory inv,Component title){super(menu,inv,title,392,234);}
    private static Component tr(String s,Object...args){return ArchiveScene.tr(s,args);}
    protected void init(){super.init();for(int i=0;i<3;i++){final int n=i;choices[i]=addRenderableWidget(AstralButton.create(tr("relic."+i+".name"),b->send(n)).bounds(leftPos+12+i*126,topPos+103,116,22).tooltip(Tooltip.create(tr("relic."+i+".help"))).build());}
        activate=addRenderableWidget(AstralButton.create(tr("invoke"),b->send(3)).bounds(leftPos+128,topPos+202,136,20).build());addRenderableWidget(AstralButton.create(Component.literal("×"),b->onClose()).bounds(leftPos+361,topPos+8,20,18).build());}
    private void send(int i){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,i);}
    protected void containerTick(){super.containerTick();for(int i=0;i<3;i++){choices[i].active=menu.value(0)==1&&menu.value(2)==0&&menu.value(1)!=i;}activate.active=menu.value(0)==1&&menu.value(2)==0;activate.setMessage(menu.value(2)>0?tr("cooldown",menu.value(2)):tr("invoke"));}
    static ItemStack icon(int i){return (i==0?Items.ENDER_PEARL:i==1?Items.SHIELD:fr.axephir.tnd.familiar.FamiliarContent.CAPSULE.get()).getDefaultInstance();}
    protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,392,234);g.text(font,title,x+12,y+12,ConstellationStyle.TEXT,false);
        Component sub=menu.value(0)==1?tr("socket_hint",RelicControls.binding()):tr("locked_hint");AstralPanel.heading(g,font,sub,x+12,y+35,365,mx,my);
        int selected=menu.value(1);ConstellationStyle.plate(g,x+176,y+55,40,36,menu.value(0)==1?ConstellationStyle.GOLD:ConstellationStyle.LOCKED,0xff172438);if(menu.value(0)==1)g.item(icon(selected),x+188,y+65);else g.item(ArchiveContent.LENS.get().getDefaultInstance(),x+188,y+65);
        for(int i=0;i<3;i++){int cx=x+12+i*126;int link=menu.value(0)==1&&selected==i?ConstellationStyle.GOLD:0xff35425a;ConstellationStyle.segment(g,x+196,y+91,x+196,y+96,0xff65728a);ConstellationStyle.segment(g,x+196,y+96,cx+58,y+96,link);ConstellationStyle.segment(g,cx+58,y+96,cx+58,y+101,link);AstralPanel.inset(g,cx,y+130,116,61);g.item(icon(i),cx+5,y+136);int yy=y+136;for(var line:font.split(tr("relic."+i+".short"),87)){if(yy>y+180)break;g.text(font,line,cx+25,yy,ConstellationStyle.TEXT,false);yy+=10;}if(menu.value(0)==1&&i==selected)g.text(font,tr("attuned"),cx+8,y+179,ConstellationStyle.GOLD,false);}
        if(mx>=x+176&&mx<x+216&&my>=y+55&&my<y+91)g.setTooltipForNextFrame(tr(menu.value(0)==1?"relic."+selected+".help":"locked_hint"),mx,my);
    }
}
