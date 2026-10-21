package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Finite processing, bounded catch-up, no forced chunks, sided vanilla hopper access. */
public final class ResonanceForgeEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    public static final int PERIOD=400;
    private final NonNullList<ItemStack> items=NonNullList.withSize(3,ItemStack.EMPTY);
    private long lastTick=-1,progress;
    private boolean settling,paused,removing;
    public ResonanceForgeEntity(BlockPos pos,BlockState state){super(MachineRegistry.FORGE_ENTITY.get(),pos,state);}
    public static ItemStack result(ItemStack input){
        if(input.is(Items.RAW_IRON))return new ItemStack(Items.IRON_INGOT,2);
        if(input.is(Items.RAW_COPPER))return new ItemStack(Items.COPPER_INGOT,2);
        if(input.is(Items.RAW_GOLD))return new ItemStack(Items.GOLD_INGOT,2);
        if(input.is(ModContent.ANCIENT_ALLOY.get().asItem()))return new ItemStack(ModContent.TEMPERED_ALLOY.get());
        return ItemStack.EMPTY;
    }
    public static boolean validInput(ItemStack stack){return !result(stack).isEmpty();}
    public static boolean validFuel(ItemStack stack){return stack.is(ModContent.ECHO_DUST.get());}
    public int status(){
        if(level!=null&&level.hasNeighborSignal(worldPosition))return 3;
        var made=result(items.get(0));if(made.isEmpty())return 0;
        if(!validFuel(items.get(1)))return 1;
        var out=items.get(2);if(!out.isEmpty()&&(!ItemStack.isSameItemSameComponents(out,made)||out.getCount()+made.getCount()>out.getMaxStackSize()))return 2;
        return 4;
    }
    public final ContainerData data=new ContainerData(){
        @Override public int get(int i){return i==0?(int)(progress*1000/PERIOD):i==1?status():0;}
        @Override public void set(int i,int value){}
        @Override public int getCount(){return 2;}
    };
    public void settle(){
        if(settling||level==null||level.isClientSide())return;settling=true;
        try{
            long now=Math.max(0,level.getGameTime());
            if(lastTick<0||now<lastTick){lastTick=now;progress=0;setChanged();return;}
            long elapsed=Math.min(now-lastTick,TndConfig.MAX_CATCHUP.getAsInt());lastTick=now;
            int state=status();boolean wasPaused=paused;paused=state==3;if(wasPaused||paused)elapsed=0;
            if(state!=4){if(state!=3)progress=0;updateLit(false);setChanged();return;}
            var made=result(items.get(0));var out=items.get(2);
            int space=(out.isEmpty()?made.getMaxStackSize():out.getMaxStackSize())-out.getCount();
            int available=Math.min(Math.min(items.get(0).getCount(),items.get(1).getCount()),space/made.getCount());
            long total=elapsed+Math.min(progress,PERIOD-1);
            int cycles=(int)Math.min(total/PERIOD,available);
            if(cycles>0){
                items.get(0).shrink(cycles);items.get(1).shrink(cycles);
                if(out.isEmpty())items.set(2,made.copyWithCount(cycles*made.getCount()));else out.grow(cycles*made.getCount());
                // A single quiet local cue per settlement, including catch-up batches.
                level.playSound(null,worldPosition,ModContent.atmosphereSound("forge_complete"),net.minecraft.sounds.SoundSource.BLOCKS,.35f,1);
            }
            progress=cycles>=available?0:total%PERIOD;updateLit(status()==4);setChanged();
        }finally{settling=false;}
    }
    private void updateLit(boolean active){if(removing||level.getBlockEntity(worldPosition)!=this||!level.getBlockState(worldPosition).is(MachineRegistry.FORGE.get()))return;var s=getBlockState();if(s.hasProperty(ResonanceForgeBlock.LIT)&&s.getValue(ResonanceForgeBlock.LIT)!=active)level.setBlock(worldPosition,s.setValue(ResonanceForgeBlock.LIT,active),3);}
    @Override public int getContainerSize(){return 3;}
    @Override public boolean isEmpty(){settle();return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){settle();return items.get(slot);}
    @Override public ItemStack removeItem(int slot,int amount){settle();var out=ContainerHelper.removeItem(items,slot,amount);setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int slot){settle();return ContainerHelper.takeItem(items,slot);}
    @Override public void setItem(int slot,ItemStack stack){settle();if(slot==0&&!ItemStack.isSameItemSameComponents(items.get(0),stack))progress=0;stack.limitSize(Math.min(64,stack.getMaxStackSize()));items.set(slot,stack);setChanged();}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==0?validInput(stack):slot==1&&validFuel(stack);}
    @Override public int[] getSlotsForFace(Direction face){return face==Direction.DOWN?new int[]{2}:face==Direction.UP?new int[]{0}:new int[]{1};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face){return canPlaceItem(slot,stack)&&(face==Direction.UP?slot==0:face!=Direction.DOWN&&slot==1);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face){return face==Direction.DOWN&&slot==2;}
    @Override public void clearContent(){settle();items.clear();progress=0;setChanged();}
    @Override public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    @Override public Component getDisplayName(){return Component.translatable("block.tnd.resonance_forge");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){settle();return new ResonanceForgeMenu(id,inv,this,data);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){removing=true;if(level!=null&&!level.isClientSide()){settle();Containers.dropContents(level,pos,this);items.clear();}super.preRemoveSideEffects(pos,state);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putLong("lastTick",lastTick);out.putLong("progress",progress);out.putBoolean("paused",paused);var list=out.list("items",ItemStackWithSlot.CODEC);for(int i=0;i<3;i++)if(!items.get(i).isEmpty())list.add(new ItemStackWithSlot(i,items.get(i)));}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);lastTick=in.getLongOr("lastTick",-1);paused=in.getBooleanOr("paused",false);progress=Math.clamp(in.getLongOr("progress",0),0,PERIOD-1);items.clear();in.listOrEmpty("items",ItemStackWithSlot.CODEC).forEach(e->{if(e.isValidInContainer(3)){e.stack().limitSize(Math.min(64,e.stack().getMaxStackSize()));items.set(e.slot(),e.stack());}});}
}
