package fr.axephir.tnd.entities;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** Hold one second for a short, bounded shockwave against hostile mobs. No terrain damage or PvP. */
public final class GravitasHammerItem extends Item {
    public GravitasHammerItem(Properties p){super(p);}
    @Override public int getUseDuration(ItemStack s,LivingEntity p){return 20;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack s){return ItemUseAnimation.BOW;}
    @Override public InteractionResult use(Level l,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);if(p.getCooldowns().isOnCooldown(stack)||!p.onGround())return InteractionResult.FAIL;
        p.startUsingItem(hand);if(p instanceof ServerPlayer s)fr.axephir.tnd.expedition.ExpeditionService.sound(s,ModContent.atmosphereSound("hammer_charge"),SoundSource.PLAYERS,.5f,1);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(entity instanceof ServerPlayer p&&p.isAlive()&&!p.isSpectator()&&p.onGround()&&!p.getCooldowns().isOnCooldown(stack)){
            var world=p.level();var forward=p.getLookAngle().multiply(1,0,1).normalize();int hit=0;
            for(var mob:world.getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(5,2,5),m->m.isAlive()&&p.hasLineOfSight(m))){
                var delta=mob.position().subtract(p.position()).multiply(1,0,1);if(delta.lengthSqr()>25||delta.normalize().dot(forward)<.35)continue;
                if(mob.hurtServer(world,p.damageSources().playerAttack(p),6))mob.knockback(1.1,-delta.x,-delta.z,p.damageSources().playerAttack(p),1);if(++hit>=4)break;
            }
            for(int i=0;i<32;i++){double a=(i/31.0-.5)*2.2+Math.atan2(forward.z,forward.x);world.sendParticles(ParticleTypes.CRIT,p.getX()+Math.cos(a)*3,p.getY()+.25,p.getZ()+Math.sin(a)*3,1,0,.2,0,.04);}
            world.playSound(null,p.blockPosition(),ModContent.atmosphereSound("hammer_impact"),SoundSource.PLAYERS,.65f,1);stack.hurtAndBreak(3,p,p.getUsedItemHand()==InteractionHand.OFF_HAND?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND);p.getCooldowns().addCooldown(stack,120);
        }return stack;
    }
}
