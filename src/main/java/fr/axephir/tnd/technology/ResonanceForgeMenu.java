package fr.axephir.tnd.technology;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ResonanceForgeMenu extends AbstractContainerMenu {
    public final int kind;
    private final Container machine;
    private final ContainerData data;
    public ResonanceForgeMenu(int id,Inventory inv){this(id,inv,0);}
    public ResonanceForgeMenu(int id,Inventory inv,int kind){this(id,inv,new SimpleContainer(3),new SimpleContainerData(2),kind);}
    public ResonanceForgeMenu(int id,Inventory inv,Container machine,ContainerData data){
        this(id,inv,machine,data,0);
    }
    public ResonanceForgeMenu(int id,Inventory inv,Container machine,ContainerData data,int kind){
        super(kind==1?MachineRegistry.SEPARATOR_MENU.get():kind==2?MachineRegistry.REAPER_MENU.get():MachineRegistry.FORGE_MENU.get(),id);this.kind=kind;checkContainerSize(machine,3);checkContainerDataCount(data,2);this.machine=machine;this.data=data;
        addSlot(new Slot(machine,0,35,49){@Override public boolean mayPlace(ItemStack s){return validInput(s);}});
        addSlot(new Slot(machine,1,35,87){@Override public boolean mayPlace(ItemStack s){return validFuel(s);}});
        addSlot(new Slot(machine,2,125,67){@Override public boolean mayPlace(ItemStack s){return false;}});
        for(int row=0;row<3;row++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+row*9+9,8+c*18,129+row*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,187));addDataSlots(data);
    }
    private boolean validInput(ItemStack s){return kind!=2&&!ResonanceForgeEntity.result(s,kind==1).isEmpty();}
    private boolean validFuel(ItemStack s){return kind==2?EchoReaperEntity.validFuel(s):ResonanceForgeEntity.validFuel(s);}
    public int progress(){return Math.clamp(data.get(0),0,1000);}
    public int status(){return Math.clamp(data.get(1),0,6);}
    public ItemStack preview(){return kind==2?new ItemStack(fr.axephir.tnd.core.ModContent.ECHO_DUST.get()):ResonanceForgeEntity.result(machine.getItem(0),kind==1);}
    @Override public boolean stillValid(Player p){return machine.stillValid(p);}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var source=slot.getItem();var copy=source.copy();boolean moved;
        if(index<3)moved=moveItemStackTo(source,3,39,true);
        else if(validInput(source))moved=moveItemStackTo(source,0,1,false);
        else if(validFuel(source))moved=moveItemStackTo(source,1,2,false);
        else return ItemStack.EMPTY;
        if(!moved)return ItemStack.EMPTY;if(source.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,source);return copy;
    }
}
