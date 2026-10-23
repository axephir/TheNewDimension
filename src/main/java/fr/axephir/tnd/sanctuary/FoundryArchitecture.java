package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.FoundryAct;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Chunk-independent carved foundry. Every gallery, crane, duct and press has grounded supports. */
public final class FoundryArchitecture {
    private static BlockState stone(String n){return SanctuaryContent.block(n).defaultBlockState();}
    private static BlockState air(){return Blocks.AIR.defaultBlockState();}
    private static BlockState stair(Direction d){return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,d);}
    private static BlockState filament(){return SanctuaryContent.FILAMENT.get().defaultBlockState();}
    public static BlockState sample(int x,int y,int z,int index,int height){
        if(!FoundryAct.contains(index))return null;
        int ax=Math.abs(x),az=Math.abs(z);boolean pit=index==12;
        // Small Roman marks repeat the required setting next to each valve, at its own floor level.
        for(int n=0;n<FoundryAct.valves(index)&&index!=15;n++){
            var p=FoundryAct.valve(index,n);int bars=FoundryAct.wanted(index,n),dx=x-p.x();
            boolean stroke=bars==1?dx==0:bars==2?Math.abs(dx)==1:Math.abs(dx)==2||dx==0;
            if(y==p.y()-1&&z>=p.z()+2&&z<=p.z()+4&&stroke)return stone("luminous_script");
        }
        // Terraced black-metal shaft with a visible floor, ribs and a protected luminous crucible.
        if(pit&&ax==24&&z==-18&&y>=-26&&y<=0)return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,x>0?Direction.WEST:Direction.EAST);
        if(y<=0){
            if(pit&&FoundryAct.shaft(x,z)){
                if(y<=-27)return stone(y==-27?"amber_seal":"obsidian_metal");
                if(ax>=25||z<=-33||z>=13)return stone(y%7==0?"obsidian_relief":"obsidian_ribbed");
                if(ax<=5&&Math.abs(z+10)<=5){
                    if(y<=-23)return stone("obsidian_relief");
                    if(ax==5||Math.abs(z+10)==5)return stone(y%4==0?"amber_seal":"obsidian_grate");
                    return stone("luminous_script");
                }
                // Four great piers meet the lower foundation and carry the overhead machinery.
                if(ax>=18&&ax<=21&&(Math.abs(z+27)<=1||Math.abs(z-7)<=1))return stone(y%9==0?"amber_seal":"obsidian_pillar");
                return air();
            }
            if(y<0)return stone("obsidian_metal");
            if(pit&&(z==32&&ax<=42||x==0&&z>=18&&z<=32))return stone("guiding_inlay");
            if(ax==30||az==36||ax<=2)return stone("guiding_inlay");
            if(ax==8&&z>=-32&&z<=32&&z%4==0)return filament();
            return stone(Math.floorMod(x,8)==0&&Math.floorMod(z,8)==0?"obsidian_relief":"obsidian_tiles");
        }
        // Keep the exact native station, supply and objective access positions unobstructed.
        if((index%8==0||index%8==5)&&y==1&&((x==-24&&z==8)||(x==24&&z==8)||(x==0&&z==-30)))return SanctuaryContent.TRACE.get().defaultBlockState();
        if(index%8==3&&ax==24&&az==24&&y<=2)return y==2?SanctuaryContent.RELAY.get().defaultBlockState():stone("obsidian_pillar");
        if(index%8==2&&z==-14&&y<=2)for(int n=0;n<4;n++)if(x==-12+n*8)return y==2?SanctuaryContent.DIAL.get().defaultBlockState():stone("obsidian_pillar");
        if((index%8==0||index%8==7)&&y==1&&z==18){if(x==12)return fr.axephir.tnd.core.ModContent.RETURN_ANCHOR.get().defaultBlockState();if(x==-12)return Blocks.ENDER_CHEST.defaultBlockState();if(x==-13)return Blocks.CRAFTING_TABLE.defaultBlockState();}
        for(int n=0;n<FoundryAct.valves(index);n++){
            var p=FoundryAct.valve(index,n);
            if(x==p.x()&&z==p.z()){
                if(y==p.y())return SanctuaryContent.FOUNDRY_VALVE.get().defaultBlockState();
                if(y==p.y()-1)return stone("obsidian_relief");
            }
            if(index==13&&x==p.x()&&z==p.z()+2&&y==0)return filament().setValue(WitnessFilamentBlock.POWER,FoundryAct.wanted(index,n));
        }
        // Real flights to the first gallery, with headroom above every tread.
        if(ax>=40&&ax<=44&&z>=-31&&z<=-12&&y<=13){int tread=1+(z+31)/2;if(y>tread)return air();return y==tread&&(z+31)%2==0?stair(Direction.SOUTH):stone("obsidian_relief");}
        if(ax>=40&&ax<=44&&z>=12&&z<=31&&y<=13){int tread=1+(31-z)/2;if(y>tread)return air();return y==tread&&(31-z)%2==0?stair(Direction.NORTH):stone("obsidian_relief");}
        // Two symmetric flights join the 10m and 20m landings around the great shaft.
        if(pit&&az>=40&&az<=44&&x>=-31&&x<=-12&&y>=10&&y<=23){int tread=11+(x+31)/2;if(y>tread)return air();return y==tread&&(x+31)%2==0?stair(Direction.EAST):stone("obsidian_ribbed");}
        if(pit){
            // Grounded bridge abutments hold a sealed upper shortcut across the shaft.
            if(ax>=28&&ax<=29&&z>=-25&&z<=-21&&y<=20)return stone(y==20?"obsidian_grate":"obsidian_relief");
            if(ax==29&&z>=-25&&z<=-21&&y>=21&&y<=23)return SanctuaryContent.GATE.get().defaultBlockState();
            if(ax==36&&Math.abs(z)==34&&(y==11||y==21))return stone("observatory_brazier");
            // Broad radial landings bring each control into the guided loop, without parkour.
            if((y==10||y==20)&&ax>=30&&ax<=44)return stone(ax==30?"guiding_inlay":"obsidian_grate");
            if((y==10||y==20)&&az>=36&&az<=44)return stone(az==36?"guiding_inlay":"obsidian_grate");
            if((y==11||y==21)&&ax==29&&Math.floorMod(z,4)!=0)return SanctuaryContent.SLAB.get().defaultBlockState();
            if((y==11||y==21)&&az==35&&ax<29&&Math.floorMod(x,4)!=0)return SanctuaryContent.SLAB.get().defaultBlockState();
            // Eight huge inclined corbels visibly carry both gallery decks.
            if(y>=1&&y<=9&&ax>=37+(9-y)/2&&ax<=44&&Math.floorMod(z+3,18)<=2)return stone("obsidian_ribbed");
            if(y>=11&&y<=19&&az>=37+(19-y)/2&&az<=44&&Math.floorMod(x+3,18)<=2)return stone("obsidian_ribbed");
            // Central faceted vessel rises from its crucible. The suspended rings attach to piers.
            if(ax<=4&&Math.abs(z+10)<=4&&y<=49)return stone((y%9==0||ax==4&&Math.abs(z+10)==4)?"amber_seal":"obsidian_metal");
            double radius=Math.hypot(x,z+10);
            if(y>=30&&y<=33&&radius>=16&&radius<=19||y>=49&&y<=52&&radius>=22&&radius<=25)return stone(y%3==0?"amber_seal":"obsidian_ribbed");
            if(ax>=18&&ax<=21&&(Math.abs(z+27)<=1||Math.abs(z-7)<=1)&&y<=54)return stone(y%9==0?"amber_seal":"obsidian_pillar");
            if(y>=52&&y<=54&&((ax<=21&&Math.abs(z+27)<=1)||(ax<=21&&Math.abs(z-7)<=1)))return stone(y==53?"luminous_script":"obsidian_relief");
        }
        // Paired bellows rise behind the final arena; nested jackets, ribs and anchored ducts.
        if(index==15){
            int bx=ax-30,bz=z+28,r=bx*bx+bz*bz;
            if(r<=42&&y<=35){
                if(y<=3||y>=32)return stone(y==33?"obsidian_grate":"obsidian_relief");
                if(r>=25)return stone(y%7==0?"amber_seal":"obsidian_ribbed");
                if(Math.abs(bx)<=1&&Math.abs(bz)<=1)return stone("luminous_script");
                return air();
            }
            if(ax>=29&&ax<=31&&z>=-28&&z<=-10&&y>=36&&y<=38)return stone(y==37?"silent_conduit":"obsidian_relief");
            if(ax>=29&&ax<=35&&z>=-11&&z<=-9&&y>=36&&y<=38)return stone("obsidian_ribbed");
            if(ax>=33&&ax<=35&&z>=-11&&z<=-9&&y>=1&&y<=38)return stone(y%8==0?"amber_seal":"obsidian_pillar");
        }
        // Turning mechanisms sit in pierced, grounded turbine housings.
        if(index!=15)for(int n=0;n<2;n++){
            var p=FoundryAct.rotor(n);int rx=x-p.x(),rz=z-p.z(),dy=y-p.y();
            if(rx==0&&rz==0&&dy==0)return SanctuaryContent.FOUNDRY_ROTOR.get().defaultBlockState();
            int ring=rx*rx+dy*dy;
            if(Math.abs(rz)<=1&&ring>=16&&ring<=30)return stone(y%3==0?"amber_seal":"obsidian_relief");
            if(Math.abs(rx)<=1&&Math.abs(rz)<=1&&y<=9)return stone(y==5?"luminous_script":"obsidian_pillar");
            if(Math.abs(rx)<=6&&Math.abs(rz)<=2&&y<=2)return stone("obsidian_relief");
            if(rx==0&&rz>=0&&rz<=28&&y==18)return stone("silent_conduit");
            if(rx==0&&rz==28&&y<=18)return stone(y%6==0?"amber_seal":"obsidian_pillar");
        }
        // Presses, anvils, open maintenance bays and overhead rails differ by chamber.
        if(!pit&&index!=15)for(int n=0;n<2;n++){
            var p=FoundryAct.press(n);int dx=x-p.x(),dz=z-p.z();
            if(dx==0&&dz==0&&y==3)return SanctuaryContent.FOUNDRY_PRESS.get().defaultBlockState();
            if(Math.abs(dx)<=4&&Math.abs(dz)<=4&&y<=1)return stone("obsidian_relief");
            if(Math.abs(dx)==4&&Math.abs(dz)<=1&&y<=15)return stone(y%6==0?"amber_seal":"obsidian_pillar");
            if(Math.abs(dx)<=4&&Math.abs(dz)<=1&&y>=14&&y<=16)return stone(y==15?"silent_conduit":"obsidian_ribbed");
            if(dx==0&&dz==0&&y>=5&&y<=14)return Blocks.IRON_CHAIN.defaultBlockState();
        }
        if(y==10&&Math.max(ax,az)>=38&&Math.max(ax,az)<=44)return stone("obsidian_grate");
        // Lower layered plinths and lit trenches preserve a broad central combat lane.
        if(!pit&&index!=15&&ax>=15&&ax<=30&&z>=29&&z<=34&&y<=3)return y==3?SanctuaryContent.SLAB.get().defaultBlockState():stone(y==2?"memory_lattice":"obsidian_relief");
        if(ax>=36&&ax<=38&&y<=height-10&&Math.floorMod(z+5,16)<=2)return stone(y%12==4?"amber_seal":"obsidian_pillar");
        if(y>=height-12&&y<=height-10&&ax<=38&&Math.floorMod(z+5,16)<=2)return stone(y==height-11?"luminous_script":"obsidian_ribbed");
        // Retain the shared vaulted walls, doors and external buttress system.
        return null;
    }
    private FoundryArchitecture(){}
}
