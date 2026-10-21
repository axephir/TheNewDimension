package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;

/** The manually seeded starter building needs real foundations, too. */
public final class OutpostTerrain {
    /** The first-login outpost uses exactly the same geology as natural sites.
     * Load first, preflight all heights, then place in individual clipped chunks.
     */
    public static java.util.Optional<BlockPos> crowned(ServerLevel level,int x,int z,net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate template){
        int width=template.getSize().getX(),depth=template.getSize().getZ();
        int radius=CrownedSitePiece.radius(width,depth),cx=x+width/2,cz=z+depth/2;
        for(int xx=(cx-radius)>>4;xx<=(cx+radius)>>4;xx++)for(int zz=(cz-radius)>>4;zz<=(cz+radius)>>4;zz++)level.getChunk(xx,zz);
        int highest=level.getMinY()+1;
        for(int ix=-2;ix<=2;ix++)for(int iz=-2;iz<=2;iz++)highest=Math.max(highest,surface(level,cx+ix*radius/2,cz+iz*radius/2));
        int base=surface(level,cx+(int)Math.round(Math.cos(-Math.PI/2-1.8)*(radius-5)),cz+(int)Math.round(Math.sin(-Math.PI/2-1.8)*(radius-5)));
        int peak=Math.max(base+48,highest+26);
        if(peak+Math.max(36,template.getSize().getY())>=level.getMaxY()-2)return java.util.Optional.empty();
        var center=new BlockPos(cx,peak,cz);
        var piece=new CrownedSitePiece(level.getServer().getStructureManager(),net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd","outpost"),center,base,level.getMinY()+1,net.minecraft.world.level.block.Rotation.NONE);
        for(int xx=(cx-radius)>>4;xx<=(cx+radius)>>4;xx++)for(int zz=(cz-radius)>>4;zz<=(cz+radius)>>4;zz++){
            var chunk=new net.minecraft.world.level.ChunkPos(xx,zz);
            var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(chunk.getMinBlockX(),level.getMinY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),level.getMaxY()-1,chunk.getMaxBlockZ());
            piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),level.getRandom(),clip,chunk,center);
        }
        return java.util.Optional.of(piece.buildingOrigin());
    }
    private static int surface(ServerLevel level,int x,int z){
        int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;
        while(y>level.getMinY()+1&&foliage(level,new BlockPos(x,y,z)))y--;
        return y;
    }
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
