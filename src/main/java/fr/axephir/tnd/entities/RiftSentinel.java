package fr.axephir.tnd.entities;

import fr.axephir.tnd.world.VestigeTime;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** Locks an impact point, announces it, then strikes. Move away or interrupt it. */
public final class RiftSentinel extends Monster {
    private static final EntityDataAccessor<Integer> WINDUP=SynchedEntityData.defineId(RiftSentinel.class,EntityDataSerializers.INT);
    private net.minecraft.core.BlockPos trialHome;
    private String trialToken="";
    public String trialToken(){return trialToken;}
    public boolean isTrialGuardian(){return trialHome!=null;}
    public void bindTrial(net.minecraft.core.BlockPos home,String token){trialHome=home.immutable();trialToken=token;setPersistenceRequired();}
    @Override protected boolean shouldDropLoot(ServerLevel level){return !isTrialGuardian()&&super.shouldDropLoot(level);}
    @Override protected int getBaseExperienceReward(ServerLevel level){return isTrialGuardian()?0:super.getBaseExperienceReward(level);}
    @Override public void die(DamageSource source){
        boolean wasDead=dead;super.die(source);
        if(!wasDead&&dead&&!isTrialGuardian()&&source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player&&player.level()==level())fr.axephir.tnd.expedition.ExpeditionService.sentinelKilled(player);
        if(!wasDead&&dead&&trialHome!=null&&level() instanceof ServerLevel world&&world.hasChunk(trialHome.getX()>>4,trialHome.getZ()>>4)
                &&world.getBlockEntity(trialHome) instanceof fr.axephir.tnd.dungeon.VaultCoreEntity vault)vault.defeated(getUUID(),trialToken);
    }
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){
        super.addAdditionalSaveData(out);if(trialHome!=null){out.store("trialHome",net.minecraft.core.BlockPos.CODEC,trialHome);out.putString("trialToken",trialToken);}
    }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){
        super.readAdditionalSaveData(in);trialHome=in.read("trialHome",net.minecraft.core.BlockPos.CODEC).orElse(null);trialToken=in.getStringOr("trialToken","");
    }
    private int recovery;
    private long provokedUntil;
    private Vec3 impact;
    public RiftSentinel(EntityType<? extends RiftSentinel> type,Level level){super(type,level);xpReward=8;}
    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.23)
                .add(Attributes.ATTACK_DAMAGE,5).add(Attributes.ARMOR,2).add(Attributes.FOLLOW_RANGE,16);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(WINDUP,0);}
    public int windup(){return entityData.get(WINDUP);}
    public boolean awake(){return isTrialGuardian()||VestigeTime.isNight(level())||level().getGameTime()<provokedUntil;}
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){
            {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));}
            @Override public boolean canUse(){return awake()&&getTarget()!=null&&getTarget().isAlive();}
            @Override public void tick(){
                var target=getTarget();if(target==null)return;
                getLookControl().setLookAt(target,30,30);
                if(windup()>0){navigation.stop();return;}
                if(distanceToSqr(target)>36)navigation.moveTo(target,1);
                else {navigation.stop();if(recovery==0&&hasLineOfSight(target))beginPulse(target);}
            }
            @Override public boolean requiresUpdateEveryTick(){return true;}
        });
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.5){@Override public boolean canUse(){return awake()&&super.canUse();}});
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true){@Override public boolean canUse(){return awake()&&super.canUse();}});
    }
    public boolean beginPulse(LivingEntity target) {
        if(windup()!=0||recovery>0||!target.isAlive()||distanceToSqr(target)>49||!hasLineOfSight(target))return false;
        impact=target.position();entityData.set(WINDUP,32);navigation.stop();
        playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE,.6f,1.3f);return true;
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        if(trialHome!=null&&tickCount%20==0&&level.hasChunk(trialHome.getX()>>4,trialHome.getZ()>>4)){
            if(!(level.getBlockEntity(trialHome) instanceof fr.axephir.tnd.dungeon.VaultCoreEntity vault)
                    ||!vault.accepts(getUUID(),trialToken)||trialHome.distToCenterSqr(position())>64*64){discard();return;}
        }
        super.customServerAiStep(level);
        if(!awake()){setTarget(null);navigation.stop();entityData.set(WINDUP,0);impact=null;}
        if(recovery>0)recovery--;
        int charge=windup();if(charge==0||impact==null)return;
        if(charge%4==0) {
            for(int i=0;i<20;i++) {
                double a=i*Math.PI/10;
                level.sendParticles(ParticleTypes.END_ROD,impact.x+Math.cos(a)*2.5,impact.y+.12,impact.z+Math.sin(a)*2.5,1,0,0,0,0);
            }
        }
        entityData.set(WINDUP,charge-1);
        if(charge==1) {
            var center=impact;impact=null;recovery=60;
            level.sendParticles(ParticleTypes.SONIC_BOOM,center.x,center.y+.5,center.z,1,0,0,0,0);
            playSound(SoundEvents.AMETHYST_BLOCK_BREAK,.8f,.65f);
            for(var player:level.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(2.5,2,2.5),p->!p.isCreative()&&!p.isSpectator()&&p.isAlive())) {
                if(player.position().distanceToSqr(center)<=6.25&&hasLineOfSight(player))
                    player.hurtServer(level,damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));
            }
        }
    }
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount) {
        boolean hurt=super.hurtServer(level,source,amount);
        if(hurt&&source.getEntity() instanceof LivingEntity) {
            provokedUntil=level.getGameTime()+200;
            if(windup()>0) {
                entityData.set(WINDUP,0);impact=null;recovery=40;
                level.sendParticles(ParticleTypes.CRIT,getX(),getEyeY(),getZ(),12,.3,.3,.3,.1);
                playSound(SoundEvents.GLASS_BREAK,.5f,1.6f);
            }
        }
        return hurt;
    }
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.ANCIENT_DEBRIS_HIT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.ANCIENT_DEBRIS_BREAK;}
}
