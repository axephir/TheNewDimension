package fr.axephir.tnd.technology;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Collision follows the visible insulated cable, so industrial wiring does not create invisible walls. */
public final class EnergyCableBlock extends Block {
    private static final VoxelShape SHAPE=Shapes.or(Block.box(5,0,5,11,16,11),Block.box(0,5,5,16,11,11),Block.box(5,5,0,11,11,16));
    public EnergyCableBlock(Properties p){super(p);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return SHAPE;}
}
