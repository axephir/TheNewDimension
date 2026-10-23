package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
public final class SanctuaryRelayBlock extends Block {
    public SanctuaryRelayBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LIT,true));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(BlockStateProperties.LIT);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player player,BlockHitResult hit){if(player instanceof ServerPlayer p){var site=SanctuaryRoute.nearest(pos.getX(),pos.getZ());int index=SanctuaryRoute.indexAt(pos.getX()-site.x(),pos.getZ()-site.z());if(index>=0){var at=SanctuaryRegions.station(site,index);if(w.hasChunkAt(at)&&w.getBlockEntity(at) instanceof SanctuaryNodeEntity node)node.disarm(p,pos);}}return InteractionResult.SUCCESS;}
}
