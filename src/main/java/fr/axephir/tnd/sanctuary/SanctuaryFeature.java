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
            for(int y=SanctuaryRoute.FLOOR-9;y<=SanctuaryRoute.FLOOR;y++)world.setBlock(pos.set(x,y,z),stone(y==SanctuaryRoute.FLOOR?floor(lx,lz,dx,dz,index):"obsidian_metal"),2);
            if(index>=0&&Math.abs(dx)<=50&&Math.abs(dz)<=50){
                int height=SanctuaryRoute.boss(index)?62:32+(index%3)*7;
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
            if(world.getBlockEntity(p) instanceof SanctuaryNodeEntity node)node.initialize(site,i);
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
    private static boolean doorway(int dx,int dz,int index,int neighbor){
        if(neighbor<0)return index==0&&dx<=-48&&Math.abs(dz)<=5;
        if(neighbor>=48)return false;
        int vx=Integer.signum(SanctuaryRoute.x(neighbor)-SanctuaryRoute.x(index)),vz=Integer.signum(SanctuaryRoute.z(neighbor)-SanctuaryRoute.z(index));
        return vx!=0&&dx*vx>=48&&Math.abs(dz)<=5||vz!=0&&dz*vz>=48&&Math.abs(dx)<=5;
    }
    public static BlockState chamber(int dx,int y,int dz,int index,int height){
        int ax=Math.abs(dx),az=Math.abs(dz),slot=index%8,act=index/8;
        int vault=height-Math.max(0,(Math.max(ax,az)-22)/3);
        boolean wall=ax>=48||az>=48;
        // Boss halls have chamfered monumental corners instead of another square room.
        if(slot==7&&ax+az>=76){if(y==3&&Math.floorMod(dx-dz,8)==0)return stone("luminous_script");return stone(y==height?"obsidian_ribbed":"obsidian_relief");}
        if(wall){
            if(y<=8&&(doorway(dx,dz,index,index-1)||doorway(dx,dz,index,index+1))){
                boolean entrance=doorway(dx,dz,index,index-1)&&index>0;
                return entrance&&((ax==48&&az<6)||(az==48&&ax<6))?SanctuaryContent.GATE.get().defaultBlockState():Blocks.AIR.defaultBlockState();
            }
            if(y>=vault)return stone(y==vault&&Math.floorMod(dx+dz,12)<2?"luminous_script":"obsidian_ribbed");
            boolean rib=(ax>=48&&Math.floorMod(dz,12)<3)||(az>=48&&Math.floorMod(dx,12)<3);
            if(y==3&&rib||y==15&&Math.floorMod(dx+dz,12)<2)return stone("luminous_script");
            return stone(rib?"obsidian_pillar":y%12==0?"obsidian_relief":"obsidian_tiles");
        }
        if(y>=vault)return stone(y==vault&&(Math.floorMod(dx,12)<2||Math.floorMod(dz,24)==0)?"luminous_script":"obsidian_ribbed");
        // Layered pillars never intrude on the main route or mechanisms.
        if(ax>=31&&ax<=34&&az>=30&&az<=33)return stone(y==3||y==height-3?"observatory_brazier":"obsidian_pillar");
        if(y>=height-4&&(ax==42||az==42))return stone("obsidian_ribbed");
        if(y>=7&&y<=9&&((ax>=37+(9-y)&&az<46&&Math.floorMod(dz,12)<2)||(az>=37+(9-y)&&ax<46&&Math.floorMod(dx,12)<2)))return stone("obsidian_relief");
        // Walkable perimeter galleries, with eight broad stair flights and lit landings.
        if(y==10&&Math.max(ax,az)>=38)return stone((slot==4&&ax>=40&&ax<=44&&Math.abs(az-12)<=2)||Math.floorMod(dx+dz,8)==0?"guiding_inlay":"obsidian_grate");
        if(ax>=40&&ax<=45&&dz>=-29&&dz<=-10&&y<=1+(dz+29)/2){if(y==1+(dz+29)/2&&(dz+29)%2==0)return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);return stone("obsidian_tiles");}
        if(y==11&&Math.max(ax,az)==38&&Math.floorMod(dx+dz,3)==0)return stone("obsidian_relief");
        if((slot==0||slot==5)&&y==1&&((dx==-24&&dz==8)||(dx==24&&dz==8)||(dx==0&&dz==-30)))return SanctuaryContent.TRACE.get().defaultBlockState();
        if(index%8==0||index%8==7){if(dx==12&&dz==18&&y==1)return fr.axephir.tnd.core.ModContent.RETURN_ANCHOR.get().defaultBlockState();if(dx==-12&&dz==18&&y==1)return Blocks.ENDER_CHEST.defaultBlockState();if(dx==-13&&dz==18&&y==1)return Blocks.CRAFTING_TABLE.defaultBlockState();}
        if(slot==3&&y<=2&&Math.abs(dx)==24&&Math.abs(dz)==24)return y==2?SanctuaryContent.RELAY.get().defaultBlockState():stone("obsidian_pillar");
        // Four visible rotating devices: no pixel hunt. Their puzzle state is native block state.
        for(int d=0;slot==2&&d<4;d++)if(dx==-12+d*8&&dz==-14&&y<=2)return y==2?SanctuaryContent.DIAL.get().defaultBlockState():stone("obsidian_pillar");
        // The second flight gives a safe descent from both perimeter galleries.
        if(ax>=40&&ax<=45&&dz>=10&&dz<=29&&y<=1+(29-dz)/2){if(y==1+(29-dz)/2&&(29-dz)%2==0)return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);return stone("obsidian_tiles");}
        // Furnished alcoves beneath the rear galleries, lit at human height.
        if(az>=39&&az<=47&&y<9){
            if(Math.floorMod(dx+42,14)==0&&ax<43)return stone(y==3?"luminous_script":"obsidian_pillar");
            if(az>=45&&Math.floorMod(dx+42,14)>=3&&Math.floorMod(dx+42,14)<=9&&ax<42&&y<=4)return stone(y==4?"memory_lattice":"obsidian_relief");
            if(az==41&&ax<42&&Math.floorMod(dx+42,14)>=4&&Math.floorMod(dx+42,14)<=8&&y==1)return SanctuaryContent.SLAB.get().defaultBlockState();
        }
        // The forecourt has anchored votive monuments, benches and engraved witness plinths.
        if(act==0&&az>=20&&az<=23&&ax>=15&&ax<=30&&Math.floorMod(dx,12)<3&&y<=7+(slot%3)*2)return stone(y==4?"amber_seal":y==7+(slot%3)*2?"luminous_script":"obsidian_pillar");
        if(act==0&&y==1&&az==16&&ax>=18&&ax<=28)return SanctuaryContent.SLAB.get().defaultBlockState();
        // Archive aisles: low stacks, readable crossings and high sealed memory banks.
        if(act==1&&ax>=18&&ax<=29&&Math.floorMod(dx,6)<2&&az<=20&&az>=5&&y<=5)return stone(y%2==0?"memory_lattice":"obsidian_metal");
        // Four restrained brazier basins and rising crystalline mineral columns in the gardens.
        if(act==3&&ax>=18&&ax<=25&&az>=18&&az<=25){int taper=(ax-18)+(az-18);if(y<=18-taper)return stone(y%5==0?"luminous_script":"obsidian_relief");}
        // Foundry chambers contain anchored reactor banks, coolant conduits and suspended service ribs.
        if(act==2&&ax>=20&&ax<=28&&az<=15&&az>=8&&y<=12){if(y==4||y==9)return stone("amber_seal");return stone(ax==20||ax==28?"obsidian_ribbed":"silent_conduit");}
        // Twin pylons frame the tribunal without blocking the central combat floor.
        if(act==4&&ax>=20&&ax<=23&&az>=18&&az<=21&&y<height-6)return stone(y%7==0?"amber_seal":"obsidian_pillar");
        // The final act carries an overhead armillary crown, supported by the perimeter pylons.
        if(act==5&&y>=height-15&&y<=height-13){double r=Math.hypot(dx,dz);if(r>=23&&r<=26)return stone(Math.floorMod(dx+dz,13)==0?"luminous_script":"obsidian_ribbed");}
        if(act==5&&y>=height-13&&y<height&&((ax==24&&az<2)||(az==24&&ax<2)))return stone("silent_conduit");
        // The last chamber holds an immense suspended black lens above the unobstructed arena.
        if(index==47){
            double lens=Math.sqrt(dx*dx+dz*dz+(y-(height-23))*(y-(height-23)));
            if(lens>=8&&lens<=10)return stone(y==height-23||ax<=1?"luminous_script":"obsidian_relief");
            if(y>=height-23&&y<=height-14&&ax<=1&&az<=1)return stone("obsidian_pillar");
            if(y==height-14&&Math.max(ax,az)<=24&&Math.min(ax,az)<=1)return stone("obsidian_ribbed");
        }
        // An original large apparatus differentiates each act and keeps every room furnished.
        if(az>=23&&az<=27&&ax<=21&&Math.floorMod(dx,7)<3&&y<=7+(slot%3)*3){
            if(act==1)return stone(y%3==0?"memory_lattice":"obsidian_relief");
            if(act==2)return stone(y==5?"amber_seal":"obsidian_ribbed");
            if(act==3&&ax%7==0)return stone(y%4==0?"luminous_script":"silent_conduit");
        }
        if((slot==4||slot==7)&&y>=height-17&&y<=height-14){double radius=Math.hypot(dx,dz);if(radius>=14&&radius<=16)return stone((Math.floorMod(dx+dz,9)==0)?"luminous_script":"obsidian_metal");}
        if(act==3&&ax>=20&&ax<=27&&az>=9&&az<=17&&y<=2)return stone(y==2?"luminous_script":"obsidian_relief");
        if(y==2&&((dx==-27&&dz==19)||(dx==27&&dz==19)))return stone("observatory_brazier");
        if(y<=2&&az==35&&ax<=27&&Math.floorMod(dx,8)<3)return stone(y==2?"memory_lattice":"obsidian_relief");
        return Blocks.AIR.defaultBlockState();
    }
}
