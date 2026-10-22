package fr.axephir.tnd.client;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** A readable advancement-style atlas. Selecting a node never spends points. */
public final class MasteryScreen extends AbstractContainerScreen<MasteryMenu> {
    private static Mastery remembered=Mastery.MINING;
    private static final Map<Mastery,Talent> SELECTIONS=new EnumMap<>(Mastery.class);
    private static final int[] NX={32,124,124,220,220,316,412},NY={72,28,116,100,28,144,144};
    private final PanelMotion motion=new PanelMotion();
    private final List<Node> nodes=new ArrayList<>();
    private final Map<Mastery,Button> categories=new EnumMap<>(Mastery.class);
    private final int[] observed=new int[Mastery.values().length*Talent.values().length];
    private boolean observedReady,distinctions,wide,panning;
    private Mastery selected=remembered;private Talent talent=SELECTIONS.getOrDefault(selected,Talent.EFFICIENCY);
    private int panelWidth,panelHeight;
    private int tier,plotX,plotY,plotW,plotH,detailX,detailW,detailScroll,detailMax;
    private double zoom=1,panX,panY;private long flashAt,pendingUntil;private Talent flashTalent;
    private Mastery flashMastery;private int pending=-1;
    private Button primary,utility,info,motionButton,treeTab,titleTab;

    public MasteryScreen(MasteryMenu menu,Inventory inventory,Component title){super(menu,inventory,title,400,240);inventoryLabelY=-10000;}
    private Component tr(String key,Object... args){return Component.translatable("tree.tnd."+key,args);}
    private MasteryProgress progress(Mastery m){return minecraft!=null&&minecraft.player!=null?minecraft.player.getData(PlayerProgression.PROFILE).get(m):MasteryProgress.empty();}
    private MasteryProgress progress(){return progress(selected);}
    private void send(int action){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
    private void sound(boolean learned){minecraft.getSoundManager().play(SimpleSoundInstance.forUI(learned?ModContent.atmosphereSound("spatial_chime"):SoundEvents.UI_BUTTON_CLICK.value(),learned?1.25f:1.3f,learned?.28f:.18f));}

    @Override protected void init(){
        super.init();panelWidth=Math.min(1100,width-12);panelHeight=Math.min(720,height-12);leftPos=(width-panelWidth)/2;topPos=(height-panelHeight)/2;
        nodes.clear();categories.clear();detailScroll=0;
        wide=panelWidth>=550&&panelHeight>=288;detailW=wide?Math.clamp(panelWidth/3,190,260):0;
        plotX=leftPos+10;plotY=topPos+84;plotW=panelWidth-20-(wide?detailW+8:0);plotH=panelHeight-84-(wide?23:47);detailX=plotX+plotW+8;
        treeTab=button(tr("talents"),leftPos+panelWidth-153,topPos+7,55,18,()->page(false));
        titleTab=button(tr("titles"),leftPos+panelWidth-94,topPos+7,55,18,()->page(true));
        motionButton=button(Component.literal("~"),leftPos+panelWidth-35,topPos+7,14,18,()->{AtmosphereOptions.ANIMATIONS.set(!AtmosphereOptions.ANIMATIONS.get());AtmosphereOptions.SPEC.save();update();});
        button(Component.literal("×"),leftPos+panelWidth-19,topPos+7,14,18,this::onClose).setTooltip(Tooltip.create(tr("close")));
        int tabW=Math.min(32,(panelWidth-20)/Mastery.values().length),start=leftPos+(panelWidth-tabW*Mastery.values().length)/2;
        for(var m:Mastery.values()){
            var b=new AtlasButton(start+m.ordinal()*tabW,topPos+30,tabW-2,24,Component.translatable("mastery.tnd."+m.id()),()->category(m)){
                @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float a){
                    boolean on=selected==m;ConstellationStyle.plate(g,getX(),getY(),getWidth(),getHeight(),on?ConstellationStyle.CYAN:isHoveredOrFocused()?0xff677c9f:0xff26334d,on?0xff213a47:0xff131e31);
                    g.item(ConstellationStyle.mastery(m),getX()+(getWidth()-16)/2,getY()+4);
                    if(progress(m).points()>0)g.fill(getX()+getWidth()-4,getY()+2,getX()+getWidth()-2,getY()+4,ConstellationStyle.GOLD);
                }
            };categories.put(m,addRenderableWidget(b));
        }
        if(distinctions){for(int i=0;i<4;i++)nodes.add(addRenderableWidget(new Node(null,i,64+i*108,92)));}
        else{var available=Talent.available(selected);for(int i=0;i<available.size();i++)nodes.add(addRenderableWidget(new Node(available.get(i),-1,NX[i],NY[i])));}
        int actionY=wide?plotY+plotH-26:plotY+plotH+6;
        int actionX=wide?detailX+10:leftPos+panelWidth-128;
        primary=button(Component.empty(),actionX,actionY,wide?detailW-20:118,20,this::primary);
        utility=button(Component.empty(),wide?actionX:actionX-82,wide?actionY-24:actionY,wide?detailW-20:78,20,this::utility);
        info=button(Component.empty(),plotX,actionY,Math.max(40,panelWidth-224),20,()->focusSelected());info.visible=!wide;
        int footer=topPos+panelHeight-18;
        button(Component.literal("−"),leftPos+panelWidth-70,footer,18,13,()->zoom(zoom/1.15,plotX+plotW/2.0,plotY+plotH/2.0)).setTooltip(Tooltip.create(tr("zoom_out")));
        button(Component.literal("+"),leftPos+panelWidth-50,footer,18,13,()->zoom(zoom*1.15,plotX+plotW/2.0,plotY+plotH/2.0)).setTooltip(Tooltip.create(tr("zoom_in")));
        button(Component.literal("↔"),leftPos+panelWidth-30,footer,20,13,this::fit).setTooltip(Tooltip.create(tr("fit")));
        fit();update();observe(false);
    }
    private Button button(Component text,int x,int y,int w,int h,Runnable action){return addRenderableWidget(new AtlasButton(x,y,w,h,text,action));}
    private void category(Mastery m){if(selected==m)return;selected=m;remembered=m;talent=SELECTIONS.getOrDefault(m,Talent.EFFICIENCY);pending=-1;rebuildWidgets();}
    private void page(boolean next){if(distinctions==next)return;distinctions=next;rebuildWidgets();}
    private void select(Node n){if(n.t!=null){talent=n.t;SELECTIONS.put(selected,talent);}else tier=n.milestone;detailScroll=0;update();}
    private Node selectedNode(){return nodes.stream().filter(n->distinctions?n.milestone==tier:n.t==talent).findFirst().orElse(null);}
    private void focusSelected(){var n=selectedNode();if(n!=null){setFocused(n);ensureVisible(n);}}
    private boolean canLearn(Talent t){var p=progress();return p.level()>=t.minimumLevel()&&p.points()>=t.cost()&&p.rank(t)<t.maxRank()&&(t.prerequisite()==null||p.rank(t.prerequisite())>0);}
    private boolean readyMode(){return minecraft.player!=null&&!minecraft.player.isCreative()&&!minecraft.player.isSpectator();}
    private long cooldown(){return minecraft.player==null||minecraft.level==null?0:Math.max(0,minecraft.player.getData(MasteryAbilities.NEXT)-minecraft.level.getGameTime());}
    private int equipped(){return minecraft.player==null?-1:minecraft.player.getData(Prestige.SELECTION);}
    private Component name(Talent t){return Component.translatable("talent.tnd."+t.id());}
    private Component description(Talent t){return Component.translatable("talent.tnd."+(t.specialized()?t.id():selected.id()+"."+t.id())+".description",Component.translatable("mastery.tnd."+MasteryRules.partner(selected).id()));}
    private Component state(Talent t){var p=progress();return tr(p.rank(t)==t.maxRank()?"state.complete":canLearn(t)?"state.available":"state.locked");}
    private Component type(Talent t){return tr(t==Talent.ability(selected)?"type.active":t.specialized()?"type.passive":"type.foundation");}
    private Component tooltip(Talent t){var p=progress();var text=name(t).copy().withStyle(ChatFormatting.WHITE).append("\n").append(type(t).copy().withStyle(ChatFormatting.AQUA)).append("\n\n").append(description(t));
        text.append("\n\n").append(tr("rank",p.rank(t),t.maxRank()));if(p.rank(t)<t.maxRank())text.append("\n").append(tr("cost",t.cost()));text.append("\n").append(state(t));
        if(t.minimumLevel()>1)text.append("\n").append(Component.translatable("mastery.tnd.required_level",t.minimumLevel()));
        if(t.prerequisite()!=null)text.append("\n").append(Component.translatable("gui.tnd.prerequisite",name(t.prerequisite())));
        if(t==Talent.ability(selected))text.append("\n").append(tr("cooldown_help"));return text;
    }
    private Component prestigeTooltip(int i){return Component.translatable("prestige.tnd.rank."+i).copy().append("\n").append(tr("prestige_level",Prestige.LEVELS[i])).append("\n\n").append(Component.translatable("prestige.tnd.help"));}
    private void primary(){
        if(distinctions){if(progress().level()>=Prestige.LEVELS[tier])send(3000+selected.ordinal()*4+tier);return;}
        if(talent==Talent.ability(selected)&&progress().rank(talent)>0){if(cooldown()==0&&readyMode()){send(2000+selected.ordinal());onClose();}return;}
        if(!canLearn(talent)||pending>=0)return;
        pending=selected.ordinal()*Talent.values().length+talent.ordinal();pendingUntil=Util.getMillis()+3000;send(pending);update();
    }
    private void utility(){if(distinctions){send(3100);return;}if(progress().rank(Talent.INSIGHT)>0){send(1000+selected.ordinal());onClose();}}
    private void update(){if(primary==null)return;var p=progress();long now=Util.getMillis();if(pending>=0&&now>=pendingUntil)pending=-1;
        treeTab.setMessage(tr(distinctions?"talents":"talents_selected"));titleTab.setMessage(tr(distinctions?"titles_selected":"titles"));
        motionButton.setTooltip(Tooltip.create(tr(AtmosphereOptions.ANIMATIONS.get()?"motion_on":"motion_off")));
        for(var entry:categories.entrySet()){var q=progress(entry.getKey());entry.getValue().setTooltip(Tooltip.create(Component.translatable("gui.tnd.mastery_summary",Component.translatable("mastery.tnd."+entry.getKey().id()),q.level(),q.experience(),q.points())));}
        if(distinctions){boolean on=equipped()==selected.ordinal()*4+tier;primary.setMessage(tr(on?"equipped":"equip"));primary.active=p.level()>=Prestige.LEVELS[tier]&&!on;utility.setMessage(Component.translatable("prestige.tnd.hide"));utility.visible=true;utility.active=equipped()>=0;info.setMessage(Component.translatable("prestige.tnd.rank."+tier));info.setTooltip(Tooltip.create(prestigeTooltip(tier)));}
        else{
            boolean ability=talent==Talent.ability(selected)&&p.rank(talent)>0;
            if(ability){long remain=cooldown();primary.setMessage(!readyMode()?tr("survival"):remain>0?Component.translatable("ability.tnd.cooldown",(remain+19)/20):tr("activate"));primary.active=readyMode()&&remain==0;}
            else{primary.active=canLearn(talent)&&pending<0;primary.setMessage(pending>=0?tr("pending"):p.rank(talent)>=talent.maxRank()?tr("complete"):tr(p.rank(talent)>0?"improve":"learn",talent.cost()));}
            primary.setTooltip(Tooltip.create(ability?Component.translatable("ability.tnd.help"):tooltip(talent)));
            utility.setMessage(Component.translatable("gui.tnd.sense"));utility.visible=p.rank(Talent.INSIGHT)>0;utility.active=utility.visible;
            utility.setTooltip(Tooltip.create(Component.translatable("gui.tnd.sense_help",MasteryRules.radius(p))));
            info.setMessage(name(talent));info.setTooltip(Tooltip.create(tooltip(talent)));
        }
        for(var n:nodes){n.setTooltip(Tooltip.create(n.t==null?prestigeTooltip(n.milestone):tooltip(n.t)));n.setMessage(n.t==null?prestigeTooltip(n.milestone):tooltip(n.t));}
    }
    private void observe(boolean feedback){
        for(var m:Mastery.values())for(var t:Talent.available(m)){int index=m.ordinal()*Talent.values().length+t.ordinal(),rank=progress(m).rank(t);
            if(observedReady&&rank>observed[index]){pending=-1;if(feedback){flashAt=Util.getMillis();flashTalent=t;flashMastery=m;sound(true);}}observed[index]=rank;
        }observedReady=true;
    }
    @Override protected void containerTick(){super.containerTick();observe(true);update();}
    private boolean inside(double x,double y){return x>=plotX&&x<plotX+plotW&&y>=plotY&&y<plotY+plotH;}
    private void fit(){
        double minX=distinctions?64:32,maxX=distinctions?388:412,minY=distinctions?92:28,maxY=distinctions?92:144;
        zoom=Math.clamp(Math.min((plotW-(wide?100:42))/(maxX-minX),(plotH-(wide?78:36))/Math.max(1,maxY-minY)),.55,1.25);
        panX=(plotW-(maxX-minX)*zoom)/2-minX*zoom;panY=(plotH-(maxY-minY)*zoom)/2-minY*zoom-(wide?10:0);positionNodes();
    }
    private void positionNodes(){for(var n:nodes){int size=(int)Math.round(32*Math.clamp(zoom,.85,1.2));n.setSize(size,size);n.setPosition(plotX+(int)Math.round(panX+n.gx*zoom)-size/2,plotY+(int)Math.round(panY+n.gy*zoom)-size/2);}}
    private void zoom(double next,double x,double y){double old=zoom;zoom=Math.clamp(next,.55,1.65);panX=x-plotX-(x-plotX-panX)*zoom/old;panY=y-plotY-(y-plotY-panY)*zoom/old;clampPan();positionNodes();}
    private void clampPan(){panX=Math.clamp(panX,-440*zoom+24,plotW-24);panY=Math.clamp(panY,-168*zoom+20,plotH-20);}
    private void ensureVisible(Node n){int pad=24;if(n.getX()<plotX+pad)panX+=plotX+pad-n.getX();if(n.getRight()>plotX+plotW-pad)panX-=n.getRight()-(plotX+plotW-pad);if(n.getY()<plotY+pad)panY+=plotY+pad-n.getY();if(n.getBottom()>plotY+plotH-pad)panY-=n.getBottom()-(plotY+plotH-pad);positionNodes();}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){boolean handled=super.mouseClicked(e,twice);panning=e.button()==0&&inside(e.x(),e.y());return handled||panning;}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(panning&&e.button()==0){panX+=dx;panY+=dy;clampPan();positionNodes();return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){panning=false;return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){
        if(inside(x,y)){zoom(zoom*Math.pow(1.12,sy),x,y);return true;}
        if(wide&&x>=detailX&&x<detailX+detailW&&y>=plotY&&y<plotY+plotH-52){detailScroll=Math.clamp(detailScroll-(int)(sy*22),0,detailMax);return true;}
        if(y>=topPos+30&&y<topPos+54){category(Mastery.values()[Math.floorMod(selected.ordinal()+(sy<0?1:-1),Mastery.values().length)]);return true;}
        return super.mouseScrolled(x,y,sx,sy);
    }
    @Override public boolean keyPressed(KeyEvent e){
        int key=e.key();if(key==GLFW.GLFW_KEY_HOME){fit();return true;}
        if(key==GLFW.GLFW_KEY_PAGE_UP||key==GLFW.GLFW_KEY_PAGE_DOWN){category(Mastery.values()[Math.floorMod(selected.ordinal()+(key==GLFW.GLFW_KEY_PAGE_DOWN?1:-1),Mastery.values().length)]);return true;}
        if(key==GLFW.GLFW_KEY_EQUAL||key==GLFW.GLFW_KEY_KP_ADD){zoom(zoom*1.15,plotX+plotW/2.0,plotY+plotH/2.0);return true;}
        if(key==GLFW.GLFW_KEY_MINUS||key==GLFW.GLFW_KEY_KP_SUBTRACT){zoom(zoom/1.15,plotX+plotW/2.0,plotY+plotH/2.0);return true;}
        if(getFocused() instanceof Node current&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){
            boolean horizontal=key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT;int sign=key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_UP?-1:1;
            nodes.stream().filter(n->n!=current&&(horizontal?n.gx-current.gx:n.gy-current.gy)*sign>0).min(Comparator.comparingDouble(n->{double a=horizontal?n.gx-current.gx:n.gy-current.gy,b=horizontal?n.gy-current.gy:n.gx-current.gx;return a*a+b*b*2;})).ifPresent(n->{setFocused(n);select(n);ensureVisible(n);});return true;
        }
        return super.keyPressed(e);
    }
    @Override public int getImageWidth(){return panelWidth;}
    @Override public int getImageHeight(){return panelHeight;}
    @Override protected boolean hasClickedOutside(double x,double y,int left,int top){return x<left||y<top||x>=left+panelWidth||y>=top+panelHeight;}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    private String fitText(Component text,int max){String s=text.getString();return font.width(s)<=max?s:font.plainSubstrByWidth(s,Math.max(0,max-10))+"…";}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;g.fillGradient(0,0,width,height,0xee070c16,0xf4090b17);ConstellationStyle.panel(g,x,y,panelWidth,panelHeight);
        g.text(font,tr("title"),x+12,y+11,ConstellationStyle.TEXT,false);
        var p=progress();var header=Component.translatable("mastery.tnd."+selected.id()).copy().append(" · ").append(tr("level",p.level()));
        g.text(font,fitText(header,panelWidth-118),x+12,y+61,ConstellationStyle.TEXT,false);
        var points=tr("points",p.points());g.text(font,points,x+panelWidth-12-font.width(points),y+61,ConstellationStyle.GOLD,false);
        long base=Experience.threshold(p.level()),next=Experience.threshold(p.level()+1);int value=(int)Math.clamp((p.experience()-base)/(double)Math.max(1,next-base)*1000,0,1000),fill=motion.value(selected.ordinal(),value)*(panelWidth-24)/1000;
        g.fill(x+12,y+75,x+panelWidth-12,y+78,0xff25314a);g.fill(x+12,y+75,x+12+fill,y+78,ConstellationStyle.CYAN);
        g.enableScissor(plotX,plotY,plotX+plotW,plotY+plotH);double seconds=AtmosphereOptions.ANIMATIONS.get()?Util.getMillis()/1000.0:0;
        ConstellationStyle.space(g,plotX,plotY,plotW,plotH,seconds);branches(g,seconds);g.disableScissor();g.outline(plotX-1,plotY-1,plotW+2,plotH+2,0xff2c3b55);
        if(wide)details(g);
        g.text(font,fitText(tr("controls"),panelWidth-94),x+12,y+panelHeight-15,ConstellationStyle.MUTED,false);
    }
    private boolean ancestor(Talent t){Talent cursor=talent;while(cursor!=null){if(cursor==t)return true;cursor=cursor.prerequisite();}return false;}
    private void branches(GuiGraphicsExtractor g,double seconds){
        for(int i=0;i<nodes.size();i++){
            Node to=nodes.get(i),from;
            if(distinctions){if(i==0)continue;from=nodes.get(i-1);}
            else{Talent pre=to.t.prerequisite();if(pre==null)continue;from=nodes.stream().filter(n->n.t==pre).findFirst().orElseThrow();}
            int x1=from.getX()+from.getWidth()/2,y1=from.getY()+from.getHeight()/2,x2=to.getX()+to.getWidth()/2,y2=to.getY()+to.getHeight()/2,elbow=from.gx==124&&to.gx==316?plotX+(int)Math.round(panX+172*zoom):(x1+x2)/2;
            boolean owned=distinctions?progress().level()>=Prestige.LEVELS[to.milestone]:progress().rank(to.t)>0;
            boolean route=distinctions?to.milestone<=tier:ancestor(to.t);int color=owned?ConstellationStyle.GOLD:route?0xff88b9c3:0xff38465f;
            ConstellationStyle.segment(g,x1,y1,elbow,y1,color);ConstellationStyle.segment(g,elbow,y1,elbow,y2,color);ConstellationStyle.segment(g,elbow,y2,x2,y2,color);
            if(owned&&AtmosphereOptions.ANIMATIONS.get()){int width=Math.abs(x2-elbow);int dot=elbow+(int)((seconds*.08+i*.17)%1*width);g.fill(dot,y2,dot+2,y2+1,0xfff7e5ba);}
        }
    }
    private record Line(Component text,int color){}
    private List<Line> detailLines(){var lines=new ArrayList<Line>();var p=progress();
        if(distinctions){lines.add(new Line(tr("cosmetic"),ConstellationStyle.CYAN));lines.add(new Line(Component.translatable("prestige.tnd.help"),ConstellationStyle.TEXT));lines.add(new Line(tr("prestige_level",Prestige.LEVELS[tier]),p.level()>=Prestige.LEVELS[tier]?ConstellationStyle.GOLD:ConstellationStyle.MUTED));lines.add(new Line(tr("current_level",p.level()),ConstellationStyle.MUTED));}
        else{lines.add(new Line(type(talent),ConstellationStyle.CYAN));lines.add(new Line(description(talent),ConstellationStyle.TEXT));lines.add(new Line(tr("rank",p.rank(talent),talent.maxRank()),ConstellationStyle.GOLD));
            if(p.rank(talent)<talent.maxRank())lines.add(new Line(tr("cost",talent.cost()),p.points()>=talent.cost()?ConstellationStyle.CYAN:0xffdcaaa0));
            if(talent.minimumLevel()>1)lines.add(new Line(Component.translatable("mastery.tnd.required_level",talent.minimumLevel()),p.level()>=talent.minimumLevel()?ConstellationStyle.CYAN:0xffdcaaa0));
            if(talent.prerequisite()!=null)lines.add(new Line(Component.translatable("gui.tnd.prerequisite",name(talent.prerequisite())),p.rank(talent.prerequisite())>0?ConstellationStyle.CYAN:0xffdcaaa0));
            if(talent==Talent.ability(selected))lines.add(new Line(tr("cooldown_help"),ConstellationStyle.MUTED));
        }return lines;
    }
    private void details(GuiGraphicsExtractor g){
        ConstellationStyle.panel(g,detailX,plotY,detailW,plotH);ItemStack icon=distinctions?new ItemStack(Prestige.SEALS.get(tier).get()):ConstellationStyle.talent(selected,talent);
        ConstellationStyle.plate(g,detailX+10,plotY+10,28,28,0xff53637e,0xff18283e);g.item(icon,detailX+16,plotY+16);
        Component heading=distinctions?Component.translatable("prestige.tnd.rank."+tier):name(talent);var headings=font.split(heading,detailW-56);
        for(int i=0;i<Math.min(3,headings.size());i++)g.text(font,headings.get(i),detailX+46,plotY+11+i*11,ConstellationStyle.TEXT,false);
        int top=plotY+48,bottom=(utility.visible?utility.getY():primary.getY())-7,yy=top-detailScroll,total=0;
        g.fill(detailX+10,plotY+43,detailX+detailW-10,plotY+44,0xff2b3a54);g.enableScissor(detailX+10,top,detailX+detailW-10,bottom);
        for(var line:detailLines()){var parts=font.split(line.text(),detailW-26);for(var part:parts){g.text(font,part,detailX+12,yy, line.color(),false);yy+=11;total+=11;}yy+=7;total+=7;}
        g.disableScissor();detailMax=Math.max(0,total-(bottom-top));detailScroll=Math.clamp(detailScroll,0,detailMax);
        if(detailMax>0){int rail=bottom-top,thumb=Math.max(12,rail*rail/(total+1)),sy=top+(rail-thumb)*detailScroll/detailMax;g.fill(detailX+detailW-6,top,detailX+detailW-4,bottom,0xff24334b);g.fill(detailX+detailW-6,sy,detailX+detailW-4,sy+thumb,ConstellationStyle.CYAN);}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractRenderState(g,mx,my,partial);
        if(mx>=leftPos+12&&mx<leftPos+panelWidth-12&&my>=topPos+60&&my<topPos+79){var p=progress();g.setTooltipForNextFrame(tr("xp",p.experience(),Math.max(0,Experience.threshold(p.level()+1)-p.experience())),mx,my);}
    }
    private class AtlasButton extends Button {
        AtlasButton(int x,int y,int w,int h,Component text,Runnable action){super(x,y,w,h,text,b->action.run(),n->n.get());}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float a){boolean hot=isHoveredOrFocused();ConstellationStyle.plate(g,getX(),getY(),getWidth(),getHeight(),active?(hot?ConstellationStyle.CYAN:0xff405574):0xff263149,active?(hot?0xff254051:0xff17273c):0xff101b2d);String label=fitText(getMessage(),getWidth()-(getWidth()<25?4:8));g.text(font,label,getX()+(getWidth()-font.width(label))/2,getY()+(getHeight()-8)/2,active?ConstellationStyle.TEXT:ConstellationStyle.DIM,false);}
        @Override public void playDownSound(SoundManager manager){sound(false);}
    }
    private final class Node extends AtlasButton {
        final Talent t;final int milestone,gx,gy;
        Node(Talent t,int milestone,int x,int y){super(0,0,32,32,Component.empty(),()->{});this.t=t;this.milestone=milestone;gx=x;gy=y;}
        @Override public void onPress(InputWithModifiers input){select(this);}
        @Override public void setFocused(boolean focused){super.setFocused(focused);if(focused&&primary!=null){select(this);ensureVisible(this);}}
        @Override public boolean isMouseOver(double x,double y){return inside(x,y)&&super.isMouseOver(x,y);}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float a){
            g.enableScissor(plotX,plotY,plotX+plotW,plotY+plotH);
            boolean chosen=distinctions?milestone==tier:t==talent,owned=distinctions?progress().level()>=Prestige.LEVELS[milestone]:progress().rank(t)>0,available=t!=null&&canLearn(t),hot=isHoveredOrFocused();
            int edge=chosen||hot?ConstellationStyle.CYAN:owned?ConstellationStyle.GOLD:available?0xff98b7cd:ConstellationStyle.LOCKED;
            int x=getX(),y=getY(),size=getWidth();if(chosen)ConstellationStyle.plate(g,x-3,y-3,size+6,size+6,0xff344c65,0xff152638);
            ConstellationStyle.plate(g,x,y,size,size,edge,owned?0xff2a302f:available?0xff19323e:0xff161f30);
            if(t==Talent.ability(selected)){g.fill(x+size/2-2,y-3,x+size/2+2,y-1,edge);g.fill(x+size/2-1,y-4,x+size/2+1,y,edge);}
            ItemStack icon=t==null?new ItemStack(Prestige.SEALS.get(milestone).get()):ConstellationStyle.talent(selected,t);g.item(icon,x+(size-16)/2,y+(size-16)/2-2);
            String badge=t==null?Integer.toString(Prestige.LEVELS[milestone]):progress().rank(t)>0?progress().rank(t)+"/"+t.maxRank():t.minimumLevel()>1?Integer.toString(t.minimumLevel()):"0/"+t.maxRank();
            int bw=font.width(badge)+4;g.fill(x+(size-bw)/2,y+size-8,x+(size+bw)/2,y+size+2,0xff0a1120);g.text(font,badge,x+(size-font.width(badge))/2,y+size-7,owned?ConstellationStyle.GOLD:ConstellationStyle.MUTED,false);
            if(!owned&&!available){g.fill(x+size-6,y+3,x+size-2,y+7,ConstellationStyle.LOCKED);g.fill(x+size-5,y+1,x+size-3,y+4,ConstellationStyle.LOCKED);}
            if((wide&&zoom>=.65||zoom>=.9)&&plotH>=160){Component label=t==null?Component.translatable("prestige.tnd.rank."+milestone):name(t);var caption=font.split(label,Math.clamp((int)(88*zoom),54,88));for(int i=0;i<Math.min(3,caption.size());i++){var line=caption.get(i);g.text(font,line,x+size/2-font.width(line)/2,y+size+7+i*10,chosen?ConstellationStyle.TEXT:ConstellationStyle.MUTED,false);}}
            long elapsed=Util.getMillis()-flashAt;
            if(t==flashTalent&&selected==flashMastery&&elapsed<900&&AtmosphereOptions.ANIMATIONS.get()){
                int radius=18+(int)(elapsed*18/900),alpha=(int)(210*(1-elapsed/900.0));for(int i=0;i<8;i++){double angle=i*Math.PI/4;int xx=x+size/2+(int)(Math.cos(angle)*radius),yy=y+size/2+(int)(Math.sin(angle)*radius);g.fill(xx,yy,xx+2,yy+2,(alpha<<24)|0xf0cc86);}
            }
            g.disableScissor();
        }
    }
}
