package fr.axephir.tnd.expedition;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class SurveyTerminalEntity extends BlockEntity {
    private boolean natural;
    public SurveyTerminalEntity(BlockPos p,BlockState s){super(MachineRegistry.SURVEY_ENTITY.get(),p,s);}
    public boolean natural(){return natural;}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("SurveySite",natural?1:0);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);natural=in.getIntOr("SurveySite",0)==1;}
}
