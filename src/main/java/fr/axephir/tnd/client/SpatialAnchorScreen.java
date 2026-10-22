package fr.axephir.tnd.client;
import fr.axephir.tnd.technology.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class SpatialAnchorScreen extends AbstractContainerScreen<SpatialAnchorMenu> {
    private Button toggle;
    public SpatialAnchorScreen(SpatialAnchorMenu menu,Inventory inv,Component title){super(menu,inv,title,300,210);}
    @Override protected void init(){super.init();toggle=Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+114,topPos+177,172,20).build();addRenderableWidget(toggle);}
    @Override protected void containerTick(){super.containerTick();toggle.setMessage(Component.translatable(menu.enabled()?"anchor.tnd.stop":"anchor.tnd.start"));}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;StonePanel.frame(g,x,y,300,210);g.text(font,title,x+12,y+12,0xff263d3d,false);StonePanel.inset(g,x+10,y+34,280,132);
        for(int dx=0;dx<3;dx++)for(int dz=0;dz<3;dz++){int xx=x+19+dx*27,yy=y+45+dz*27;g.fill(xx,yy,xx+24,yy+24,menu.chunks()>0?0xff528f90:0xff30444b);if(dx==1&&dz==1)g.item(SpatialAnchors.BLOCK.get().asItem().getDefaultInstance(),xx+4,yy+4);}
        g.text(font,Component.translatable("anchor.tnd.chunks",menu.chunks()),x+117,y+43,0xffc4e6df,false);
        int yy=y+62;for(var line:font.split(Component.translatable("anchor.tnd.status."+menu.status()),160)){g.text(font,line,x+117,yy,0xffdce3cf,false);yy+=11;}
        g.text(font,menu.energy()+" / 128 000 FE",x+117,y+106,0xffe4cf9b,false);g.fill(x+117,y+120,x+278,y+126,0xff172c34);g.fill(x+117,y+120,x+117+161*menu.energy()/128000,y+126,0xff77c8c2);
        g.text(font,Component.translatable("anchor.tnd.cost"),x+117,y+136,0xffbdccce,false);g.text(font,Component.translatable("anchor.tnd.limit"),x+16,y+181,0xff2e4140,false);
        g.text(font,Component.translatable("anchor.tnd.assembly"),x+16,y+153,0xffc1cacc,false);
    }
}
