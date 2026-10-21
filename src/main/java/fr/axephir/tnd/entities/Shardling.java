package fr.axephir.tnd.entities;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A small articulated relic, deliberately not a reskinned vanilla animal. */
public final class Shardling extends TamableAnimal {
    public Shardling(EntityType<? extends Shardling> type,Level level){super(type,level);xpReward=0;}
    public static AttributeSupplier.Builder attributes() {
        return createAnimalAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.28)
                .add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,24);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,true));
        goalSelector.addGoal(3,new FollowOwnerGoal(this,1.15,7,2));
        goalSelector.addGoal(4,new TemptGoal(this,1,stack->stack.is(ModContent.COMPOSITE_CRYSTAL.get()),false));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.75));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,7));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2,new OwnerHurtTargetGoal(this));
    }
    @Override protected int getBaseExperienceReward(ServerLevel level){return 0;}
    @Override public boolean isFood(ItemStack stack){return false;}
    @Override public AgeableMob getBreedOffspring(ServerLevel level,AgeableMob other){return null;}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(!isTame()&&stack.is(ModContent.COMPOSITE_CRYSTAL.get())) {
            if(!level().isClientSide()) {
                stack.consume(1,player);tame(player);setPersistenceRequired();setOrderedToSit(false);
                level().broadcastEntityEvent(this,(byte)7);playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.8f,1.4f);
                if(player instanceof ServerPlayer p)p.sendOverlayMessage(Component.translatable("message.tnd.shardling_tamed"));
            }
            return InteractionResult.SUCCESS;
        }
        if(isOwnedBy(player)&&stack.is(ModContent.RESONANT_SHARD.get())&&getHealth()<getMaxHealth()) {
            if(!level().isClientSide()){stack.consume(1,player);heal(8);playSound(SoundEvents.AMETHYST_BLOCK_RESONATE,.5f,1.5f);}
            return InteractionResult.SUCCESS;
        }
        if(isOwnedBy(player)&&stack.isEmpty()) {
            if(!level().isClientSide()) {
                setOrderedToSit(!isOrderedToSit());setInSittingPose(isOrderedToSit());navigation.stop();setTarget(null);
                if(player instanceof ServerPlayer p)p.sendOverlayMessage(Component.translatable(isOrderedToSit()?"message.tnd.shardling_wait":"message.tnd.shardling_follow"));
            }
            return InteractionResult.SUCCESS;
        }
        if(player instanceof ServerPlayer p&&!isTame()&&stack.isEmpty())p.sendOverlayMessage(Component.translatable("message.tnd.shardling_hint"));
        else if(player instanceof ServerPlayer p&&isOwnedBy(player)&&stack.is(ModContent.RESONANT_SHARD.get()))p.sendOverlayMessage(Component.translatable("message.tnd.shardling_healthy"));
        return super.mobInteract(player,hand);
    }
    @Override public boolean canAttack(LivingEntity target) {
        if(!isTame()||isOrderedToSit()||target instanceof Player)return false;
        if(target instanceof TamableAnimal pet&&pet.isTame())return false;
        return super.canAttack(target);
    }
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.AMETHYST_BLOCK_HIT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.AMETHYST_BLOCK_BREAK;}
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if(!isTame()||isOrderedToSit()||tickCount%200!=0||!(getOwner() instanceof ServerPlayer owner)||distanceToSqr(owner)>256)return;
        // One small loaded-only survey every ten seconds. No automatic mining or XP.
        BlockPos best=null;double distance=Double.MAX_VALUE;
        for(BlockPos pos:BlockPos.betweenClosed(blockPosition().offset(-6,-4,-6),blockPosition().offset(6,4,6))) {
            if(!level.hasChunk(pos.getX()>>4,pos.getZ()>>4)||!level.getBlockState(pos).is(ModContent.RESONANT_ORE.get()))continue;
            double d=pos.distSqr(blockPosition());if(d<distance){best=pos.immutable();distance=d;}
        }
        if(best!=null) {
            getLookControl().setLookAt(best.getX()+.5,best.getY()+.5,best.getZ()+.5);
            for(int i=1;i<=8;i++) {
                double t=i/8.0;
                level.sendParticles(owner,ParticleTypes.ENCHANT,false,false,getX()+(best.getX()+.5-getX())*t,
                        getEyeY()+(best.getY()+.5-getEyeY())*t,getZ()+(best.getZ()+.5-getZ())*t,1,.04,.04,.04,0);
            }
        }
    }
}
