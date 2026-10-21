package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.technology.ExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class AncientConsoleBlock extends Block {
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public AncientConsoleBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(LIT,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(LIT);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(player instanceof ServerPlayer p)activate(p,pos);
        return InteractionResult.SUCCESS;
    }
    public static boolean activate(ServerPlayer p,BlockPos pos) {
        var level=p.level();var state=level.getBlockState(pos);
        if(!(state.getBlock() instanceof AncientConsoleBlock))return false;
        if(!p.isCreative()&&!ProgressionService.claim(p,"console_action",40,1))return false;
        boolean repaired=!state.getValue(LIT),natural=!ProgressionEvents.playerPlaced(level,pos);
        if(repaired) {
            ItemStack shard=p.getMainHandItem().is(ModContent.RESONANT_SHARD.get())?p.getMainHandItem():p.getOffhandItem();
            if(!p.isCreative()&&!shard.is(ModContent.RESONANT_SHARD.get())) {
                p.sendOverlayMessage(Component.translatable("message.tnd.console_needs_shard"));return false;
            }
            if(!p.isCreative())shard.shrink(1);
            level.setBlock(pos,state.setValue(LIT,true),3);
            if(natural&&!p.isCreative()) {
                ItemStack reward=new ItemStack(ModContent.RELIC_FRAGMENT.get(),1+level.getRandom().nextInt(2));
                if(!p.getInventory().add(reward))p.drop(reward,false);
            }
        }
        String key="console/"+level.dimension().identifier()+"/"+pos.asLong();
        if(natural&&!p.getData(PlayerProgression.ACTIVITIES).entries().containsKey(key)
                &&ProgressionService.claim(p,"console_studies",24000,8)&&ProgressionService.claim(p,key,Long.MAX_VALUE,1))
            ProgressionService.award(p,Mastery.ANCIENT_TECH,repaired?90:25);
        int calibrated=0,bonus=15+p.getData(PlayerProgression.PROFILE).get(Mastery.ANCIENT_TECH).rank(Talent.EFFICIENCY)*2;
        for(BlockPos at:BlockPos.betweenClosed(pos.offset(-6,-4,-6),pos.offset(6,4,6))) {
            if(!level.hasChunk(at.getX()>>4,at.getZ()>>4))continue;
            if(level.getBlockEntity(at) instanceof ExtractorBlockEntity machine&&machine.calibrateRelic(bonus))calibrated++;
        }
        if(calibrated>0&&ProgressionService.claim(p,"relic_calibration",24000,8))ProgressionService.award(p,Mastery.ENGINEERING,20);
        p.sendOverlayMessage(Component.translatable("message.tnd.console_active",calibrated,bonus));
        level.playSound(null,pos,repaired?net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE:net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,net.minecraft.sounds.SoundSource.BLOCKS,.5f,1f);
        level.sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1.2,pos.getZ()+.5,24,.5,.4,.5,.1);
        return true;
    }
}
