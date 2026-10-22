package fr.axephir.tnd.narrative;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
/** Visual anchor only: no inventory, energy, commands, mutable source state or independent narrative ownership. */
public final class ConvergenceTerminalEntity extends BlockEntity {
    public ConvergenceTerminalEntity(BlockPos at,BlockState state){super(ConvergenceContent.TERMINAL_ENTITY.get(),at,state);}
}
