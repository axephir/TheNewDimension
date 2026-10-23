package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A closed threshold explains its prerequisite without opening a remote station or loading chunks. */
public final class SanctuaryGateBlock extends Block {
    public SanctuaryGateBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && SanctuaryRegions.protectedAt(level, pos)) {
            var site = SanctuaryRoute.nearest(pos.getX(), pos.getZ());
            int index = SanctuaryRoute.indexAt(site.localX(pos.getX()), site.localZ(pos.getZ()));
            if (index > 0) {
                serverPlayer.sendOverlayMessage(Component.translatable("observatory.tnd.gate_wait",
                    index, Component.translatable("observatory.tnd.room." + (index - 1))));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
