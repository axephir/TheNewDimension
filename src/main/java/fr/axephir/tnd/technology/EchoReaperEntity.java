package fr.axephir.tnd.technology;
import fr.axephir.tnd.core.*;
import fr.axephir.tnd.world.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Harvests actual mature blocks in a 5x5 bed at the machine's height. No offline simulation. */
public final class EchoReaperEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    private final NonNullList<ItemStack> items=NonNullList.withSize(3,ItemStack.EMPTY);
    private int progress,charges,cursor;
    public EchoReaperEntity(BlockPos p,BlockState s){super(MachineRegistry.REAPER_ENTITY.get(),p,s);}
    public static boolean validFuel(ItemStack s){return s.is(Items.COAL)||s.is(Items.CHARCOAL);}
    private BlockPos target(){
        if(level==null)return null;
        for(int offset=0;offset<25;offset++){int i=(cursor+offset)%25;var p=worldPosition.offset(i%5-2,0,i/5-2);
            if(p.equals(worldPosition)||!level.hasChunk(p.getX()>>4,p.getZ()>>4))continue;
            var s=level.getBlockState(p);if(s.is(ModContent.ECHO_BLOOM.get())&&s.getValue(EchoBloomBlock.CHARGE)==3)return p;
        }return null;
    }
    public int status(){
        if(level==null)return 0;
        if(level.hasNeighborSignal(worldPosition))return 3;
        if(!level.dimension().equals(DevelopmentCommands.DIMENSION))return 5;
        if(!VestigeTime.isNight(level))return 6;
        if(charges==0&&!validFuel(items.get(1)))return 1;
        var out=items.get(2);if(!out.isEmpty()&&(!out.is(ModContent.ECHO_DUST.get())||out.getCount()>=out.getMaxStackSize()))return 2;
        return target()==null?0:4;
    }
    public void work(){
        if(level==null||level.isClientSide())return;
        int state=status();var block=level.getBlockState(worldPosition);
        if(block.is(AwakeningContent.REAPER.get())&&block.getValue(ResonanceForgeBlock.LIT)!=(state==4))level.setBlock(worldPosition,block.setValue(ResonanceForgeBlock.LIT,state==4),3);
        if(state!=4){if(state!=3)progress=0;setChanged();return;}
        progress+=20;if(progress<200){setChanged();return;}
        var p=target();if(p==null){progress=0;return;}
        // Capacity/fuel and the real block are checked before either resource is consumed.
        if(charges==0){items.get(1).shrink(1);charges=8;}
        level.setBlock(p,level.getBlockState(p).setValue(EchoBloomBlock.CHARGE,0),3);charges--;progress=0;
        if(items.get(2).isEmpty())items.set(2,new ItemStack(ModContent.ECHO_DUST.get()));else items.get(2).grow(1);
        cursor=((p.getZ()-worldPosition.getZ()+2)*5+(p.getX()-worldPosition.getX()+2)+1)%25;
        if(level instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(ParticleTypes.ENCHANT,p.getX()+.5,p.getY()+1,p.getZ()+.5,10,.3,.3,.3,.04);
        level.playSound(null,worldPosition,ModContent.atmosphereSound("reaper_harvest"),net.minecraft.sounds.SoundSource.BLOCKS,.35f,1);setChanged();
    }
    public final ContainerData data=new ContainerData(){public int get(int i){return i==0?progress*5:i==1?status():0;}public void set(int i,int v){}public int getCount(){return 2;}};
    @Override public int getContainerSize(){return 3;}
    @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int i){return items.get(i);}
    @Override public ItemStack removeItem(int i,int n){var out=ContainerHelper.removeItem(items,i,n);setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
    @Override public void setItem(int i,ItemStack s){s.limitSize(Math.min(64,s.getMaxStackSize()));items.set(i,s);setChanged();}
    @Override public boolean canPlaceItem(int i,ItemStack s){return i==1&&validFuel(s);}
    @Override public int[] getSlotsForFace(Direction d){return d==Direction.DOWN?new int[]{2}:new int[]{1};}
    @Override public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return d!=Direction.DOWN&&canPlaceItem(i,s);}
    @Override public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return d==Direction.DOWN&&i==2;}
    @Override public void clearContent(){items.clear();setChanged();}
    @Override public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
    @Override public Component getDisplayName(){return Component.translatable("block.tnd.echo_reaper");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new ResonanceForgeMenu(id,inv,this,data,2);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){if(level!=null&&!level.isClientSide()){Containers.dropContents(level,p,this);items.clear();}super.preRemoveSideEffects(p,s);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("Work",progress);out.putInt("FuelUses",charges);out.putInt("Cursor",cursor);var list=out.list("items",ItemStackWithSlot.CODEC);for(int i=0;i<3;i++)if(!items.get(i).isEmpty())list.add(new ItemStackWithSlot(i,items.get(i)));}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);progress=Math.clamp(in.getIntOr("Work",0),0,199);charges=Math.clamp(in.getIntOr("FuelUses",0),0,8);cursor=Math.floorMod(in.getIntOr("Cursor",0),25);items.clear();in.listOrEmpty("items",ItemStackWithSlot.CODEC).forEach(e->{if(e.isValidInContainer(3)){e.stack().limitSize(Math.min(64,e.stack().getMaxStackSize()));items.set(e.slot(),e.stack());}});}
}
