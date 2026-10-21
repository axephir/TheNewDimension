package fr.axephir.tnd.city;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class CodexMenu extends AbstractContainerMenu {
    private final ContainerData data;private final Player owner;
    public CodexMenu(int id,Inventory inv){this(id,inv,null,0);}
    public CodexMenu(int id,Inventory inv,ServerPlayer owner,int entry){super(CityContent.CODEX_MENU.get(),id);this.owner=owner;
        data=owner==null?new SimpleContainerData(2):new ContainerData(){int selected=Math.clamp(entry,0,7);public int get(int i){return i==0?owner.getData(Codex.NOTES):selected;}public void set(int i,int v){if(i==1)selected=Math.clamp(v,0,7);}public int getCount(){return 2;}};addDataSlots(data);
    }
    public int mask(){return data.get(0);}public int selected(){return data.get(1);}public boolean unlocked(int i){return i>=0&&i<8&&(mask()&(1<<i))!=0;}
    @Override public boolean stillValid(Player p){return p.isAlive()&&(owner==null||p==owner);}@Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int id){
        if(!(p instanceof ServerPlayer sp)||!stillValid(p))return false;
        if(id>=0&&id<8){data.set(1,id);return true;}
        if(id==100&&(sp.getData(Codex.NOTES)&255)==255&&(sp.getData(Codex.NOTES)&256)==0){
            sp.setData(Codex.NOTES,sp.getData(Codex.NOTES)|256);var reward=new ItemStack(CityContent.DECOR.get("nacre_memorial").get());if(!sp.getInventory().add(reward))sp.drop(reward,false);return true;
        }return false;
    }
}
