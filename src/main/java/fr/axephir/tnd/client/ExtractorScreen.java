package fr.axephir.tnd.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import fr.axephir.tnd.technology.*;
import java.util.*;

public final class ExtractorScreen extends AbstractContainerScreen<ExtractorMenu> {
    private final PanelMotion motion=new PanelMotion();
    private final Map<ExtractionMode,Button> modes=new EnumMap<>(ExtractionMode.class);
    public ExtractorScreen(ExtractorMenu menu,Inventory inventory,Component title){super(menu,inventory,title,176,193);}
    @Override protected void init() {
        super.init();modes.clear();
        for(var mode:ExtractionMode.values()) {
            var b=new Button(leftPos+7+mode.ordinal()*33,topPos+22,32,18,Component.translatable("mode.tnd."+mode.id()),v->{
                if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,mode.ordinal());
            },n->n.get()) {
                @Override protected void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
                    StonePanel.frame(g,getX(),getY(),getWidth(),getHeight());
                    int color=menu.mode()==mode?0xff6e9758:isHoveredOrFocused()?0xffd8d3a3:active?0xff929b83:0xff4b5347;
                    g.fill(getX()+3,getY()+3,getX()+29,getY()+15,color);
                    g.item(new net.minecraft.world.item.ItemStack(mode.output),getX()+8,getY()+1);
                }
            };
            b.setTooltip(Tooltip.create(Component.translatable("mode.tnd."+mode.id()).append("\n").append(Component.translatable(mode==ExtractionMode.DIAMOND?"gui.tnd.extractor.diamond_help":"gui.tnd.extractor.mode_help",mode.timeFactor))));
            modes.put(mode,b);addRenderableWidget(b);
        }
    }
    @Override protected void containerTick(){super.containerTick();modes.forEach((m,b)->b.active=(m==menu.mode()||menu.outputEmpty())&&(m!=ExtractionMode.DIAMOND||menu.hasStabilizer()));}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y) {}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        int x=leftPos,y=topPos;
        StonePanel.frame(g,x,y,imageWidth,imageHeight);
        g.text(font,title,x+9,y+7,0xff30382c,false);
        StonePanel.inset(g,x+8,y+44,160,56);
        label(g,Component.translatable("gui.tnd.extractor.fuel"),x+35,y+49);
        label(g,Component.translatable("gui.tnd.extractor.module"),x+79,y+49);
        label(g,Component.translatable("gui.tnd.extractor.output"),x+133,y+49);
        for(int sx:new int[]{27,71,125})StonePanel.slot(g,x+sx,y+71);
        g.fill(x+96,y+74,x+118,y+83,0xff19271e);
        g.fill(x+96,y+75,x+96+motion.value(0,menu.progress())*22/1000,y+82,0xff9bb665);
        g.fill(x+114,y+72,x+116,y+85,0xffa8af95);
        g.fill(x+116,y+74,x+118,y+83,0xffa8af95);
        g.text(font,Component.translatable("gui.tnd.extractor.period",menu.periodSeconds()),x+13,y+61,0xffc6d0b8,false);
        g.text(font,Component.translatable("gui.tnd.extractor.relic",menu.relicCalibration()),x+13,y+90,0xffc6d0b8,false);
        g.text(font,Component.translatable("container.inventory"),x+8,y+102,0xff394232,false);
        for(int row=0;row<3;row++)for(int c=0;c<9;c++)StonePanel.slot(g,x+8+c*18,y+111+row*18);
        for(int c=0;c<9;c++)StonePanel.slot(g,x+8+c*18,y+169);
    }
    private void label(GuiGraphicsExtractor g,Component text,int center,int y){g.text(font,text,center-font.width(text)/2,y,0xffd2dbc3,false);}
}
