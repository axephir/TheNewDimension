package fr.axephir.tnd.city;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class PortalMonitorMenu extends AbstractContainerMenu {
    private final Container machine;private final ContainerData data;
    public PortalMonitorMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(1),new SimpleContainerData(8));}
    public PortalMonitorMenu(int id,Inventory inv,Container c,ContainerData d){super(CityContent.MONITOR_MENU.get(),id);machine=c;data=d;
        addSlot(new Slot(c,0,22,64){@Override public boolean mayPlace(ItemStack s){return s.is(fr.axephir.tnd.core.ModContent.COMPOSITE_CRYSTAL.get());}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,22+col*18,151+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,22+col*18,209));addDataSlots(d);
    }
    public int value(int i){return data.get(i);}public boolean charging(){return value(2)==7;}
    @Override public boolean stillValid(Player p){return machine.stillValid(p);}
    @Override public boolean clickMenuButton(Player p,int id){if(!(machine instanceof PortalMonitorEntity e)||!e.stillValid(p))return false;if(id==0)return e.start(p);if(id==1){e.cancel();return true;}if(id==2&&p instanceof net.minecraft.server.level.ServerPlayer sp){Codex.open(sp,0);return true;}return false;}
    @Override public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var item=slot.getItem();var copy=item.copy();boolean moved=i==0?moveItemStackTo(item,1,37,true):item.is(fr.axephir.tnd.core.ModContent.COMPOSITE_CRYSTAL.get())&&moveItemStackTo(item,0,1,false);if(!moved)return ItemStack.EMPTY;if(item.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,item);return copy;}
}
