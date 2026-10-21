package fr.axephir.tnd.city;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class PortalMonitorBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public PortalMonitorBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PortalMonitorEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){return w.isClientSide()||t!=CityContent.MONITOR_ENTITY.get()?null:(l,p,state,e)->((PortalMonitorEntity)e).tick();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult h){if(player instanceof net.minecraft.server.level.ServerPlayer sp&&w.getBlockEntity(p) instanceof PortalMonitorEntity e){Codex.collect(sp,0);player.openMenu(e);}return InteractionResult.SUCCESS;}
}
