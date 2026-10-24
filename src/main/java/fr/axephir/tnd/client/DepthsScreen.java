package fr.axephir.tnd.client;
import fr.axephir.tnd.depths.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class DepthsScreen extends AbstractContainerScreen<DepthsMenu>{
 public DepthsScreen(DepthsMenu m,Inventory inv,Component c){super(m,inv,c,316,214);}
 protected void init(){super.init();addRenderableWidget(AstralButton.create(Component.translatable("depths.tnd.engage"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+10,topPos+150,144,20).build());addRenderableWidget(AstralButton.create(Component.translatable("depths.tnd.extract"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1)).bounds(leftPos+162,topPos+150,144,20).build());addRenderableWidget(AstralButton.create(Component.translatable("depths.tnd.reset"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,2)).bounds(leftPos+10,topPos+179,296,20).build());}
 protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float p){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,316,214);AstralPanel.heading(g,font,Component.translatable("depths.tnd.title"),x+12,y+11,290,mx,my);g.text(font,Component.translatable("depths.tnd.chamber."+Math.clamp(menu.value(0),0,7)),x+12,y+32,ConstellationStyle.GOLD,false);
  for(int i=0;i<8;i++){int xx=x+23+i*38;boolean done=(menu.value(1)&1<<i)!=0;int color=done?ConstellationStyle.CYAN:i==menu.value(0)?ConstellationStyle.GOLD:ConstellationStyle.LOCKED;if(i<7)ConstellationStyle.segment(g,xx+8,y+66,xx+30,y+66,ConstellationStyle.DIM);ConstellationStyle.plate(g,xx-6,y+59,14,14,color,0xff10192d);g.text(font,Integer.toString(i+1),xx-2,y+62,color,false);}
  g.text(font,Component.translatable("depths.tnd.progress",menu.value(3),DepthsLayout.target(Math.clamp(menu.value(0),0,7))),x+12,y+87,ConstellationStyle.TEXT,false);int yy=y+104;for(var line:font.split(Component.translatable("depths.tnd.brief"),290)){g.text(font,line,x+12,yy,ConstellationStyle.MUTED,false);yy+=10;}
 }
}
