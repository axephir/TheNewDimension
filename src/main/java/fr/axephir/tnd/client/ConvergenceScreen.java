package fr.axephir.tnd.client;

import fr.axephir.tnd.narrative.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Complete paginated instructions and explicit reversible choices, including in the read-only projection. */
public final class ConvergenceScreen extends AbstractContainerScreen<ConvergenceMenu> {
    private Button primary,secondary,previous,next;private final Button[] chapters=new Button[3];private int chapter,textPage,lastPhase=-1;
    public ConvergenceScreen(ConvergenceMenu menu,Inventory inv,Component title){super(menu,inv,title,330,230);}
    @Override protected void init(){super.init();primary=Button.builder(Component.empty(),b->choose(menu.finished()?4:menu.reactor()?0:1)).bounds(leftPos+12,topPos+195,177,21).build();secondary=Button.builder(Component.empty(),b->choose(2)).bounds(leftPos+196,topPos+195,122,21).build();addRenderableWidget(primary);addRenderableWidget(secondary);
        previous=Button.builder(Component.literal("‹"),b->textPage=Math.max(0,textPage-1)).bounds(leftPos+268,topPos+9,22,18).build();next=Button.builder(Component.literal("›"),b->textPage++).bounds(leftPos+296,topPos+9,22,18).build();addRenderableWidget(previous);addRenderableWidget(next);
        for(int i=0;i<3;i++){final int c=i;chapters[i]=Button.builder(Component.translatable("story.tnd.chapter."+i),b->{chapter=c;textPage=0;}).bounds(leftPos+12+i*104,topPos+36,100,19).build();addRenderableWidget(chapters[i]);}
    }
    private void choose(int action){minecraft.player.connection.send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new ConvergenceNetworking.Choice(menu.containerId,action)));}
    private java.util.List<net.minecraft.util.FormattedCharSequence> lines(){String key=menu.finished()?"finished":menu.phase()>=2?"restore_text":menu.phase()==1?"preparing":chapter==0?menu.reactor()?"reactor_intro":"restorer_intro":chapter==1?"assembly_restorer":"assembly_reactor";return font.split(Component.translatable("story.tnd."+key),302);}
    @Override protected void containerTick(){super.containerTick();if(menu.phase()!=lastPhase){textPage=0;lastPhase=menu.phase();}int pages=Math.max(1,(lines().size()+9)/10);textPage=Math.clamp(textPage,0,pages-1);previous.active=textPage>0;next.active=textPage+1<pages;
        boolean echo=menu.phase()>=2&&menu.phase()<=5;primary.visible=menu.finished()||echo&&!menu.reactor()||menu.reactor();primary.active=menu.finished()&&!menu.lensPresent()||echo&&menu.phase()!=2&&menu.phase()!=5||menu.reactor()&&menu.readiness()==0;
        primary.setMessage(Component.translatable(menu.finished()?"story.tnd.lens":echo?"story.tnd.accept":"story.tnd.activate"));secondary.setMessage(Component.translatable(menu.phase()==1?"story.tnd.cancel":echo?"story.tnd.refuse":"story.tnd.close"));
        for(var button:chapters)button.active=menu.phase()==0;
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;g.fill(x-1,y-1,x+331,y+231,0xff697f8e);g.fill(x,y,x+330,y+230,0xff101a2b);g.fill(x+7,y+59,x+323,y+60,0xff3b536c);g.text(font,title,x+12,y+15,0xffcfeded,false);
        var lines=lines();int pages=Math.max(1,(lines.size()+9)/10);int at=Math.clamp(textPage,0,pages-1);for(int i=at*10;i<Math.min(lines.size(),(at+1)*10);i++)g.text(font,lines.get(i),x+14,y+66+(i%10)*10,0xffc7d3df,false);
        g.text(font,(at+1)+" / "+pages,x+231,y+15,0xffa2b9c7,false);
        if(menu.phase()==1){g.fill(x+14,y+177,x+316,y+183,0xff24334b);g.fill(x+14,y+177,x+14+302*menu.progress()/1000,y+183,0xff8dc5dc);}
        else if(menu.phase()<2){var status=Component.translatable(menu.reactor()?"story.tnd.ready."+menu.readiness():menu.readiness()==0?"story.tnd.restorer_ready":"story.tnd.restorer_incomplete",menu.notes());int yy=y+171;for(var line:font.split(status,302)){g.text(font,line,x+14,yy,menu.readiness()==0?0xffa8d9bb:0xffd6bba1,false);yy+=10;}}
    }
}
