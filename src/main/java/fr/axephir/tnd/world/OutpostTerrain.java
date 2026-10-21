package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;

/** The manually seeded starter building needs real foundations, too. */
public final class OutpostTerrain {
    public static BlockPos prepare(ServerLevel level,int x,int z,int width,int depth,int height) {
        // An unvisited chunk reports minY from getHeight: load the footprint first.
        for(int cx=x>>4;cx<=(x+width-1)>>4;cx++)
            for(int cz=z>>4;cz<=(z+depth-1)>>4;cz++)level.getChunk(cx,cz);
        int highest=level.getMinY()+1;
        for(int dx=2;dx<width-2;dx++)for(int dz=2;dz<depth-2;dz++) {
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x+dx,z+dz)-1;
            while(y>level.getMinY()&&foliage(level,new BlockPos(x+dx,y,z+dz)))y--;
            highest=Math.max(highest,y);
        }
        highest=Math.min(highest,level.getMaxY()-height-1);
        for(int dx=2;dx<width-2;dx++)for(int dz=2;dz<depth-2;dz++) {
            for(int y=highest-1;y>Math.max(level.getMinY(),highest-128);y--) {
                BlockPos p=new BlockPos(x+dx,y,z+dz);
                var state=level.getBlockState(p);
                if(!state.isAir()&&state.getFluidState().isEmpty()&&!foliage(level,p))break;
                boolean edge=dx==2||dx==width-3||dz==2||dz==depth-3;
                level.setBlock(p,(edge?ModContent.MOSSY_ASHSTONE_BRICKS.get():ModContent.ASHSTONE.get()).defaultBlockState(),2);
            }
        }
        return new BlockPos(x,highest,z);
    }
    private static boolean foliage(ServerLevel level,BlockPos p) {
        var state=level.getBlockState(p);
        // REPLACEABLE_BY_TREES also contains water (for mangroves). Water is
        // the surface to build above, never foliage to descend through.
        return state.getFluidState().isEmpty() && (state.is(BlockTags.LOGS)
                || state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE_BY_TREES));
    }
    private OutpostTerrain() {}
}
