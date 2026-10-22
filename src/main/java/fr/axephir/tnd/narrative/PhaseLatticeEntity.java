package fr.axephir.tnd.narrative;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class PhaseLatticeEntity extends BlockEntity {
    public PhaseLatticeEntity(BlockPos p,BlockState s){super(ConvergenceContent.LATTICE_ENTITY.get(),p,s);}
}
