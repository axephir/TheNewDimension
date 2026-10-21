package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Surface deposits: never erase a structure and never leave the decorated chunk. */
public final class EchoBloomFeature extends Feature<NoneFeatureConfiguration> {
    public EchoBloomFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c) {
        return generate(c.level(),c.origin(),c.random());
    }
    public boolean generate(net.minecraft.world.level.WorldGenLevel level,BlockPos origin,net.minecraft.util.RandomSource random) {
        int x0=origin.getX()&~15,z0=origin.getZ()&~15;
        if(random.nextInt(5)!=0)return false;
        boolean made=false;
        for(int i=0;i<3;i++) {
            int x=x0+2+random.nextInt(12),z=z0+2+random.nextInt(12);
            var pos=new BlockPos(x,level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z),z);
            if(level.ensureCanWrite(pos)&&level.getBlockState(pos).isAir()&&level.getBlockState(pos.below()).is(VestigeLandscapeFeature.SURFACES)) {
                level.setBlock(pos,ModContent.ECHO_BLOOM.get().defaultBlockState().setValue(EchoBloomBlock.CHARGE,random.nextInt(4)),2);made=true;
            }
        }
        return made;
    }
}
