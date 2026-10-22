package fr.axephir.tnd.narrative;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class PhaseLatticeBlock extends Block implements EntityBlock {
    public PhaseLatticeBlock(Properties p){super(p);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PhaseLatticeEntity(p,s);}
}
