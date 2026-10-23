package fr.axephir.tnd.sanctuary;

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

public final class TemperingForgeBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public TemperingForgeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,FACING);}
    public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TemperingForgeEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){return w.isClientSide()||t!=TemperingContent.ENTITY.get()?null:(l,p,b,e)->{if(l.getGameTime()%5==0)((TemperingForgeEntity)e).tick();};}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(!w.isClientSide()&&w.getBlockEntity(p) instanceof TemperingForgeEntity e)player.openMenu(e);return InteractionResult.SUCCESS;}
    public void animateTick(BlockState s,Level w,BlockPos p,net.minecraft.util.RandomSource random){if(s.getValue(LIT)&&random.nextInt(4)==0)w.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.getX()+.5,p.getY()+1.1,p.getZ()+.5,0,.02,0);}
}
