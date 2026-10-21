package fr.axephir.tnd.city;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.Industry;
import fr.axephir.tnd.world.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;

/** Refundable physical crystal, atomic completion, loaded-time charge and bounded monitor searches. */
public final class PortalMonitorEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    public static final int COST=16000,DURATION=120,RANGE=8;
    public final SimpleEnergyHandler energy=new SimpleEnergyHandler(32000){@Override protected void onEnergyChanged(int old){setChanged();}};
    public final EnergyHandler port=new EnergyHandler(){
        public long getAmountAsLong(){return energy.getAmountAsLong();}public long getCapacityAsLong(){return 32000;}
        public int insert(int n,TransactionContext t){if(n<0)throw new IllegalArgumentException();return energy.insert(Math.min(n,8000),t);}
        public int extract(int n,TransactionContext t){if(n<0)throw new IllegalArgumentException();return 0;}
    };
    private ItemStack crystal=ItemStack.EMPTY;
    private BlockPos bound;
    private int progress;
    private boolean charging,failed;
    private long searchTick=Long.MIN_VALUE;private PortalControllerEntity cachedPortal;
    public PortalMonitorEntity(BlockPos p,BlockState s){super(CityContent.MONITOR_ENTITY.get(),p,s);}
    public int missingFrames(){int n=0;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if((x!=0||z!=0)&&!is(worldPosition.offset(x,-1,z),Industry.FRAME.get()))n++;return n;}
    public int missingCoils(){int n=0;for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})if(!is(worldPosition.offset(x,0,z),CityContent.DECOR.get("relay_coil").get()))n++;return n;}
    private boolean is(BlockPos p,net.minecraft.world.level.block.Block b){return level!=null&&level.hasChunkAt(p)&&level.getBlockState(p).is(b);}
    public boolean assembled(){return missingFrames()==0&&missingCoils()==0;}
    public boolean charging(){return charging;}public int progress(){return progress;}
    public PortalControllerEntity portal(){
        if(level==null)return null;
        if(charging&&bound!=null)return level.hasChunkAt(bound)&&level.getBlockEntity(bound) instanceof PortalControllerEntity p?p:null;
        if(searchTick==level.getGameTime()&&cachedPortal!=null&&!cachedPortal.isRemoved()&&level.getBlockEntity(cachedPortal.getBlockPos())==cachedPortal)return cachedPortal;
        PortalControllerEntity best=null;double distance=RANGE*RANGE+1;
        for(var p:BlockPos.betweenClosed(worldPosition.offset(-RANGE,-3,-RANGE),worldPosition.offset(RANGE,3,RANGE)))
            if(p.distSqr(worldPosition)<=RANGE*RANGE&&level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof PortalControllerEntity portal&&p.distSqr(worldPosition)<distance){distance=p.distSqr(worldPosition);best=portal;}
        searchTick=level.getGameTime();cachedPortal=best;return best;
    }
    public static PortalMonitorEntity findFor(Level w,BlockPos controller){
        PortalMonitorEntity best=null;double distance=RANGE*RANGE+1;
        for(var p:BlockPos.betweenClosed(controller.offset(-RANGE,-3,-RANGE),controller.offset(RANGE,3,RANGE)))
            if(p.distSqr(controller)<=RANGE*RANGE&&w.hasChunkAt(p)&&w.getBlockEntity(p) instanceof PortalMonitorEntity m&&m.assembled()){
                var partner=m.portal();double d=p.distSqr(controller);if(partner!=null&&partner.getBlockPos().equals(controller)&&d<distance){best=m;distance=d;}
            }
        return best;
    }
    public int status(){
        if(level==null||level.dimension()!=Level.OVERWORLD)return 9;
        if(missingFrames()>0)return 1;if(missingCoils()>0)return 2;
        var p=portal();if(p==null)return 3;
        if(p.getBlockState().getValue(PortalControllerBlock.ACTIVE))return 8;
        if(level.hasNeighborSignal(worldPosition))return 11;
        if(!crystal.is(ModContent.COMPOSITE_CRYSTAL.get()))return 4;
        if(energy.getAmountAsInt()<COST)return 5;
        return charging?7:failed?10:6;
    }
    public boolean start(Player p){
        if(!stillValid(p)||(status()!=6&&status()!=10))return false;
        var portal=portal();if(portal==null)return false;
        charging=true;failed=false;progress=0;bound=portal.getBlockPos();portal.charging(true);
        level.playSound(null,worldPosition,ModContent.atmosphereSound("gate_charge"),net.minecraft.sounds.SoundSource.BLOCKS,.75f,1);setChanged();return true;
    }
    public void cancel(){
        if(bound!=null&&level!=null&&level.hasChunkAt(bound)&&level.getBlockEntity(bound) instanceof PortalControllerEntity p)p.charging(false);
        charging=false;progress=0;bound=null;setChanged();
    }
    public void tick(){
        if(!(level instanceof ServerLevel w))return;
        if(charging){
            if(status()!=7){cancel();return;}
            if(++progress>=DURATION){var p=portal();var target=w.getServer().getLevel(DevelopmentCommands.DIMENSION);
                boolean success=p!=null&&target!=null&&p.activateTo(target,crystal);
                failed=!success;cancel();
                if(success){w.playSound(null,worldPosition,ModContent.atmosphereSound("gate_open"),net.minecraft.sounds.SoundSource.BLOCKS,.9f,1);w.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,worldPosition.getX()+.5,worldPosition.getY()+1.2,worldPosition.getZ()+.5,36,.9,.7,.9,.05);}
            }setChanged();
        }
        if(w.getGameTime()%20==0){var s=getBlockState();boolean lit=assembled()&&(charging||status()==8);if(s.getValue(PortalMonitorBlock.LIT)!=lit)w.setBlock(worldPosition,s.setValue(PortalMonitorBlock.LIT,lit),3);}
    }
    public final ContainerData data=new ContainerData(){
        public int get(int i){var p=i>=5&&i<=7?portal():null;return switch(i){case 0->energy.getAmountAsInt();case 1->progress;case 2->status();case 3->missingFrames();case 4->missingCoils();case 5->p==null?0:p.getBlockPos().getX()-worldPosition.getX();case 6->p==null?0:p.getBlockPos().getY()-worldPosition.getY();case 7->p==null?0:p.getBlockPos().getZ()-worldPosition.getZ();default->0;};}
        public void set(int i,int v){}public int getCount(){return 8;}
    };
    @Override public int getContainerSize(){return 1;}@Override public boolean isEmpty(){return crystal.isEmpty();}@Override public ItemStack getItem(int slot){return crystal;}
    @Override public ItemStack removeItem(int s,int n){var out=crystal.split(n);if(charging)cancel();setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int s){var out=crystal;crystal=ItemStack.EMPTY;return out;}
    @Override public void setItem(int s,ItemStack item){if(charging)cancel();item.limitSize(64);crystal=item;failed=false;setChanged();}
    @Override public boolean canPlaceItem(int s,ItemStack item){return s==0&&item.is(ModContent.COMPOSITE_CRYSTAL.get());}
    @Override public int[] getSlotsForFace(Direction face){return new int[]{0};}
    @Override public boolean canPlaceItemThroughFace(int s,ItemStack item,Direction d){return canPlaceItem(s,item);}
    @Override public boolean canTakeItemThroughFace(int s,ItemStack item,Direction d){return !charging;}
    @Override public void clearContent(){cancel();crystal=ItemStack.EMPTY;setChanged();}
    @Override public boolean stillValid(Player p){return level!=null&&p.level()==level&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
    @Override public Component getDisplayName(){return Component.translatable("block.tnd.portal_monitor");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new PortalMonitorMenu(id,inv,this,data);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){if(level!=null&&!level.isClientSide()){cancel();Containers.dropContents(level,p,this);crystal=ItemStack.EMPTY;}super.preRemoveSideEffects(p,s);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);energy.serialize(out);if(!crystal.isEmpty())out.store("crystal",ItemStack.CODEC,crystal);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);energy.set(Math.clamp(in.getIntOr("energy",0),0,32000));crystal=in.read("crystal",ItemStack.CODEC).orElse(ItemStack.EMPTY);crystal.limitSize(64);charging=false;progress=0;bound=null;}
}
