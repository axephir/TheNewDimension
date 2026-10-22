package fr.axephir.tnd.technology;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class SpatialAnchorBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public SpatialAnchorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SpatialAnchorEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){return w.isClientSide()||t!=SpatialAnchors.ENTITY.get()?null:(l,p,state,e)->{if(l.getGameTime()%20==0)((SpatialAnchorEntity)e).tick();};}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult h){if(w.getBlockEntity(p) instanceof SpatialAnchorEntity be&&!w.isClientSide())player.openMenu(be);return InteractionResult.SUCCESS;}
}
