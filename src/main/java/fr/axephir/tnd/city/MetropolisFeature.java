package fr.axephir.tnd.city;

import fr.axephir.tnd.core.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Bounded, chunk-local city construction. Independent of visit order; never searches or writes neighbouring chunks. */
public final class MetropolisFeature extends Feature<NoneFeatureConfiguration> {
    public MetropolisFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return generate(c.level(),c.origin(),c.random());}
    public boolean generate(WorldGenLevel w,BlockPos at,net.minecraft.util.RandomSource random){
        int x0=at.getX()&~15,z0=at.getZ()&~15;var region=CityRegions.nearest(x0+8,z0+8);
        if(region.distance(x0+8,z0+8)>CityRegions.RADIUS+24)return false;
        w.getChunk(x0>>4,z0>>4).setData(CityRegions.GENERATED,true);
        var clip=new BoundingBox(x0,w.getMinY(),z0,x0+15,w.getMaxY()-1,z0+15);
        var metal=AwakeningContent.NIGHTSLAG.get().defaultBlockState();var vein=AwakeningContent.VEINED_NIGHTSLAG.get().defaultBlockState();
        for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++){
            double d=region.distance(x+.5,z+.5);if(d>CityRegions.RADIUS)continue;
            // The noise plateau stops below this street. A dark mineral plinth binds all blocks into one city.
            for(int y=64;y<CityRegions.FLOOR;y++)w.setBlock(new BlockPos(x,y,z),((x+z+y*3)%47==0?vein:metal),2);
            w.setBlock(new BlockPos(x,CityRegions.FLOOR,z),CityContent.DECOR.get("city_asphalt").get().defaultBlockState(),2);
        }
        // 128m blocks of eight furnished towers. Lower neighbourhoods fit wholly below the ellipsoid.
        int px0=Math.floorDiv(x0-region.x()+64,128),pz0=Math.floorDiv(z0-region.z()+64,128);
        for(int px=px0;px<=Math.floorDiv(x0+15-region.x()+64,128);px++)for(int pz=pz0;pz<=Math.floorDiv(z0+15-region.z()+64,128);pz++){
            int cx=region.x()+px*128,cz=region.z()+pz*128;
            double far=Math.hypot(Math.abs(cx-region.x())+91,Math.abs(cz-region.z())+91);
            if(far>CityRegions.RADIUS-12)continue;
            double headroom=region.roof(far)-CityRegions.FLOOR-14;
            int height=headroom>=111?111:headroom>=79?79:headroom>=47?47:0;if(height==0)continue;
            String prefix=height==111?"neon_district":height==79?"neon_midrise":"neon_lowrise";
            var rotation=Rotation.values()[Math.floorMod(CityRegions.mix(cx*31L+cz),4)];
            var origin=new BlockPos(cx-63,CityRegions.FLOOR,cz-63);
            TiledCityPlacement.place(w,origin,new Vec3i(127,height,127),rotation,origin,clip,random,prefix);
        }
        // Exactly one fixed 47m laboratory in the central plaza, regardless of chunk order or seed.
        var lab=region.laboratory();if(clip.intersects(new BoundingBox(lab.getX(),lab.getY(),lab.getZ(),lab.getX()+46,lab.getY()+26,lab.getZ()+46))){
            var template=w.getLevel().getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","incubation_laboratory")).orElseThrow();
            template.placeInWorld(w,lab,lab,new StructurePlaceSettings().setIgnoreEntities(true).setBoundingBox(clip),random,2);
        }
        // A mineral crown surrounds the whole metropolis. All needles are clipped to this chunk and fit under the dome.
        for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++){
            double d=region.distance(x+.5,z+.5);if(d>CityRegions.RADIUS-16||d<CityRegions.RADIUS-130)continue;
            int cx=region.x()+Math.floorDiv(x-region.x()+32,64)*64,cz=region.z()+Math.floorDiv(z-region.z()+32,64)*64;
            double cd=region.distance(cx,cz);if(cd>CityRegions.RADIUS-40||cd<CityRegions.RADIUS-115)continue;
            long hash=CityRegions.mix(cx*17L+cz*97L);int height=Math.min(34+Math.floorMod(hash,18),(int)region.roof(cd+10)-CityRegions.FLOOR-8);
            double width=Math.max(Math.abs(x-cx),Math.abs(z-cz));
            if(width<=7)for(int y=48;y<CityRegions.FLOOR;y++)w.setBlock(new BlockPos(x,y,z),(Math.floorMod(x+y+z,23)==0?vein:metal),2);
            for(int y=0;y<=height;y++)if(width<=6.5*(1-y/(double)Math.max(1,height))+.2)w.setBlock(new BlockPos(x,CityRegions.FLOOR+1+y,z),Math.floorMod(x+z+y,13)==0?vein:metal,2);
            // Walkable, softly lit paths between the spires keep the edge from becoming an empty asphalt band.
            if(width>8&&(Math.floorMod(x-region.x(),32)<3||Math.floorMod(z-region.z(),32)<3))w.setBlock(new BlockPos(x,CityRegions.FLOOR,z),CityContent.DECOR.get("city_tiles").get().defaultBlockState(),2);
        }
        // A contiguous voxel shell. Vertical risers connect neighbouring heights near the rim; all pass through.
        for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++){
            double d=region.distance(x+.5,z+.5);if(d>CityRegions.RADIUS+1)continue;
            int top=(int)Math.floor(region.roof(Math.min(d,CityRegions.RADIUS)));
            int bottom=(int)Math.floor(region.roof(Math.min(CityRegions.RADIUS,d+1.42)));
            for(int y=Math.max(CityRegions.FLOOR,bottom);y<=top&&y<w.getMaxY();y++){
                boolean rib=Math.floorMod(x-region.x(),32)==0||Math.floorMod(z-region.z(),32)==0||y%24==0;
                w.setBlock(new BlockPos(x,y,z),(rib?CityContent.DOME_RIB:CityContent.DOME).get().defaultBlockState(),2);
            }
        }
        fr.axephir.tnd.depths.DepthsArchitecture.generate(w,x0,z0);
        w.getChunk(x0>>4,z0>>4).setData(fr.axephir.tnd.depths.DepthsMigration.GENERATED,true);
        return true;
    }
}
