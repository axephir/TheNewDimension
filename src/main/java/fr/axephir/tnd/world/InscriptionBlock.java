package fr.axephir.tnd.world;

import fr.axephir.tnd.domain.Mastery;
import fr.axephir.tnd.progression.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class InscriptionBlock extends Block {
    private final String glyph;
    public InscriptionBlock(Properties properties,String glyph){super(properties);this.glyph=glyph;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(player instanceof ServerPlayer p) {
            if(!p.isCreative()&&!ProgressionService.claim(p,"reading",40,1))return InteractionResult.SUCCESS;
            p.sendSystemMessage(Component.translatable("message.tnd.lore."+glyph));
            if(!ProgressionEvents.playerPlaced(level,pos)&&ProgressionService.claim(p,"glyph/"+glyph,Long.MAX_VALUE,1))
                ProgressionService.award(p,Mastery.ANCIENT_TECH,25);
            p.level().sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,10,.3,.2,.3,.05);
        }
        return InteractionResult.SUCCESS;
    }
}
