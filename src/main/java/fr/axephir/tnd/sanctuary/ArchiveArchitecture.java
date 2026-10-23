package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.ArchiveAct;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Carved monolith stacks, terraced atlas and load-bearing armillary vaults. Pure local sampling. */
public final class ArchiveArchitecture {
    private static BlockState stone(String n){return SanctuaryContent.block(n).defaultBlockState();}
    private static BlockState air(){return Blocks.AIR.defaultBlockState();}
    private static BlockState stair(Direction d){return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,d);}
    public static BlockState sample(int x,int y,int z,int i,int height){
        if(!ArchiveAct.contains(i))return null;
        int ax=Math.abs(x),az=Math.abs(z),edge=Math.max(ax,az);boolean atlas=i==20;
        if(y<=0){
            if(y<0)return stone("obsidian_metal");
            if(az==33||ax==33)return stone("guiding_inlay");
            if(i==16||i==18)for(int n=0;n<3;n++){var p=ArchiveAct.mirror(i,n);if(z>=p.z()+2&&z<=p.z()+4&&Math.abs(x-p.x())<=2){int v=ArchiveAct.wanted(i,n);boolean mark=v==1?x==p.x():v==2?Math.abs(x-p.x())==1:x==p.x()||Math.abs(x-p.x())==2;if(mark)return stone("luminous_script");}}
            if(i==21){int[][] t={{-24,8},{24,8},{0,-30}};for(int n=0;n<3;n++)if(z>=t[n][1]+2&&z<=t[n][1]+4&&Math.abs(x-t[n][0])<=2){int v=n==2?1:n==0?2:3;if(v==1?x==t[n][0]:v==2?Math.abs(x-t[n][0])==1:x==t[n][0]||Math.abs(x-t[n][0])==2)return stone("luminous_script");}}
            if(ax<=2||az<=2)return stone("guiding_inlay");
            double r=Math.hypot(x,z);return stone(r>27&&r<29?"silent_conduit":Math.floorMod(x,10)==0&&Math.floorMod(z,10)==0?"obsidian_relief":"obsidian_tiles");
        }
        // Authored controls and their approaches precede every furnishing rule.
        if((i==16||i==21)&&y==1&&((x==-24&&z==8)||(x==24&&z==8)||(x==0&&z==-30)))return SanctuaryContent.TRACE.get().defaultBlockState();
        if(i==19&&ax==24&&az==24&&y<=2)return y==2?SanctuaryContent.RELAY.get().defaultBlockState():stone("obsidian_pillar");
        if(i==16||i==18)for(int n=0;n<3;n++){var p=ArchiveAct.mirror(i,n);if(x==p.x()&&z==p.z()&&y==1)return ArchiveContent.MIRROR.get().defaultBlockState();}
        if(i==20){var p=ArchiveAct.relay();if(x==p.x()&&z==p.z()&&y==1)return ArchiveContent.RELAY.get().defaultBlockState();}
        if(i==23&&y==1&&z==24&&(x==-22||x==0||x==22))return ArchiveContent.RELAY.get().defaultBlockState();
        if((i==16||i==23)&&y==1&&z==18){if(x==12)return fr.axephir.tnd.core.ModContent.RETURN_ANCHOR.get().defaultBlockState();if(x==-12)return Blocks.ENDER_CHEST.defaultBlockState();if(x==-13)return Blocks.CRAFTING_TABLE.defaultBlockState();}
        if(i==19&&Math.abs(ax-24)<=2&&Math.abs(az-24)<=3&&y<=4)return air();
        // Twelve shallow half-step flights reach broad +12 landings. Both orientations stay open.
        if(ax>=40&&ax<=44&&z>=-37&&z<=-14&&y<=15){int t=1+(z+37)/2;return y>t?air():y==t&&(z+37)%2==0?stair(Direction.SOUTH):stone("obsidian_relief");}
        if(ax>=40&&ax<=44&&z>=14&&z<=37&&y<=15){int t=1+(37-z)/2;return y>t?air():y==t&&(37-z)%2==0?stair(Direction.NORTH):stone("obsidian_relief");}
        if(atlas&&az>=40&&az<=44&&x>=-37&&x<=-14&&y>=12&&y<=27){int t=13+(x+37)/2;return y>t?air():y==t&&(x+37)%2==0?stair(Direction.EAST):stone("obsidian_ribbed");}
        if(atlas&&ax==33&&z>=-26&&z<=-22&&y>=25&&y<=27)return SanctuaryContent.GATE.get().defaultBlockState();
        if((y==12||atlas&&y==24)&&edge>=34&&edge<=44)return stone(edge==34?"guiding_inlay":"obsidian_grate");
        if((y==13||atlas&&y==25)&&edge==33&&Math.floorMod(x+z,5)!=0)return SanctuaryContent.SLAB.get().defaultBlockState();
        // A sealed 3m-wide upper bridge becomes a useful permanent shortcut after the atlas.
        if(atlas&&y==24&&ax>=32&&ax<=34&&z>=-26&&z<=-22)return stone("obsidian_grate");
        
        if(edge>=44)return null; // shared chamfered masonry, deep wall alcoves and vault
        if(y>=height-Math.max(0,(edge-14)*(edge-14)/88))return null;
        // Massive sloped corbels carry the two elevated promenades all the way to the walls.
        if(y>=1&&y<=11&&ax>=35+(11-y)/2&&ax<=43&&Math.floorMod(z+7,18)<=2)return stone(y%6==0?"memory_lattice":"obsidian_pillar");
        if(atlas&&y>=13&&y<=23&&az>=35+(23-y)/2&&az<=43&&Math.floorMod(x+7,18)<=2)return stone(y%6==0?"memory_lattice":"obsidian_pillar");
        // Three distinct archive bays divide the long aisles. Chamfered feet, incised leaves,
        // a recessed luminous face and a serrated crown make each monolith read in relief.
        if(i!=23&&!atlas)for(int centre:new int[]{-25,-3,25}){
            int mx=ax-24,mz=z-centre;boolean relayKeep=i==19&&Math.abs(centre)==25;
            if(!relayKeep&&Math.abs(mx)<=5&&Math.abs(mz)<=4){
                int top=centre==-25?34:centre==-3?27:39;
                if(y<=2&&Math.abs(mx)+Math.abs(mz)<=7)return stone(y==2?"obsidian_ribbed":"obsidian_relief");
                if(y>=3&&y<=top&&Math.abs(mx)<=3&&Math.abs(mz)<=2){
                    if(Math.abs(mx)==3||Math.abs(mz)==2)return stone(y%7==0?"luminous_script":"obsidian_relief");
                    return stone(y%5==0?"memory_lattice":"obsidian_metal");
                }
                if(y>=top+1&&y<=top+4&&Math.abs(mx)<=Math.max(0,top+4-y)&&Math.abs(mz)<=1)return stone(y==top+2?"amber_seal":"obsidian_ribbed");
                if(y>=5&&y<=top-3&&mx==-4&&Math.abs(mz)<=1)return stone(y%4==0?"luminous_script":"memory_lattice");
            }
        }
        if(atlas&&ax<=6&&az<=6){
            int r=ax+az;
            if(y<=3&&r<=9-y)return stone(y==3?"obsidian_grate":"obsidian_relief");
            if(y>=4&&y<=40&&ax<=2&&az<=2)return stone(y%8==0?"luminous_script":ax==2||az==2?"obsidian_ribbed":"silent_conduit");
            if(y>=9&&y<=37&&y%9<=1&&r>=4&&r<=6)return stone(y%9==0?"amber_seal":"obsidian_relief");
        }
        // Dense stacks of carved leaves have recessed faces, gold latches, sloping feet and caps.
        if(i!=23&&!atlas&&ax>=17&&ax<=29&&z>=17&&z<=30){
            int bay=Math.floorMod(x+30,6),deep=Math.floorMod(z-17,6),top=25+(Math.floorMod(x/6,3))*4;
            if(bay<=2&&deep<=3&&y<=top){
                if(y<=2||y>=top-1)return stone("obsidian_relief");
                if(bay==0||bay==2)return stone(y%9==0?"amber_seal":"obsidian_ribbed");
                return stone(y%4==0?"memory_lattice":deep==0?"obsidian_relief":"obsidian_metal");
            }
        }
        // Four grounded black blades carry the armillary instrument over the atlas / arena.
        if((atlas||i==23)&&ax>=26&&ax<=28&&az>=29&&az<=31&&y<=height-15)return stone(y%13==0?"luminous_script":"obsidian_pillar");
        if(atlas||i==23){
            double r=Math.hypot(x,z);int middle=atlas?42:38;
            if(y>=middle&&y<=middle+2&&r>=23&&r<=27)return stone(y==middle+1?"luminous_script":"obsidian_ribbed");
            if(y>=middle+14&&y<=middle+16&&r>=15&&r<=18)return stone(y==middle+15?"amber_seal":"obsidian_relief");
            if(y>=middle-1&&y<=middle+2&&(ax<=28&&az>=29&&az<=31||az<=30&&ax>=26&&ax<=28))return stone("obsidian_relief");
            if(ax<=1&&az<=1&&y>=middle+5&&y<=height-3)return stone(y%7==0?"amber_seal":"silent_conduit");
            int sphere=x*x+z*z+(y-middle-6)*(y-middle-6);if(sphere<=42&&sphere>=20)return stone(ax<=1?"luminous_script":"memory_lattice");
        }
        // Cross-vaults and ribbed abutments visibly close the great interior volume.
        if(az>=31&&az<=33){int arch=height-10-ax*ax/100;if(y>=arch&&y<=arch+2)return stone(y==arch+1?"silent_conduit":"obsidian_relief");if(ax>=30&&ax<=32&&y<arch)return stone(y%12==0?"luminous_script":"obsidian_pillar");}
        if((y==13||atlas&&y==25)&&edge>=35&&edge<=43&&Math.floorMod(x+z,16)==0)return stone("observatory_brazier");
        if(ax==36&&az==32&&y==13)return stone("observatory_brazier");
        if(i!=23&&y==3&&ax==10&&z>=-32&&z<=32&&z%8==0)return stone("observatory_brazier");
        if(i!=23&&y<=2&&ax==10&&z>=-32&&z<=32&&z%8==0)return stone("obsidian_relief");
        // Small rooms differ in their lower silhouette without obstructing the central combat lane.
        if(i==21&&ax>=12&&ax<=27&&z>=-34&&z<=-32&&y<=8){int h=3+Math.floorMod(x,5);if(y<=h)return stone(y==h?"memory_lattice":"obsidian_relief");}
        if(i==22&&ax>=18&&ax<=26&&z>=-30&&z<=-27&&y<=19)return stone(y%6==0?"luminous_script":"obsidian_ribbed");
        return air();
    }
    private ArchiveArchitecture(){}
}
