package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class PortalControllerBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public PortalControllerBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(ACTIVE, false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(ACTIVE); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PortalControllerEntity(pos, state); }
    @Override public void animateTick(BlockState state,Level world,BlockPos pos,net.minecraft.util.RandomSource random){
        if(!state.getValue(ACTIVE))return;
        double time=world.getGameTime()*.09;for(int i=0;i<2;i++){double angle=time+i*Math.PI;
            world.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,pos.getX()+.5+Math.cos(angle)*.7,pos.getY()+1+Math.sin(time*.7)*.4,pos.getZ()+.5+Math.sin(angle)*.7,0,.02,0);
        }
        if(random.nextInt(5)==0)world.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+1.4,pos.getZ()+.5,0,.015,0);
    }
    @Override protected InteractionResult useItemOn(ItemStack item, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof PortalControllerEntity portal) {
            if (!state.getValue(ACTIVE) && item.is(ModContent.COMPOSITE_CRYSTAL.get())) portal.activate(serverPlayer, item);
            else if (state.getValue(ACTIVE)) portal.travel(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof PortalControllerEntity portal) portal.travel(serverPlayer);
        return InteractionResult.SUCCESS;
    }
}
