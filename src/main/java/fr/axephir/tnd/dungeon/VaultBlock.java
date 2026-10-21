package fr.axephir.tnd.dungeon;

import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

/** The facing rotates the entire encounter layout with the structure template. */
public class VaultBlock extends Block {
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SEAL=IntegerProperty.create("seal",0,2);
    public static final BooleanProperty MIRRORED=BooleanProperty.create("mirrored");
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    private final boolean core;
    public VaultBlock(Properties properties,boolean core){super(properties);this.core=core;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SEAL,0).setValue(LIT,false).setValue(MIRRORED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,SEAL,LIT,MIRRORED);}
    @Override protected BlockState rotate(BlockState state,Rotation r){return state.setValue(FACING,r.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror m){return m==Mirror.NONE?state:rotate(state,m.getRotation(state.getValue(FACING))).cycle(MIRRORED);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p){
            var origin=core?pos:pos.subtract(offset(state,state.getValue(SEAL)));
            if(level.hasChunk(origin.getX()>>4,origin.getZ()>>4)&&level.getBlockEntity(origin) instanceof VaultCoreEntity vault){
                if(core)vault.claim(p);else vault.start(p,state.getValue(SEAL));
            }else p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.tnd.vault_unbound"));
        }
        return InteractionResult.SUCCESS;
    }
    public static BlockPos offset(BlockState state,int index){return turn(state,index==0?-18:index==1?18:0,index==2?22:0);}
    public static BlockPos turn(BlockState state,int x,int z){return turn(state.getValue(FACING),state.getValue(MIRRORED)?-x:x,z);}
    private static BlockPos turn(Direction facing,int x,int z){return switch(facing){
        case EAST->new BlockPos(-z,0,x);case SOUTH->new BlockPos(-x,0,-z);case WEST->new BlockPos(z,0,-x);default->new BlockPos(x,0,z);
    };}
}
