package fr.axephir.tnd.assault;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
public final class AssaultNodeBlock extends Block implements EntityBlock {
    public enum Family implements StringRepresentable {WARD,SHADE,CHOIR;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Family> FAMILY=EnumProperty.create("family",Family.class);
    public static final BooleanProperty SUMMIT=BooleanProperty.create("summit"),LIT=BlockStateProperties.LIT;
    public AssaultNodeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FAMILY,Family.WARD).setValue(SUMMIT,false).setValue(LIT,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FAMILY,SUMMIT,LIT);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AssaultNodeEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide()||type!=AssaultContent.NODE_ENTITY.get()?null:(w,p,state,be)->{if(w.getGameTime()%20==0)((AssaultNodeEntity)be).tick();};}
}
