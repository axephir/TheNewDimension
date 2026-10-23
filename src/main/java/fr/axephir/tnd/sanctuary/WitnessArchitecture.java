package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.WitnessAct;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
/** The mechanisms have authored supports and approaches, independent of neighbour generation order. */
public final class WitnessArchitecture {
    static BlockState sample(int x,int y,int z,int index,int height){
        if(index<0||index>=8)return null;
        int secret=WitnessAct.secret(index);
        if(secret>=0){var at=WitnessAct.reliquary(index);if(x==at.x()&&y==at.y()&&z==at.z())return SanctuaryContent.RELIQUARY.get().defaultBlockState();}
        if(index==3&&x>=42&&x<=47&&z>=5&&z<=9&&y>=1&&y<=4){
            if(z==5||z==9||y==4||x==47)return SanctuaryContent.block("obsidian_relief").defaultBlockState();
            if(x==43)return SanctuaryContent.GATE.get().defaultBlockState();
            return Blocks.AIR.defaultBlockState();
        }
        if(index==4&&Math.abs(x)==42&&z==0&&y==11)return SanctuaryContent.COUPLER.get().defaultBlockState();
        if(index==5)for(int n=0;n<3;n++){
            var at=WitnessAct.coupler(n);
            if(x==at.x()&&z==at.z()&&y==at.y())return SanctuaryContent.COUPLER.get().defaultBlockState();
            if(x==at.x()&&z==at.z()+2&&y==0)return SanctuaryContent.FILAMENT.get().defaultBlockState().setValue(WitnessFilamentBlock.POWER,1).setValue(WitnessFilamentBlock.FACING,direction(WitnessAct.facing(n)));
        }
        if((index==2&&Math.abs(x)==24&&z==-14||index==5&&Math.abs(x)==30&&z==-6)&&y>=1&&y<=2)return SanctuaryContent.block(y==2?"memory_lattice":"obsidian_pillar").defaultBlockState();
        if(y==0&&Math.abs(x)==8&&z>=-36&&z<=36&&Math.floorMod(z,4)==0)return SanctuaryContent.FILAMENT.get().defaultBlockState();
        if(height>0&&Math.abs(x)<=27&&Math.floorMod(x,3)==0&&(Math.abs(z)==28||Math.abs(z)==30)&&y==height-7-x*x/96)return SanctuaryContent.FILAMENT.get().defaultBlockState();
        return null;
    }
    static Direction direction(int tone){return switch(tone&3){case 0->Direction.NORTH;case 1->Direction.EAST;case 2->Direction.SOUTH;default->Direction.WEST;};}
    private WitnessArchitecture(){}
}
