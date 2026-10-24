package fr.axephir.tnd.deepindustry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
/** No cross-chunk veins, and exposed ocean nodules rather than a second underground mining lottery. */
public final class DeepOreFeature extends Feature<NoneFeatureConfiguration>{
 private final boolean ocean;public DeepOreFeature(boolean ocean){super(NoneFeatureConfiguration.CODEC);this.ocean=ocean;}
 public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return generate(c.level(),c.origin(),c.random())>0;}
 public int generate(WorldGenLevel w,BlockPos origin,net.minecraft.util.RandomSource r){int x0=origin.getX()&~15,z0=origin.getZ()&~15,n=0;
  if(ocean){for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++){int y=w.getHeight(Heightmap.Types.OCEAN_FLOOR,x,z)-1;if(y>45||y<w.getMinY()+1)continue;var p=new BlockPos(x,y,z);if(!w.getBiome(p).is(net.minecraft.tags.BiomeTags.IS_OCEAN)||!substrate(w,p))continue;boolean water=true;for(int d=1;d<=12;d++)water&=w.getFluidState(p.above(d)).is(FluidTags.WATER);if(water&&r.nextInt(90)==0){w.setBlock(p,DeepIndustry.ORES.get("thalassite_nodule").get().defaultBlockState(),2);n++;}}}
  else for(int attempt=0;attempt<8;attempt++){int cx=x0+r.nextInt(16),cz=z0+r.nextInt(16),cy=Math.max(w.getMinY()+3,-48)+r.nextInt(65);for(int d=0;d<7;d++){int x=cx+r.nextInt(3)-1,z=cz+r.nextInt(3)-1,y=cy+r.nextInt(3)-1;if(x<x0||x>x0+15||z<z0||z>z0+15||y>16||y<-48)continue;var p=new BlockPos(x,y,z);if(!w.ensureCanWrite(p)||!w.getBlockState(p).is(BlockTags.BASE_STONE_OVERWORLD))continue;String id=w.getBlockState(p).is(net.minecraft.world.level.block.Blocks.DEEPSLATE)?"deepslate_ferronyte_ore":"ferronyte_ore";w.setBlock(p,DeepIndustry.ORES.get(id).get().defaultBlockState(),2);n++;}}
  return n;}
 private static boolean substrate(WorldGenLevel w,BlockPos p){var s=w.getBlockState(p);return w.ensureCanWrite(p)&&(s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(net.minecraft.world.level.block.Blocks.GRAVEL)||s.is(net.minecraft.world.level.block.Blocks.SAND)||s.is(net.minecraft.world.level.block.Blocks.CLAY));}
}
