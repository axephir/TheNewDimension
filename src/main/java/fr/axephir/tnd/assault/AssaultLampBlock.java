package fr.axephir.tnd.assault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
public final class AssaultLampBlock extends Block {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public AssaultLampBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,true));}
    @Override protected void tick(BlockState state,net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){if(!state.getValue(LIT))level.setBlock(pos,state.setValue(LIT,true),3);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT);}
}
