package fr.axephir.tnd.city;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.*;

/** Nacre security: snapshots aim, collision-respecting movement and a visible recovery after each attack. */
public final class CityMob extends Monster {
    public enum Kind { ENFORCER("neon_enforcer",28,54,.30,9,6), DRONE("suture_drone",40,24,.26,4,2), REAVER("signal_reaver",24,36,.36,7,3), MARSHAL("nacre_marshal",40,180,.25,12,10), SURGEON("choir_surgeon",44,140,.28,8,6), PROWLER("glass_prowler",30,125,.40,9,4);
        public final String id;public final int windup;final double hp,speed,damage,armor;
        Kind(String id,int w,double h,double s,double d,double a){this.id=id;windup=w;hp=h;speed=s;damage=d;armor=a;}
    }
    private static final EntityDataAccessor<Integer> CHARGE=SynchedEntityData.defineId(CityMob.class,EntityDataSerializers.INT),RECOVERY=SynchedEntityData.defineId(CityMob.class,EntityDataSerializers.INT);
    private final Kind kind;
    private Vec3 mark=Vec3.ZERO,heading=Vec3.ZERO;
    private int burst;private BlockPos post;
    private final net.minecraft.server.level.ServerBossEvent bossBar=new net.minecraft.server.level.ServerBossEvent(java.util.UUID.randomUUID(),net.minecraft.network.chat.Component.empty(),net.minecraft.world.BossEvent.BossBarColor.PURPLE,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> struck=new HashSet<>();
    public CityMob(EntityType<? extends CityMob> type,Level world,Kind kind){super(type,world);this.kind=kind;xpReward=kind.ordinal()>=3?80:kind==Kind.ENFORCER?16:10;
        if(kind==Kind.DRONE){moveControl=new FlyingMoveControl<>(this,20,true);navigation=new FlyingPathNavigation(this,world);setNoGravity(true);}
    }
    public static AttributeSupplier.Builder attributes(Kind k){return createMonsterAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FLYING_SPEED,.35).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.ARMOR,k.armor).add(Attributes.SCALE,k==Kind.MARSHAL?1.25:1).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,k==Kind.ENFORCER?.5:0);}
    public boolean boss(){return kind.ordinal()>=3;}public boolean heavy(){return kind==Kind.ENFORCER||kind==Kind.MARSHAL;}public boolean ranged(){return kind==Kind.DRONE||kind==Kind.SURGEON;}public boolean agile(){return kind==Kind.REAVER||kind==Kind.PROWLER;}
    public Kind kind(){return kind;}public int charge(){return entityData.get(CHARGE);}public int recovery(){return entityData.get(RECOVERY);}public Vec3 mark(){return mark;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CHARGE,0);b.define(RECOVERY,0);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            public boolean canUse(){return getTarget() instanceof Player p&&valid(p);}
            public boolean requiresUpdateEveryTick(){return true;}
            public void tick(){var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,25,25);
                if(charge()>0||burst>0){navigation.stop();return;}
                if(post!=null&&distanceToSqr(Vec3.atCenterOf(post))>32*32){navigation.moveTo(post.getX()+.5,post.getY(),post.getZ()+.5,1.1);return;}
                double range=ranged()?225:49;
                if(distanceToSqr(t)>range||!hasLineOfSight(t)){
                    if(ranged())navigation.moveTo(t.getX(),t.getY()+2.5,t.getZ(),1);else navigation.moveTo(t,1);
                }else if(recovery()==0)beginAttack(t);
                else if(kind==Kind.DRONE)moveControl.setWantedPosition(getX(),t.getY()+2.5,getZ(),.65);
                else if(agile()&&recovery()>10){double a=Math.atan2(getZ()-t.getZ(),getX()-t.getX())+(getId()%2==0?.8:-.8);navigation.moveTo(t.getX()+Math.cos(a)*5,t.getY(),t.getZ()+Math.sin(a)*5,1.1);}
            }
        });
        goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,.6));goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,16));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean valid(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
    public boolean beginAttack(LivingEntity t){
        if(!(t instanceof Player p)||!valid(p)||charge()>0||recovery()>0||burst>0||distanceToSqr(t)>(ranged()?400:64)||!hasLineOfSight(t))return false;
        setTarget(p);mark=ranged()?t.getEyePosition():t.position();heading=mark.subtract(position()).multiply(1,0,1).normalize();struck.clear();navigation.stop();entityData.set(CHARGE,kind.windup);
        cue(ranged()?"city_drone":heavy()?"city_alarm":"city_dash",.65f,agile()?.65f:1);return true;
    }
    @Override protected void customServerAiStep(ServerLevel w){
        super.customServerAiStep(w);if(boss()){bossBar.setName(getDisplayName());bossBar.setProgress(getHealth()/getMaxHealth());if(tickCount%20==0)for(var player:w.players()){if(player.isAlive()&&Math.abs(player.getY()-getY())<14&&distanceToSqr(player)<32*32)bossBar.addPlayer(player);else bossBar.removePlayer(player);}}if(recovery()>0)entityData.set(RECOVERY,recovery()-1);
        if(charge()>0){
            int c=charge();if(c%4==0){
                if(ranged())beam(w,getEyePosition(),mark,true);
                else for(int i=0;i<20;i++){double a=i*Math.PI/10;double radius=kind==Kind.MARSHAL?4:heavy()?1.8:1.1;w.sendParticles(ParticleTypes.ELECTRIC_SPARK,mark.x+Math.cos(a)*radius,mark.y+.1,mark.z+Math.sin(a)*radius,1,0,.015,0,0);}
            }
            entityData.set(CHARGE,c-1);if(c==1){burst=ranged()?13:heavy()?8:10;entityData.set(RECOVERY,ranged()?90:boss()?75:55);
                if(kind==Kind.SURGEON)for(var ally:w.getEntitiesOfClass(CityMob.class,getBoundingBox().inflate(10),a->a!=this&&a.isAlive()&&hasLineOfSight(a)).stream().limit(6).toList()){ally.heal(6);w.sendParticles(ParticleTypes.HAPPY_VILLAGER,ally.getX(),ally.getEyeY(),ally.getZ(),8,.3,.3,.3,0);}}
        }else if(burst>0){
            if(ranged()){if(burst%6==1)beam(w,getEyePosition(),mark,false);}
            else {
                Vec3 before=position();double speed=agile()?.95:.65;
                // Native collision resolution prevents crossing walls or deleting terrain.
                move(MoverType.SELF,heading.scale(Math.min(speed,Math.sqrt(position().subtract(mark).multiply(1,0,1).lengthSqr()))));
                if(horizontalCollision)burst=1;
                double radius=kind==Kind.MARSHAL?4:agile()?1.2:1.8;
                for(var p:w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius,1,radius),this::valid))if(!struck.contains(p.getUUID())&&hasLineOfSight(p)&&p.position().distanceToSqr(position())<radius*radius+2){
                    if(p.hurtServer(w,damageSources().mobAttack(this),(float)kind.damage)){struck.add(p.getUUID());p.knockback(.55,-heading.x,-heading.z,damageSources().mobAttack(this),1);cue("city_impact",.6f,1);}
                }
                if(burst==1)w.sendParticles(ParticleTypes.CRIT,getX(),getY()+1,getZ(),20,.6,.5,.6,.04);
            }burst--;
        }
    }
    private void beam(ServerLevel w,Vec3 start,Vec3 aim,boolean warning){
        Vec3 end=w.clip(new ClipContext(start,aim,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getLocation();Vec3 delta=end.subtract(start);double length=delta.length();
        for(double d=0;d<length;d+=warning?.9:.3){Vec3 v=start.add(delta.scale(d/Math.max(.001,length)));w.sendParticles(warning?ParticleTypes.END_ROD:ParticleTypes.ELECTRIC_SPARK,v.x,v.y,v.z,1,0,0,0,0);}
        if(warning)return;cue("city_laser",.6f,1);
        for(var p:w.getEntitiesOfClass(Player.class,new AABB(start,end).inflate(.7),this::valid)){
            Vec3 center=p.getBoundingBox().getCenter();double t=Math.clamp(center.subtract(start).dot(delta)/Math.max(.001,delta.lengthSqr()),0,1);
            if(p.getBoundingBox().inflate(.12).clip(start,end).isPresent()&&hasLineOfSight(p))p.hurtServer(w,damageSources().mobAttack(this),4);
        }
    }
    private void cue(String name,float volume,float pitch){playSound(ModContent.atmosphereSound(name),volume,pitch);}
    @Override public boolean hurtServer(ServerLevel w,DamageSource source,float amount){boolean hit=super.hurtServer(w,source,amount);if(hit&&(agile()&&amount>=4||kind==Kind.SURGEON&&amount>=8)&&charge()>0){entityData.set(CHARGE,0);entityData.set(RECOVERY,40);burst=0;w.sendParticles(ParticleTypes.CRIT,getX(),getEyeY(),getZ(),16,.4,.4,.4,.05);}return hit;}
    @Override public void travel(Vec3 input){if(kind==Kind.DRONE)travelFlying(input,.2f);else super.travel(input);}
    @Override protected void checkFallDamage(double y,boolean onGround,BlockState state,BlockPos p){if(kind!=Kind.DRONE)super.checkFallDamage(y,onGround,state,p);}
    @Override protected void playStepSound(BlockPos p,BlockState s){if(kind!=Kind.DRONE)cue("city_footstep",.16f,kind==Kind.ENFORCER?.8f:1.2f);}
    @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.ANCIENT_DEBRIS_HIT;}
    @Override protected SoundEvent getDeathSound(){return ModContent.atmosphereSound("city_impact");}
    public void bindPost(BlockPos at){post=at.immutable();}
    @Override public void startSeenByPlayer(net.minecraft.server.level.ServerPlayer p){super.startSeenByPlayer(p);if(boss()&&Math.abs(p.getY()-getY())<14&&distanceToSqr(p)<32*32)bossBar.addPlayer(p);}
    @Override public void stopSeenByPlayer(net.minecraft.server.level.ServerPlayer p){super.stopSeenByPlayer(p);bossBar.removePlayer(p);}
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);if(post!=null)out.putLong("AssaultPost",post.asLong());}
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);post=in.getLong("AssaultPost").map(BlockPos::of).orElse(null);entityData.set(CHARGE,0);entityData.set(RECOVERY,20);burst=0;struck.clear();}
}
