package fr.axephir.tnd.narrative;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A restoration terminal remains usable even while the player's projection has lost physical interaction. */
public final class ConvergenceTerminalBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public final boolean reactor;
    public ConvergenceTerminalBlock(Properties p,boolean reactor){super(p);this.reactor=reactor;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ConvergenceTerminalEntity(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player p,BlockHitResult hit){if(p instanceof ServerPlayer sp)ConvergenceMenu.open(sp,at,reactor);return InteractionResult.SUCCESS;}
    @Override protected MenuProvider getMenuProvider(BlockState s,Level w,BlockPos at){return new SimpleMenuProvider((id,inv,p)->new ConvergenceMenu(id,inv,(ServerPlayer)p,at,reactor),net.minecraft.network.chat.Component.translatable("story.tnd."+(reactor?"reactor":"restorer")));}
}
