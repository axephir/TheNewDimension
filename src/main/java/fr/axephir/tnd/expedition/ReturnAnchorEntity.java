package fr.axephir.tnd.expedition;
import java.util.UUID;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Replacing an anchor creates a fresh identity, invalidating old bound prisms. */
public final class ReturnAnchorEntity extends BlockEntity {
    private UUID token=UUID.randomUUID();
    public ReturnAnchorEntity(BlockPos p,BlockState s){super(MachineRegistry.ANCHOR_ENTITY.get(),p,s);}
    public UUID token(){return token;}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putString("anchorToken",token.toString());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);try{token=UUID.fromString(in.getStringOr("anchorToken",""));}catch(IllegalArgumentException e){token=UUID.randomUUID();}}
}
