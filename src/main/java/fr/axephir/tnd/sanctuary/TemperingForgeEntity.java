package fr.axephir.tnd.sanctuary;

import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;

/** Deliberate one-time tempering: native item attributes, exact components retained, no offline work. */
public final class TemperingForgeEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    public static final int CAPACITY=128000,COST=80000,PERIOD=200;
    public static final Identifier MODIFIER=Identifier.fromNamespaceAndPath("tnd","foundry_temper");
    private final NonNullList<ItemStack> items=NonNullList.withSize(4,ItemStack.EMPTY);
    public final SimpleEnergyHandler energy=new SimpleEnergyHandler(CAPACITY){protected void onEnergyChanged(int old){setChanged();}};
    public final EnergyHandler port=new EnergyHandler(){
        public long getAmountAsLong(){return energy.getAmountAsLong();}public long getCapacityAsLong(){return CAPACITY;}
        public int insert(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return energy.insert(Math.min(8000,amount),t);}
        public int extract(int amount,TransactionContext t){if(amount<0)throw new IllegalArgumentException();return 0;}
    };
    private int choice=-1,progress;private boolean armed,removing;private long lastWork=-1;
    public TemperingForgeEntity(BlockPos p,BlockState s){super(TemperingContent.ENTITY.get(),p,s);}
    public static boolean eligible(ItemStack s){
        if(s.isEmpty()||s.getCount()!=1||!s.isDamageableItem())return false;
        var attributes=s.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY);
        if(attributes.modifiers().stream().anyMatch(e->e.modifier().is(MODIFIER)))return false;
        return attributes.modifiers().stream().anyMatch(e->e.attribute().equals(Attributes.ATTACK_DAMAGE)&&e.slot().test(EquipmentSlot.MAINHAND));
    }
    public static ItemStack tempered(ItemStack s,int choice){
        if(choice<0||choice>2||!eligible(s))return ItemStack.EMPTY;
        var result=s.copyWithCount(1);var attribute=choice==0?Attributes.ATTACK_DAMAGE:choice==1?Attributes.ATTACK_SPEED:Attributes.KNOCKBACK_RESISTANCE;
        var modifier=new AttributeModifier(MODIFIER,choice==0?.12:choice==1?.10:.15,choice==2?AttributeModifier.Operation.ADD_VALUE:AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).withModifierAdded(attribute,modifier,EquipmentSlotGroup.MAINHAND));
        CustomData.update(DataComponents.CUSTOM_DATA,result,t->t.putInt("tndFoundryTemper",choice));return result;
    }
    public boolean built(){
        if(level==null)return false;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){if(x==0&&z==0)continue;var at=worldPosition.offset(x,-1,z);if(!level.hasChunkAt(at)||!level.getBlockState(at).is(SanctuaryContent.block("obsidian_tiles")))return false;}
        for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var at=worldPosition.offset(x,0,z);if(!level.hasChunkAt(at)||!level.getBlockState(at).is(SanctuaryContent.block("obsidian_pillar"))||!level.getBlockState(at.above()).is(SanctuaryContent.block("amber_seal")))return false;}
        return true;
    }
    public int status(){
        if(level==null||!built())return 1;if(level.hasNeighborSignal(worldPosition))return 2;
        if(!eligible(items.get(0)))return 3;if(choice<0)return 4;
        if(!items.get(1).is(SanctuaryContent.SHARD.get())||items.get(1).getCount()<8||!items.get(2).is(SanctuaryContent.DUST.get())||items.get(2).getCount()<2)return 5;
        if(!items.get(3).isEmpty())return 6;if(energy.getAmountAsInt()<COST)return 7;
        return armed?9:8;
    }
    public final ContainerData data=new ContainerData(){public int get(int n){return switch(n){case 0->choice+1;case 1->progress;case 2->status();case 3->energy.getAmountAsInt()&65535;case 4->energy.getAmountAsInt()>>>16;case 5->armed?1:0;default->0;};}public void set(int n,int v){}public int getCount(){return 6;}};
    public boolean action(Player p,int action){
        if(!p.isAlive()||p.isSpectator()||!stillValid(p))return false;
        if(action>=0&&action<3&&!armed){choice=action;progress=0;setChanged();return true;}
        if(action==4){armed=false;progress=0;setChanged();return true;}
        if(action!=3||status()!=8)return false;armed=true;setChanged();return true;
    }
    public void tick(){
        if(!(level instanceof net.minecraft.server.level.ServerLevel w)||removing||lastWork==w.getGameTime())return;lastWork=w.getGameTime();boolean working=status()==9;
        if(working){progress+=5;if(progress>=PERIOD){
            // Validate at the commit point; inputs, output space and energy are server owned.
            var result=tempered(items.get(0),choice);if(!result.isEmpty()&&status()==9){items.set(3,result);items.set(0,ItemStack.EMPTY);items.get(1).shrink(8);items.get(2).shrink(2);energy.set(energy.getAmountAsInt()-COST);FoundryScene.sound(w,worldPosition,"foundry_temper",.65f,1);w.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,worldPosition.getX()+.5,worldPosition.getY()+1.2,worldPosition.getZ()+.5,18,.4,.3,.4,.035);}progress=0;armed=false;working=false;
        }setChanged();}
        if(w.getBlockEntity(worldPosition)==this&&getBlockState().getValue(TemperingForgeBlock.LIT)!=working)w.setBlock(worldPosition,getBlockState().setValue(TemperingForgeBlock.LIT,working),3);
    }
    public int getContainerSize(){return 4;}public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    public ItemStack getItem(int i){return items.get(i);}
    private void inputChanged(int i){if(i==0){progress=0;armed=false;}setChanged();}
    public ItemStack removeItem(int i,int amount){var item=ContainerHelper.removeItem(items,i,amount);if(!item.isEmpty())inputChanged(i);return item;}
    public ItemStack removeItemNoUpdate(int i){var item=ContainerHelper.takeItem(items,i);if(!item.isEmpty())inputChanged(i);return item;}
    public void setItem(int i,ItemStack s){s.limitSize(i==0||i==3?1:Math.min(64,s.getMaxStackSize()));items.set(i,s);inputChanged(i);}
    public boolean canPlaceItem(int i,ItemStack s){return i==0?eligible(s):i==1?s.is(SanctuaryContent.SHARD.get()):i==2&&s.is(SanctuaryContent.DUST.get());}
    public int[] getSlotsForFace(Direction d){return d==Direction.DOWN?new int[]{3}:d==Direction.UP?new int[]{0}:new int[]{1,2};}
    public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return canPlaceItem(i,s)&&(d==Direction.UP?i==0:d!=Direction.DOWN&&(i==1||i==2));}
    public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return i==3&&d==Direction.DOWN;}
    public void clearContent(){items.clear();armed=false;progress=0;setChanged();}
    public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64;}
    public Component getDisplayName(){return Component.translatable("block.tnd.mnemonic_tempering_forge");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new TemperingMenu(id,inv,this,data);}
    public void preRemoveSideEffects(BlockPos p,BlockState state){removing=true;if(level!=null&&!level.isClientSide()){Containers.dropContents(level,p,this);items.clear();}super.preRemoveSideEffects(p,state);}
    protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("choice",choice);out.putInt("progress",progress);out.putInt("energy",energy.getAmountAsInt());out.putBoolean("armed",armed);var list=out.list("items",ItemStackWithSlot.CODEC);for(int i=0;i<4;i++)if(!items.get(i).isEmpty())list.add(new ItemStackWithSlot(i,items.get(i)));}
    protected void loadAdditional(ValueInput in){super.loadAdditional(in);choice=Math.clamp(in.getIntOr("choice",-1),-1,2);progress=Math.clamp(in.getIntOr("progress",0),0,PERIOD-1);energy.set(Math.clamp(in.getIntOr("energy",0),0,CAPACITY));armed=in.getBooleanOr("armed",false);items.clear();in.listOrEmpty("items",ItemStackWithSlot.CODEC).forEach(e->{if(e.isValidInContainer(4)){e.stack().limitSize(e.slot()==0||e.slot()==3?1:Math.min(64,e.stack().getMaxStackSize()));items.set(e.slot(),e.stack());}});}
}
