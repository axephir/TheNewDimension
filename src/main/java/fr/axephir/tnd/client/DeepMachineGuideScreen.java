package fr.axephir.tnd.client;
import fr.axephir.tnd.deepindustry.DeepIndustry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** A separate construction page keeps live inventory items clear of the instructions. */
public final class DeepMachineGuideScreen extends Screen{
 private final Screen parent;private DeepIndustry.Kind kind;private int x,y;
 public DeepMachineGuideScreen(Screen parent,DeepIndustry.Kind kind){super(Component.translatable("industry.tnd.guide"));this.parent=parent;this.kind=kind;}
 protected void init(){x=(width-332)/2;y=(height-232)/2;for(var k:DeepIndustry.Kind.values()){var b=AstralButton.create(Component.translatable("block.tnd."+k.id),v->kind=k).bounds(x+8,y+31+k.ordinal()*19,121,18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("block.tnd."+k.id))).build();b.setOverrideRenderHighlightedSprite(()->kind==k);addRenderableWidget(b);}addRenderableWidget(AstralButton.create(Component.literal("×"),b->onClose()).bounds(x+305,y+6,18,18).build());}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float t){AstralPanel.frame(g,x,y,332,232);g.text(font,title,x+10,y+11,ConstellationStyle.TEXT,false);AstralPanel.inset(g,x+136,y+30,188,194);g.item(DeepIndustry.MACHINES.get(kind).get().asItem().getDefaultInstance(),x+144,y+37);AstralPanel.heading(g,font,Component.translatable("block.tnd."+kind.id),x+166,y+40,149,mx,my);int yy=y+61;for(var line:font.split(Component.translatable("deep.tnd.guide."+kind.id),169)){if(yy>y+213)break;g.text(font,line,x+144,yy,ConstellationStyle.TEXT,false);yy+=10;}
  g.text(font,Component.translatable("deep.tnd.foundation"),x+10,y+173,ConstellationStyle.MUTED,false);int n=kind==DeepIndustry.Kind.BORE?7:3,cell=5;for(int a=0;a<n;a++)for(int b=0;b<n;b++){int color=a==n/2&&b==n/2?ConstellationStyle.CYAN:a==0||b==0||a==n-1||b==n-1?ConstellationStyle.GOLD:0xff27334a;g.fill(x+48+a*cell,y+188+b*cell,x+52+a*cell,y+192+b*cell,color);}g.text(font,n+" × "+n,x+10,y+197,ConstellationStyle.TEXT,false);super.extractRenderState(g,mx,my,t);}
 public void onClose(){minecraft.gui.setScreen(parent);}public boolean isPauseScreen(){return false;}
}
