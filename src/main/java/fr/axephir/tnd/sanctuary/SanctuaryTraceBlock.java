package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class SanctuaryTraceBlock extends Block {
    public SanctuaryTraceBlock(Properties p){super(p);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(player instanceof ServerPlayer sp){var site=SanctuaryRoute.nearest(p.getX(),p.getZ());int index=SanctuaryRoute.indexAt(p.getX()-site.x(),p.getZ()-site.z());if(index>=0){var at=SanctuaryRegions.station(site,index);if(w.hasChunkAt(at)&&w.getBlockEntity(at) instanceof SanctuaryNodeEntity node)node.trace(sp,p);}}return InteractionResult.SUCCESS;}
}
