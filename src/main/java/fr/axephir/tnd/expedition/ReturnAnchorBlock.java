package fr.axephir.tnd.expedition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ReturnAnchorBlock extends Block implements EntityBlock {
    public ReturnAnchorBlock(Properties p){super(p);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ReturnAnchorEntity(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
        if(p instanceof ServerPlayer server){if(!RecallPrismItem.bind(server,p.getMainHandItem(),pos))p.sendOverlayMessage(Component.translatable("message.tnd.recall_bind_help"));}return InteractionResult.SUCCESS;
    }
    @Override public void animateTick(BlockState s,Level l,BlockPos p,net.minecraft.util.RandomSource r){if(r.nextInt(8)==0)l.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX()+.5,p.getY()+1.1,p.getZ()+.5,0,.02,0);}
}
