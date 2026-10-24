package fr.axephir.tnd.deepindustry;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class DeepMachineMenu extends AbstractContainerMenu {
    private final Container inventory;private final ContainerData data;
    public DeepMachineMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(8),new SimpleContainerData(11));}
    public DeepMachineMenu(int id,Inventory inv,Container machine,ContainerData data){super(DeepIndustry.MENU.get(),id);inventory=machine;this.data=data;
        for(int i=0;i<3;i++){final int slot=i;addSlot(new Slot(machine,i,16+i*36,56){public boolean mayPlace(ItemStack s){return machine instanceof DeepMachineEntity m?m.canPlaceItem(slot,s):slot==2?s.is(DeepIndustry.item("maintenance_kit")):true;}});}
        for(int i=0;i<5;i++)addSlot(new Slot(machine,i+3,130+i*18,56){public boolean mayPlace(ItemStack s){return false;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,43+18*col,157+18*row));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,43+col*18,215));addDataSlots(data);
    }
    public int value(int n){return data.get(n);}public DeepIndustry.Kind kind(){return DeepIndustry.Kind.values()[Math.clamp(value(0),0,6)];}public int energy(){return(value(1)&65535)|((value(2)&65535)<<16);}public int cursor(){return(value(8)&65535)|((value(9)&65535)<<16);}
    public boolean stillValid(Player p){return inventory.stillValid(p);}public boolean clickMenuButton(Player p,int id){return inventory instanceof DeepMachineEntity e&&e.action(p,id);}
    public ItemStack quickMoveStack(Player p,int n){if(n<0||n>=slots.size())return ItemStack.EMPTY;var slot=slots.get(n);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();boolean moved=false;if(n<8)moved=moveItemStackTo(s,8,44,true);else for(int i:new int[]{2,0,1})if(slots.get(i).mayPlace(s)&&moveItemStackTo(s,i,i+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
