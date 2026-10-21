package fr.axephir.tnd.dungeon;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Two temporary absorption hearts. Refuses to replace an existing absorption effect. */
public final class ResonantAegisItem extends Item {
    public ResonantAegisItem(Properties p){super(p);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)activate(p,player.getItemInHand(hand));return InteractionResult.SUCCESS;}
    public static boolean activate(ServerPlayer p,ItemStack stack){
        if(!stack.is(ModContent.RESONANT_AEGIS.get())||p.isSpectator()||p.getCooldowns().isOnCooldown(stack))return false;
        if(p.hasEffect(MobEffects.ABSORPTION)){p.sendOverlayMessage(Component.translatable("message.tnd.aegis_existing"));return false;}
        ItemStack fuel=ItemStack.EMPTY;
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModContent.ECHO_DUST.get())){fuel=p.getInventory().getItem(i);break;}
        if(fuel.isEmpty()&&!p.isCreative()){p.sendOverlayMessage(Component.translatable("message.tnd.lens_fuel"));return false;}
        if(!p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,300,0,false,true,true)))return false;
        if(!p.isCreative())fuel.shrink(1);p.getCooldowns().addCooldown(stack,1200);
        p.level().playSound(null,p.blockPosition(),SoundEvents.BEACON_POWER_SELECT,SoundSource.PLAYERS,.7f,1.2f);
        p.sendOverlayMessage(Component.translatable("message.tnd.aegis_active"));return true;
    }
}
