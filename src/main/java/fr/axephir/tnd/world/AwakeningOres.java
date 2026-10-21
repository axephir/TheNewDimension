package fr.axephir.tnd.world;
import fr.axephir.tnd.core.AwakeningContent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
/** Small surface veins, entirely within the current chunk; never replaces structures. */
public final class AwakeningOres {
    public static int generate(WorldGenLevel world,BlockPos origin,RandomSource random){
        int count=0,x0=origin.getX()&~15,z0=origin.getZ()&~15;
        for(int attempt=0;attempt<6;attempt++){
            if(random.nextInt(4)!=0)continue;
            int x=x0+2+random.nextInt(12),z=z0+2+random.nextInt(12);
            var at=new BlockPos(x,world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z)-1,z);
            String biome=world.getBiome(at.above()).unwrapKey().map(k->k.identifier().toString()).orElse("");
            if(!biome.startsWith("tnd:"))continue;
            var ore=(biome.endsWith("glass_woodland")||biome.endsWith("ashen_basin")||biome.endsWith("hollow_shelves"))?AwakeningContent.NOCTILITE_ORE:AwakeningContent.AURALITE_ORE;
            for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++){
                var p=at.offset(dx,0,dz);
                if(world.ensureCanWrite(p)&&world.getBlockState(p).is(VestigeLandscapeFeature.SURFACES)&&world.isEmptyBlock(p.above())){world.setBlock(p,ore.get().defaultBlockState(),2);count++;}
            }
        }return count;
    }
    private AwakeningOres(){}
}
