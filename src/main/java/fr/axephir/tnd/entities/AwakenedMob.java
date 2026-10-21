package fr.axephir.tnd.entities;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.VestigeTime;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.*;

/** Two silhouettes and attack rhythms, sharing bounded, server-authoritative telegraphs. */
public final class AwakenedMob extends Monster {
    private static final EntityDataAccessor<Integer> CHARGE=SynchedEntityData.defineId(AwakenedMob.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECOVERY=SynchedEntityData.defineId(AwakenedMob.class,EntityDataSerializers.INT);
    private final boolean heavy;
    private List<Vec3> marks=List.of();
    private int burst;
    private long provokedUntil;
    private boolean alternate,previousAwake;
    public AwakenedMob(EntityType<? extends AwakenedMob> type,Level world,boolean heavy){super(type,world);this.heavy=heavy;xpReward=heavy?18:9;}
    public static AttributeSupplier.Builder attributes(boolean heavy){return createMonsterAttributes().add(Attributes.MAX_HEALTH,heavy?72:30).add(Attributes.MOVEMENT_SPEED,heavy?.21:.30).add(Attributes.ATTACK_DAMAGE,heavy?8:4).add(Attributes.ARMOR,heavy?6:1).add(Attributes.KNOCKBACK_RESISTANCE,heavy?.65:0).add(Attributes.FOLLOW_RANGE,24);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CHARGE,0);b.define(RECOVERY,0);}
    public boolean heavy(){return heavy;}
    public int charge(){return entityData.get(CHARGE);}
    public int recovery(){return entityData.get(RECOVERY);}
    public List<Vec3> marks(){return List.copyOf(marks);}
    public boolean awake(){return !heavy||VestigeTime.isNight(level())||level().getGameTime()<provokedUntil;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            @Override public boolean canUse(){return awake()&&getTarget()!=null&&getTarget().isAlive();}
            @Override public boolean requiresUpdateEveryTick(){return true;}
            @Override public void tick(){var target=getTarget();if(target==null)return;getLookControl().setLookAt(target,25,25);
                if(charge()>0||burst>0){navigation.stop();return;}
                if(distanceToSqr(target)>(heavy?36:81))navigation.moveTo(target,1);
                else if(recovery()==0&&hasLineOfSight(target)){navigation.stop();beginAttack(target);}
                else if(!heavy&&distanceToSqr(target)<9){var away=position().subtract(target.position()).normalize();navigation.moveTo(getX()+away.x*3,getY(),getZ()+away.z*3,.8);}
            }
        });
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.55){@Override public boolean canUse(){return awake()&&super.canUse();}});
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true){@Override public boolean canUse(){return awake()&&super.canUse();}});
    }
    public boolean beginAttack(LivingEntity target){
        if(charge()>0||burst>0||recovery()>0||!target.isAlive()||distanceToSqr(target)>144||!hasLineOfSight(target))return false;
        var center=target.position();var forward=center.subtract(position()).multiply(1,0,1).normalize();var side=new Vec3(-forward.z,0,forward.x);
        if(heavy){marks=alternate?List.of(center.add(side.scale(-3)),center,center.add(side.scale(3))):List.of(center);alternate=!alternate;}
        else marks=List.of(center.add(side.scale(-2.2)),center,center.add(side.scale(2.2)));
        entityData.set(CHARGE,heavy?40:32);navigation.stop();
        cue(heavy?"lithoguard_charge":"stalker_charge",.65f,1);return true;
    }
    @Override protected void customServerAiStep(ServerLevel world){
        super.customServerAiStep(world);
        boolean active=awake();if(heavy&&active&&!previousAwake)cue("lithoguard_wake",.65f,1);previousAwake=active;
        if(!active){setTarget(null);navigation.stop();marks=List.of();entityData.set(CHARGE,0);return;}
        if(recovery()>0)entityData.set(RECOVERY,recovery()-1);
        int charge=charge();
        if(charge>0){
            if(charge%4==0)for(var mark:marks)ring(world,mark,heavy?(marks.size()==1?3.5:2):1.05,heavy&&marks.size()==1?24:12);
            entityData.set(CHARGE,charge-1);
            if(charge==1){burst=heavy?1:9;entityData.set(RECOVERY,heavy?65:55);}
        }else if(burst>0){
            if(heavy){for(var mark:marks)impact(world,mark,marks.size()==1?3.5:2,8);}
            else if(burst%3==0){int index=3-burst/3;impact(world,marks.get(index),1.05,4);}
            burst--;if(burst==0)marks=List.of();
        }
    }
    private void ring(ServerLevel world,Vec3 mark,double radius,int points){
        for(int i=0;i<points;i++){double a=i*Math.PI*2/points+charge()*.012;world.sendParticles(heavy?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.END_ROD,mark.x+Math.cos(a)*radius,mark.y+.18,mark.z+Math.sin(a)*radius,1,0,.015,0,0);}
        if(charge()<12)world.sendParticles(ParticleTypes.CRIT,mark.x,mark.y+.2,mark.z,3,.15,.15,.15,.015);
    }
    private void impact(ServerLevel world,Vec3 center,double radius,float damage){
        cue(heavy?"lithoguard_impact":"stalker_shot",heavy?.8f:.5f,1);
        world.sendParticles(heavy?ParticleTypes.EXPLOSION:ParticleTypes.CRIT,center.x,center.y+.3,center.z,heavy?1:12,.2,.2,.2,.05);
        for(var p:world.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(radius,2,radius),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator())){
            if(p.position().subtract(center).multiply(1,0,1).lengthSqr()<=radius*radius&&hasLineOfSight(p)&&p.hurtServer(world,damageSources().mobAttack(this),damage)){
                p.knockback(heavy?.8:.25,center.x-p.getX(),center.z-p.getZ(),damageSources().mobAttack(this),1);
            }
        }
    }
    private void cue(String name,float volume,float pitch){playSound(ModContent.atmosphereSound(name),volume,pitch);}
    @Override public boolean hurtServer(ServerLevel world,DamageSource source,float amount){
        boolean hit=super.hurtServer(world,source,amount);
        if(hit&&source.getEntity() instanceof LivingEntity){provokedUntil=world.getGameTime()+400;
            if(!heavy&&charge()>0&&amount>=3){marks=List.of();entityData.set(CHARGE,0);entityData.set(RECOVERY,40);world.sendParticles(ParticleTypes.CRIT,getX(),getEyeY(),getZ(),16,.4,.4,.4,.06);}
        }return hit;
    }
    @Override protected void playStepSound(BlockPos p,BlockState s){if(heavy)cue("lithoguard_step",.25f,.9f+random.nextFloat()*.2f);else super.playStepSound(p,s);}
    @Override protected SoundEvent getHurtSound(DamageSource source){return heavy?SoundEvents.ANCIENT_DEBRIS_HIT:ModContent.atmosphereSound("stalker_hurt");}
    @Override protected SoundEvent getDeathSound(){return ModContent.atmosphereSound(heavy?"lithoguard_fall":"stalker_fall");}
    @Override public void die(DamageSource source){boolean wasDead=dead;super.die(source);if(!wasDead&&dead&&level() instanceof ServerLevel world)world.sendParticles(ParticleTypes.END_ROD,getX(),getY()+1,getZ(),heavy?36:18,.8,.8,.8,.05);}
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);out.putBoolean("Alternate",alternate);}
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);alternate=in.getBooleanOr("Alternate",false);marks=List.of();burst=0;entityData.set(CHARGE,0);entityData.set(RECOVERY,20);}
}
