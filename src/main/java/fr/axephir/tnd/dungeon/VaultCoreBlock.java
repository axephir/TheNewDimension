package fr.axephir.tnd.dungeon;

import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

/** Only the central mechanism owns persistent data; the three seals are ordinary blocks. */
public final class VaultCoreBlock extends VaultBlock implements EntityBlock {
    public VaultCoreBlock(Properties properties){super(properties,true);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new VaultCoreEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        if(level.isClientSide()||type!=MachineRegistry.VAULT_ENTITY.get())return null;
        return (world,pos,s,be)->{if(world.getGameTime()%20==0)((VaultCoreEntity)be).tickEncounter();};
    }
}
