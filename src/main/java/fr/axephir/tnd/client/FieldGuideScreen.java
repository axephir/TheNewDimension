package fr.axephir.tnd.client;
import fr.axephir.tnd.core.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A client-only, localized quick reference. Available from the inventory without an item or permissions. */
public final class FieldGuideScreen extends Screen {
    private final Screen parent;
    private int page,x,y;
    public FieldGuideScreen(Screen parent){super(Component.translatable("gui.tnd.guide"));this.parent=parent;}
    @Override protected void init(){x=(width-300)/2;y=(height-216)/2;
        for(int i=0;i<6;i++){final int p=i;var b=Button.builder(Component.translatable("guide.tnd.tab."+i),v->page=p).bounds(x+9,y+31+i*28,83,24).build();b.setOverrideRenderHighlightedSprite(()->page==p);addRenderableWidget(b);}
        addRenderableWidget(Button.builder(Component.translatable("industry.tnd.open"),b->minecraft.gui.setScreen(new IndustrialGuideScreen(this,fr.axephir.tnd.technology.Industry.Kind.DYNAMO))).bounds(x+155,y+5,110,20).build());
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(x+274,y+6,17,17).build());
    }
    private ItemStack icon(int row){return switch(page){
        case 0->new ItemStack(row==0?ModContent.COMPOSITE_CRYSTAL.get():row==1?ModContent.STAR_CHART.get().asItem():ModContent.RECALL_PRISM.get());
        case 1->new ItemStack(row==0?AwakeningContent.LITHIC_HEART.get():row==1?AwakeningContent.GLASS_FILAMENT.get():AwakeningContent.HAMMER.get());
        case 2->new ItemStack(row==0?AwakeningContent.RAW_AURALITE.get():row==1?AwakeningContent.RAW_NOCTILITE.get():AwakeningContent.UMBRA_ALLOY.get());
        case 3->new ItemStack(row==0?AwakeningContent.SEPARATOR.get().asItem():row==1?fr.axephir.tnd.technology.MachineRegistry.FORGE.get().asItem():AwakeningContent.REAPER.get().asItem());
        case 4->new ItemStack(row==0?ModContent.SURVEY_TERMINAL.get().asItem():row==1?ModContent.RELIC_FRAGMENT.get():ModContent.CARTOGRAPHER_TROPHY.get().asItem());
        default->new ItemStack(row==0?ModContent.RECALL_PRISM.get():row==1?ModContent.RETURN_ANCHOR.get().asItem():ModContent.ECHO_DUST.get());
    };}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
        StonePanel.frame(g,x,y,300,216);g.text(font,title,x+11,y+11,0xff29382e,false);
        StonePanel.inset(g,x+98,y+29,193,176);
        g.text(font,Component.translatable("guide.tnd.tab."+page),x+107,y+36,0xffe9d79f,false);
        for(int row=0;row<3;row++){
            int yy=y+54+row*49;g.fill(x+104,yy,x+285,yy+46,0xff26382f);g.item(icon(row),x+108,yy+5);
            var lines=font.split(Component.translatable("guide.tnd.page."+page+"."+row),151);
            for(int i=0;i<Math.min(4,lines.size());i++)g.text(font,lines.get(i),x+128,yy+4+i*10,0xffd7ddc8,false);
        }super.extractRenderState(g,mx,my,partial);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
