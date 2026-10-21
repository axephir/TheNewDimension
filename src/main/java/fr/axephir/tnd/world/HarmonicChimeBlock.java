package fr.axephir.tnd.world;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
/** A deliberate, local interaction. No autonomous overlapping sound loops. */
public final class HarmonicChimeBlock extends Block {
    public HarmonicChimeBlock(Properties p){super(p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(l instanceof net.minecraft.server.level.ServerLevel server&&player.getCooldowns().isOnCooldown(asItem().getDefaultInstance())==false){
            player.getCooldowns().addCooldown(asItem().getDefaultInstance(),60);server.playSound(null,p,ModContent.atmosphereSound("ruin_chime"),SoundSource.BLOCKS,.55f,.95f+server.getRandom().nextFloat()*.1f);
            server.sendParticles(ParticleTypes.ENCHANT,p.getX()+.5,p.getY()+1.1,p.getZ()+.5,24,.5,.3,.5,.08);
        }return InteractionResult.SUCCESS;
    }
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){if(r.nextInt(4)==0)l.addParticle(ParticleTypes.END_ROD,p.getX()+r.nextDouble(),p.getY()+1.1,p.getZ()+r.nextDouble(),0,.01,0);}
}
