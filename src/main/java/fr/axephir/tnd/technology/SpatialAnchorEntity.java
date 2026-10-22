package fr.axephir.tnd.technology;

import fr.axephir.tnd.city.CityContent;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;

public final class SpatialAnchorEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY=128000,COST_PER_SECOND=320;
    private UUID owner;private boolean enabled,active;private int status;private long lastCharge=Long.MIN_VALUE;
    public final SimpleEnergyHandler energy=new SimpleEnergyHandler(CAPACITY){@Override protected void onEnergyChanged(int old){setChanged();}};
    public final EnergyHandler port=new EnergyHandler(){public long getAmountAsLong(){return energy.getAmountAsLong();}public long getCapacityAsLong(){return CAPACITY;}public int insert(int n,TransactionContext t){if(n<0)throw new IllegalArgumentException();return energy.insert(Math.min(8000,n),t);}public int extract(int n,TransactionContext t){if(n<0)throw new IllegalArgumentException();return 0;}};
    public SpatialAnchorEntity(BlockPos p,BlockState s){super(SpatialAnchors.ENTITY.get(),p,s);}
    public UUID owner(){return owner;}public boolean active(){return active;}public boolean enabled(){return enabled;}public int status(){return status;}
    public boolean valid(Player p){return p.isAlive()&&level==p.level()&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64;}
    public boolean assembled(){if(level==null)return false;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
        var p=worldPosition.offset(x,-1,z);if((x!=0||z!=0)&&(!level.hasChunkAt(p)||!level.getBlockState(p).is(Industry.FRAME.get())))return false;
        if(x!=0&&z!=0){p=worldPosition.offset(x,0,z);if(!level.hasChunkAt(p)||!level.getBlockState(p).is(CityContent.DECOR.get("relay_coil").get()))return false;}}
        int cx=worldPosition.getX()>>4,cz=worldPosition.getZ()>>4;for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1})if(!level.getWorldBorder().isWithinBounds(new BlockPos((cx+dx)*16,worldPosition.getY(),(cz+dz)*16)))return false;return true;}
    public boolean toggle(ServerPlayer p){
        if(!valid(p)||owner!=null&&!owner.equals(p.getUUID()))return false;
        var ref=GlobalPos.of(level.dimension(),worldPosition);var refs=SpatialAnchors.validate(p);
        if(!enabled){if(!refs.contains(ref)&&refs.size()>=2){status=5;return false;}owner=p.getUUID();if(!refs.contains(ref)){var copy=new ArrayList<>(refs);copy.add(ref);p.setData(SpatialAnchors.OWNED,List.copyOf(copy));}enabled=true;}
        else{enabled=false;release();}
        setChanged();tick();return true;
    }
    public void tick(){
        if(!(level instanceof ServerLevel w))return;var p=owner==null?null:w.getServer().getPlayerList().getPlayer(owner);
        var activeSet=SpatialAnchors.ACTIVE.computeIfAbsent(w.getServer(),s->new HashSet<>());
        status=!enabled?0:!assembled()?1:w.hasNeighborSignal(worldPosition)?2:p==null||!p.getData(SpatialAnchors.OWNED).contains(GlobalPos.of(w.dimension(),worldPosition))?3:energy.getAmountAsInt()<COST_PER_SECOND?4:!active&&activeSet.size()>=fr.axephir.tnd.core.TndConfig.ACTIVE_ANCHORS.get()?5:6;
        if(status!=6){release();light(false);return;}
        long now=w.getGameTime();if(now-lastCharge<20&&lastCharge!=Long.MIN_VALUE)return;
        if(!active){int cx=worldPosition.getX()>>4,cz=worldPosition.getZ()>>4;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)SpatialAnchors.TICKETS.forceChunk(w,worldPosition,cx+x,cz+z,true,false);active=true;activeSet.add(this);}
        energy.set(energy.getAmountAsInt()-COST_PER_SECOND);lastCharge=now;setChanged();light(true);
    }
    private void light(boolean on){if(level!=null&&!isRemoved()&&level.getBlockEntity(worldPosition)==this&&getBlockState().getValue(SpatialAnchorBlock.LIT)!=on)level.setBlock(worldPosition,getBlockState().setValue(SpatialAnchorBlock.LIT,on),3);}
    public void release(){if(level instanceof ServerLevel w&&active){int cx=worldPosition.getX()>>4,cz=worldPosition.getZ()>>4;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)SpatialAnchors.TICKETS.forceChunk(w,worldPosition,cx+x,cz+z,false,false);var set=SpatialAnchors.ACTIVE.get(w.getServer());if(set!=null)set.remove(this);active=false;}}
    public final ContainerData data=new ContainerData(){public int get(int i){return switch(i){case 0->energy.getAmountAsInt()&65535;case 1->energy.getAmountAsInt()>>16;case 2->status;case 3->enabled?1:0;case 4->active?9:0;default->0;};}public void set(int i,int v){}public int getCount(){return 5;}};
    @Override public Component getDisplayName(){return Component.translatable("block.tnd.spatial_anchor");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SpatialAnchorMenu(id,inv,this,data);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){release();super.preRemoveSideEffects(p,s);}
    @Override protected void saveAdditional(ValueOutput o){super.saveAdditional(o);energy.serialize(o);o.putBoolean("Enabled",enabled);if(owner!=null)o.putString("Owner",owner.toString());}
    @Override protected void loadAdditional(ValueInput i){super.loadAdditional(i);energy.set(Math.clamp(i.getIntOr("energy",0),0,CAPACITY));enabled=i.getBooleanOr("Enabled",false);try{owner=UUID.fromString(i.getStringOr("Owner",""));}catch(IllegalArgumentException e){owner=null;enabled=false;}active=false;status=0;lastCharge=Long.MIN_VALUE;}
}
