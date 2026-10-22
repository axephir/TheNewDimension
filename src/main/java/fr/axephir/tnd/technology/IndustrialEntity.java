package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Loaded-time automation only. No free offline production, chunk loading, player XP or fabricated ores. */
public final class IndustrialEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    public final Industry.Kind kind;
    public final SimpleEnergyHandler energy;
    public final EnergyHandler port;
    private final NonNullList<ItemStack> items=NonNullList.withSize(11,ItemStack.EMPTY);
    private int progress,status,burn,drill,cursor,rotation;
    int networkRound;
    private boolean armed;
    private long lastWork=-1;
    // 0 ready, 1 input, 2 output full, 3 redstone, 4 running, 5 FE, 6 structure,
    // 7 no target, 8 night/weather, 9 overworld, 10 arm quarry, 11 exhausted, 12 unloaded.
    public IndustrialEntity(BlockPos pos,BlockState state){
        super(Industry.ENTITY.get(),pos,state);kind=((IndustrialBlock)state.getBlock()).kind;
        energy=new SimpleEnergyHandler(kind.capacity){@Override protected void onEnergyChanged(int old){setChanged();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}};
        port=new EnergyHandler(){
            public long getAmountAsLong(){return energy.getAmountAsLong();}public long getCapacityAsLong(){return kind.capacity;}
            public int insert(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return kind==Industry.Kind.DYNAMO||kind==Industry.Kind.SOLAR?0:energy.insert(Math.min(amount,8000),t);}
            public int extract(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return kind.source()&&!(level!=null&&level.hasNeighborSignal(worldPosition))?energy.extract(Math.min(amount,2560),t):0;}
        };
    }
    public int status(){return status;}public int drillRemaining(){return drill;}public int cursor(){return cursor;}public boolean armed(){return armed;}
    public final ContainerData data=new ContainerData(){
        public int get(int i){return switch(i){case 0->kind.ordinal();case 1->energy.getAmountAsInt()&65535;case 2->energy.getAmountAsInt()>>>16;case 3->progress;case 4->status;case 5->drill;case 6->armed?1:0;case 7->cursor/49;case 8->burn;default->0;};}
        public void set(int i,int v){}public int getCount(){return 9;}
    };
    public boolean toggleQuarry(Player p){if(kind!=Industry.Kind.QUARRY||!stillValid(p))return false;armed=!armed;progress=0;setChanged();return true;}
    public void tickServer(){
        if(!(level instanceof ServerLevel w)||lastWork==w.getGameTime())return;lastWork=w.getGameTime();boolean lit=false;
        if(w.hasNeighborSignal(worldPosition)){status=3;updateLit(false);return;}
        if(kind.source()){
            if(kind==Industry.Kind.DYNAMO){
                int room=kind.capacity-energy.getAmountAsInt();
                if(burn==0&&room>=320&&coal(items.get(0))){items.get(0).shrink(1);burn=25;}
                if(burn>0&&room>=320){energy.set(energy.getAmountAsInt()+320);burn--;lit=true;status=4;}else status=room<320?2:1;
            }else if(kind==Industry.Kind.SOLAR){
                if(w.dimension()!=Level.OVERWORLD)status=9;
                else if(!solarBuilt())status=6;
                else if(!w.isBrightOutside()||w.isRaining()||w.isThundering())status=8;
                else {int made=Math.min(320,kind.capacity-energy.getAmountAsInt());energy.set(energy.getAmountAsInt()+made);lit=made>0;status=lit?4:2;}
            }else status=energy.getAmountAsInt()>0?4:5;
            IndustrialNetwork.distribute(this);updateLit(lit||kind==Industry.Kind.BATTERY&&energy.getAmountAsInt()>0);setChanged();return;
        }
        if((kind==Industry.Kind.QUARRY||kind==Industry.Kind.FARM||kind==Industry.Kind.RANCH||kind==Industry.Kind.PASTURE)&&w.dimension()!=Level.OVERWORLD){status=9;updateLit(false);return;}
        if((kind==Industry.Kind.CRUSHER||kind==Industry.Kind.FURNACE||kind==Industry.Kind.REPAIR)&&!foundation()){status=6;progress=0;updateLit(false);return;}
        if(kind==Industry.Kind.QUARRY){
            if(!armed){status=10;updateLit(false);return;}
            if(!derrickBuilt()){status=6;progress=0;updateLit(false);return;}
            if(drill==0&&!items.get(0).is(Industry.DRILL.get())){status=1;updateLit(false);return;}
        }
        status=7;Job job=switch(kind){case CRUSHER->crushing();case FURNACE->smelting();case QUARRY->quarry(w);case FARM->farming(w);case RANCH->ranching(w);case PASTURE->pasture(w);case REPAIR->repair();default->null;};
        if(job==null){progress=0;updateLit(false);return;}
        if(!fits(job.drops)){status=2;updateLit(false);return;}
        if(energy.getAmountAsInt()<job.cost){status=5;updateLit(false);return;}
        status=4;progress+=10;lit=true;
        if(progress>=job.period){
            progress=0;
            if(job.apply.getAsBoolean()){
                energy.set(energy.getAmountAsInt()-job.cost);insert(job.drops);
                w.playSound(null,worldPosition,SoundEvents.COPPER_BULB_TURN_ON,SoundSource.BLOCKS,.18f,.8f+w.getRandom().nextFloat()*.3f);
                w.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,worldPosition.getX()+.5,worldPosition.getY()+1.05,worldPosition.getZ()+.5,3,.2,.05,.2,.015);
            }
        }
        updateLit(lit);setChanged();
    }
    private Job repair(){
        var input=items.get(0);int restored=fr.axephir.tnd.assault.AssaultEquipment.repair(items.get(1));
        if(!fr.axephir.tnd.assault.AssaultContent.gear(input)||!input.isDamaged()||restored==0){status=1;return null;}
        var result=input.copyWithCount(1);result.setDamageValue(Math.max(0,result.getDamageValue()-restored));
        return new Job(List.of(result),2000,80,()->{items.get(0).shrink(1);items.get(1).shrink(1);return true;});
    }
    private record Job(List<ItemStack> drops,int cost,int period,BooleanSupplier apply){}
    private boolean loaded(BlockPos p){return level.hasChunkAt(p);}
    public boolean foundation(){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if((x!=0||z!=0)&&!frame(worldPosition.offset(x,-1,z)))return false;return true;}
    private boolean frame(BlockPos p){return loaded(p)&&level.getBlockState(p).is(Industry.FRAME.get());}
    public boolean derrickBuilt(){
        if(!frame(worldPosition.above()))return false;
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if((Math.abs(x)==3||Math.abs(z)==3)&&!frame(worldPosition.offset(x,-1,z)))return false;
        for(int x:new int[]{-3,3})for(int z:new int[]{-3,3})for(int y=0;y<3;y++)if(!frame(worldPosition.offset(x,y,z)))return false;return true;
    }
    public boolean solarBuilt(){
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var p=worldPosition.offset(x,0,z);if(!loaded(p)||level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ())>p.getY()+1||(x!=0||z!=0)&&!level.getBlockState(p).is(Industry.PANEL.get()))return false;}return true;
    }
    public static boolean coal(ItemStack s){return s.is(Items.COAL)||s.is(Items.CHARCOAL);}
    public static ItemStack crushed(ItemStack s){return s.is(Items.RAW_IRON)?new ItemStack(Industry.IRON_DUST.get(),2):s.is(Items.RAW_COPPER)?new ItemStack(Industry.COPPER_DUST.get(),2):s.is(Items.RAW_GOLD)?new ItemStack(Industry.GOLD_DUST.get(),2):ItemStack.EMPTY;}
    // An explicit conversion list prevents recipe XP loops and charcoal energy feedback.
    public static ItemStack smelted(ItemStack s){
        if(s.is(Industry.IRON_DUST.get())||s.is(Items.RAW_IRON))return new ItemStack(Items.IRON_INGOT);
        if(s.is(Industry.COPPER_DUST.get())||s.is(Items.RAW_COPPER))return new ItemStack(Items.COPPER_INGOT);
        if(s.is(Industry.GOLD_DUST.get())||s.is(Items.RAW_GOLD))return new ItemStack(Items.GOLD_INGOT);
        if(s.is(AwakeningContent.AURALITE_GRIT.get())||s.is(AwakeningContent.RAW_AURALITE.get()))return new ItemStack(AwakeningContent.AURALITE_INGOT.get());
        if(s.is(AwakeningContent.NOCTILITE_GRIT.get())||s.is(AwakeningContent.RAW_NOCTILITE.get()))return new ItemStack(AwakeningContent.NOCTILITE_CRYSTAL.get());
        if(s.is(Items.SAND)||s.is(Items.RED_SAND))return new ItemStack(Items.GLASS);
        if(s.is(Items.COBBLESTONE))return new ItemStack(Items.STONE);if(s.is(Items.STONE))return new ItemStack(Items.SMOOTH_STONE);
        if(s.is(Items.CLAY_BALL))return new ItemStack(Items.BRICK);if(s.is(Items.NETHERRACK))return new ItemStack(Items.NETHER_BRICK);
        return ItemStack.EMPTY;
    }
    private Job crushing(){var out=crushed(items.get(0));if(out.isEmpty()){status=1;return null;}return new Job(List.of(out),2400,80,()->{items.get(0).shrink(1);return true;});}
    private Job smelting(){var out=smelted(items.get(0));if(out.isEmpty()){status=1;return null;}return new Job(List.of(out),1200,60,()->{items.get(0).shrink(1);return true;});}
    private boolean quarryable(BlockState s,BlockPos p){return !s.hasBlockEntity()&&s.getDestroySpeed(level,p)>=0&&(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)||Set.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.TUFF,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.ROOTED_DIRT,Blocks.GRASS_BLOCK,Blocks.GRAVEL,Blocks.SAND,Blocks.RED_SAND,Blocks.CLAY,Blocks.CALCITE).contains(s.getBlock()));}
    private Job quarry(ServerLevel w){
        int layers=worldPosition.getY()-2-w.getMinY()+1;
        for(int n=0;n<64&&cursor<layers*49;n++){
            var p=worldPosition.offset(cursor%7-3,-2-cursor/49,cursor/7%7-3);
            if(!loaded(p)){status=12;return null;}var state=w.getBlockState(p);
            if(!quarryable(state,p)){cursor++;setChanged();continue;}
            var tool=new ItemStack(Items.DIAMOND_PICKAXE);var drops=Block.getDrops(state,w,p,null,null,tool);
            return new Job(drops,fr.axephir.tnd.progression.ProgressionEvents.isOre(state)?4800:480,20,()->{
                var event=new net.neoforged.neoforge.event.level.block.BreakBlockEvent(w,p,state,net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(w));
                if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).isCanceled()){cursor++;return false;}
                if(!w.setBlock(p,Blocks.AIR.defaultBlockState(),3))return false;
                if(drill==0){items.get(0).shrink(1);drill=1024;}drill--;cursor++;return true;
            });
        }
        if(cursor>=layers*49)status=11;return null;
    }
    private Job farming(ServerLevel w){
        for(int n=0;n<81;n++){int i=Math.floorMod(rotation+n,81);var p=worldPosition.offset(i%9-4,0,i/9-4);if(!loaded(p))continue;var s=w.getBlockState(p);
            if(!(s.getBlock() instanceof CropBlock crop)||!crop.isMaxAge(s))continue;
            var drops=new ArrayList<>(Block.getDrops(s,w,p,null));var seed=new ItemStack(crop.asItem());boolean retained=false;
            // Wheat/beetroot block items are seeds; carrots and potatoes are their own planting material.
            for(var d:drops)if(d.is(seed.getItem())&&!d.isEmpty()){d.shrink(1);retained=true;break;}
            if(!retained)continue;drops.removeIf(ItemStack::isEmpty);
            return new Job(drops,800,40,()->{if(!w.setBlock(p,crop.getStateForAge(0),3))return false;rotation=(i+1)%81;return true;});
        }return null;
    }
    private List<Animal> animals(ServerLevel w){return w.getEntitiesOfClass(Animal.class,new AABB(worldPosition).inflate(5,2,5),a->a.isAlive()&&!a.isRemoved()).stream().sorted(Comparator.comparingInt(Animal::getId)).limit(33).toList();}
    private Job ranching(ServerLevel w){
        var animals=animals(w);if(animals.size()>=16)return null;
        var feed=items.get(0);if(feed.getCount()<2){status=1;return null;}
        for(var a:animals)if(a.getAge()==0&&!a.isInLove()&&a.isFood(feed))for(var b:animals)if(a!=b&&a.getType()==b.getType()&&b.getAge()==0&&!b.isInLove()&&b.isFood(feed)&&a.distanceToSqr(b)<16){
            return new Job(List.of(),1600,100,()->{items.get(0).shrink(2);a.setInLove(null);b.setInLove(null);return true;});
        }return null;
    }
    private Job pasture(ServerLevel w){
        // Collect actual drops (including laid eggs and sheared wool), never invent passive animal products.
        for(var e:w.getEntitiesOfClass(ItemEntity.class,new AABB(worldPosition).inflate(5,2,5),e->e.isAlive()&&!e.hasPickUpDelay())){
            var copy=e.getItem().copy();if(fits(List.of(copy)))return new Job(List.of(copy),120,20,()->{e.discard();return true;});
            status=2;
        }
        var animals=animals(w);
        for(var a:animals)if(a instanceof Sheep sheep&&sheep.readyForShearing()&&items.get(0).is(Items.SHEARS)){
            // Vanilla shearing owns its loot table, color, regrowth and mod hooks; collector picks up the resulting entities.
            if(!fits(List.of(new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(sheep.getColor().getName()+"_wool")),3)))){status=2;return null;}
            return new Job(List.of(),600,40,()->{sheep.shear(w,SoundSource.BLOCKS,items.get(0));var tool=items.get(0);tool.setDamageValue(tool.getDamageValue()+1);if(tool.getDamageValue()>=tool.getMaxDamage())tool.shrink(1);return true;});
        }
        for(var a:animals)if(a.getType()==net.minecraft.world.entity.EntityTypes.COW&&!a.isBaby()&&items.get(1).is(Items.BUCKET)&&w.getGameTime()-a.getPersistentData().getLongOr("tnd_last_milk",-1200)>=1200){
            return new Job(List.of(new ItemStack(Items.MILK_BUCKET)),400,40,()->{items.get(1).shrink(1);a.getPersistentData().putLong("tnd_last_milk",w.getGameTime());return true;});
        }return null;
    }
    private boolean fits(List<ItemStack> drops){var simulated=new ArrayList<ItemStack>();for(int i=2;i<(kind==Industry.Kind.REPAIR?3:11);i++)simulated.add(items.get(i).copy());return merge(simulated,drops);}
    private static boolean merge(List<ItemStack> into,List<ItemStack> drops){
        for(var original:drops){var remaining=original.copy();for(var out:into)if(ItemStack.isSameItemSameComponents(out,remaining)){int moved=Math.min(remaining.getCount(),out.getMaxStackSize()-out.getCount());out.grow(moved);remaining.shrink(moved);}
            for(int i=0;i<into.size()&&!remaining.isEmpty();i++)if(into.get(i).isEmpty()){int moved=Math.min(remaining.getCount(),remaining.getMaxStackSize());into.set(i,remaining.copyWithCount(moved));remaining.shrink(moved);}if(!remaining.isEmpty())return false;
        }return true;
    }
    private void insert(List<ItemStack> drops){var out=new ArrayList<ItemStack>();for(int i=2;i<(kind==Industry.Kind.REPAIR?3:11);i++)out.add(items.get(i).copy());if(!merge(out,drops))throw new IllegalStateException("Industrial output changed during an atomic operation");for(int i=0;i<out.size();i++)items.set(i+2,out.get(i));}
    private void updateLit(boolean active){var s=getBlockState();if(s.getValue(IndustrialBlock.LIT)!=active&&level.getBlockEntity(worldPosition)==this)level.setBlock(worldPosition,s.setValue(IndustrialBlock.LIT,active),3);}
    @Override public boolean canPlaceItem(int slot,ItemStack s){
        if(slot>1)return false;if(slot==1)return kind==Industry.Kind.REPAIR?fr.axephir.tnd.assault.AssaultEquipment.repair(s)>0:kind==Industry.Kind.PASTURE&&s.is(Items.BUCKET);
        return switch(kind){case DYNAMO->coal(s);case CRUSHER->!crushed(s).isEmpty();case FURNACE->!smelted(s).isEmpty();case QUARRY->s.is(Industry.DRILL.get());case RANCH->s.is(Items.WHEAT)||s.is(Items.CARROT)||s.is(Items.POTATO)||s.is(Items.BEETROOT)||s.is(Items.WHEAT_SEEDS)||s.is(Items.BEETROOT_SEEDS)||s.is(Items.MELON_SEEDS)||s.is(Items.PUMPKIN_SEEDS);case PASTURE->s.is(Items.SHEARS);case REPAIR->fr.axephir.tnd.assault.AssaultContent.gear(s)&&s.isDamaged();default->false;};
    }
    @Override public int[] getSlotsForFace(Direction d){return d==Direction.DOWN?(kind==Industry.Kind.REPAIR?new int[]{2}:new int[]{2,3,4,5,6,7,8,9,10}):new int[]{0,1};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack s,Direction d){return d!=Direction.DOWN&&canPlaceItem(slot,s);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack s,Direction d){return d==Direction.DOWN&&slot>=2;}
    @Override public int getContainerSize(){return 11;}@Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int s){return items.get(s);}@Override public ItemStack removeItem(int s,int count){var out=ContainerHelper.removeItem(items,s,count);setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int s){return ContainerHelper.takeItem(items,s);}
    @Override public void setItem(int s,ItemStack stack){stack.limitSize(Math.min(64,stack.getMaxStackSize()));if(s<2&&!ItemStack.isSameItemSameComponents(items.get(s),stack))progress=0;items.set(s,stack);setChanged();}
    @Override public void clearContent(){items.clear();progress=0;setChanged();}
    @Override public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64;}
    @Override public Component getDisplayName(){return Component.translatable("block.tnd."+kind.id);}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new IndustrialMenu(id,inv,this,data);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){if(level!=null&&!level.isClientSide()){Containers.dropContents(level,p,this);items.clear();}super.preRemoveSideEffects(p,s);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);energy.serialize(out);out.putInt("progress",progress);out.putInt("burn",burn);out.putInt("drill",drill);out.putInt("cursor",cursor);out.putInt("rotation",rotation);out.putBoolean("armed",armed);var list=out.list("items",ItemStackWithSlot.CODEC);for(int i=0;i<11;i++)if(!items.get(i).isEmpty())list.add(new ItemStackWithSlot(i,items.get(i)));}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);energy.set(Math.clamp(in.getIntOr("energy",0),0,kind.capacity));progress=Math.clamp(in.getIntOr("progress",0),0,99);burn=Math.clamp(in.getIntOr("burn",0),0,25);drill=Math.clamp(in.getIntOr("drill",0),0,1024);cursor=Math.clamp(in.getIntOr("cursor",0),0,100000);rotation=Math.floorMod(in.getIntOr("rotation",0),81);armed=in.getBooleanOr("armed",false);items.clear();in.listOrEmpty("items",ItemStackWithSlot.CODEC).forEach(e->{if(e.isValidInContainer(11)){e.stack().limitSize(Math.min(64,e.stack().getMaxStackSize()));items.set(e.slot(),e.stack());}});}
}
