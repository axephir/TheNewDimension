package fr.axephir.tnd.depths;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class DepthsMenu extends AbstractContainerMenu{
 private final DepthsNodeEntity node;private final ContainerData data;
 public DepthsMenu(int id,Inventory inv){this(id,inv,null);}
 public DepthsMenu(int id,Inventory inv,DepthsNodeEntity n){super(DepthsContent.MENU.get(),id);node=n;data=n==null?new SimpleContainerData(5):new ContainerData(){public int get(int i){return switch(i){case 0->n.index();case 1->n.mask();case 2->n.spawned();case 3->n.killed();case 4->n.active()?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return 5;}};addDataSlots(data);}
 public int value(int n){return data.get(n);}public ItemStack quickMoveStack(Player p,int n){return ItemStack.EMPTY;}
 public boolean stillValid(Player p){return p.isAlive()&&(node==null||node.natural()&&node.getLevel()==p.level()&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(node.getBlockPos()))<64);}
 public boolean clickMenuButton(Player p,int id){return p instanceof ServerPlayer sp&&node!=null&&stillValid(p)&&node.action(sp,id);}
}
