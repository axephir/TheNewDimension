package fr.axephir.tnd.client;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.expedition.*;
import fr.axephir.tnd.progression.PlayerProgression;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

public final class ExpeditionScreen extends AbstractContainerScreen<ExpeditionMenu> {
    private int page;
    private final PanelMotion motion=new PanelMotion();
    private final List<Button> claims=new ArrayList<>();
    public ExpeditionScreen(ExpeditionMenu m,Inventory i,Component title){super(m,i,title,300,216);}
    private ExpeditionProgress progress(){return minecraft!=null&&minecraft.player!=null?minecraft.player.getData(PlayerProgression.EXPEDITIONS):ExpeditionProgress.empty();}
    @Override protected void init(){super.init();claims.clear();
        for(int row=0;row<4;row++){final int r=row;claims.add(addRenderableWidget(Button.builder(Component.empty(),b->{
            if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,page*4+r);
        }).bounds(leftPos+219,topPos+40+row*38,71,17).build()));}
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page=0;update();}).bounds(leftPos+9,topPos+193,25,16).build());
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page=1;update();}).bounds(leftPos+266,topPos+193,25,16).build());
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(leftPos+274,topPos+6,17,17).build());update();
    }
    private void update(){var s=progress();for(int row=0;row<claims.size();row++){
        var e=Expedition.values()[page*4+row];var b=claims.get(row);b.active=s.complete(e)&&!s.claimed(e);
        b.setMessage(Component.translatable(s.claimed(e)?"gui.tnd.expedition.claimed":s.complete(e)?"gui.tnd.expedition.claim":"gui.tnd.expedition.pending"));
        var tip=Component.translatable("expedition.tnd."+e.id()+".description");
        if(e==Expedition.SURVEY||e==Expedition.ATLAS)for(int i=0;i<ExpeditionService.BIOMES.size();i++)tip.append("\n").append(Component.literal((s.biomes()&(1<<i))!=0?"✓ ":"· ")).append(Component.translatable("biome.tnd."+ExpeditionService.BIOMES.get(i)));
        tip.append("\n\n").append(Component.translatable("gui.tnd.expedition.rewards"));
        for(var reward:ExpeditionService.rewards(e))tip.append("\n").append(Component.literal(reward.getCount()+" × ")).append(reward.getHoverName());
        tip.append("\n").append(Component.literal(ExpeditionService.xp(e)+" XP · ")).append(Component.translatable("mastery.tnd."+ExpeditionService.mastery(e).id()));b.setTooltip(Tooltip.create(tip));
    }}
    @Override protected void containerTick(){super.containerTick();update();}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;var s=progress();StonePanel.frame(g,x,y,imageWidth,imageHeight);
        g.text(font,title,x+10,y+10,0xff29382e,false);
        g.text(font,Component.translatable("gui.tnd.expedition.summary",Integer.bitCount(s.claimed())),x+10,y+24,0xff455442,false);
        for(int row=0;row<4;row++){
            var e=Expedition.values()[page*4+row];int yy=y+37+row*38;StonePanel.inset(g,x+8,yy,284,35);
            g.text(font,Component.translatable("expedition.tnd."+e.id()),x+14,yy+4,s.claimed(e)?0xffa7c7a0:0xffe9dbaf,false);
            g.text(font,Component.translatable("expedition.tnd."+e.id()+".short"),x+14,yy+15,0xffc8d4c0,false);
            g.fill(x+14,yy+27,x+202,yy+30,0xff172b22);g.fill(x+14,yy+27,x+14+motion.value(e.ordinal(),s.progress(e)*1000/e.goal)*188/1000,yy+30,0xff82b49a);
            var count=Component.literal(s.progress(e)+" / "+e.goal);g.text(font,count,x+254-font.width(count)/2,yy+23,0xffd2dfc7,false);
        }
        g.text(font,Component.translatable("gui.tnd.expedition.page",page+1),x+120,y+197,0xff394d3e,false);
    }
}
