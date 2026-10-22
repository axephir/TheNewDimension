package fr.axephir.tnd.client;
import fr.axephir.tnd.technology.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class SpatialAnchorScreen extends AbstractContainerScreen<SpatialAnchorMenu> {
    private Button toggle;private final PanelMotion motion=new PanelMotion();
    public SpatialAnchorScreen(SpatialAnchorMenu menu,Inventory inv,Component title){super(menu,inv,title,300,210);}
    @Override protected void init(){super.init();toggle=AstralButton.create(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+114,topPos+177,172,20).build();addRenderableWidget(toggle);}
    @Override protected void containerTick(){super.containerTick();toggle.setMessage(Component.translatable(menu.enabled()?"anchor.tnd.stop":"anchor.tnd.start"));}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,300,210);g.text(font,title,x+12,y+12,ConstellationStyle.TEXT,false);AstralPanel.inset(g,x+10,y+34,280,132);
        for(int dx=0;dx<3;dx++)for(int dz=0;dz<3;dz++){int xx=x+19+dx*27,yy=y+45+dz*27;g.fill(xx,yy,xx+24,yy+24,menu.chunks()>0?0xff528f90:0xff30444b);if(dx==1&&dz==1)g.item(SpatialAnchors.BLOCK.get().asItem().getDefaultInstance(),xx+4,yy+4);}
        g.text(font,Component.translatable("anchor.tnd.chunks",menu.chunks()),x+117,y+43,ConstellationStyle.CYAN,false);
        int yy=y+62;for(var line:font.split(Component.translatable("anchor.tnd.status."+menu.status()),160)){g.text(font,line,x+117,yy,ConstellationStyle.TEXT,false);yy+=11;}
        g.text(font,menu.energy()+" / 128 000 FE",x+117,y+106,ConstellationStyle.GOLD,false);g.fill(x+117,y+120,x+278,y+126,0xff202c45);g.fill(x+117,y+120,x+117+161*motion.value(0,menu.energy()*1000/128000)/1000,y+126,ConstellationStyle.CYAN);
        g.text(font,Component.translatable("anchor.tnd.cost"),x+117,y+136,ConstellationStyle.MUTED,false);g.text(font,Component.translatable("anchor.tnd.limit"),x+16,y+181,ConstellationStyle.MUTED,false);
        g.text(font,Component.translatable("anchor.tnd.assembly"),x+16,y+153,ConstellationStyle.MUTED,false);
    }
}
