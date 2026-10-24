package fr.axephir.tnd.deepindustry;

import fr.axephir.tnd.technology.Industry;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Real inventories, bounded terrain scans and exact FE commits. Only loaded ticks can make progress. */
public final class DeepMachineEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    public final DeepIndustry.Kind kind;public final SimpleEnergyHandler energy;public final EnergyHandler port;
    private final NonNullList<ItemStack> items=NonNullList.withSize(8,ItemStack.EMPTY);
    private int progress,wear=1000,cursor,burn,recipe,status,round;private boolean armed,removing;private long last=-1;
    // ready / structure / redstone / input / FE / output / service / target / unloaded / arm / dimension / working / protected
    public DeepMachineEntity(BlockPos p,BlockState s){super(DeepIndustry.ENTITY.get(),p,s);kind=((DeepMachineBlock)s.getBlock()).kind;
        energy=new SimpleEnergyHandler(kind.capacity){protected void onEnergyChanged(int old){setChanged();}};
        port=new EnergyHandler(){public long getAmountAsLong(){return energy.getAmountAsLong();}public long getCapacityAsLong(){return kind.capacity;}
            public int insert(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return kind==DeepIndustry.Kind.TURBINE?0:energy.insert(Math.min(32000,amount),t);}
            public int extract(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return kind==DeepIndustry.Kind.TURBINE&&!removing&&(level==null||!level.hasNeighborSignal(worldPosition))?energy.extract(Math.min(16000,amount),t):0;}};
    }
    public int status(){return status;}public int wear(){return wear;}public int cursor(){return cursor;}public boolean armed(){return armed;}public int recipe(){return recipe;}
    public ContainerData data=new ContainerData(){public int get(int i){return switch(i){case 0->kind.ordinal();case 1->energy.getAmountAsInt()&65535;case 2->energy.getAmountAsInt()>>>16;case 3->progress;case 4->status;case 5->wear;case 6->armed?1:0;case 7->recipe;case 8->cursor&65535;case 9->cursor>>>16;case 10->burn;default->0;};}public void set(int n,int v){}public int getCount(){return 11;}};
    public boolean built(){
        if(level==null)return false;int radius=kind==DeepIndustry.Kind.BORE?3:1;
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if((radius==1&&(x!=0||z!=0)||radius==3&&(Math.abs(x)==radius||Math.abs(z)==radius))&&!frame(worldPosition.offset(x,-1,z)))return false;
        if(kind==DeepIndustry.Kind.DREDGER)return true;
        int high=kind==DeepIndustry.Kind.BORE?4:1;
        for(int x:new int[]{-radius,radius})for(int z:new int[]{-radius,radius})for(int y=0;y<=high;y++)if(!frame(worldPosition.offset(x,y,z)))return false;
        if(kind==DeepIndustry.Kind.BORE)for(int x=-3;x<=3;x++)if(!frame(worldPosition.offset(x,5,0)))return false;
        return true;
    }
    private boolean frame(BlockPos p){return level.hasChunkAt(p)&&level.getBlockState(p).is(Industry.FRAME.get());}
    public boolean action(Player p,int id){if(!p.isAlive()||p.isSpectator()||!stillValid(p))return false;
        if(id==0&&(kind==DeepIndustry.Kind.BORE||kind==DeepIndustry.Kind.DREDGER)){armed=!armed;progress=0;setChanged();return true;}
        if(id==1&&!DeepRecipes.recipes(kind).isEmpty()){recipe=(recipe+1)%DeepRecipes.recipes(kind).size();progress=0;setChanged();return true;}
        return false;
    }
    public void tickServer(){
        if(!(level instanceof ServerLevel w)||removing||last==w.getGameTime())return;last=w.getGameTime();
        if(w.hasNeighborSignal(worldPosition)){state(2,false);return;}
        if(!built()){progress=0;state(1,false);return;}
        if(kind==DeepIndustry.Kind.TURBINE){turbine(w);return;}
        if((kind==DeepIndustry.Kind.BORE||kind==DeepIndustry.Kind.DREDGER)&&w.dimension()!=Level.OVERWORLD){state(10,false);return;}
        if((kind==DeepIndustry.Kind.BORE||kind==DeepIndustry.Kind.DREDGER)&&!armed){state(9,false);return;}
        if(kind==DeepIndustry.Kind.DREDGER&&!ocean()){state(10,false);return;}
        Job job=kind==DeepIndustry.Kind.BORE||kind==DeepIndustry.Kind.DREDGER?extraction(w):processing();if(job==null){progress=0;state(status,false);return;}
        if(!fits(job.drops)){state(5,false);return;}if(energy.getAmountAsInt()<job.energy){state(4,false);return;}
        if(wear<job.wear){if(!items.get(2).is(DeepIndustry.item("maintenance_kit"))){state(6,false);return;}items.get(2).shrink(1);wear=1000;setChanged();}
        progress+=5;state(11,true);if(progress<job.ticks){setChanged();return;}
        progress=0;if(job.commit.getAsBoolean()){
            energy.set(energy.getAmountAsInt()-job.energy);wear-=job.wear;insert(job.drops);w.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,worldPosition.getX()+.5,worldPosition.getY()+1.1,worldPosition.getZ()+.5,3,.3,.1,.3,.01);
            if(w.getGameTime()%40<5)w.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("foundry_work"),net.minecraft.sounds.SoundSource.BLOCKS,.2f,1.2f);
        }setChanged();
    }
    private Job processing(){var list=DeepRecipes.recipes(kind);if(list.isEmpty()){status=3;return null;}var r=list.get(Math.floorMod(recipe,list.size()));if(!r.matches(items.get(0),items.get(1))){status=3;return null;}
        return new Job(List.of(new ItemStack(r.result(),r.count())),r.energy(),r.ticks(),r.wear(),()->{if(!r.matches(items.get(0),items.get(1)))return false;items.get(0).shrink(r.a());items.get(1).shrink(r.b());return true;});}
    public boolean ocean(){if(level==null||level.dimension()!=Level.OVERWORLD||worldPosition.getY()>55||!level.getBiome(worldPosition).is(net.minecraft.tags.BiomeTags.IS_OCEAN))return false;for(int y=1;y<=8;y++)if(!level.getFluidState(worldPosition.above(y)).is(FluidTags.WATER))return false;return true;}
    private Job extraction(ServerLevel w){
        var bit=items.get(1);if(!bit.is(DeepIndustry.item("hardened_drill_bit"))||bit.getDamageValue()>=bit.getMaxDamage()){status=3;return null;}
        int size=kind==DeepIndustry.Kind.DREDGER?17:17,total=kind==DeepIndustry.Kind.DREDGER?size*size*3:Math.max(0,worldPosition.getY()-1-w.getMinY())*size*size;
        for(int scan=0;scan<48&&cursor<total;scan++){
            var at=worldPosition.offset(cursor%size-size/2,-2-cursor/(size*size),cursor/size%size-size/2);
            if(!w.hasChunkAt(at)){status=8;return null;}var s=w.getBlockState(at);
            if(kind==DeepIndustry.Kind.DREDGER?!s.is(DeepIndustry.ORES.get("thalassite_nodule").get()):!terrain(s,at)){cursor++;setChanged();continue;}
            var tool=new ItemStack(Items.NETHERITE_PICKAXE);var drops=Block.getDrops(s,w,at,null,null,tool);
            int cost=kind==DeepIndustry.Kind.DREDGER?16000:s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)?12000:900;
            return new Job(drops,cost,kind==DeepIndustry.Kind.DREDGER?60:10,kind==DeepIndustry.Kind.DREDGER?10:2,()->{
                if(!w.getBlockState(at).equals(s))return false;
                var event=new net.neoforged.neoforge.event.level.block.BreakBlockEvent(w,at,s,net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(w));
                if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).isCanceled()){cursor++;status=12;return false;}
                w.setBlock(at,kind==DeepIndustry.Kind.DREDGER?Blocks.GRAVEL.defaultBlockState():Blocks.AIR.defaultBlockState(),3);cursor++;bit.setDamageValue(bit.getDamageValue()+1);if(bit.getDamageValue()>=bit.getMaxDamage())items.set(1,ItemStack.EMPTY);return true;
            });
        }status=7;return null;
    }
    private boolean terrain(BlockState s,BlockPos at){return !s.hasBlockEntity()&&s.getDestroySpeed(level,at)>=0&&(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)||s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(Blocks.DIRT)||s.is(Blocks.GRAVEL)||s.is(Blocks.SAND)||s.is(Blocks.CLAY));}
    private void turbine(ServerLevel w){
        if(burn==0){if(energy.getAmountAsInt()>kind.capacity-640000){state(5,false);distribute();return;}
            if(!items.get(0).is(DeepIndustry.item("resonant_fuel_cell"))||!items.get(1).is(Items.WATER_BUCKET)){state(3,false);distribute();return;}
            var bucket=List.of(new ItemStack(Items.BUCKET));if(!fits(bucket)){state(5,false);distribute();return;}
            if(wear<20){if(!items.get(2).is(DeepIndustry.item("maintenance_kit"))){state(6,false);distribute();return;}items.get(2).shrink(1);wear=1000;}
            items.get(0).shrink(1);items.get(1).shrink(1);insert(bucket);burn=400;wear-=20;
        }
        int made=Math.min(8000,kind.capacity-energy.getAmountAsInt());if(made==8000&&burn>=5){energy.set(energy.getAmountAsInt()+8000);burn-=5;state(11,true);}else state(5,false);distribute();setChanged();
    }
    private void distribute(){var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(worldPosition);seen.add(worldPosition);var ports=new LinkedHashMap<BlockPos,Direction>();int visited=0;
        while(!queue.isEmpty()&&visited++<128){var p=queue.remove();for(var d:Direction.values()){var q=p.relative(d);if(!seen.add(q)||!level.hasChunkAt(q))continue;if(level.getBlockState(q).is(Industry.CABLE.get()))queue.add(q);else if(level.getCapability(Capabilities.Energy.BLOCK,q,d.getOpposite())!=null)ports.put(q,d.getOpposite());}}
        var list=new ArrayList<>(ports.entrySet());int budget=16000;if(list.isEmpty())return;int start=Math.floorMod(round++,list.size());
        for(int i=0;i<list.size()&&budget>0;i++){var p=list.get((start+i)%list.size());if(level.getBlockEntity(p.getKey()) instanceof DeepMachineEntity e&&e.kind==DeepIndustry.Kind.TURBINE)continue;budget-=EnergyHandlerUtil.move(energy,level.getCapability(Capabilities.Energy.BLOCK,p.getKey(),p.getValue()),budget,null);}
    }
    private record Job(List<ItemStack> drops,int energy,int ticks,int wear,BooleanSupplier commit){}
    private boolean fits(List<ItemStack> drops){var copy=new ArrayList<ItemStack>();for(int i=3;i<8;i++)copy.add(items.get(i).copy());return merge(copy,drops);}
    private static boolean merge(List<ItemStack> output,List<ItemStack> drops){for(var raw:drops){var s=raw.copy();for(var d:output)if(ItemStack.isSameItemSameComponents(s,d)){int n=Math.min(s.getCount(),d.getMaxStackSize()-d.getCount());d.grow(n);s.shrink(n);}for(int i=0;i<output.size()&&!s.isEmpty();i++)if(output.get(i).isEmpty()){int n=Math.min(s.getCount(),s.getMaxStackSize());output.set(i,s.copyWithCount(n));s.shrink(n);}if(!s.isEmpty())return false;}return true;}
    private void insert(List<ItemStack> drops){var copy=new ArrayList<ItemStack>();for(int i=3;i<8;i++)copy.add(items.get(i).copy());if(!merge(copy,drops))throw new IllegalStateException("Deep industrial output overflow");for(int i=0;i<5;i++)items.set(i+3,copy.get(i));}
    private void state(int n,boolean lit){status=n;if(level!=null&&level.getBlockEntity(worldPosition)==this&&getBlockState().getValue(DeepMachineBlock.LIT)!=lit)level.setBlock(worldPosition,getBlockState().setValue(DeepMachineBlock.LIT,lit),3);}
    public int getContainerSize(){return 8;}public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}public ItemStack getItem(int i){return items.get(i);}
    public boolean canPlaceItem(int slot,ItemStack s){if(slot==2)return s.is(DeepIndustry.item("maintenance_kit"));if(slot<0||slot>1)return false;
        if(kind==DeepIndustry.Kind.BORE||kind==DeepIndustry.Kind.DREDGER)return slot==1&&s.is(DeepIndustry.item("hardened_drill_bit"));
        if(kind==DeepIndustry.Kind.TURBINE)return s.is(slot==0?DeepIndustry.item("resonant_fuel_cell"):Items.WATER_BUCKET);
        return DeepRecipes.recipes(kind).stream().anyMatch(r->s.is(slot==0?r.first():r.second()));}
    public int[] getSlotsForFace(Direction d){return d==Direction.DOWN?new int[]{3,4,5,6,7}:d==Direction.UP?new int[]{0}:new int[]{1,2};}
    public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return canPlaceItem(i,s)&&(d==Direction.UP?i==0:d!=Direction.DOWN&&(i==1||i==2));}public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return d==Direction.DOWN&&i>=3;}
    private void changed(int slot){if(slot<2)progress=0;setChanged();}public ItemStack removeItem(int i,int n){var s=ContainerHelper.removeItem(items,i,n);if(!s.isEmpty())changed(i);return s;}public ItemStack removeItemNoUpdate(int i){var s=ContainerHelper.takeItem(items,i);if(!s.isEmpty())changed(i);return s;}
    public void setItem(int i,ItemStack s){s.limitSize(Math.min(64,s.getMaxStackSize()));if(i<2&&!ItemStack.isSameItemSameComponents(items.get(i),s))progress=0;items.set(i,s);setChanged();}public void clearContent(){items.clear();progress=0;setChanged();}
    public boolean stillValid(Player p){return p.isAlive()&&level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64;}
    public Component getDisplayName(){return Component.translatable("block.tnd."+kind.id);}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new DeepMachineMenu(id,inv,this,data);}
    public void preRemoveSideEffects(BlockPos p,BlockState s){removing=true;if(level!=null&&!level.isClientSide()){Containers.dropContents(level,p,this);items.clear();}super.preRemoveSideEffects(p,s);}
    protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("energy",energy.getAmountAsInt());out.putInt("progress",progress);out.putInt("wear",wear);out.putInt("cursor",cursor);out.putInt("burn",burn);out.putInt("recipe",recipe);out.putBoolean("armed",armed);var list=out.list("items",ItemStackWithSlot.CODEC);for(int i=0;i<8;i++)if(!items.get(i).isEmpty())list.add(new ItemStackWithSlot(i,items.get(i)));}
    protected void loadAdditional(ValueInput in){super.loadAdditional(in);energy.set(Math.clamp(in.getIntOr("energy",0),0,kind.capacity));progress=Math.clamp(in.getIntOr("progress",0),0,319);wear=Math.clamp(in.getIntOr("wear",1000),0,1000);cursor=Math.clamp(in.getIntOr("cursor",0),0,1000000);burn=Math.clamp(in.getIntOr("burn",0),0,400)/5*5;recipe=Math.clamp(in.getIntOr("recipe",0),0,2);armed=in.getBooleanOr("armed",false);items.clear();in.listOrEmpty("items",ItemStackWithSlot.CODEC).forEach(e->{if(e.isValidInContainer(8)){e.stack().limitSize(Math.min(64,e.stack().getMaxStackSize()));items.set(e.slot(),e.stack());}});}
}
