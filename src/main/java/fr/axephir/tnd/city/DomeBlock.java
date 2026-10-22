package fr.axephir.tnd.city;

import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** A force field, not glass: no collision, no drops, no tool target and no piston movement. */
public final class DomeBlock extends Block {
    public DomeBlock(Properties p){super(p);}
    @Override protected boolean skipRendering(BlockState state,BlockState adjacent,Direction side){return adjacent.getBlock() instanceof DomeBlock||super.skipRendering(state,adjacent,side);}
    @Override public net.minecraft.world.level.material.PushReaction getPistonPushReaction(BlockState s){return net.minecraft.world.level.material.PushReaction.BLOCK;}
}
