package fr.axephir.tnd.sanctuary;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Authored room sections: carved courts, structural arcades and six distinct inhabited interiors.
 * All samples are local and deterministic, including the foundations; no neighbour chunk reads. */
public final class SanctuaryInterior {
    private static BlockState stone(String name){return SanctuaryContent.block(name).defaultBlockState();}
    private static BlockState air(){return Blocks.AIR.defaultBlockState();}
    private static BlockState stair(Direction facing){return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,facing);}
    private static BlockState slab(boolean top){return SanctuaryContent.SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE,top?SlabType.TOP:SlabType.BOTTOM);}

    /** Sunken lateral courts have four shallow terraced edges, not an inaccessible sheer pit.
     * Surveillance rooms retain their four original floor-level relay approaches. */
    public static int floorLevel(int x,int z,int index){
        int ax=Math.abs(x);
        if(index/8==2&&(ax-24)*(ax-24)+(Math.abs(z)-17)*(Math.abs(z)-17)<=25)return 0;
        if(index%8==7||index%8==3)return 0;
        int inset=Math.min(Math.min(ax-12,30-ax),Math.min(z+34,-16-z));
        return inset<=0?0:-Math.min(4,inset);
    }

    public static BlockState foundation(int x,int y,int z,int index){
        int surface=floorLevel(x,z,index),ax=Math.abs(x),az=Math.abs(z);
        if(y>surface)return air();
        if(y<surface)return stone("obsidian_metal");
        if(surface<0)return stone(surface==-4&&Math.floorMod(x+z,7)<2?"luminous_script":"obsidian_relief");
        if(ax<=2||az<=2)return stone(Math.floorMod(x+z,9)<2?"guiding_inlay":"obsidian_tiles");
        if(ax==11||ax==31||z==-15||z==-35)return stone("obsidian_ribbed");
        int ring=x*x+z*z;
        if((ring>210&&ring<260)||(ring>1020&&ring<1090))return stone(index/8%2==0?"amber_seal":"luminous_script");
        // Broad quiet flagstones around detailed fittings avoid an all-over luminous grid.
        return stone(Math.floorMod(x,9)==0&&Math.floorMod(z,9)==0?"obsidian_relief":"obsidian_tiles");
    }

    public static BlockState sample(int x,int y,int z,int index,int height){
        int ax=Math.abs(x),az=Math.abs(z),slot=index%8,act=index/8;
        int surface=floorLevel(x,z,index);
        var authored=WitnessArchitecture.sample(x,y,z,index,height);if(authored!=null)return authored;
        if(y<=0){
            int inset=Math.min(Math.min(ax-12,30-ax),Math.min(z+34,-16-z));
            // A stair REPLACES its terrace surface. Placing it one block above the
            // surface makes a 1.5-block first step out of the lowest court.
            boolean court=slot!=7&&slot!=3&&inset>=0&&inset<4;
            boolean reactor=act==2&&(ax-24)*(ax-24)+(az-17)*(az-17)<=25;
            if(court&&!reactor&&y==surface){
                Direction up=inset==ax-12?(x>0?Direction.WEST:Direction.EAST):inset==30-ax?(x>0?Direction.EAST:Direction.WEST):inset==z+34?Direction.NORTH:Direction.SOUTH;
                return stair(up);
            }
            return foundation(x,y,z,index);
        }
        // Elliptical barrel vault with deep transverse ribs and recessed coffer panels.
        int vault=height-Math.max(0,(Math.max(ax,az)-14)*(Math.max(ax,az)-14)/88);
        boolean door=SanctuaryFeature.doorway(x,z,index,index-1)||SanctuaryFeature.doorway(x,z,index,index+1);
        if(ax>=48||az>=48){
            if(y<=8&&door){
                boolean entry=SanctuaryFeature.doorway(x,z,index,index-1)&&index>0;
                return entry&&((ax==48&&az<=5)||(az==48&&ax<=5))?SanctuaryContent.GATE.get().defaultBlockState():air();
            }
            return stone(y>=vault?"obsidian_ribbed":y==3||y==13?"obsidian_relief":"obsidian_metal");
        }
        // Chamfered walls terminate in stepped buttresses; every room loses its square corners.
        int chamfer=ax+az;
        if(chamfer>=83)return stone(chamfer==83&&y%11==3?"luminous_script":"obsidian_relief");
        if(y>=vault){
            boolean rib=Math.floorMod(z+2,16)<=3||Math.floorMod(x+1,24)<=2;
            if(y==vault&&rib)return stone("obsidian_relief");
            return stone(y==vault+1&&Math.floorMod(z+2,16)==4?"luminous_script":"obsidian_ribbed");
        }
        // Native objectives keep their established coordinates and usable surrounding floor.
        if((slot==0||slot==5)&&y==1&&((x==-24&&z==8)||(x==24&&z==8)||(x==0&&z==-30)))return SanctuaryContent.TRACE.get().defaultBlockState();
        if(slot==0||slot==7){
            if(x==12&&z==18&&y==1)return fr.axephir.tnd.core.ModContent.RETURN_ANCHOR.get().defaultBlockState();
            if(x==-12&&z==18&&y==1)return Blocks.ENDER_CHEST.defaultBlockState();
            if(x==-13&&z==18&&y==1)return Blocks.CRAFTING_TABLE.defaultBlockState();
        }
        if(slot==3&&y<=2&&ax==24&&az==24)return y==2?SanctuaryContent.RELAY.get().defaultBlockState():stone("obsidian_pillar");
        for(int d=0;slot==2&&d<4;d++)if(x==-12+d*8&&z==-14&&y<=2)return y==2?SanctuaryContent.DIAL.get().defaultBlockState():stone("obsidian_pillar");

        // Stairwells remain open above every tread: a gallery must never cap its own flight.
        // Both ten-block climbs end before the fixed resonance landings at z = +/-12.
        if(ax>=40&&ax<=45&&z>=-31&&z<=-13&&y<=12){
            int step=1+(z+31)/2;
            if(y>step)return air();
            return y==step&&(z+31)%2==0?stair(Direction.SOUTH):stone("obsidian_tiles");
        }
        if(ax>=40&&ax<=45&&z>=13&&z<=31&&y<=12){
            int step=1+(31-z)/2;
            if(y>step)return air();
            return y==step&&(31-z)%2==0?stair(Direction.NORTH):stone("obsidian_tiles");
        }
        if(y==10&&Math.max(ax,az)>=38)return stone(slot==4&&ax>=40&&ax<=44&&Math.abs(az-12)<=2?"guiding_inlay":"obsidian_grate");
        if(y==11&&Math.max(ax,az)==38&&Math.floorMod(x+z,4)==0)return slab(false);
        // Corbelled gallery underside, fluted supports and capitals create actual depth.
        int bay=Math.floorMod(z+7,14),backBay=Math.floorMod(x+7,14);
        if(y>=7&&y<=9&&((ax>=37+(9-y)&&bay<=2)||(az>=37+(9-y)&&backBay<=2)))return stone(y==9?"obsidian_ribbed":"obsidian_relief");
        if((ax>=44&&bay<=2)||(az>=44&&backBay<=2)){
            if(y<=2||y==12||y==13)return stone("obsidian_relief");
            return stone(y==4||y==19?"luminous_script":"obsidian_pillar");
        }
        if(y<=8&&Math.min(ax,az)<=5&&Math.max(ax,az)>=44)return air();
        // Deep framed alcoves, ledges and inset luminous panels behind the lower arcade.
        if(Math.max(ax,az)>=46&&y<=8)return stone(y==2||y==7?"obsidian_relief":y>=3&&y<=5?"memory_lattice":"obsidian_metal");
        if((ax==43&&bay>=5&&bay<=9||az==43&&backBay>=5&&backBay<=9)&&y==1)return slab(false);
        if(y>=14&&Math.max(ax,az)>=45&&((y-14)%9==0))return stone("obsidian_relief");

        // Two complete load-bearing arcades divide the immense volume into readable bays.
        // Ring arches meet their columns and the roof: no floating crossbars.
        if(az>=28&&az<=30){
            int arch=height-7-(ax*ax)/96;
            if(ax<=33&&y>=arch&&y<=arch+2)return stone(y==arch&&(az==29||ax%11==0)?"luminous_script":"obsidian_relief");
            if(ax>=30&&ax<=33&&y<arch){
                if(y<=2||y>=arch-2)return stone("obsidian_ribbed");
                return stone(y==4?"amber_seal":"obsidian_pillar");
            }
        }
        // Clerestory shoulders carry the secondary arches above the side aisles.
        if(ax>=34&&ax<=36&&y>=height-18&&y<=height-16)return stone("obsidian_relief");
        if(y==height-15&&ax==35&&Math.floorMod(z+7,14)<=2)return stone("luminous_script");

        // Rear terraces are furnished platforms with two real steps, not inaccessible solid banks.
        // Their footprint leaves both fixed supply chests and the floor resonance lane clear.
        if(slot!=7&&ax>=12&&ax<=28&&z>=27&&z<=36){
            int tier=Math.min(2,z-26);
            if(y<=tier)return y==tier&&z<29?stair(Direction.SOUTH):stone(y==2?"obsidian_tiles":"obsidian_relief");
            if(z==36&&y==3)return slab(false);
        }

        BlockState furnished=furnishing(x,y,z,index,height);
        if(furnished!=null)return furnished;

        // Human-height lamps illuminate the lower aisles; their rods attach to the vault.
        if(ax>=17&&ax<=19&&az>=35&&az<=37&&y==1)return stone("obsidian_relief");
        if(ax==18&&az==36){
            if(y<=3)return stone("obsidian_pillar");
            if(y==4)return stone("observatory_brazier");
            if(y==5)return slab(true);
            return Blocks.IRON_CHAIN.defaultBlockState();
        }
        return air();
    }

    private static BlockState furnishing(int x,int y,int z,int index,int height){
        int ax=Math.abs(x),az=Math.abs(z),act=index/8,slot=index%8;
        // Bosses have an open 60m arena, framed by real stepped spectator tribunes.
        if(slot==7){
            if(ax>=29&&ax<=36&&az<=23&&az>=8){
                int tier=(ax-29)/2+1;
                if(y<=tier)return y==tier&&(ax-29)%2==0?stair(x>0?Direction.EAST:Direction.WEST):stone(y==tier?"obsidian_tiles":"obsidian_relief");
            }
            if(ax>=25&&ax<=27&&az>=32&&az<=34&&y<=height-13)return stone(y<=3||y>=height-15?"obsidian_relief":y==5?"amber_seal":"obsidian_pillar");
        }
        if(slot!=7){
            switch(act){
                case 0 -> {
                    // Witness niches and layered reliquaries beneath a rising fan of stone.
                    if(ax>=17&&ax<=26&&z>=29&&z<=34){
                        int u=ax-21,v=z-32,r=u*u+v*v;
                        if(y==3&&r<=22)return slab(false);
                        if(r<=4&&y>=3&&y<=8)return stone(y==7?"luminous_script":"obsidian_pillar");
                        if(y>=9&&y<=13&&Math.abs(u)+Math.abs(v)<=13-y)return stone(y==9?"amber_seal":"obsidian_relief");
                    }
                }
                case 1 -> {
                    // Open shelves with individual plinths, aisles and recessed luminous drawers.
                    // The shelves have actual air gaps, rather than opaque rectangular stacks.
                    if(ax>=19&&ax<=27&&z>=6&&z<=21){
                        boolean end=z<=7||z>=20,post=(ax==19||ax==27)&&end;
                        if(post&&y<=9)return stone(y==2||y==8?"obsidian_relief":"obsidian_pillar");
                        if((y==2||y==5||y==8)&&(ax<=21||ax>=25))return slab(true);
                        if((y==3||y==6)&&(ax==20||ax==26)&&Math.floorMod(z,4)<=1)return stone("memory_lattice");
                    }
                    if(ax>=16&&ax<=26&&z>=30&&z<=34&&y>=3&&y<=5){
                        if(y==3)return stone("obsidian_relief");
                        if(y==4&&z==33&&ax%3==0)return stone("memory_lattice");
                        if(y==5)return slab(false);
                    }
                }
                case 2 -> {
                    // Faceted reactor vessels: hollow jackets, capstones, risers and high pipes.
                    int rx=ax-24,rz=az-17,r=rx*rx+rz*rz;
                    if(r<=25&&y<=16){
                        if(y<=2||y>=14)return stone(y==15?"obsidian_grate":"obsidian_relief");
                        if(r>=13)return stone(y==6||y==11?"obsidian_ribbed":"silent_conduit");
                        if(y==3||y==12||rx==0&&rz==0)return stone("amber_seal");
                    }
                    if(ax==24&&az>=17&&az<=35&&y>=17&&y<=18)return stone(y==17?"silent_conduit":"obsidian_ribbed");
                    if(ax==24&&az==35&&y<=18)return stone(y%6==0?"amber_seal":"obsidian_pillar");
                    if(ax>=13&&ax<=27&&z>=30&&z<=34&&y==3)return stone("obsidian_grate");
                }
                case 3 -> {
                    // Branching petrified trunks grow from stepped mineral beds and real roots.
                    int tx=ax-23,tz=z-32,r=tx*tx+tz*tz;
                    if(y==3&&r<=35)return stone(r>23?"obsidian_relief":"silent_conduit");
                    if(y>=4&&y<=17&&Math.abs(tx)<=Math.max(1,4-y/5)&&Math.abs(tz)<=Math.max(1,4-y/5))return stone(y%6==0?"luminous_script":"obsidian_metal");
                    if(y>=9&&y<=21){
                        int branch=(y-7)/3;
                        if((Math.abs(tx-branch)<=1||Math.abs(tx+branch)<=1)&&Math.abs(tz-(y%2))<=1)return stone(y==20?"luminous_script":"obsidian_relief");
                        if(Math.abs(tz-branch)<=1&&Math.abs(tx)<=1)return stone("obsidian_relief");
                    }
                    // Low carved root ribs articulate the edges of the sunken garden courts.
                    if(z<=-18&&z>=-32&&ax>=14&&ax<=28&&y<=2&&floorLevel(x,z,index)==0&&Math.floorMod(ax+z,9)==0)return stone(y==2?"silent_conduit":"obsidian_relief");
                }
                case 4 -> {
                    // Raised judge's benches and serrated ceremonial fins behind the tribunal.
                    if(ax>=14&&ax<=27&&z>=29&&z<=35){
                        if(y==3||y==5&&z>=33)return slab(false);
                        if(ax%4==0&&y>=3&&y<=15-(ax%5))return stone(y==6?"amber_seal":"obsidian_pillar");
                    }
                }
                case 5 -> {
                    // Paired armillary instruments: pierced circles with visible grounded feet.
                    int u=ax-23,v=y-11;
                    if(z>=30&&z<=32){
                        int r=u*u+v*v;
                        if(r>=43&&r<=64)return stone((u==0||v==0)?"luminous_script":"obsidian_relief");
                        if(y>=3&&y<=6&&Math.abs(u)<=2)return stone("obsidian_pillar");
                        if(y==11&&Math.abs(u)<=2)return stone("amber_seal");
                    }
                }
            }
        }
        // A complete circular crown hangs from four roof rods; the final lens is visibly suspended.
        if((act==5||slot==4)&&y>=height-16&&y<=height-14){
            int r=x*x+z*z;
            if(r>=23*23&&r<=26*26)return stone(y==height-15?"luminous_script":"obsidian_relief");
        }
        if((act==5||slot==4)&&ax>=24&&ax<=26&&az<=1&&y>=height-14)return stone("obsidian_pillar");
        if((act==5||slot==4)&&az>=24&&az<=26&&ax<=1&&y>=height-14)return stone("obsidian_pillar");
        if(index==47){
            int h=y-(height-23),r=x*x+z*z+h*h;
            if(r>=64&&r<=100)return stone(h==0||ax<=1?"luminous_script":"obsidian_relief");
            if(y>=height-23&&y<=height&&ax<=1&&az<=1)return stone("obsidian_pillar");
        }
        return null;
    }
    private SanctuaryInterior(){}
}
