package fr.axephir.tnd.city;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class ArchiveTerminalBlock extends Block {
    public static final IntegerProperty ENTRY=IntegerProperty.create("entry",0,7);
    public ArchiveTerminalBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ENTRY,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ENTRY);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level w,BlockPos pos,Player p,BlockHitResult h){if(p instanceof ServerPlayer sp){int entry=state.getValue(ENTRY);Codex.collect(sp,entry);Codex.open(sp,entry);}return InteractionResult.SUCCESS;}
}
