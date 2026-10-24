package fr.axephir.tnd.depths;
import fr.axephir.tnd.city.CityRegions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DepthsOreBlock extends Block{
 public DepthsOreBlock(Properties p){super(p);}
 protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(player instanceof ServerPlayer sp){var r=CityRegions.nearest(p.getX(),p.getZ());for(int i=1;i<8;i+=2){var node=DepthsLayout.node(r,i);if(p.equals(node.offset(4,0,0))&&w.hasChunkAt(node)&&w.getBlockEntity(node) instanceof DepthsNodeEntity n){n.claim(sp);break;}}}return InteractionResult.SUCCESS;}
}
