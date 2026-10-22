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
        var help=AstralButton.create(Component.literal("?"),b->{}).bounds(leftPos+149,topPos+4,18,18)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.tnd.machine.help."+menu.kind))).build();
        addRenderableWidget(help);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,imageWidth,imageHeight);
        AstralPanel.heading(g,font,title,x+9,y+7,133,mx,my);
        g.text(font,Component.translatable("gui.tnd.machine.subtitle."+menu.kind),x+9,y+21,ConstellationStyle.MUTED,false);
        AstralPanel.inset(g,x+8,y+35,160,76);
        g.text(font,Component.translatable(menu.kind==2?"gui.tnd.reaper.input":"gui.tnd.forge.input"),x+13,y+38,ConstellationStyle.TEXT,false);
        g.text(font,Component.translatable(menu.kind==2?"gui.tnd.reaper.fuel":"gui.tnd.forge.fuel"),x+13,y+76,ConstellationStyle.TEXT,false);
        g.text(font,Component.translatable("gui.tnd.extractor.output"),x+119,y+53,ConstellationStyle.TEXT,false);
        AstralPanel.slot(g,x+35,y+49);AstralPanel.slot(g,x+35,y+87);AstralPanel.slot(g,x+125,y+67);
        g.fill(x+64,y+71,x+111,y+79,0xff202c45);g.fill(x+65,y+72,x+65+motion.value(0,menu.progress())*44/1000,y+78,ConstellationStyle.CYAN);
        g.fill(x+108,y+68,x+110,y+82,ConstellationStyle.CYAN);g.fill(x+110,y+70,x+112,y+80,ConstellationStyle.CYAN);
        if(menu.kind==2)g.item(fr.axephir.tnd.core.ModContent.ECHO_BLOOM.get().asItem().getDefaultInstance(),x+35,y+49);
        var preview=menu.preview();if(!preview.isEmpty()){g.item(preview,x+82,y+47);g.text(font,"×"+preview.getCount(),x+96,y+53,ConstellationStyle.TEXT,false);}
        g.text(font,Component.translatable((menu.kind==2?"gui.tnd.reaper.state.":"gui.tnd.forge.state.")+menu.status()),x+9,y+114,ConstellationStyle.MUTED,false);
        for(int row=0;row<3;row++)for(int c=0;c<9;c++)AstralPanel.slot(g,x+8+c*18,y+129+row*18);
        for(int c=0;c<9;c++)AstralPanel.slot(g,x+8+c*18,y+187);
    }
}
