package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class ArchiveDeviceBlock extends Block {
    public static final IntegerProperty STEP=IntegerProperty.create("step",0,3);
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public final boolean relay;
    public ArchiveDeviceBlock(Properties p,boolean relay){super(p);this.relay=relay;registerDefaultState(stateDefinition.any().setValue(STEP,0).setValue(LIT,false));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(STEP,LIT);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p){var site=SanctuaryRoute.nearest(at.getX(),at.getZ());int i=SanctuaryRoute.indexAt(site.localX(at.getX()),site.localZ(at.getZ()));
            if(i>=16&&i<24){var station=SanctuaryRegions.station(site,i);if(w.hasChunkAt(station)&&w.getBlockEntity(station) instanceof SanctuaryNodeEntity n)ArchiveScene.use(n,p,at);}}
        return InteractionResult.SUCCESS;
    }
}
