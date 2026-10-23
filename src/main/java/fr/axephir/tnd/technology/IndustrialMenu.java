package fr.axephir.tnd.technology;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class IndustrialMenu extends AbstractContainerMenu {
    private final Container machine;private final ContainerData data;
    public IndustrialMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(11),new SimpleContainerData(9));}
    public IndustrialMenu(int id,Inventory inv,Container machine,ContainerData data){
        super(Industry.MENU.get(),id);this.machine=machine;this.data=data;checkContainerSize(machine,11);checkContainerDataCount(data,9);
        addSlot(new Slot(machine,0,23,57){@Override public boolean mayPlace(ItemStack s){return valid(0,s);}@Override public boolean isActive(){return inputVisible(0);}});
        addSlot(new Slot(machine,1,23,86){@Override public boolean mayPlace(ItemStack s){return valid(1,s);}@Override public boolean isActive(){return inputVisible(1);}});
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)addSlot(new Slot(machine,2+row*3+col,111+col*18,48+row*18){@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean isActive(){return outputVisible(getContainerSlot());}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,156+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,214));addDataSlots(data);
    }
    private boolean valid(int slot,ItemStack s){
        if(machine instanceof IndustrialEntity e)return e.canPlaceItem(slot,s);
        if(kind()==Industry.Kind.SORTER)return slot==1||!machine.getItem(1).isEmpty()&&ItemStack.isSameItemSameComponents(s,machine.getItem(1));
        if(kind().ordinal()>=10){var r=ObservatoryProcessing.recipe(kind());return r!=null&&s.is(slot==0?r.first():r.second());}
        if(slot==1)return kind()==Industry.Kind.REPAIR?fr.axephir.tnd.assault.AssaultEquipment.repair(s)>0:kind()==Industry.Kind.PASTURE&&s.is(net.minecraft.world.item.Items.BUCKET);
        return switch(kind()){case DYNAMO->IndustrialEntity.coal(s);case CRUSHER->!IndustrialEntity.crushed(s).isEmpty();case FURNACE->!IndustrialEntity.smelted(s).isEmpty();case QUARRY->s.is(Industry.DRILL.get());case RANCH->true;case PASTURE->s.is(net.minecraft.world.item.Items.SHEARS);case REPAIR->fr.axephir.tnd.assault.AssaultContent.gear(s)&&s.isDamaged();default->false;};
    }
    public Industry.Kind kind(){return Industry.Kind.values()[Math.clamp(data.get(0),0,Industry.Kind.values().length-1)];}
    public boolean inputVisible(int slot){return slot==1?kind()==Industry.Kind.PASTURE||kind()==Industry.Kind.REPAIR||kind().ordinal()>=10:kind()!=Industry.Kind.SOLAR&&kind()!=Industry.Kind.BATTERY&&kind()!=Industry.Kind.FARM;}
    public boolean outputsVisible(){return !kind().source()&&kind()!=Industry.Kind.RANCH;}
    public boolean outputVisible(int slot){return outputsVisible()&&(kind()!=Industry.Kind.REPAIR||slot==2);}
    public int energy(){return (data.get(1)&65535)|((data.get(2)&65535)<<16);}public int status(){return Math.clamp(data.get(4),0,12);}
    public int progress(){int period=switch(kind()){case CRUSHER,REPAIR->80;case FURNACE->60;case QUARRY->20;case FARM->40;case RANCH->100;case PASTURE->40;case REFINERY->120;case CRUCIBLE->180;case ASSEMBLER->240;case SORTER->10;default->1;};return Math.clamp(data.get(3)*1000/period,0,1000);}
    public int drill(){return data.get(5);}public boolean armed(){return data.get(6)!=0;}public int depth(){return data.get(7);}
    @Override public boolean clickMenuButton(Player p,int id){return id==0&&machine instanceof IndustrialEntity e&&e.toggleQuarry(p);}
    @Override public boolean stillValid(Player p){return machine.stillValid(p);}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();boolean moved;
        if(index<11)moved=moveItemStackTo(s,11,47,true);else if(valid(0,s))moved=moveItemStackTo(s,0,1,false);else if(valid(1,s))moved=moveItemStackTo(s,1,2,false);else return ItemStack.EMPTY;
        if(!moved)return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;
    }
}
