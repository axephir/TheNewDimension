package fr.axephir.tnd.sanctuary;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** One attuned socket. Choices and activation are server-authoritative; no client supplied item data. */
public final class RelicMenu extends AbstractContainerMenu {
    private final Player owner;private final ContainerData data;
    public RelicMenu(int id,Inventory inv){super(ArchiveContent.MENU.get(),id);owner=inv.player;
        data=owner instanceof ServerPlayer p?new ContainerData(){public int get(int i){return switch(i){case 0->p.getData(ArchiveContent.UNLOCKED)?1:0;case 1->p.getData(ArchiveContent.SELECTION);case 2->RelicPowers.remaining(p);default->0;};}public void set(int i,int v){}public int getCount(){return 3;}}:new SimpleContainerData(3);addDataSlots(data);}
    public static void open(ServerPlayer p){p.openMenu(new SimpleMenuProvider((id,inv,player)->new RelicMenu(id,inv),ArchiveScene.tr("relic_title")));}
    public int value(int i){return data.get(i);}
    public boolean stillValid(Player p){return p==owner&&p.isAlive();}
    public ItemStack quickMoveStack(Player p,int n){return ItemStack.EMPTY;}
    public boolean clickMenuButton(Player p,int id){if(!(p instanceof ServerPlayer sp)||!stillValid(p)||!p.getData(ArchiveContent.UNLOCKED))return false;
        if(id>=0&&id<3){if(RelicPowers.remaining(sp)>0)return false;p.setData(ArchiveContent.SELECTION,id);ArchiveScene.sound(sp.level(),sp.blockPosition(),"archive_turn",.35f,1);return true;}
        return id==3&&RelicPowers.activate(sp);
    }
}
