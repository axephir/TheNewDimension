package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
/** Only an action and a destination index travel from the client; every operation is checked at the station. */
public final class SanctuaryMenu extends AbstractContainerMenu {
    private final SanctuaryNodeEntity node;private final ContainerData data;
    public SanctuaryMenu(int id,Inventory inv){this(id,inv,null);}
    public SanctuaryMenu(int id,Inventory inv,SanctuaryNodeEntity node){super(SanctuaryContent.MENU.get(),id);this.node=node;data=node==null?new SimpleContainerData(34):new ContainerData(){
        public int get(int n){return switch(n){case 0->node.index();case 1,2,3->(int)(node.mask()>>>((n-1)*16))&65535;case 4->node.active()?1:0;case 5->node.spawned();case 6->node.killed();case 7->node.target();case 8->node.traces();case 9->node.natural()?1:0;case 10->node.sequence();case 11->Integer.bitCount(node.relays());case 12->node.claimState(inv.player);case 13,14,15,16->node.dialTone(n-13)+1;case 17->node.enhanced()?1:0;case 18->WitnessScene.circuit(node);case 19->node.bridgeStage;case 20->Integer.bitCount(node.discoveries(inv.player));case 21->node.circuitLatched?1:0;case 22->node.foundry()?1:0;case 23->FoundryScene.circuit(node);case 24->node.foundryLatched?1:0;case 25->node.foundryCooling/20;case 26->node.foundrySpan;case 27->node.archive()?1:0;case 28->ArchiveScene.circuit(node);case 29->node.archiveEcho;case 30->node.archiveHold/20;case 31->node.archiveSpan;case 32->node.archiveCooling/20;case 33->node.archiveLatched?1:0;default->0;};}public void set(int n,int v){}public int getCount(){return 34;}};addDataSlots(data);}
    public int value(int n){return data.get(n);}public int index(){return Math.clamp(value(0),0,47);}public long mask(){return (value(1)&65535L)|((value(2)&65535L)<<16)|((value(3)&65535L)<<32);}
    public boolean available(){return value(9)==1&&SanctuaryRoute.available(mask(),index());}
    public boolean stillValid(Player p){return p.isAlive()&&(node==null||node.natural()&&node.getLevel()==p.level()&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(node.getBlockPos()))<=64);}
    public ItemStack quickMoveStack(Player p,int n){return ItemStack.EMPTY;}
    public boolean clickMenuButton(Player p,int action){return p instanceof ServerPlayer sp&&node!=null&&stillValid(p)&&node.action(sp,action);}
}
