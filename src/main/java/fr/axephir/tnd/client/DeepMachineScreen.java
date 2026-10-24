package fr.axephir.tnd.client;
import fr.axephir.tnd.deepindustry.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
/** Industrial service panel: visible inputs, exact recipes and a construction page kept outside the slots. */
public final class DeepMachineScreen extends AbstractContainerScreen<DeepMachineMenu>{
 private final PanelMotion motion=new PanelMotion();private Button control;
 public DeepMachineScreen(DeepMachineMenu m,Inventory i,Component c){super(m,i,c,256,238);}
 protected void init(){super.init();addRenderableWidget(AstralButton.create(Component.literal("?"),b->minecraft.gui.setScreen(new DeepMachineGuideScreen(this,menu.kind()))).bounds(leftPos+228,topPos+5,20,18).build());control=addRenderableWidget(AstralButton.create(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,DeepRecipes.recipes(menu.kind()).isEmpty()?0:1)).bounds(leftPos+8,topPos+128,240,20).build());}
 protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
 protected void containerTick(){super.containerTick();boolean drill=menu.kind()==DeepIndustry.Kind.BORE||menu.kind()==DeepIndustry.Kind.DREDGER;control.visible=drill||DeepRecipes.recipes(menu.kind()).size()>1;control.setMessage(Component.translatable(drill?(menu.value(6)==1?"industry.tnd.stop":"industry.tnd.arm"):"deep.tnd.next_recipe"));}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float t){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,256,238);AstralPanel.heading(g,font,Component.translatable("block.tnd."+menu.kind().id),x+8,y+9,215,mx,my);
  g.fill(x+9,y+27,x+247,y+34,0xff202c45);g.fill(x+10,y+28,x+10+motion.value(0,menu.energy()*1000/menu.kind().capacity)*236/1000,y+33,ConstellationStyle.GOLD);
  for(int i=0;i<3;i++)AstralPanel.slot(g,x+16+i*36,y+56);for(int i=0;i<5;i++)AstralPanel.slot(g,x+130+i*18,y+56);
  for(int i=0;i<3;i++)g.text(font,Component.translatable("deep.tnd.slot."+i),x+12+i*36,y+43,ConstellationStyle.MUTED,false);g.text(font,Component.translatable("deep.tnd.outputs"),x+130,y+43,ConstellationStyle.MUTED,false);
  int period=menu.kind()==DeepIndustry.Kind.DREDGER?60:menu.kind()==DeepIndustry.Kind.BORE?10:400;var recipes=DeepRecipes.recipes(menu.kind());if(!recipes.isEmpty())period=recipes.get(Math.floorMod(menu.value(7),recipes.size())).ticks();int cycle=menu.kind()==DeepIndustry.Kind.TURBINE?(menu.value(10)>0?400-menu.value(10):0):menu.value(3);g.fill(x+130,y+80,x+218,y+85,0xff202c45);g.fill(x+131,y+81,x+131+Math.clamp(cycle*86/period,0,86),y+84,ConstellationStyle.CYAN);g.fill(x+130,y+93,x+218,y+97,0xff202c45);g.fill(x+131,y+94,x+131+menu.value(5)*86/1000,y+96,menu.value(5)<100?0xffffaa78:ConstellationStyle.GOLD);
  g.text(font,menu.energy()+" / "+menu.kind().capacity+" FE",x+10,y+80,ConstellationStyle.MUTED,false);g.text(font,Component.translatable("deep.tnd.wear",menu.value(5)/10),x+10,y+92,menu.value(5)<100?0xffffaa78:ConstellationStyle.CYAN,false);
  String text="deep.tnd.state."+menu.value(4);int line=0;for(var s:font.split(Component.translatable(text),236)){if(line==2)break;g.text(font,s,x+10,y+105+line++*10,ConstellationStyle.TEXT,false);}
  if(!control.visible)g.text(font,Component.translatable("deep.tnd.redstone"),x+10,y+134,ConstellationStyle.DIM,false);
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)AstralPanel.slot(g,x+43+col*18,y+157+row*18);for(int col=0;col<9;col++)AstralPanel.slot(g,x+43+col*18,y+215);
  if(!DeepRecipes.recipes(menu.kind()).isEmpty()){var all=DeepRecipes.recipes(menu.kind());var r=all.get(Math.floorMod(menu.value(7),all.size()));g.item(new net.minecraft.world.item.ItemStack(r.result()),x+226,y+78);if(!menu.getSlot(0).hasItem())g.item(new net.minecraft.world.item.ItemStack(r.first()),x+16,y+56);if(!menu.getSlot(1).hasItem())g.item(new net.minecraft.world.item.ItemStack(r.second()),x+52,y+56);g.text(font,"×"+r.a(),x+34,y+60,ConstellationStyle.DIM,false);g.text(font,"×"+r.b(),x+70,y+60,ConstellationStyle.DIM,false);g.text(font,"×"+r.count(),x+226,y+96,ConstellationStyle.CYAN,false);}
 }
}
