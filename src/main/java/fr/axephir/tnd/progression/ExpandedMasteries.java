package fr.axephir.tnd.progression;

import fr.axephir.tnd.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Successful player actions only. No mining XP is attached to automated machine output. */
public final class ExpandedMasteries {
    private static int rank(ServerPlayer p,Mastery m,Talent t){return p.getData(PlayerProgression.PROFILE).get(m).rank(t);}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void fish(ItemFishedEvent e) {
        if(e.isCanceled()||e.getDrops().isEmpty()||!(e.getEntity() instanceof ServerPlayer p)||p.isCreative())return;
        ProgressionService.award(p,Mastery.FISHING,24);
        if(p.getRandom().nextDouble()<rank(p,Mastery.FISHING,Talent.YIELD)*0.02)e.getDrops().add(e.getDrops().getFirst().copyWithCount(1));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void birth(BabyEntitySpawnEvent e) {
        if(e.isCanceled()||!(e.getCausedByPlayer() instanceof ServerPlayer p)||e.getChild()==null||p.isCreative())return;
        ProgressionService.award(p,Mastery.BREEDING,35);
        var health=e.getChild().getAttribute(Attributes.MAX_HEALTH);
        int r=rank(p,Mastery.BREEDING,Talent.EFFICIENCY);
        if(health!=null&&r>0) {
            health.addOrReplacePermanentModifier(new AttributeModifier(id("healthy_offspring"),r*0.04,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            e.getChild().setHealth(e.getChild().getMaxHealth());
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void kill(LivingDeathEvent e) {
        if(e.isCanceled()||e.getEntity() instanceof fr.axephir.tnd.entities.FracturedCustodian||e.getEntity() instanceof fr.axephir.tnd.entities.RiftSentinel sentinel&&sentinel.isTrialGuardian()||!(e.getSource().getEntity() instanceof ServerPlayer p))return;
        if(e.getEntity() instanceof Animal&&!(e.getEntity() instanceof fr.axephir.tnd.entities.Shardling))ProgressionService.award(p,Mastery.HUNTING,25);
        else if(e.getEntity() instanceof Enemy)ProgressionService.award(p,e.getSource().getDirectEntity() instanceof Projectile?Mastery.RANGED:Mastery.MELEE,
                20+(long)Math.min(100,e.getEntity().getMaxHealth()));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void huntDrops(LivingDropsEvent e) {
        if(e.isCanceled()||e.getDrops().isEmpty()||!(e.getEntity() instanceof Animal)||e.getEntity() instanceof fr.axephir.tnd.entities.Shardling||!(e.getSource().getEntity() instanceof ServerPlayer p)||p.isCreative())return;
        if(p.getRandom().nextDouble()<rank(p,Mastery.HUNTING,Talent.YIELD)*0.02) {
            var first=e.getDrops().iterator().next();
            e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(p.level(),first.getX(),first.getY(),first.getZ(),first.getItem().copyWithCount(1)));
        }
    }
    @SubscribeEvent
    public void combat(LivingIncomingDamageEvent e) {
        if(e.getSource().getEntity() instanceof ServerPlayer p&&!p.isCreative()&&!(e.getEntity() instanceof net.minecraft.world.entity.player.Player)) {
            Mastery m=e.getSource().getDirectEntity() instanceof Projectile?Mastery.RANGED:Mastery.MELEE;
            double bonus=rank(p,m,Talent.EFFICIENCY)*0.03;
            if(e.getEntity() instanceof Animal)bonus+=rank(p,Mastery.HUNTING,Talent.EFFICIENCY)*0.02;
            e.setAmount((float)(e.getAmount()*(1+bonus)));
        }
        if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof Mob)
            e.setAmount(e.getAmount()*(1-rank(p,Mastery.DEFENSE,Talent.EFFICIENCY)*0.02f));
    }
    @SubscribeEvent
    public void defended(LivingDamageEvent.Post e) {
        if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof Mob
                && e.getInflictedDamage()+e.getBlockedDamage()>0&&ProgressionService.claim(p,"defense",100,1))
            ProgressionService.award(p,Mastery.DEFENSE,Math.min(12,2+(long)Math.ceil(e.getInflictedDamage()+e.getBlockedDamage())));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void shielded(LivingShieldBlockEvent e) {
        if(e.getEntity() instanceof ServerPlayer p&&e.getDamageSource().getEntity() instanceof Mob
                &&e.getBlocked()&&e.getBlockedDamage()>0&&ProgressionService.claim(p,"defense",100,1))
            ProgressionService.award(p,Mastery.DEFENSE,Math.min(12,2+(long)Math.ceil(e.getBlockedDamage())));
    }
    @SubscribeEvent
    public void brewed(PlayerBrewedPotionEvent e) {
        if(e.getEntity() instanceof ServerPlayer p&&ProgressionService.claim(p,"brew/"+e.getStack().getComponents().hashCode(),24000,8))
            ProgressionService.award(p,Mastery.ALCHEMY,24);
    }
    @SubscribeEvent
    public void drink(LivingEntityUseItemEvent.Finish e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isCreative())return;
        var contents=e.getItem().get(DataComponents.POTION_CONTENTS);
        if(contents==null)return;
        if(ProgressionService.claim(p,"potion/"+contents.hashCode(),24000,12))ProgressionService.award(p,Mastery.ALCHEMY,8);
        int rank=rank(p,Mastery.ALCHEMY,Talent.EFFICIENCY);
        if(rank==0)return;
        for(var effect:contents.getAllEffects()) {
            var current=p.getEffect(effect.getEffect());
            if(!effect.getEffect().value().isBeneficial()||effect.isInfiniteDuration()||current==null
                    ||current.getAmplifier()!=effect.getAmplifier()||current.getDuration()>effect.getDuration()+2)continue;
            p.addEffect(new MobEffectInstance(effect.getEffect(),(int)Math.min(Integer.MAX_VALUE,effect.getDuration()*(1+rank*0.05)),
                    effect.getAmplifier(),effect.isAmbient(),effect.isVisible(),effect.showIcon()));
        }
    }
    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        boolean active=!p.isCreative()&&!p.isSpectator()&&p.isAlive();
        modifier(p,Attributes.MOVEMENT_SPEED,"explorer_stride",active?rank(p,Mastery.EXPLORATION,Talent.EFFICIENCY)*0.015:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.BLOCK_INTERACTION_RANGE,"builder_reach",active&&p.getMainHandItem().getItem() instanceof BlockItem?rank(p,Mastery.BUILDING,Talent.EFFICIENCY)*0.15:0,AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.LUCK,"angler_luck",active&&p.getMainHandItem().is(Items.FISHING_ROD)?rank(p,Mastery.FISHING,Talent.EFFICIENCY)*0.2:0,AttributeModifier.Operation.ADD_VALUE);
        if(!active)return;
        var pos=p.blockPosition();
        String region=p.level().dimension().identifier()+"/"+Math.floorDiv(pos.getX(),64)+"/"+Math.floorDiv(pos.getZ(),64);
        if(ProgressionService.claim(p,"region/"+region,Long.MAX_VALUE,1))ProgressionService.award(p,Mastery.EXPLORATION,12);
        if(p.tickCount%100==0&&p.getRandom().nextFloat()<rank(p,Mastery.FARMING,Talent.EFFICIENCY)*0.04) {
            for(int i=0;i<8;i++) {
                BlockPos at=pos.offset(p.getRandom().nextInt(11)-5,p.getRandom().nextInt(5)-2,p.getRandom().nextInt(11)-5);
                if(!p.level().hasChunk(at.getX()>>4,at.getZ()>>4))continue;
                var state=p.level().getBlockState(at);
                if(state.getBlock() instanceof CropBlock crop&&!crop.isMaxAge(state)) {crop.growCrops(p.level(),at,state);break;}
            }
        }
    }
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath("tnd",name);}
    private static void modifier(ServerPlayer p,Holder<Attribute> type,String key,double value,AttributeModifier.Operation op) {
        var instance=p.getAttribute(type);if(instance==null)return;
        var id=id(key);var old=instance.getModifier(id);
        if(value==0){if(old!=null)instance.removeModifier(id);}
        else if(old==null||old.amount()!=value)instance.addOrUpdateTransientModifier(new AttributeModifier(id,value,op));
    }
}
