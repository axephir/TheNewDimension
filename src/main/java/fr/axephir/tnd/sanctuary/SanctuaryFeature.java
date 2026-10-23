package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

import net.minecraft.core.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Authored analytic architecture. Every write is in the decorating chunk; no templates or neighbour reads. */
public final class SanctuaryFeature extends Feature<NoneFeatureConfiguration> {
    public SanctuaryFeature(){super(NoneFeatureConfiguration.CODEC);}
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return generate(c.level(),c.origin());}
    public boolean generate(WorldGenLevel world,BlockPos origin){
        int x0=origin.getX()&~15,z0=origin.getZ()&~15;var site=SanctuaryRoute.nearest(x0+8,z0+8);if(!site.contains(x0+8,z0+8))return false;
        var chunk=world.getChunk(x0>>4,z0>>4);chunk.setData(SanctuaryRegions.GENERATED,true);
        var pos=new BlockPos.MutableBlockPos();
        for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++){
            int lx=x-site.x(),lz=z-site.z();if(!site.contains(x,z))continue;
            int index=SanctuaryRoute.indexAt(lx,lz);int dx=index<0?999:lx-SanctuaryRoute.x(index),dz=index<0?999:lz-SanctuaryRoute.z(index);
            int edge=Math.min(SanctuaryRoute.HALF_X-Math.abs(lx),SanctuaryRoute.HALF_Z-Math.abs(lz));
            // Continuous mineral foundations connect every chamber, crown and approach to terrain.
            boolean hall=index>=0&&Math.abs(dx)<=50&&Math.abs(dz)<=50;
            for(int y=index>=8&&index<16?-30:-9;y<=0;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),
                hall?SanctuaryInterior.sample(dx,y,dz,index,0):stone(y==0?floor(lx,lz,dx,dz,index):"obsidian_metal"),2);
            if(index>=0&&Math.abs(dx)<=50&&Math.abs(dz)<=50){
                int height=fr.axephir.tnd.domain.FoundryAct.contains(index)?fr.axephir.tnd.domain.FoundryAct.height(index):SanctuaryRoute.boss(index)?62:32+(index%3)*7;
                for(int y=1;y<=height;y++){
                    var state=chamber(dx,y,dz,index,height);pos.set(x,SanctuaryRoute.FLOOR+y,z);if(!state.isAir()||!chunk.getBlockState(pos).isAir())world.setBlock(pos,state,2);
                }
                // Stepped external roof fins, anchored buttresses and an axial illuminated crest.
                int rise=Math.max(0,12-Math.abs(dx)/3);
                for(int y=height+1;y<=height+rise;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(Math.abs(dx)<3?"silent_conduit":"obsidian_ribbed"),2);
            }else if(corridor(lx,lz)){
                for(int y=1;y<=10;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),y==10?stone("obsidian_ribbed"):Blocks.AIR.defaultBlockState(),2);
            }
            // A broad entrance avenue joins the first threshold. Luminous joints are usable block light.
            if(lz>=-329&&lz<=-311&&lx<=-498){for(int y=1;y<=9;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),Blocks.AIR.defaultBlockState(),2);}
            // A monumental double-pylon gate marks the arrival before the first great hall.
            int avenueZ=lz+320;
            if(lx>=-578&&lx<=-550&&Math.abs(avenueZ)>=20&&Math.abs(avenueZ)<=29){
                int summit=60-Math.abs(lx+564)/2;for(int y=1;y<=summit;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(y==8||y==40?"luminous_script":"obsidian_pillar"),2);
            }
            if(lx>=-572&&lx<=-556&&Math.abs(avenueZ)<=29)for(int y=48;y<=52;y++)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(y==49?"luminous_script":"obsidian_relief"),2);
            if(edge<70){
                int gx=Math.floorDiv(lx+20,40)*40,gz=Math.floorDiv(lz+20,40)*40;double width=Math.max(Math.abs(lx-gx),Math.abs(lz-gz));
                long h=fr.axephir.tnd.city.CityRegions.mix(gx*71L+gz*131L);int peak=45+Math.floorMod(h,56);
                if(!(lz>=-332&&lz<=-308&&lx<0))for(int y=1;y<=peak;y++)if(width<=10*(1-y/(double)peak)+.3)world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(y%19==0?"luminous_script":"obsidian_metal"),2);
            }
            // One central needle rises between four halls. Its crown is carried by four radial beams.
            // The 27-block base fits the interstitial court and never intersects an authored doorway.
            int needleRadius=Math.max(Math.abs(lx),Math.abs(lz));
            if(needleRadius<=32){
                double radial=Math.hypot(lx,lz);
                for(int y=1;y<=122;y++){
                    int width=y<90?13-y/18:y<113?8:Math.max(1,8-(y-113));
                    if(needleRadius<=width){
                        boolean seam=Math.abs(lx)==2||Math.abs(lz)==2;
                        world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(seam&&y%18<14?"silent_conduit":y%23==0?"luminous_script":"obsidian_metal"),2);
                    }else if(y>=104&&y<=108&&((radial>=27&&radial<=32)||Math.min(Math.abs(lx),Math.abs(lz))<=1)){
                        world.setBlock(pos.set(x,SanctuaryRoute.FLOOR+y,z),stone(y==105?"luminous_script":"obsidian_ribbed"),2);
                    }
                }
            }
        }
        for(int i=0;i<SanctuaryRoute.COUNT;i++){
            var p=SanctuaryRegions.station(site,i);if((p.getX()>>4)!=(x0>>4)||(p.getZ()>>4)!=(z0>>4))continue;
            world.setBlock(p,SanctuaryContent.NODE.get().defaultBlockState(),2);
            if(world.getBlockEntity(p) instanceof SanctuaryNodeEntity node)node.initializeWitness(site,i);
        }
        for(int i=0;i<48;i++)for(int side:new int[]{-1,1}){
            var p=new BlockPos(site.x()+SanctuaryRoute.x(i)+side*35,SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(i)+35);
            if((p.getX()>>4)!=(x0>>4)||(p.getZ()>>4)!=(z0>>4))continue;
            world.setBlock(p,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH),2);
            if(world.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest){chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd","chests/observatory_supplies")));chest.setLootTableSeed(fr.axephir.tnd.city.CityRegions.mix(site.key()+world.getSeed()+i*71L+side));}
        }
        return true;
    }
    private static String floor(int lx,int lz,int dx,int dz,int index){
        if(lz>=-329&&lz<=-311&&lx<=-498)return Math.abs(lz+320)==6||Math.floorMod(lx,12)==0?"guiding_inlay":"obsidian_tiles";
        if(corridor(lx,lz))return (Math.floorMod(lx+lz,8)==0||Math.abs(dz)==3)?"guiding_inlay":"obsidian_tiles";
        if(index<0||Math.abs(dx)>50||Math.abs(dz)>50)return Math.floorMod(lx+lz,29)==0?"silent_conduit":"obsidian_metal";
        if(Math.abs(dx)==47||Math.abs(dz)==47||Math.abs(dx)<2&&Math.floorMod(dz,8)<2)return "guiding_inlay";
        int r=dx*dx+dz*dz;if(r>210&&r<280||r>1100&&r<1180)return index/8%2==0?"amber_seal":"luminous_script";
        return (Math.floorMod(dx,12)==0||Math.floorMod(dz,12)==0)?"obsidian_ribbed":"obsidian_tiles";
    }
    private static BlockState stone(String s){return SanctuaryContent.block(s).defaultBlockState();}
    private static boolean corridor(int lx,int lz){
        int row=Math.floorDiv(lz+384,128);
        if(row>=0&&row<6&&Math.abs(lz-(-320+row*128))<=5&&Math.abs(lx)<=448)return true;
        int turn=Math.floorDiv(lz+320,128);
        return turn>=0&&turn<5&&Math.abs(lx-(turn%2==0?448:-448))<=5;
    }
    static boolean doorway(int dx,int dz,int index,int neighbor){
        if(neighbor<0)return index==0&&dx<=-48&&Math.abs(dz)<=5;
        if(neighbor>=48)return false;
        int vx=Integer.signum(SanctuaryRoute.x(neighbor)-SanctuaryRoute.x(index)),vz=Integer.signum(SanctuaryRoute.z(neighbor)-SanctuaryRoute.z(index));
        return vx!=0&&dx*vx>=48&&Math.abs(dz)<=5||vz!=0&&dz*vz>=48&&Math.abs(dx)<=5;
    }
    public static BlockState chamber(int dx,int y,int dz,int index,int height){
        return SanctuaryInterior.sample(dx,y,dz,index,height);
    }
}
