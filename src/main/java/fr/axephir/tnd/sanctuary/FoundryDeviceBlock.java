package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

/** Small real mechanisms in monumental architectural housings, animated through four native frames. */
public final class FoundryDeviceBlock extends Block {
    public enum Kind{VALVE,ROTOR,PRESS}
    public static final IntegerProperty STEP=IntegerProperty.create("step",0,3);
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public final Kind kind;
    public FoundryDeviceBlock(Properties p,Kind kind){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(STEP,0).setValue(LIT,false));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(STEP,LIT);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player player,BlockHitResult hit){
        if(kind==Kind.VALVE&&player instanceof ServerPlayer p){
            var site=SanctuaryRoute.nearest(at.getX(),at.getZ());int i=SanctuaryRoute.indexAt(site.localX(at.getX()),site.localZ(at.getZ()));
            if(i>=8&&i<16){var station=SanctuaryRegions.station(site,i);if(w.hasChunkAt(station)&&w.getBlockEntity(station) instanceof SanctuaryNodeEntity node)FoundryScene.use(node,p,at);}
        }return InteractionResult.SUCCESS;
    }
}
