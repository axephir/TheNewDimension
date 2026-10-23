package fr.axephir.tnd.sanctuary;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import fr.axephir.tnd.domain.SanctuaryRoute;
public final class WitnessDeviceBlock extends Block {
    public enum Kind{COUPLER,RELIQUARY}
    public static final IntegerProperty TONE=IntegerProperty.create("tone",0,3);
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public final Kind kind;
    public WitnessDeviceBlock(Properties p,Kind kind){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(TONE,0).setValue(LIT,false));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(TONE,LIT);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p){var site=SanctuaryRoute.nearest(at.getX(),at.getZ());int index=SanctuaryRoute.indexAt(at.getX()-site.x(),at.getZ()-site.z());
            if(index>=0&&index<8){var nodeAt=SanctuaryRegions.station(site,index);if(w.hasChunkAt(nodeAt)&&w.getBlockEntity(nodeAt) instanceof SanctuaryNodeEntity node)WitnessScene.use(node,p,at,kind);}}
        return InteractionResult.SUCCESS;
    }
}
