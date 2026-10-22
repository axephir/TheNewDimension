package fr.axephir.tnd.client;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

/** Foundations, specializations and cosmetic milestones have separate, keyboard-accessible pages. */
public final class MasteryScreen extends AbstractContainerScreen<MasteryMenu> {
    private final PanelMotion motion=new PanelMotion();
    private Mastery selected=Mastery.MINING;private int page;
    private Button sense,ability,clear;
    private final Map<Talent,Button> talents=new EnumMap<>(Talent.class);
    private final Map<Mastery,Button> categories=new EnumMap<>(Mastery.class);
    private final Button[] pages=new Button[3],milestones=new Button[4];
    public MasteryScreen(MasteryMenu menu,Inventory inventory,Component title){super(menu,inventory,title,360,232);inventoryLabelY=-10000;}
    private void send(int action){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
    @Override protected void init(){super.init();talents.clear();categories.clear();
        for(var m:Mastery.values()){var b=Button.builder(Component.translatable(m==Mastery.ANCIENT_TECH?"gui.tnd.ancient_short":"mastery.tnd."+m.id()),v->{selected=m;updateButtons();}).bounds(leftPos+8,topPos+30+m.ordinal()*12,105,12).build();b.setOverrideRenderHighlightedSprite(()->selected==m);categories.put(m,b);addRenderableWidget(b);}
        for(int i=0;i<3;i++){final int tab=i;pages[i]=addRenderableWidget(Button.builder(Component.translatable("mastery.tnd.tab."+i),b->{page=tab;updateButtons();}).bounds(leftPos+122+i*77,topPos+78,74,18).build());pages[i].setOverrideRenderHighlightedSprite(()->page==tab);}
        for(var t:Talent.values()){var b=Button.builder(Component.empty(),v->send(selected.ordinal()*Talent.values().length+t.ordinal())).bounds(leftPos+131,topPos+104,211,18).build();talents.put(t,b);addRenderableWidget(b);}
        for(int i=0;i<4;i++){final int tier=i;milestones[i]=addRenderableWidget(Button.builder(Component.empty(),b->send(3000+selected.ordinal()*4+tier)).bounds(leftPos+131,topPos+101+i*23,211,20).build());}
        ability=addRenderableWidget(Button.builder(Component.translatable("ability.tnd.activate"),b->send(2000+selected.ordinal())).bounds(leftPos+131,topPos+195,211,20).build());
        clear=addRenderableWidget(Button.builder(Component.translatable("prestige.tnd.hide"),b->send(3100)).bounds(leftPos+131,topPos+195,211,20).build());
        sense=addRenderableWidget(Button.builder(Component.translatable("gui.tnd.sense"),b->{send(1000+selected.ordinal());onClose();}).bounds(leftPos+279,topPos+32,65,16).build());
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(leftPos+335,topPos+7,17,17).build());updateButtons();
    }
    private MasteryProgress progress(Mastery m){return minecraft!=null&&minecraft.player!=null?minecraft.player.getData(PlayerProgression.PROFILE).get(m):MasteryProgress.empty();}
    private void updateButtons(){var p=progress(selected);sense.active=p.rank(Talent.INSIGHT)>0;sense.setTooltip(Tooltip.create(Component.translatable("gui.tnd.sense_help",MasteryRules.radius(p))));
        categories.forEach((m,b)->b.setTooltip(Tooltip.create(Component.translatable("gui.tnd.mastery_summary",Component.translatable("mastery.tnd."+m.id()),progress(m).level(),progress(m).experience(),progress(m).points()))));
        var available=Talent.available(selected);
        talents.forEach((t,b)->{int index=available.indexOf(t);b.visible=index>=0&&(page==0&&!t.specialized()||page==1&&t.specialized());if(!b.visible)return;
            int row=t.specialized()?index-5:index;b.setY(topPos+(t.specialized()?105+row*43:102+row*22));
            b.setMessage(t.specialized()?Component.literal(p.rank(t)>0?"◆ ":"○ ").append(Component.translatable("talent.tnd."+t.id())):Component.translatable("gui.tnd.talent_rank",Component.translatable("talent.tnd."+t.id()),p.rank(t),t.maxRank(),t.cost()));
            var tip=Component.translatable(t.specialized()?"talent.tnd."+t.id()+".description":"talent.tnd."+selected.id()+"."+t.id()+".description",Component.translatable("mastery.tnd."+MasteryRules.partner(selected).id())).copy();
            tip.append("\n").append(Component.translatable("gui.tnd.talent_cost",t.cost()));if(t.minimumLevel()>1)tip.append("\n").append(Component.translatable("mastery.tnd.required_level",t.minimumLevel()));if(t.prerequisite()!=null)tip.append("\n").append(Component.translatable("gui.tnd.prerequisite",Component.translatable("talent.tnd."+t.prerequisite().id())));b.setTooltip(Tooltip.create(tip));
            b.active=p.level()>=t.minimumLevel()&&p.points()>=t.cost()&&p.rank(t)<t.maxRank()&&(t.prerequisite()==null||p.rank(t.prerequisite())>0);
        });
        long now=minecraft.player==null?0:minecraft.level.getGameTime(),remaining=minecraft.player==null?0:Math.max(0,minecraft.player.getData(MasteryAbilities.NEXT)-now);
        ability.visible=page==1;ability.active=p.rank(Talent.ability(selected))>0&&remaining==0&&!minecraft.player.isCreative()&&!minecraft.player.isSpectator();ability.setMessage(Component.translatable(remaining>0?"ability.tnd.cooldown":"ability.tnd.activate",(remaining+19)/20));ability.setTooltip(Tooltip.create(Component.translatable("ability.tnd.help")));
        int equipped=minecraft.player==null?-1:minecraft.player.getData(Prestige.SELECTION);
        for(int i=0;i<4;i++){milestones[i].visible=page==2;milestones[i].active=p.level()>=Prestige.LEVELS[i];milestones[i].setMessage(Component.translatable("prestige.tnd.milestone",Component.translatable("prestige.tnd.rank."+i),Prestige.LEVELS[i],equipped==selected.ordinal()*4+i?"◆":""));milestones[i].setTooltip(Tooltip.create(Component.translatable("prestige.tnd.help")));}
        clear.visible=page==2;clear.active=equipped>=0;
    }
    @Override protected void containerTick(){super.containerTick();updateButtons();}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;StonePanel.frame(g,x,y,imageWidth,imageHeight);g.text(font,title,x+12,y+12,0xff29362c,false);StonePanel.inset(g,x+6,y+27,109,185);StonePanel.inset(g,x+120,y+27,232,48);
        var p=progress(selected);g.text(font,Component.translatable("mastery.tnd."+selected.id()),x+128,y+33,0xfff0e4b8,false);g.text(font,Component.translatable("gui.tnd.level",p.level()),x+128,y+46,0xffdce5cd,false);g.text(font,Component.translatable("gui.tnd.points",p.points()),x+210,y+62,0xffedcd7c,false);
        long base=Experience.threshold(p.level()),next=Experience.threshold(p.level()+1);int fill=motion.value(selected.ordinal(),(int)Math.clamp((p.experience()-base)*1000/Math.max(1,next-base),0,1000))*212/1000;g.fill(x+128,y+57,x+340,y+60,0xff17231c);g.fill(x+128,y+57,x+128+fill,y+60,0xff9fcf89);
        long remaining=Math.max(0,next-p.experience());String xp=remaining<1_000_000?Long.toString(remaining):String.format(Locale.ROOT,"%.1fM",remaining/1e6);g.text(font,xp+" XP →",x+128,y+62,0xffa7b6a3,false);
        if(page==1){for(int i=0;i<2;i++){var t=Talent.available(selected).get(5+i);var lines=font.split(Component.translatable("talent.tnd."+t.id()+".summary"),207);for(int line=0;line<Math.min(2,lines.size());line++)g.text(font,lines.get(line),x+134,y+126+i*43+line*10,p.rank(t)>0?0xff386242:0xff555c4e,false);}}
        if(page==2)g.text(font,Component.translatable("prestige.tnd.cosmetic"),x+133,y+187,0xff455344,false);
        g.text(font,Component.translatable("activity.tnd."+selected.id()),x+10,y+219,0xff384132,false);
    }
}
