package fr.axephir.tnd.client;
import fr.axephir.tnd.city.CodexMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Paginated paragraphs are fully readable at 854x480; no silently truncated lore. */
public final class CodexScreen extends AbstractContainerScreen<CodexMenu> {
    private int textPage,last=-1;private Button prev,next,reward;private final Button[] entries=new Button[8];
    public CodexScreen(CodexMenu menu,Inventory inv,Component title){super(menu,inv,title,330,230);}
    @Override protected void init(){super.init();
        for(int i=0;i<8;i++){final int entry=i;entries[i]=AstralButton.create(Component.empty(),b->{textPage=0;minecraft.gameMode.handleInventoryButtonClick(menu.containerId,entry);}).bounds(leftPos+8,topPos+36+i*22,108,20).build();entries[i].setOverrideRenderHighlightedSprite(()->menu.selected()==entry);addRenderableWidget(entries[i]);}
        prev=AstralButton.create(Component.literal("‹"),b->textPage=Math.max(0,textPage-1)).bounds(leftPos+128,topPos+204,25,18).build();next=AstralButton.create(Component.literal("›"),b->textPage++).bounds(leftPos+291,topPos+204,25,18).build();addRenderableWidget(prev);addRenderableWidget(next);
        reward=AstralButton.create(Component.translatable("codex.tnd.claim"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100)).bounds(leftPos+169,topPos+7,146,20).build();addRenderableWidget(reward);
    }
    private java.util.List<net.minecraft.util.FormattedCharSequence> lines(){return font.split(Component.translatable(menu.unlocked(menu.selected())?"codex.tnd.text."+menu.selected():"codex.tnd.locked_text"),184);}
    @Override protected void containerTick(){super.containerTick();if(last!=menu.selected()){textPage=0;last=menu.selected();}int pages=Math.max(1,(lines().size()+12)/13);textPage=Math.clamp(textPage,0,pages-1);prev.active=textPage>0;next.active=textPage+1<pages;
        reward.visible=(menu.mask()&255)==255;reward.active=(menu.mask()&256)==0;
        for(int i=0;i<8;i++)entries[i].setMessage(Component.translatable(menu.unlocked(i)?"codex.tnd.short."+i:"codex.tnd.locked"));
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){int x=leftPos,y=topPos;AstralPanel.frame(g,x,y,330,230);g.text(font,Component.translatable("codex.tnd.header",Integer.bitCount(menu.mask()&255)),x+10,y+13,ConstellationStyle.TEXT,false);AstralPanel.inset(g,x+122,y+34,199,191);
        g.text(font,Component.translatable(menu.unlocked(menu.selected())?"codex.tnd.short."+menu.selected():"codex.tnd.locked"),x+131,y+43,ConstellationStyle.GOLD,false);
        var lines=lines();for(int i=textPage*13;i<Math.min(lines.size(),(textPage+1)*13);i++)g.text(font,lines.get(i),x+130,y+61+(i%13)*10,ConstellationStyle.TEXT,false);
        int pages=Math.max(1,(lines.size()+12)/13);g.text(font,(textPage+1)+" / "+pages,x+207,y+209,ConstellationStyle.TEXT,false);
    }
}
