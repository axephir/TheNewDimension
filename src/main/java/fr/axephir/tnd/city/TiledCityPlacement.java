package fr.axephir.tnd.city;

import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Native 32x32 subtemplates retain rotation, chest NBT and clipping without scanning all eight towers per chunk. */
public final class TiledCityPlacement {
    public static void place(WorldGenLevel world,BlockPos origin,Vec3i size,Rotation rotation,BlockPos reference,BoundingBox clip,RandomSource random){
        var pivot=new BlockPos(size.getX()/2,0,size.getZ()/2);
        var settings=new StructurePlaceSettings().setIgnoreEntities(true).setRotation(rotation).setRotationPivot(BlockPos.ZERO).setBoundingBox(clip);
        for(int x=0;x<size.getX();x+=32)for(int z=0;z<size.getZ();z+=32){
            var start=StructureTemplate.transform(new BlockPos(x,0,z),Mirror.NONE,rotation,pivot).offset(origin);
            var end=StructureTemplate.transform(new BlockPos(Math.min(x+31,size.getX()-1),size.getY()-1,Math.min(z+31,size.getZ()-1)),Mirror.NONE,rotation,pivot).offset(origin);
            var box=new BoundingBox(Math.min(start.getX(),end.getX()),start.getY(),Math.min(start.getZ(),end.getZ()),Math.max(start.getX(),end.getX()),end.getY(),Math.max(start.getZ(),end.getZ()));
            if(!clip.intersects(box))continue;
            var id=Identifier.fromNamespaceAndPath("tnd","neon_district/part_"+(x/32)+"_"+(z/32));
            var tile=world.getLevel().getStructureManager().get(id).orElseThrow(()->new IllegalStateException("Missing city tile "+id));
            tile.placeInWorld(world,start,reference,settings,random,2);
        }
    }
    private TiledCityPlacement(){}
}
