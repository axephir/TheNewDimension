package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
public final class SanctuaryDialBlock extends Block {
    public static final IntegerProperty TONE=IntegerProperty.create("tone",0,3);
    public SanctuaryDialBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(TONE,0));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(TONE);}
    protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){if(!w.isClientSide()){
        var site=fr.axephir.tnd.domain.SanctuaryRoute.nearest(p.getX(),p.getZ());int index=fr.axephir.tnd.domain.SanctuaryRoute.indexAt(p.getX()-site.x(),p.getZ()-site.z());boolean linked=false;
        if(index>=0&&index%8==2){var nodePos=SanctuaryRegions.station(site,index);if(w.hasChunkAt(nodePos)&&w.getBlockEntity(nodePos) instanceof SanctuaryNodeEntity node&&node.natural()){
            int cx=site.x()+fr.axephir.tnd.domain.SanctuaryRoute.x(index),cz=site.z()+fr.axephir.tnd.domain.SanctuaryRoute.z(index),dial=(p.getX()-cx+12)/8;int[] values=new int[4];var positions=new BlockPos[4];boolean ready=dial>=0&&dial<4;
            for(int n=0;n<4;n++){positions[n]=new BlockPos(cx-12+n*8,fr.axephir.tnd.domain.SanctuaryRoute.FLOOR+2,cz-14);if(!w.hasChunkAt(positions[n])||!w.getBlockState(positions[n]).is(SanctuaryContent.DIAL.get()))ready=false;else values[n]=w.getBlockState(positions[n]).getValue(TONE);}
            if(ready){var next=fr.axephir.tnd.domain.SanctuaryPuzzles.turn(values,index,dial);for(int n=0;n<4;n++)w.setBlock(positions[n],w.getBlockState(positions[n]).setValue(TONE,next[n]),3);linked=true;}
        }}
        if(!linked)w.setBlock(p,s.setValue(TONE,(s.getValue(TONE)+1)%4),3);
        int tone=w.getBlockState(p).getValue(TONE);w.playSound(null,p,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.35f,.65f+tone*.2f);player.sendOverlayMessage(Component.translatable("observatory.tnd.tone",tone+1));
    }return InteractionResult.SUCCESS;}
}
