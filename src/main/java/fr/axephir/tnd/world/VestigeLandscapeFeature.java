package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Chunk-local landforms. Preflight the whole volume before modifying any terrain. */
public final class VestigeLandscapeFeature extends Feature<NoneFeatureConfiguration> {
    public static final net.minecraft.tags.TagKey<Block> SURFACES=BlockTags.create(Identifier.fromNamespaceAndPath("tnd","vestige_surfaces"));
    public VestigeLandscapeFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return generate(c.level(),c.origin(),c.random())>0;}
    public int generate(WorldGenLevel world,BlockPos origin,RandomSource random){
        int x0=origin.getX()&~15,z0=origin.getZ()&~15,placed=0;
        for(int attempt=0;attempt<4;attempt++){
            int x=x0+3+random.nextInt(10),z=z0+3+random.nextInt(10);
            var at=new BlockPos(x,world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z),z);
            if(!world.getBlockState(at.below()).is(SURFACES))continue;
            String biome=world.getBiome(at).unwrapKey().map(k->k.identifier().getPath()).orElse("");
            if(biome.equals("glass_woodland")){
                int height=7+random.nextInt(6);
                if(!open(world,at,2,height))continue;
                for(int y=0;y<height;y++)put(world,at.above(y),y<3?ModContent.VITREOUS_STONE.get():ModContent.PALE_CRYSTAL.get());
                for(int side:new int[]{-1,1}){
                    put(world,at.offset(side,3,0),ModContent.VITREOUS_STONE.get());
                    for(int y=3;y<height-2;y++)put(world,at.offset(side*2,y,0),ModContent.PALE_CRYSTAL.get());
                    put(world,at.offset(0,4,side),ModContent.PALE_CRYSTAL.get());
                    for(int y=4;y<height-1;y++)put(world,at.offset(0,y,side*2),ModContent.PALE_CRYSTAL.get());
                }
                placed++;
            }else if(biome.equals("ashen_basin")){
                if(attempt%2==0){if(pool(world,at))placed++;}
                else if(open(world,at,1,5)){
                    int h=2+random.nextInt(3);for(int y=0;y<h;y++)put(world,at.above(y),ModContent.FOSSIL_COLUMN.get());
                    put(world,at.offset(1,h-1,0),ModContent.FOSSIL_COLUMN.get());placed++;
                }
            }else if(biome.equals("obsidian_dunes")){
                if(!open(world,at,2,6))continue;
                for(int dx=-2;dx<=2;dx++)for(int y=world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,at.getX()+dx,at.getZ());y<at.getY()+5-Math.abs(dx);y++)put(world,new BlockPos(at.getX()+dx,y,at.getZ()),ModContent.VITREOUS_STONE.get());
                put(world,at.above(5),ModContent.PALE_CRYSTAL.get());placed++;
            }else if(random.nextInt(8)==0&&open(world,at,1,6)){
                int h=2+random.nextInt(4);for(int y=0;y<h;y++)put(world,at.above(y),ModContent.FOSSIL_COLUMN.get());
                put(world,at.offset(1,0,0),ModContent.VITREOUS_STONE.get());placed++;
            }
        }
        AwakeningOres.generate(world,origin,random);
        return placed;
    }
    private boolean open(WorldGenLevel world,BlockPos at,int radius,int height){
        if(at.getY()+height>=world.getMaxY()||at.getY()<world.getMinY()+4)return false;
        for(var p:BlockPos.betweenClosed(at.offset(-radius,0,-radius),at.offset(radius,height,radius)))
            if(!world.ensureCanWrite(p)||!(world.getBlockState(p).isAir()||p.getY()<at.getY()+2&&world.getBlockState(p).is(SURFACES)))return false;
        for(var p:BlockPos.betweenClosed(at.offset(-radius,-1,-radius),at.offset(radius,-1,radius)))
            {int top=world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,p.getX(),p.getZ());
            if(top>at.getY()+2||top<at.getY()-3||!world.getBlockState(new BlockPos(p.getX(),top-1,p.getZ())).is(SURFACES))return false;}
        return true;
    }
    private boolean pool(WorldGenLevel world,BlockPos at){
        if(at.getY()<world.getMinY()+4||at.getY()+2>=world.getMaxY())return false;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            if(dx*dx+dz*dz>9)continue;
            for(int dy=-3;dy<=1;dy++){
                var p=at.offset(dx,dy,dz);var state=world.getBlockState(p);
                if(!world.ensureCanWrite(p)||!(state.isAir()||state.is(SURFACES)))return false;
                if(dy==-3&&!state.is(SURFACES))return false;
                if(dy==-1&&!state.is(SURFACES))return false;
                if(dy>=0&&!state.isAir())return false;
            }
        }
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            int d=dx*dx+dz*dz;if(d>9)continue;
            put(world,at.offset(dx,-3,dz),ModContent.ASHEN_SILT.get());
            for(int dy=-2;dy<=-1;dy++)put(world,at.offset(dx,dy,dz),d<=4?Blocks.WATER:ModContent.ASHEN_SILT.get());
        }
        return true;
    }
    private static void put(WorldGenLevel world,BlockPos pos,Block block){world.setBlock(pos,block.defaultBlockState(),2);}
}
