package fr.axephir.tnd.technology;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class ResonanceForgeBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public ResonanceForgeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ResonanceForgeEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){if(l.isClientSide()||t!=MachineRegistry.FORGE_ENTITY.get())return null;return (world,p,state,e)->{if(world.getGameTime()%20==0)((ResonanceForgeEntity)e).settle();};}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof ResonanceForgeEntity e)player.openMenu(e);return InteractionResult.SUCCESS;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,net.minecraft.util.RandomSource random){if(s.getValue(LIT)&&random.nextInt(3)==0)l.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,p.getX()+.5,p.getY()+1.05,p.getZ()+.5,0,.025,0);}
}
