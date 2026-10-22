package fr.axephir.tnd.client;
import fr.axephir.tnd.city.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PortalMonitorScreen extends AbstractContainerScreen<PortalMonitorMenu> {
    private final PanelMotion motion=new PanelMotion();private Button start;
    public PortalMonitorScreen(PortalMonitorMenu m,Inventory inv,Component title){super(m,inv,title,206,233);}
    @Override protected void init(){super.init();
        addRenderableWidget(AstralButton.create(Component.literal("?"),b->minecraft.gui.setScreen(new GateGuideScreen(this))).bounds(leftPos+181,topPos+5,18,18).build());
        start=AstralButton.create(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.charging()?1:0)).bounds(leftPos+62,topPos+63,135,20).build();addRenderableWidget(start);
        addRenderableWidget(AstralButton.create(Component.translatable("codex.tnd.name"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,2)).bounds(leftPos+62,topPos+88,135,19).build());
    }
    @Override protected void containerTick(){super.containerTick();int status=menu.value(2);start.active=status==6||status==7||status==10;start.setMessage(Component.translatable(status==7?"gate.tnd.cancel":status==8?"gate.tnd.online":"gate.tnd.start"));}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;
        AstralPanel.frame(g,x,y,206,233);AstralPanel.heading(g,font,title,x+9,y+10,164,mx,my);
        g.text(font,Component.translatable("gate.tnd.assembly",8-menu.value(3),4-menu.value(4)),x+9,y+28,menu.value(3)+menu.value(4)==0?ConstellationStyle.CYAN:0xffe6b2a5,false);
        g.fill(x+10,y+42,x+196,y+49,0xff202c45);g.fill(x+11,y+43,x+11+motion.value(0,menu.value(0)*1000/32000)*184/1000,y+48,ConstellationStyle.CYAN);
        g.text(font,menu.value(0)+" / 32000 FE",x+10,y+52,ConstellationStyle.MUTED,false);AstralPanel.slot(g,x+22,y+64);
        g.text(font,Component.translatable("gate.tnd.crystal"),x+10,y+88,ConstellationStyle.MUTED,false);
        g.fill(x+11,y+112,x+196,y+119,0xff202c45);g.fill(x+12,y+113,x+12+motion.value(1,menu.value(1)*1000/120)*183/1000,y+118,0xffb892cf);
        var text=Component.translatable("gate.tnd.state."+menu.value(2));var lines=font.split(text,185);for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),x+10,y+124+i*10,ConstellationStyle.MUTED,false);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)AstralPanel.slot(g,x+22+col*18,y+151+row*18);for(int col=0;col<9;col++)AstralPanel.slot(g,x+22+col*18,y+209);
    }
}
