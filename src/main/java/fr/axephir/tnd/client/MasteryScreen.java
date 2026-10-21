package fr.axephir.tnd.client;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

public final class MasteryScreen extends AbstractContainerScreen<MasteryMenu> {
    private final PanelMotion motion=new PanelMotion();
    private Mastery selected = Mastery.MINING;
    private Button sense;
    private final Map<Talent,Button> talents = new EnumMap<>(Talent.class);
    private final Map<Mastery,Button> categories = new EnumMap<>(Mastery.class);
    public MasteryScreen(MasteryMenu menu, Inventory inventory, Component title) {
        super(menu,inventory,title,316,232);
        inventoryLabelY=-10000; titleLabelX=12; titleLabelY=9;
    }
    @Override protected void init() {
        super.init(); talents.clear(); categories.clear();
        for(Mastery m:Mastery.values()) {
            Button b=Button.builder(Component.translatable(m==Mastery.ANCIENT_TECH?"gui.tnd.ancient_short":"mastery.tnd."+m.id()),v -> {selected=m;updateButtons();})
                    .bounds(leftPos+9,topPos+29+m.ordinal()*12,108,12).build();
            b.setOverrideRenderHighlightedSprite(()->selected==m);
            categories.put(m,b);addRenderableWidget(b);
        }
        for(Talent t:Talent.values()) {
            Button b=Button.builder(Component.empty(),v -> {
                if(minecraft!=null&&minecraft.gameMode!=null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,selected.ordinal()*Talent.values().length+t.ordinal());
            }).bounds(leftPos+137,topPos+102+t.ordinal()*23,169,20).build();
            talents.put(t,b);addRenderableWidget(b);
        }
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(leftPos+291,topPos+7,17,17).build());
        sense=addRenderableWidget(Button.builder(Component.translatable("gui.tnd.sense"),b->{
            if(minecraft!=null&&minecraft.gameMode!=null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+selected.ordinal());onClose();
            }
        }).bounds(leftPos+220,topPos+44,82,14).build());
        updateButtons();
    }
    private MasteryProgress progress(Mastery m) {
        return minecraft!=null&&minecraft.player!=null?minecraft.player.getData(PlayerProgression.PROFILE).get(m):MasteryProgress.empty();
    }
    private void updateButtons() {
        MasteryProgress p=progress(selected);
        if(sense!=null) {
            sense.active=p.rank(Talent.INSIGHT)>0;
            sense.setTooltip(Tooltip.create(Component.translatable("gui.tnd.sense_help",MasteryRules.radius(p))));
        }
        categories.forEach((m,b)->b.setTooltip(Tooltip.create(Component.translatable("gui.tnd.mastery_summary",
                Component.translatable("mastery.tnd."+m.id()),progress(m).level(),progress(m).experience(),progress(m).points()))));
        talents.forEach((t,b)->{
            String id=t.name().toLowerCase(Locale.ROOT);
            b.setMessage(Component.translatable("gui.tnd.talent_rank",Component.translatable("talent.tnd."+id),p.rank(t),t.maxRank(),t.cost()));
            Component description=Component.translatable("talent.tnd."+selected.id()+"."+id+".description",Component.translatable("mastery.tnd."+MasteryRules.partner(selected).id()));
            var tip=description.copy().append(Component.literal("\n")).append(Component.translatable("gui.tnd.talent_cost",t.cost()));
            if(t.prerequisite()!=null)tip.append(Component.literal("\n")).append(Component.translatable("gui.tnd.prerequisite",Component.translatable("talent.tnd."+t.prerequisite().name().toLowerCase(Locale.ROOT))));
            b.setTooltip(Tooltip.create(tip));
            b.active=p.points()>=t.cost()&&p.rank(t)<t.maxRank()&&(t.prerequisite()==null||p.rank(t.prerequisite())>0);
        });
    }
    @Override protected void containerTick(){super.containerTick();updateButtons();}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y) {} // Labels are positioned with their panels.
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
        int x=leftPos,y=topPos;
        StonePanel.frame(g,x,y,imageWidth,imageHeight);
        g.text(font,title,x+12,y+12,0xff29362c,false);
        StonePanel.inset(g,x+7,y+26,112,185);
        StonePanel.inset(g,x+125,y+26,183,69);
        MasteryProgress p=progress(selected);
        g.text(font,Component.translatable("mastery.tnd."+selected.id()),x+132,y+32,0xfff0e4b8,false);
        g.text(font,Component.translatable("gui.tnd.level",p.level()),x+132,y+46,0xffdce5cd,false);
        long base=Experience.threshold(p.level()),next=Experience.threshold(p.level()+1);
        int fill=(int)Math.clamp((p.experience()-base)*169L/Math.max(1,next-base),0,169);
        fill=motion.value(selected.ordinal(),fill*1000/169)*169/1000;
        g.fill(x+132,y+59,x+301,y+66,0xff17231c);
        g.fill(x+132,y+59,x+132+fill,y+66,0xff78a95d);
        if(fill>0)g.fill(x+132,y+59,x+132+fill,y+60,0xffb1d882);
        g.text(font,Component.translatable("gui.tnd.experience",p.experience(),next),x+132,y+70,0xffccd9c3,false);
        g.text(font,Component.translatable("gui.tnd.points",p.points()),x+132,y+83,0xffedcd7c,false);
        // Root-to-branch connectors are lit as prerequisites are learned.
        for(Talent t:Talent.values()) {
            int ty=y+112+t.ordinal()*23;
            int color=p.rank(t)>0?0xff749c59:0xff626d57;
            g.fill(x+127,ty,x+135,ty+2,color);
            if(t.ordinal()>0)g.fill(x+127,y+112,x+129,ty,color);
            for(int i=0;i<t.maxRank();i++)g.fill(x+143+i*7,ty+11,x+148+i*7,ty+13,i<p.rank(t)?0xff668b4d:0xff777d6d);
        }
        g.text(font,Component.translatable("activity.tnd."+selected.id()),x+10,y+217,0xff384132,false);
    }
}
