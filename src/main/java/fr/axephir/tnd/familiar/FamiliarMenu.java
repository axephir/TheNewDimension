package fr.axephir.tnd.familiar;

import fr.axephir.tnd.entities.Shardling;
import fr.axephir.tnd.city.CityRegions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Owner-only menu; no client-supplied entity UUID, stats, food, levels or ownership. */
public final class FamiliarMenu extends AbstractContainerMenu {
    private final ServerPlayer owner;private final ContainerData data;private final int[] snapshot=new int[18];private long updated=Long.MIN_VALUE;
    public FamiliarMenu(int id,Inventory inv){this(id,inv,null);}
    public FamiliarMenu(int id,Inventory inv,ServerPlayer owner){super(FamiliarContent.MENU.get(),id);this.owner=owner;
        data=owner==null?new SimpleContainerData(18):new ContainerData(){public int get(int i){refresh();return snapshot[i];}public void set(int i,int v){}public int getCount(){return 18;}};addDataSlots(data);
    }
    public static void open(ServerPlayer p){if(fr.axephir.tnd.narrative.Convergence.inEcho(p)){fr.axephir.tnd.narrative.ConvergenceMenu.open(p,fr.axephir.tnd.narrative.Convergence.echoMachine(p),false);return;}var ledger=FamiliarLedger.get(p.level().getServer());var pet=ledger.loaded(p.level().getServer(),p.getUUID());if(pet!=null)ledger.snapshot(pet);var r=ledger.record(p.getUUID());
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new FamiliarMenu(id,inv,p),r==null?Component.translatable("familiar.tnd.title"):Component.literal(r.getStringOr("Name",""))));}
    public int value(int i){return data.get(i);}public int entityId(){return value(9)&65535|value(10)<<16;}public int x(){return value(11)&65535|value(12)<<16;}public int z(){return value(13)&65535|value(14)<<16;}
    private void refresh(){long now=owner.level().getGameTime();if(updated==now)return;updated=now;java.util.Arrays.fill(snapshot,0);
        var ledger=FamiliarLedger.get(owner.level().getServer());var pet=ledger.loaded(owner.level().getServer(),owner.getUUID());if(pet!=null)ledger.snapshot(pet);var r=ledger.record(owner.getUUID());
        var city=CityRegions.nearest(owner.getBlockX(),owner.getBlockZ());int x=city.x(),z=city.z();if(r!=null){snapshot[0]=1;snapshot[1]=r.getIntOr("Level",1);snapshot[2]=r.getIntOr("Xp",0);snapshot[3]=r.getIntOr("Health",0);snapshot[4]=r.getIntOr("MaxHealth",240);snapshot[5]=r.getBooleanOr("Stored",false)?4:pet==null?0:r.getBooleanOr("Downed",false)?3:pet.bondTicks()>0?5:r.getBooleanOr("Waiting",false)?2:1;
            int s=Shardling.stage(snapshot[1]);snapshot[6]=s;snapshot[7]=Math.round((float)(3+(snapshot[1]-1)*.35+s*1.5)*10);snapshot[8]=s*3;x=r.getIntOr("X",0);z=r.getIntOr("Z",0);snapshot[15]=r.getStringOr("Dimension","").equals("tnd:vestige")?1:r.getStringOr("Dimension","").equals("minecraft:overworld")?0:2;
            if(pet!=null&&pet.level()==owner.level()){int id=pet.getId();snapshot[9]=id&65535;snapshot[10]=id>>16;snapshot[17]=pet.distanceToSqr(owner)<=36?1:0;}}
        snapshot[11]=x&65535;snapshot[12]=x>>16;snapshot[13]=z&65535;snapshot[14]=z>>16;snapshot[16]=Math.min(32767,owner.getInventory().countItem(FamiliarContent.NUTRIENT.get()));
    }
    @Override public boolean stillValid(Player p){return p.isAlive()&&(owner==null||p==owner);}
    @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int id){
        if(!(p instanceof ServerPlayer sp)||!stillValid(p)||sp!=owner)return false;
        var ledger=FamiliarLedger.get(sp.level().getServer());var pet=ledger.loaded(sp.level().getServer(),sp.getUUID());boolean ok=false;
        if(id==0&&pet!=null)ok=pet.toggleWaiting();
        else if(id==1&&pet!=null)ok=ledger.pack(sp,pet);
        else if(id==2){var r=ledger.record(sp.getUUID());if(r!=null&&r.getBooleanOr("Stored",false)&&sp.getInventory().getFreeSlot()>=0){
            var ticket=ledger.ticket(sp.getUUID());boolean present=false;for(int n=0;n<sp.getInventory().getContainerSize();n++)if(ItemStack.isSameItemSameComponents(ticket,sp.getInventory().getItem(n))){present=true;break;}
            if(!present){r.putString("Ticket",java.util.UUID.randomUUID().toString());ledger.setDirty();ok=sp.getInventory().add(ledger.ticket(sp.getUUID()));}}}
        else if(id==3&&pet!=null&&pet.level()==sp.level()&&pet.distanceToSqr(sp)<=36&&(pet.downed()||pet.getHealth()<pet.getMaxHealth())){
            for(int n=0;n<sp.getInventory().getContainerSize();n++){var stack=sp.getInventory().getItem(n);if(stack.is(FamiliarContent.NUTRIENT.get())){pet.recover();stack.shrink(1);ledger.snapshot(pet);ok=true;break;}}}
        if(ok){updated=Long.MIN_VALUE;broadcastChanges();}return ok;
    }
    public boolean rename(ServerPlayer p,String name){if(owner!=p||!stillValid(p))return false;return FamiliarLedger.get(p.level().getServer()).rename(p,name);}
}
