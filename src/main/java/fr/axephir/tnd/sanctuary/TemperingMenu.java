package fr.axephir.tnd.sanctuary;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class TemperingMenu extends AbstractContainerMenu {
    private final Container machine;private final ContainerData data;
    public TemperingMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(4),new SimpleContainerData(6));}
    public TemperingMenu(int id,Inventory inv,Container machine,ContainerData data){
        super(TemperingContent.MENU.get(),id);this.machine=machine;this.data=data;checkContainerSize(machine,4);checkContainerDataCount(data,6);
        int[][] at={{24,54},{24,88},{60,88},{150,70}};
        for(int i=0;i<4;i++){final int n=i;addSlot(new Slot(machine,i,at[i][0],at[i][1]){public boolean mayPlace(ItemStack s){return n==0?TemperingForgeEntity.eligible(s):n==1?s.is(SanctuaryContent.SHARD.get()):n==2&&s.is(SanctuaryContent.DUST.get());}});}
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,9+r*9+c,12+c*18,141+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,12+c*18,199));addDataSlots(data);
    }
    public int choice(){return data.get(0)-1;}public int progress(){return data.get(1);}public int status(){return data.get(2);}public int energy(){return (data.get(3)&65535)|((data.get(4)&65535)<<16);}public boolean armed(){return data.get(5)==1;}
    public ItemStack preview(){return TemperingForgeEntity.tempered(machine.getItem(0),choice());}
    public boolean stillValid(Player p){return p.isAlive()&&machine.stillValid(p);}
    public boolean clickMenuButton(Player p,int action){return p instanceof net.minecraft.server.level.ServerPlayer&&machine instanceof TemperingForgeEntity e&&stillValid(p)&&e.action(p,action);}
    public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var source=slot.getItem();var copy=source.copy();boolean moved;
        if(index<4)moved=moveItemStackTo(source,4,40,true);
        else if(TemperingForgeEntity.eligible(source))moved=moveItemStackTo(source,0,1,false);
        else if(source.is(SanctuaryContent.SHARD.get()))moved=moveItemStackTo(source,1,2,false);
        else if(source.is(SanctuaryContent.DUST.get()))moved=moveItemStackTo(source,2,3,false);else return ItemStack.EMPTY;
        if(!moved)return ItemStack.EMPTY;if(source.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,source);return copy;
    }
}
