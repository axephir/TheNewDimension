package fr.axephir.tnd.sanctuary;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
/** A solid engraved flagstone, not a particle-only light. */
public final class WitnessFilamentBlock extends Block {
    public static final IntegerProperty POWER=IntegerProperty.create("power",0,3);
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public WitnessFilamentBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(POWER,0).setValue(FACING,Direction.EAST));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(POWER,FACING);}
}
