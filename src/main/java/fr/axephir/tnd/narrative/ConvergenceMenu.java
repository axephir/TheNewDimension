package fr.axephir.tnd.narrative;

import fr.axephir.tnd.city.Codex;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ConvergenceMenu extends AbstractContainerMenu {
    private final ServerPlayer owner;private final BlockPos terminal;private final boolean reactor;private final ContainerData data;
    public ConvergenceMenu(int id,Inventory inv){this(id,inv,null,BlockPos.ZERO,false);}
    public ConvergenceMenu(int id,Inventory inv,ServerPlayer owner,BlockPos terminal,boolean reactor){super(ConvergenceContent.MENU.get(),id);this.owner=owner;this.terminal=terminal.immutable();this.reactor=reactor;
        data=owner==null?new SimpleContainerData(7):new ContainerData(){public int get(int i){return switch(i){case 0->owner.getData(Convergence.PHASE);case 1->Convergence.progress(owner);case 2->Integer.bitCount(owner.getData(Codex.NOTES)&255);case 3->reactor?1:0;case 4->reactor?Convergence.readiness(owner,terminal):Convergence.assembled(owner.level(),terminal,false)?0:3;case 5->owner.getData(Convergence.FINISHED)?1:0;case 6->SpatialLensItem.present(owner)?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return 7;}};addDataSlots(data);
    }
    /** Vanilla spectator interaction uses the block menu provider, so both entry paths share discovery bookkeeping. */
    public static ConvergenceMenu create(int id,Inventory inv,ServerPlayer p,BlockPos at,boolean reactor){
        if(Convergence.inEcho(p)&&p.getData(Convergence.PHASE)>=3&&p.getData(Convergence.PHASE)<=4&&at.distSqr(Convergence.echoMachine(p))<4&&p.distanceToSqr(Vec3.atCenterOf(at))<=64)p.setData(Convergence.TRACES,p.getData(Convergence.TRACES)|8);
        return new ConvergenceMenu(id,inv,p,at,reactor);
    }
    public static void open(ServerPlayer p,BlockPos at,boolean reactor){if(Convergence.inEcho(p)&&at.distSqr(Convergence.echoMachine(p))>4)return;p.openMenu(new SimpleMenuProvider((id,inv,player)->create(id,inv,p,at,reactor),Component.translatable("story.tnd."+(reactor?"reactor":"restorer"))));}
    public int phase(){return data.get(0);}public int progress(){return data.get(1);}public int notes(){return data.get(2);}public boolean reactor(){return data.get(3)>0;}public int readiness(){return data.get(4);}public boolean finished(){return data.get(5)>0;}public boolean lensPresent(){return data.get(6)>0;}
    @Override public boolean stillValid(Player p){return p.isAlive()&&(owner==null||p==owner&&(Convergence.inEcho(owner)?terminal.distSqr(Convergence.echoMachine(owner))<4:owner.level().getBlockState(terminal).getBlock() instanceof ConvergenceTerminalBlock&&owner.distanceToSqr(Vec3.atCenterOf(terminal))<=64));}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    public boolean select(ServerPlayer p,int action){if(p!=owner||!stillValid(p))return false;
        if(action==0&&reactor&&!Convergence.inEcho(p))return Convergence.begin(p,terminal,fr.axephir.tnd.core.TndConfig.STORY_RADIUS.get());
        if(action==1&&!reactor&&Convergence.inEcho(p))return Convergence.requestRestoration(p);
        if(action==2){if(phase()==1)Convergence.cancel(p);p.closeContainer();return true;}
        if(action==4&&finished()&&!Convergence.inEcho(p))return SpatialLensItem.reissue(p);
        return false;
    }
}
