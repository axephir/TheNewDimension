package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.*;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.PlayerProgression;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Three physical slots: fuel, tuning module, output. No hidden infinite resource store. */
public final class ExtractorBlockEntity extends BlockEntity implements Container, MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private long lastTick = -1;
    private long progress;
    private int calibration;
    private int synergy;
    private boolean settling;
    private ExtractionMode mode=ExtractionMode.IRON;
    private int relicCalibration;
    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) { case 0 -> (int) (progress * 1000 / period()); case 1 -> Math.max(1,period()/20); case 2 -> mode.ordinal(); case 3 -> relicCalibration; default -> 0; };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 4; }
    };

    public ExtractorBlockEntity(BlockPos pos, BlockState state) { super(MachineRegistry.EXTRACTOR_ENTITY.get(), pos, state); }
    public int periodTicks() {return period();}
    public ExtractionMode mode(){return mode;}
    public int relicCalibration(){return relicCalibration;}
    private int period() {
        double speed = 1 + (calibration+relicCalibration) / 100.0 + synergy * 0.02;
        if (items.get(1).is(ModContent.EFFICIENCY_MODULE.get())) speed += 0.5;
        if(items.get(1).is(ModContent.STABILIZER_MODULE.get()))speed+=0.25;
        return Math.max(20, (int) (TndConfig.EXTRACTOR_TICKS.getAsInt()*mode.timeFactor / speed));
    }
    public void tune(Player player) {
        settle();
        var profile = player.getData(PlayerProgression.PROFILE);
        var engineering = profile.get(Mastery.ENGINEERING);
        int old=calibration;
        calibration = Math.max(calibration,engineering.rank(Talent.EFFICIENCY)*5+engineering.rank(Talent.YIELD)*3
                +engineering.rank(Talent.PRECISION)+engineering.rank(Talent.ENGINEERING_TUNER)*10+(int)(Experience.prestigeBonus(engineering.level())*100));
        synergy = Math.max(synergy,Math.min(profile.get(Mastery.ENGINEERING).rank(Talent.SYNERGY),profile.get(Mastery.MINING).rank(Talent.SYNERGY)));
        if(calibration>old&&player instanceof net.minecraft.server.level.ServerPlayer server
                &&fr.axephir.tnd.progression.ProgressionService.claim(server,"machine_tuning",24000,8))
            fr.axephir.tnd.progression.ProgressionService.award(server,Mastery.ENGINEERING,15);
        setChanged();
    }

    public boolean calibrateRelic(int bonus) {
        if(bonus<=relicCalibration)return false;
        settle();relicCalibration=Math.clamp(bonus,0,25);setChanged();return true;
    }
    public boolean selectMode(ExtractionMode selected) {
        settle();
        if(mode==selected)return true;
        if(!items.get(2).isEmpty()||(selected==ExtractionMode.DIAMOND&&!items.get(1).is(ModContent.STABILIZER_MODULE.get())))return false;
        mode=selected;progress=0;setChanged();return true;
    }
    public void settle() {
        if (settling || level == null || level.isClientSide()) return;
        settling = true;
        try {
            long now = Math.max(0, level.getGameTime());
            if (lastTick < 0) { lastTick = now; setChanged(); return; }
            ItemStack fuel = items.get(0), output = items.get(2);
            if(mode==ExtractionMode.DIAMOND&&!items.get(1).is(ModContent.STABILIZER_MODULE.get())) {lastTick=now;setChanged();return;}
            if ((!output.isEmpty() && !output.is(mode.output))
                    || (!fuel.isEmpty() && !validFuel(fuel))) { lastTick = now; progress = 0; return; }
            var result = Production.advance(new Production.State(lastTick, progress, fuel.getCount(), output.getCount()),
                    new Production.Rules(period(), 1, 64, TndConfig.MAX_CATCHUP.getAsInt()), now);
            int made = result.output() - output.getCount();
            fuel.shrink(fuel.getCount() - result.fuel());
            if (made > 0) {
                if (output.isEmpty()) items.set(2, new ItemStack(mode.output, made));
                else output.grow(made);
            }
            lastTick = result.lastTick(); progress = result.progressTicks();
            setChanged();
        } finally { settling = false; }
    }
    public static boolean validModule(ItemStack stack){return stack.is(ModContent.EFFICIENCY_MODULE.get())||stack.is(ModContent.STABILIZER_MODULE.get());}
    public static boolean validFuel(ItemStack stack) { return stack.is(Items.COAL) || stack.is(Items.CHARCOAL); }
    @Override public int getContainerSize() { return 3; }
    @Override public boolean isEmpty() { settle(); return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { settle(); return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        settle(); var result = ContainerHelper.removeItem(items, slot, amount); setChanged(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { settle(); return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        settle(); stack.limitSize(slot == 1 ? 1 : 64); items.set(slot, stack); setChanged();
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 ? validFuel(stack) : slot == 1 && validModule(stack);
    }
    @Override public void clearContent() { settle(); items.clear(); setChanged(); }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.tnd.extractor"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        settle(); return new ExtractorMenu(id, inventory, this, data);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            settle();
            Containers.dropContents(level, pos, this);
            items.clear();
        }
        super.preRemoveSideEffects(pos, state);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("lastTick", lastTick); output.putLong("progress", progress);
        output.putInt("calibration", calibration); output.putInt("synergy", synergy);
        output.putInt("mode",mode.ordinal());output.putInt("relicCalibration",relicCalibration);
        var list = output.list("items", ItemStackWithSlot.CODEC);
        for (int slot = 0; slot < 3; slot++) if (!items.get(slot).isEmpty()) list.add(new ItemStackWithSlot(slot, items.get(slot)));
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        lastTick = input.getLongOr("lastTick", -1); progress = Math.max(0, input.getLongOr("progress", 0));
        calibration = Math.clamp(input.getIntOr("calibration", 0), 0, 60);
        synergy = Math.clamp(input.getIntOr("synergy", 0), 0, 5);
        mode=ExtractionMode.values()[Math.clamp(input.getIntOr("mode",0),0,ExtractionMode.values().length-1)];
        relicCalibration=Math.clamp(input.getIntOr("relicCalibration",0),0,25);
        items.clear();
        input.listOrEmpty("items", ItemStackWithSlot.CODEC).forEach(entry -> {
            if (entry.isValidInContainer(3)) items.set(entry.slot(), entry.stack());
        });
    }
}
