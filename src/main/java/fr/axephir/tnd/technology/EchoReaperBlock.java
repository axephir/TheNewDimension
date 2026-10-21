package fr.axephir.tnd.technology;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
/** Uses native lit/facing state and the same hopper conventions as the forge. */
public final class EchoReaperBlock extends Block implements EntityBlock {
    public EchoReaperBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ResonanceForgeBlock.LIT,false).setValue(ResonanceForgeBlock.FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ResonanceForgeBlock.LIT,ResonanceForgeBlock.FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(ResonanceForgeBlock.FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(ResonanceForgeBlock.FACING,r.rotate(s.getValue(ResonanceForgeBlock.FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(ResonanceForgeBlock.FACING)));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new EchoReaperEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()||t!=MachineRegistry.REAPER_ENTITY.get()?null:(world,p,state,e)->{if(world.getGameTime()%20==0)((EchoReaperEntity)e).work();};}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult h){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof EchoReaperEntity e)player.openMenu(e);return InteractionResult.SUCCESS;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,net.minecraft.util.RandomSource r){if(s.getValue(ResonanceForgeBlock.LIT)){double a=l.getGameTime()*.14;for(int i=0;i<2;i++)l.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX()+.5+Math.cos(a+i*Math.PI)*.6,p.getY()+1.1,p.getZ()+.5+Math.sin(a+i*Math.PI)*.6,0,.015,0);}}
}
