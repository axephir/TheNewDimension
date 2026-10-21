package fr.axephir.tnd.world;

import fr.axephir.tnd.core.AwakeningContent;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.*;

/** Deterministic geology and staircase; every write is inside the current chunk BB.
 * Building templates remain independent so their authored interiors and saved block entities are preserved.
 */
public final class CrownedSitePiece extends StructurePiece {
    private final StructureTemplate building;
    private final Identifier template;
    private final BlockPos summit;
    private final int base,plateau,radius;
    private final Rotation facing;
    private Rotation inverse(){return switch(facing){case CLOCKWISE_90->Rotation.COUNTERCLOCKWISE_90;case COUNTERCLOCKWISE_90->Rotation.CLOCKWISE_90;default->facing;};}
    private final Map<Long,Integer> path;
    public static int radius(int width,int depth){return (int)Math.ceil(Math.hypot(width/2.,depth/2.))+39;}
    public CrownedSitePiece(StructureTemplateManager manager,Identifier template,BlockPos summit,int base,int minY,Rotation rotation){
        super(CrownedSiteStructure.PIECE.get(),0,bounds(manager.getOrCreate(template),summit,minY));
        this.template=template;this.building=manager.getOrCreate(template);this.summit=summit;this.base=base;this.facing=rotation;
        radius=radius(building.getSize().getX(),building.getSize().getZ());plateau=radius-34;path=makePath();
    }
    public CrownedSitePiece(StructurePieceSerializationContext c,CompoundTag tag){
        super(CrownedSiteStructure.PIECE.get(),tag);
        template=Identifier.parse(tag.getStringOr("SiteTemplate","tnd:outpost"));building=c.structureTemplateManager().getOrCreate(template);
        summit=new BlockPos(tag.getIntOr("SummitX",0),tag.getIntOr("SummitY",100),tag.getIntOr("SummitZ",0));
        base=tag.getIntOr("BaseY",summit.getY()-48);facing=Rotation.valueOf(tag.getStringOr("SiteRotation","NONE"));
        radius=radius(building.getSize().getX(),building.getSize().getZ());plateau=radius-34;path=makePath();
    }
    private static BoundingBox bounds(StructureTemplate t,BlockPos p,int minY){int r=radius(t.getSize().getX(),t.getSize().getZ());
        return new BoundingBox(p.getX()-r,minY,p.getZ()-r,p.getX()+r,p.getY()+Math.max(36,t.getSize().getY())-1,p.getZ()+r);}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag tag){
        tag.putString("SiteTemplate",template.toString());tag.putString("SiteRotation",facing.name());tag.putInt("BaseY",base);
        tag.putInt("SummitX",summit.getX());tag.putInt("SummitY",summit.getY());tag.putInt("SummitZ",summit.getZ());
    }
    public BlockPos buildingOrigin(){return summit.offset(-building.getSize().getX()/2,0,-building.getSize().getZ()/2);}
    public BlockPos summit(){return summit;}
    public Rotation siteRotation(){return facing;}
    public List<BlockPos> ascent(){return path.entrySet().stream().map(e->local(unpackX(e.getKey()),e.getValue(),unpackZ(e.getKey()))).toList();}
    private BlockPos local(int x,int y,int z){BlockPos v=new BlockPos(x,0,z).rotate(facing);return new BlockPos(summit.getX()+v.getX(),y,summit.getZ()+v.getZ());}
    private static long pack(int x,int z){return (long)x<<32|(z&0xffffffffL);}
    private static int unpackX(long k){return (int)(k>>32);}
    private static int unpackZ(long k){return (int)k;}
    private Map<Long,Integer> makePath(){
        Map<Long,Integer> result=new HashMap<>();
        // A generous, five-block-wide curling ascent. The crown has a matching gate.
        int count=radius*4;
        for(int i=0;i<=count;i++){
            double t=i/(double)count,a=-Math.PI/2-1.8*(1-t),r=radius-5-(radius-plateau-8)*t;
            int x=(int)Math.round(Math.cos(a)*r),z=(int)Math.round(Math.sin(a)*r);
            int y=base+(int)Math.floor((summit.getY()-base)*t);
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)result.merge(pack(x+dx,z+dz),y,Math::max);
        }
        for(int z=-plateau-5;z<=-building.getSize().getZ()/2+2;z++)
            for(int x=-2;x<=2;x++)result.put(pack(x,z),summit.getY());
        return Map.copyOf(result);
    }
    private static long mix(int x,int z){long n=x*341873128712L+z*132897987541L;n=(n^(n>>>27))*0x3c79ac492ba7b653L;return n^(n>>>33);}
    private int ground(int x,int z){
        double d=Math.hypot(x,z),a=Math.atan2(z,x);
        double edge=radius-4+Math.sin(a*9)*2.2+Math.cos(a*17)*1.5;
        if(d>edge)return Integer.MIN_VALUE;
        if(d<=plateau)return summit.getY();
        double t=Math.max(0,(edge-d)/(edge-plateau));
        double ridges=Math.pow(Math.abs(Math.sin(a*11+.45)),5)*9*Math.sin(t*Math.PI);
        return base-5+(int)Math.floor((summit.getY()-base+5)*Math.pow(t,.72)+ridges);
    }
    private BlockState mineral(int x,int y,int z){
        long h=mix(x/3+y/4,z/3-y/7);
        boolean vein=Math.floorMod(x+2*z+y/2,29)<2||Math.floorMod(h,37)==0;
        return (vein?AwakeningContent.VEINED_NIGHTSLAG.get():AwakeningContent.NIGHTSLAG.get()).defaultBlockState();
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos reference){
        int x0=Math.max(clip.minX(),boundingBox.minX()),x1=Math.min(clip.maxX(),boundingBox.maxX());
        int z0=Math.max(clip.minZ(),boundingBox.minZ()),z1=Math.min(clip.maxZ(),boundingBox.maxZ());
        var pos=new BlockPos.MutableBlockPos();
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            int dx=x-summit.getX(),dz=z-summit.getZ();var unrotated=new BlockPos(dx,0,dz).rotate(inverse());int top=ground(unrotated.getX(),unrotated.getZ());
            if(top==Integer.MIN_VALUE)continue;
            int natural=world.getHeight(world instanceof net.minecraft.server.level.ServerLevel?Heightmap.Types.WORLD_SURFACE:Heightmap.Types.WORLD_SURFACE_WG,x,z)-1;
            // Fill down into the original ground; no thin shell and no air under a wing.
            for(int y=Math.max(boundingBox.minY(),natural-5);y<=top;y++){
                pos.set(x,y,z);if(clip.isInside(pos))world.setBlock(pos,mineral(dx,y,dz),2);
            }
        }
        // Lower splinters grow out of the flanks; a larger inner ring crowns the summit.
        for(int ring=0;ring<2;ring++)for(int i=0;i<24;i++){
            double a=i*Math.PI/12;
            if(ring==0&&Math.abs(a-3*Math.PI/2)<.16)continue;
            int r=ring==0?plateau+5:plateau+16+(i%3)*3;
            int px=(int)Math.round(Math.cos(a)*r),pz=(int)Math.round(Math.sin(a)*r);
            int start=ground(px,pz)-3, h=ring==0?18+(i*7%15):13+(i*11%19),w=ring==0?4:3;
            for(int dy=0;dy<h;dy++){
                int spread=(int)Math.ceil(w*Math.pow(1-dy/(double)h,1.4));
                int lean=(int)(dy/(double)h*(ring==0?3:5));
                int cx=px+(int)Math.round(Math.cos(a)*lean),cz=pz+(int)Math.round(Math.sin(a)*lean);
                for(int ox=-spread;ox<=spread;ox++)for(int oz=-spread;oz<=spread;oz++){
                    if(Math.abs(ox)+Math.abs(oz)>spread+1)continue;
                    var p=local(cx+ox,start+dy,cz+oz);
                    if(clip.isInside(p)&&boundingBox.isInside(p))world.setBlock(p,mineral(cx+ox,start+dy,cz+oz),2);
                }
            }
        }
        // Pave and clear last, including where the stair cuts through a spur.
        for(var e:path.entrySet()){
            int x=unpackX(e.getKey()),z=unpackZ(e.getKey()),y=e.getValue();
            var p=local(x,y,z);if(!clip.isInside(p))continue;
            BlockState floor=AwakeningContent.DECOR.get("umbra_tiles").get().defaultBlockState();
            for(var d:Direction.Plane.HORIZONTAL)if(path.getOrDefault(pack(x+d.getStepX(),z+d.getStepZ()),y)==y+1){
                var rotated=facing.rotate(d);
                floor=AwakeningContent.STAIRS.get("umbra_tiles").get().defaultBlockState().setValue(StairBlock.FACING,rotated);break;
            }
            world.setBlock(p,floor,2);
            for(int dy=1;dy<=4;dy++)if(clip.isInside(p.above(dy)))world.setBlock(p.above(dy),Blocks.AIR.defaultBlockState(),2);
            for(int dy=1;dy<=3;dy++)if(clip.isInside(p.below(dy)))world.setBlock(p.below(dy),mineral(x,y-dy,z),2);
            if(Math.floorMod(x*7+z*11,37)==0&& !path.containsKey(pack(x-1,z))){var lamp=p.above();if(clip.isInside(lamp))world.setBlock(lamp,AwakeningContent.DECOR.get("violet_lamp").get().defaultBlockState(),2);}
        }
        var settings=new StructurePlaceSettings().setIgnoreEntities(true).setRotation(facing)
                .setRotationPivot(new BlockPos(building.getSize().getX()/2,0,building.getSize().getZ()/2)).setBoundingBox(clip);
        building.placeInWorld(world,buildingOrigin(),reference,settings,random,2);
    }
}
