package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class EchoLensItem extends Item {
    public EchoLensItem(Properties p){super(p);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer p)reveal(p,player.getItemInHand(hand));return InteractionResult.SUCCESS;
    }
    public static int reveal(ServerPlayer p,ItemStack lens) {
        if(!lens.is(ModContent.ECHO_LENS.get())||p.isSpectator()||p.getCooldowns().isOnCooldown(lens))return 0;
        var targets=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),e->e instanceof Enemy&&e.isAlive()&&e.distanceToSqr(p)<=256);
        if(targets.isEmpty()){p.sendOverlayMessage(Component.translatable("message.tnd.lens_empty"));return 0;}
        ItemStack fuel=ItemStack.EMPTY;
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModContent.ECHO_DUST.get())){fuel=p.getInventory().getItem(i);break;}
        if(!p.isCreative()&&fuel.isEmpty()){p.sendOverlayMessage(Component.translatable("message.tnd.lens_fuel"));return 0;}
        if(!p.isCreative())fuel.shrink(1);
        for(var target:targets)target.addEffect(new MobEffectInstance(MobEffects.GLOWING,200,0,false,false,true));
        p.getCooldowns().addCooldown(lens,400);
        p.level().playSound(null,p.blockPosition(),SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.PLAYERS,.6f,.8f);
        p.sendOverlayMessage(Component.translatable("message.tnd.lens_revealed",targets.size()));return targets.size();
    }
}
