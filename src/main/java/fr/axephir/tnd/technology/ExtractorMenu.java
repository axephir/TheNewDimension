package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ExtractorMenu extends AbstractContainerMenu {
    private final Container machine;
    private final ContainerData data;
    public ExtractorMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(3), new SimpleContainerData(4)); }
    public ExtractorMenu(int id, Inventory inventory, Container machine, ContainerData data) {
        super(MachineRegistry.EXTRACTOR_MENU.get(), id);
        checkContainerSize(machine, 3); checkContainerDataCount(data, 4);
        this.machine = machine; this.data = data;
        addSlot(new Slot(machine, 0, 27, 71) {
            @Override public boolean mayPlace(ItemStack stack) { return ExtractorBlockEntity.validFuel(stack); }
        });
        addSlot(new Slot(machine, 1, 71, 71) {
            @Override public boolean mayPlace(ItemStack stack) { return ExtractorBlockEntity.validModule(stack); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(machine, 2, 125, 71) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 111 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 169));
        addDataSlots(data);
    }
    public int progress() { return Math.clamp(data.get(0), 0, 1000); }
    public int periodSeconds() { return data.get(1); }
    public ExtractionMode mode() {return ExtractionMode.values()[Math.clamp(data.get(2),0,ExtractionMode.values().length-1)];}
    public int relicCalibration() {return data.get(3);}
    public boolean hasStabilizer() {return machine.getItem(1).is(ModContent.STABILIZER_MODULE.get());}
    public boolean outputEmpty() {return machine.getItem(2).isEmpty();}
    @Override public boolean clickMenuButton(Player player,int button) {
        return button>=0&&button<ExtractionMode.values().length&&stillValid(player)
                &&machine instanceof ExtractorBlockEntity extractor&&extractor.selectMode(ExtractionMode.values()[button]);
    }
    @Override public boolean stillValid(Player player) { return machine.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem(), copy = source.copy();
        boolean moved;
        if (index < 3) moved = moveItemStackTo(source, 3, 39, true);
        else if (ExtractorBlockEntity.validFuel(source)) moved = moveItemStackTo(source, 0, 1, false);
        else if (ExtractorBlockEntity.validModule(source)) moved = moveItemStackTo(source, 1, 2, false);
        else return ItemStack.EMPTY;
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }
}
