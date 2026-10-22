package fr.axephir.tnd.familiar;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;

public final class IncubationComputerBlock extends BaseEntityBlock {
    public static final MapCodec<IncubationComputerBlock> CODEC=simpleCodec(IncubationComputerBlock::new);
    public IncubationComputerBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new IncubationComputerEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){return w.isClientSide()||t!=FamiliarContent.COMPUTER_ENTITY.get()?null:(l,p,state,e)->((IncubationComputerEntity)e).tick();}
    private void interact(Level w,BlockPos at,Player player){if(player instanceof ServerPlayer p&&w.getBlockEntity(at) instanceof IncubationComputerEntity c)c.start(p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player p,BlockHitResult hit){interact(w,at,p);return InteractionResult.SUCCESS;}
    @Override protected InteractionResult useItemOn(ItemStack i,BlockState s,Level w,BlockPos at,Player p,InteractionHand hand,BlockHitResult hit){interact(w,at,p);return InteractionResult.SUCCESS;}
}
