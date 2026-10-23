package fr.axephir.tnd.client;
import fr.axephir.tnd.domain.SanctuaryRoute;

import fr.axephir.tnd.sanctuary.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** A readable route overview. Visiting a node selects it; travelling requires a separate server-checked action. */
public final class SanctuaryScreen extends AbstractContainerScreen<SanctuaryMenu> {
    private int selected=-1;private boolean syncedSelection;private Button begin,claim,travel,retry;
    public SanctuaryScreen(SanctuaryMenu menu,Inventory inv,Component title){super(menu,inv,title,400,234);}
    protected void init(){super.init();if(selected<0)selected=menu.index();
        for(int i=0;i<48;i++){final int n=i;int col=i/8%2==0?i%8:7-i%8;
            var b=new AstralButton(leftPos+12+col*21,topPos+51+i/8*23,17,17,Component.literal(Integer.toString(i+1)),v->selected=n,a->a.get()){
                protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){boolean done=SanctuaryRoute.done(menu.mask(),n),next=SanctuaryRoute.available(menu.mask(),n);int color=selected==n?ConstellationStyle.CYAN:done?ConstellationStyle.GOLD:next?ConstellationStyle.TEXT:ConstellationStyle.LOCKED;
                    ConstellationStyle.plate(g,getX(),getY(),getWidth(),getHeight(),color,selected==n?0xff254051:0xff111c2f);
                    String label=SanctuaryRoute.boss(n)?"◆":Integer.toString(n+1);g.text(font,label,getX()+(getWidth()-font.width(label))/2,getY()+5,color,false);
                }
            };
            b.setTooltip(Tooltip.create(Component.translatable("observatory.tnd.station",i+1,tr("room."+i))));addRenderableWidget(b);
        }
        begin=addRenderableWidget(AstralButton.create(tr("start"),b->action(0)).bounds(leftPos+10,topPos+208,112,19).build());
        claim=addRenderableWidget(AstralButton.create(tr("claim"),b->action(1)).bounds(leftPos+128,topPos+208,68,19).build());
        addRenderableWidget(AstralButton.create(tr("leave"),b->action(2)).bounds(leftPos+202,topPos+208,85,19).build());
        travel=addRenderableWidget(AstralButton.create(tr("travel"),b->action(100+selected)).bounds(leftPos+293,topPos+208,97,19).build());
        retry=addRenderableWidget(AstralButton.create(Component.literal("↻"),b->action(menu.index()%8==2?4:3)).bounds(leftPos+347,topPos+8,18,18).tooltip(Tooltip.create(tr("retry_help"))).build());
        addRenderableWidget(AstralButton.create(Component.literal("×"),b->onClose()).bounds(leftPos+372,topPos+8,18,18).build());
    }
    private static Component tr(String key,Object...args){return Component.translatable("observatory.tnd."+key,args);}
    private void action(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    protected void containerTick(){super.containerTick();if(!syncedSelection&&menu.value(9)==1){selected=menu.index();syncedSelection=true;}begin.active=selected==menu.index()&&menu.available()&&menu.value(4)==0;begin.setMessage(tr(menu.value(4)!=0?"running":"action."+(menu.index()%8)));claim.active=selected==menu.index()&&menu.value(12)==1;claim.setMessage(tr(menu.value(12)==2?"claimed":"claim"));travel.active=SanctuaryRoute.done(menu.mask(),selected);retry.active=selected==menu.index()&&(menu.value(4)!=0||menu.available()&&menu.index()%8==2);}
    protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,400,234);g.text(font,title,x+12,y+12,ConstellationStyle.TEXT,false);
        g.text(font,tr("progress",Long.bitCount(menu.mask()),48),x+12,y+32,ConstellationStyle.MUTED,false);
        for(int i=0;i<47;i++){int col=i/8%2==0?i%8:7-i%8,ncol=(i+1)/8%2==0?(i+1)%8:7-(i+1)%8;
            ConstellationStyle.segment(g,x+20+col*21,y+59+i/8*23,x+20+ncol*21,y+59+(i+1)/8*23,SanctuaryRoute.done(menu.mask(),i)?0xff8d7753:0xff28364f);}
        AstralPanel.inset(g,x+190,y+39,200,158);
        AstralPanel.heading(g,font,tr("act."+(selected/8)),x+199,y+47,180,mx,my);
        AstralPanel.heading(g,font,tr("room."+selected),x+199,y+62,178,mx,my);
        int yy=y+79;var lines=font.split(tr(menu.value(17)==1&&selected<8?"witness.objective."+selected:selected==39?"objective.duo":"objective."+(selected%8)),178);for(int i=0;i<Math.min(7,lines.size());i++)g.text(font,lines.get(i),x+199,yy+i*10,ConstellationStyle.TEXT,false);
        if(selected==menu.index()){
            int slot=selected%8;if(slot==2){int[] v=fr.axephir.tnd.domain.SanctuaryPuzzles.target(selected);for(int i=0;i<4;i++)v[i]++;int hintY=y+124;for(var hint:font.split(tr("rule."+(selected/8)),178)){g.text(font,hint,x+199,hintY,ConstellationStyle.MUTED,false);hintY+=10;}g.text(font,tr("alignment",v[0],v[1],v[2],v[3]),x+199,y+154,ConstellationStyle.CYAN,false);g.text(font,tr("current_alignment",tone(13),tone(14),tone(15),tone(16)),x+199,y+165,ConstellationStyle.TEXT,false);}
            else if(slot==4){g.text(font,tr("pad_count",menu.value(10)),x+199,y+154,ConstellationStyle.CYAN,false);if(menu.value(17)==1)g.text(font,tr("witness.bridge_count",menu.value(19)),x+199,y+165,ConstellationStyle.MUTED,false);}
            else if(slot==3){g.text(font,tr("relays",menu.value(11)),x+199,y+154,ConstellationStyle.CYAN,false);g.text(font,tr("enemies",menu.value(6),menu.value(7)),x+199,y+165,ConstellationStyle.MUTED,false);}
            else if(slot==0||slot==5){g.text(font,tr("trace_count",Integer.bitCount(menu.value(8))),x+199,y+154,ConstellationStyle.CYAN,false);if(slot==5&&menu.value(17)==1)g.text(font,tr("witness.circuit_count",menu.value(18)<0?"–":Integer.bitCount(menu.value(18))),x+199,y+165,ConstellationStyle.MUTED,false);}
            else g.text(font,tr("enemies",menu.value(6),menu.value(7)),x+199,y+154,ConstellationStyle.CYAN,false);
        }
        String state=SanctuaryRoute.done(menu.mask(),selected)?"done":SanctuaryRoute.available(menu.mask(),selected)?"available":"locked";
        g.text(font,tr(state),x+199,y+179,ConstellationStyle.MUTED,false);g.text(font,menu.value(17)==1?tr("witness.discoveries",menu.value(20)):tr("map_hint"),x+12,y+194,ConstellationStyle.MUTED,false);
    }
    private Object tone(int field){return menu.value(field)>0?Integer.toString(menu.value(field)):"–";}
}
