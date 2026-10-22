package fr.axephir.tnd.client;
import fr.axephir.tnd.core.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Paginated practical help. Every sentence remains reachable at every GUI scale. */
public final class FieldGuideScreen extends Screen {
    private final Screen parent;private int page,row,textPage,x,y;private Button previous,next;private final Button[] entries=new Button[3];
    public FieldGuideScreen(Screen parent){super(Component.translatable("gui.tnd.guide"));this.parent=parent;}
    @Override protected void init(){x=(width-300)/2;y=(height-216)/2;
        for(int i=0;i<6;i++){final int p=i;var b=Button.builder(Component.translatable("guide.tnd.tab."+i),v->{page=p;row=0;textPage=0;update();}).bounds(x+9,y+31+i*28,83,24).build();b.setOverrideRenderHighlightedSprite(()->page==p);addRenderableWidget(b);}
        addRenderableWidget(Button.builder(Component.translatable("gate.tnd.guide"),b->minecraft.gui.setScreen(new GateGuideScreen(this))).bounds(x+99,y+5,50,20).build());
        addRenderableWidget(Button.builder(Component.translatable("industry.tnd.open"),b->minecraft.gui.setScreen(new IndustrialGuideScreen(this,fr.axephir.tnd.technology.Industry.Kind.DYNAMO))).bounds(x+155,y+5,110,20).build());
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(x+274,y+6,17,17).build());
        for(int i=0;i<3;i++){final int r=i;entries[i]=addRenderableWidget(Button.builder(Component.empty(),b->{row=r;textPage=0;update();}).bounds(x+110+i*57,y+51,51,22).build());entries[i].setOverrideRenderHighlightedSprite(()->row==r);}
        previous=addRenderableWidget(Button.builder(Component.literal("‹"),b->{textPage--;update();}).bounds(x+108,y+182,23,18).build());next=addRenderableWidget(Button.builder(Component.literal("›"),b->{textPage++;update();}).bounds(x+260,y+182,23,18).build());update();
    }
    private java.util.List<net.minecraft.util.FormattedCharSequence> lines(){return font.split(Component.translatable("guide.tnd.page."+page+"."+row),171);}
    private void update(){int count=Math.max(1,(lines().size()+8)/9);textPage=Math.clamp(textPage,0,count-1);previous.active=textPage>0;next.active=textPage+1<count;for(int i=0;i<3;i++)entries[i].setTooltip(Tooltip.create(icon(i).getHoverName()));}
    private ItemStack icon(int r){return switch(page){
        case 0->new ItemStack(r==0?ModContent.COMPOSITE_CRYSTAL.get():r==1?ModContent.STAR_CHART.get().asItem():ModContent.RECALL_PRISM.get());
        case 1->new ItemStack(r==0?AwakeningContent.LITHIC_HEART.get():r==1?AwakeningContent.GLASS_FILAMENT.get():AwakeningContent.HAMMER.get());
        case 2->new ItemStack(r==0?AwakeningContent.RAW_AURALITE.get():r==1?AwakeningContent.RAW_NOCTILITE.get():AwakeningContent.UMBRA_ALLOY.get());
        case 3->new ItemStack(r==0?AwakeningContent.SEPARATOR.get().asItem():r==1?fr.axephir.tnd.technology.MachineRegistry.FORGE.get().asItem():AwakeningContent.REAPER.get().asItem());
        case 4->new ItemStack(r==0?ModContent.SURVEY_TERMINAL.get().asItem():r==1?ModContent.RELIC_FRAGMENT.get():ModContent.CARTOGRAPHER_TROPHY.get().asItem());
        default->new ItemStack(r==0?ModContent.RECALL_PRISM.get():r==1?ModContent.RETURN_ANCHOR.get().asItem():ModContent.ECHO_DUST.get());};}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){StonePanel.frame(g,x,y,300,216);g.text(font,title,x+11,y+11,0xff29382e,false);StonePanel.inset(g,x+98,y+29,193,176);g.text(font,Component.translatable("guide.tnd.tab."+page),x+107,y+36,0xffe9d79f,false);
        var lines=lines();int first=textPage*9;for(int i=first;i<Math.min(lines.size(),first+9);i++)g.text(font,lines.get(i),x+108,y+83+(i-first)*10,0xffd7ddc8,false);g.text(font,(textPage+1)+" / "+Math.max(1,(lines.size()+8)/9),x+180,y+187,0xffc8d8c3,false);super.extractRenderState(g,mx,my,partial);g.nextStratum();for(int i=0;i<3;i++)g.item(icon(i),x+127+i*57,y+54);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
