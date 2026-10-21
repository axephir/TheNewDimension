package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.Mastery;
import fr.axephir.tnd.progression.ProgressionService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** A relocatable mineral aggregate that charges only in Vestige's night phase. */
public final class EchoBloomBlock extends Block {
    public static final IntegerProperty CHARGE=IntegerProperty.create("charge",0,3);
    private static final VoxelShape SHAPE=Block.box(2,0,2,14,14,14);
    public EchoBloomBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(CHARGE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CHARGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return SHAPE;}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if(canCharge(level,state)&&random.nextInt(3)==0)level.setBlock(pos,state.cycle(CHARGE),3);
    }
    public static boolean canCharge(Level level,BlockState state){return VestigeTime.isNight(level)&&state.getValue(CHARGE)<3;}
    public static boolean harvest(ServerPlayer p,BlockPos pos) {
        var level=p.level();var state=level.getBlockState(pos);
        if(!(state.getBlock() instanceof EchoBloomBlock))return false;
        if(!VestigeTime.isNight(level)){p.sendOverlayMessage(Component.translatable("message.tnd.bloom_day"));return false;}
        if(state.getValue(CHARGE)!=3){p.sendOverlayMessage(Component.translatable("message.tnd.bloom_charging"));return false;}
        level.setBlock(pos,state.setValue(CHARGE,0),3);
        fr.axephir.tnd.expedition.ExpeditionService.harvest(p,pos);
        var dust=new ItemStack(ModContent.ECHO_DUST.get(),1+level.getRandom().nextInt(2));
        if(!p.getInventory().add(dust))p.drop(dust,false);
        if(ProgressionService.claim(p,"echo_harvest",24000,12))ProgressionService.award(p,Mastery.ANCIENT_TECH,12);
        level.sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5,20,.3,.3,.3,.05);
        level.playSound(null,pos,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.7f,1.2f);return true;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(player instanceof ServerPlayer p)harvest(p,pos);return InteractionResult.SUCCESS;
    }
}
