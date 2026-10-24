package fr.axephir.tnd.deepindustry;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class DeepMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public final DeepIndustry.Kind kind;
    public DeepMachineBlock(Properties p,DeepIndustry.Kind kind){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT,FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DeepMachineEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()||t!=DeepIndustry.ENTITY.get()?null:(w,p,state,be)->{if(w.getGameTime()%5==0)((DeepMachineEntity)be).tickServer();};}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(!w.isClientSide()&&w.getBlockEntity(p) instanceof DeepMachineEntity be)player.openMenu(be);return InteractionResult.SUCCESS;}
    @Override public void animateTick(BlockState s,Level w,BlockPos p,net.minecraft.util.RandomSource r){if(s.getValue(LIT)&&r.nextInt(4)==0)w.addParticle(kind==DeepIndustry.Kind.TURBINE?ParticleTypes.SMOKE:ParticleTypes.ELECTRIC_SPARK,p.getX()+.2+r.nextDouble()*.6,p.getY()+1.02,p.getZ()+.2+r.nextDouble()*.6,0,.025,0);}
    @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
    @Override protected int getAnalogOutputSignal(BlockState s,Level w,BlockPos p,Direction d){return w.getBlockEntity(p) instanceof DeepMachineEntity e?net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil.getRedstoneSignalFromEnergyHandler(e.energy):0;}
}
