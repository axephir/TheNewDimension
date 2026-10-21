package fr.axephir.tnd.client;

import fr.axephir.tnd.technology.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Exact FE counts, honest blocked states, output buffers and an explicit quarry arming control. */
public final class IndustrialScreen extends AbstractContainerScreen<IndustrialMenu> {
    private final PanelMotion motion=new PanelMotion();private Button toggle,help;
    public IndustrialScreen(IndustrialMenu m,Inventory inv,Component title){super(m,inv,title,176,238);}
    @Override protected void init(){
        super.init();help=Button.builder(Component.literal("?"),b->minecraft.gui.setScreen(new IndustrialGuideScreen(this,menu.kind()))).bounds(leftPos+150,topPos+5,18,18).build();addRenderableWidget(help);
        toggle=Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+8,topPos+127,160,20).build();addRenderableWidget(toggle);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override protected void containerTick(){super.containerTick();toggle.visible=menu.kind()==Industry.Kind.QUARRY;toggle.setMessage(Component.translatable(menu.armed()?"industry.tnd.stop":"industry.tnd.arm"));}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;StonePanel.frame(g,x,y,imageWidth,imageHeight);
        g.text(font,Component.translatable("block.tnd."+menu.kind().id),x+8,y+9,0xff29382e,false);
        g.fill(x+9,y+27,x+167,y+35,0xff1b2d28);g.fill(x+10,y+28,x+10+motion.value(0,menu.energy()*1000/menu.kind().capacity)*156/1000,y+34,0xffd7a85c);
        StonePanel.inset(g,x+8,y+40,160,67);
        if(menu.inputVisible(0))StonePanel.slot(g,x+23,y+57);
        if(menu.inputVisible(1))StonePanel.slot(g,x+23,y+86);
        if(menu.outputsVisible())for(int row=0;row<3;row++)for(int col=0;col<3;col++)StonePanel.slot(g,x+111+col*18,y+48+row*18);
        g.text(font,Component.translatable(menu.inputVisible(0)?"industry.tnd.input":"industry.tnd.area."+menu.kind().id),x+12,y+44,0xffd7ddc8,false);
        if(!menu.kind().source()){g.fill(x+51,y+68,x+100,y+76,0xff192c28);g.fill(x+52,y+69,x+52+motion.value(1,menu.progress())*47/1000,y+75,0xff83b6aa);}
        if(!menu.outputsVisible()){
            var lines=font.split(Component.translatable("industry.tnd.detail."+menu.kind().id),103);
            for(int i=0;i<Math.min(4,lines.size());i++)g.text(font,lines.get(i),x+55,y+(menu.inputVisible(0)?48:61)+i*10,0xffd7ddc8,false);
        }
        g.text(font,menu.energy()+" / "+menu.kind().capacity+" FE",x+9,y+109,0xff364b40,false);
        var lines=font.split(Component.translatable("industry.tnd.state."+menu.status()),158);for(int i=0;i<(menu.kind()==Industry.Kind.QUARRY?0:Math.min(2,lines.size()));i++)g.text(font,lines.get(i),x+9,y+137+i*9,0xff394b3d,false);
        if(menu.kind()==Industry.Kind.QUARRY){
            g.text(font,menu.drill()>0?Component.translatable("industry.tnd.bit",menu.drill()):Component.translatable("industry.tnd.bit_wait"),x+13,y+83,0xffd7ddc8,false);
            g.text(font,Component.translatable("industry.tnd.depth",menu.depth()),x+13,y+95,0xffd7ddc8,false);
        }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)StonePanel.slot(g,x+8+col*18,y+156+row*18);
        for(int col=0;col<9;col++)StonePanel.slot(g,x+8+col*18,y+214);
        // Keep quarry status above its button, with its exact arming state on the button itself.
        if(menu.kind()==Industry.Kind.QUARRY)g.text(font,Component.translatable("industry.tnd.state."+menu.status()),x+9,y+119,0xff394b3d,false);
    }
}
