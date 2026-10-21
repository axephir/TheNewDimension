package fr.axephir.tnd.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Restrained mineral glints: no ticking block entities or global particle scans. */
public final class NightslagBlock extends Block {
    public NightslagBlock(Properties p){super(p);}
    @Override public void animateTick(BlockState state,Level level,BlockPos p,RandomSource random){
        if(random.nextInt(256)==0&&level.getBlockState(p.above()).isAir())
            level.addParticle(ParticleTypes.END_ROD,p.getX()+random.nextDouble(),p.getY()+1.05,p.getZ()+random.nextDouble(),0,.006,0);
    }
}
