package fr.axephir.tnd.city;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import fr.axephir.tnd.world.DevelopmentCommands;

/** Shared spatial contract for biomes, terrain, city tiles, one laboratory and the shield. No chunk loads. */
public final class CityRegions {
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.neoforged.neoforge.attachment.AttachmentType<?>,net.neoforged.neoforge.attachment.AttachmentType<Boolean>> GENERATED=fr.axephir.tnd.progression.PlayerProgression.TYPES.register("metropolis_chunk",()->net.neoforged.neoforge.attachment.AttachmentType.builder(()->false).serialize(com.mojang.serialization.Codec.BOOL.fieldOf("urban")).build());
    public static void bootstrap(){}
    public static final int CELL=3072,RADIUS=640,FLOOR=88,DOME_HEIGHT=162;
    public record Region(int cellX,int cellZ,int x,int z) {
        public double distance(double px,double pz){return Math.hypot(px-x,pz-z);}
        public BlockPos laboratory(){return new BlockPos(x-23,FLOOR,z-23);}
        public BlockPos computer(){return laboratory().offset(23,2,17);}
        public double roof(double distance){return FLOOR+DOME_HEIGHT*Math.sqrt(Math.max(0,1-distance*distance/(RADIUS*(double)RADIUS)));}
    }
    public static Region region(int cellX,int cellZ){
        long h=mix(cellX*341873128712L+cellZ*132897987541L+0x4e41435245394cL);
        return new Region(cellX,cellZ,cellX*CELL+CELL/2+(Math.floorMod(h,7)-3)*128,
                cellZ*CELL+CELL/2+((int)Math.floorMod(h>>>12,7)-3)*128);
    }
    public static Region nearest(int x,int z){
        int cx=Math.floorDiv(x,CELL),cz=Math.floorDiv(z,CELL);Region best=null;double d=Double.MAX_VALUE;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){var r=region(cx+dx,cz+dz);double n=r.distance(x,z);if(n<d){d=n;best=r;}}
        return best;
    }
    public static boolean contains(int x,int z){return nearest(x,z).distance(x,z)<=RADIUS+2;}
    public static boolean protectedAt(Level level,BlockPos p){if(!level.dimension().equals(DevelopmentCommands.DIMENSION)||!level.hasChunkAt(p))return false;
        // Never freeze an old player base just because a new city grid covers its coordinates.
        if(level.getBiome(p).unwrapKey().map(k->k.identifier().toString().equals("tnd:neon_city")).orElse(false))return true;
        return contains(p.getX(),p.getZ())&&level.getChunkAt(p).getData(GENERATED);}
    public static double blend(int x,int z){return Math.clamp((RADIUS+112-nearest(x,z).distance(x,z))/96,0,1);}
    public static long mix(long n){n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
    private CityRegions(){}
}
