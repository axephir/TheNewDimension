package fr.axephir.tnd.client;

import fr.axephir.tnd.technology.ResonanceForgeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ResonanceForgeScreen extends AbstractContainerScreen<ResonanceForgeMenu> {
    private final PanelMotion motion=new PanelMotion();
    public ResonanceForgeScreen(ResonanceForgeMenu m,Inventory inv,Component title){super(m,inv,title,176,211);}
    @Override protected void init(){
        super.init();
        var help=net.minecraft.client.gui.components.Button.builder(Component.literal("?"),b->{}).bounds(leftPos+149,topPos+4,18,18)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.tnd.machine.help."+menu.kind))).build();
        addRenderableWidget(help);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;StonePanel.frame(g,x,y,imageWidth,imageHeight);
        g.text(font,title,x+9,y+7,0xff30382c,false);
        g.text(font,Component.translatable("gui.tnd.machine.subtitle."+menu.kind),x+9,y+21,0xff495844,false);
        StonePanel.inset(g,x+8,y+35,160,76);
        g.text(font,Component.translatable(menu.kind==2?"gui.tnd.reaper.input":"gui.tnd.forge.input"),x+13,y+38,0xffc8d6b9,false);
        g.text(font,Component.translatable(menu.kind==2?"gui.tnd.reaper.fuel":"gui.tnd.forge.fuel"),x+13,y+76,0xffc8d6b9,false);
        g.text(font,Component.translatable("gui.tnd.extractor.output"),x+119,y+53,0xffc8d6b9,false);
        StonePanel.slot(g,x+35,y+49);StonePanel.slot(g,x+35,y+87);StonePanel.slot(g,x+125,y+67);
        g.fill(x+64,y+71,x+111,y+79,0xff182a26);g.fill(x+65,y+72,x+65+motion.value(0,menu.progress())*44/1000,y+78,0xff81b6aa);
        g.fill(x+108,y+68,x+110,y+82,0xffb9c7ad);g.fill(x+110,y+70,x+112,y+80,0xffb9c7ad);
        if(menu.kind==2)g.item(fr.axephir.tnd.core.ModContent.ECHO_BLOOM.get().asItem().getDefaultInstance(),x+35,y+49);
        var preview=menu.preview();if(!preview.isEmpty()){g.item(preview,x+82,y+47);g.text(font,"×"+preview.getCount(),x+96,y+53,0xffdae3ce,false);}
        g.text(font,Component.translatable((menu.kind==2?"gui.tnd.reaper.state.":"gui.tnd.forge.state.")+menu.status()),x+9,y+114,0xff3c4c3b,false);
        for(int row=0;row<3;row++)for(int c=0;c<9;c++)StonePanel.slot(g,x+8+c*18,y+129+row*18);
        for(int c=0;c<9;c++)StonePanel.slot(g,x+8+c*18,y+187);
    }
}
