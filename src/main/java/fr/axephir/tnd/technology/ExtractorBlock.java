package fr.axephir.tnd.technology;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ExtractorBlock extends Block implements EntityBlock {
    public ExtractorBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ExtractorBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != MachineRegistry.EXTRACTOR_ENTITY.get()) return null;
        return (world, pos, blockState, entity) -> {
            if (world.getGameTime() % 20 == 0) ((ExtractorBlockEntity) entity).settle();
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer && level.getBlockEntity(pos) instanceof ExtractorBlockEntity machine) {
            machine.tune(player);
            player.openMenu(machine);
        }
        return InteractionResult.SUCCESS;
    }
}
