package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class SanctuaryNodeBlock extends Block implements EntityBlock {
    public SanctuaryNodeBlock(Properties p){super(p);}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SanctuaryNodeEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> type){return w.isClientSide()||type!=SanctuaryContent.NODE_ENTITY.get()?null:(l,p,b,e)->{if(l.getGameTime()%20==0)((SanctuaryNodeEntity)e).tick();};}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(!w.isClientSide()&&w.getBlockEntity(p) instanceof SanctuaryNodeEntity node)player.openMenu(node);return InteractionResult.SUCCESS;}
}
