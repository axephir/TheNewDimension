package fr.axephir.tnd.entities;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.dungeon.VaultCoreEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Finite sanctuary boss. Attacks snapshot their geometry; walls, exits and pets are preserved. */
public final class FracturedCustodian extends Monster {
    private static final EntityDataAccessor<Integer> CHARGE=SynchedEntityData.defineId(FracturedCustodian.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPOSED=SynchedEntityData.defineId(FracturedCustodian.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED=SynchedEntityData.defineId(FracturedCustodian.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(UUID.randomUUID(),Component.empty(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    private BlockPos home;
    private String token="";
    private Vec3 impact,second;
    private boolean ring,nextRing;
    private int recovery=40;
    private float stagger;
    public FracturedCustodian(EntityType<? extends FracturedCustodian> type,Level level){super(type,level);xpReward=0;}
    public static AttributeSupplier.Builder attributes(){return createMonsterAttributes().add(Attributes.MAX_HEALTH,120)
            .add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.FOLLOW_RANGE,28)
            .add(Attributes.KNOCKBACK_RESISTANCE,1);}
    public void bind(BlockPos pos,String key){home=pos.immutable();token=key;setPersistenceRequired();}
    public String trialToken(){return token;}
    public boolean bound(){return home!=null;}
    public int charge(){return entityData.get(CHARGE);}
    public int exposed(){return entityData.get(EXPOSED);}
    public boolean enraged(){return entityData.get(ENRAGED);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CHARGE,0);b.define(EXPOSED,0);b.define(ENRAGED,false);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            @Override public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
            @Override public boolean requiresUpdateEveryTick(){return true;}
            @Override public void tick(){var p=getTarget();if(p==null)return;getLookControl().setLookAt(p,30,30);
                if(charge()>0||exposed()>0){navigation.stop();return;}
                if(distanceToSqr(p)>100)navigation.moveTo(p,1);else{navigation.stop();if(recovery==0&&hasLineOfSight(p)){beginAttack(p,nextRing);nextRing=!nextRing;}}
            }
        });
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    public boolean beginAttack(LivingEntity target,boolean crown){
        if(charge()>0||exposed()>0||!target.isAlive()||distanceToSqr(target)>144||!hasLineOfSight(target))return false;
        ring=crown;impact=crown?position():target.position();
        var look=new Vec3(target.getLookAngle().x,0,target.getLookAngle().z);
        if(look.lengthSqr()<.01)look=new Vec3(0,0,1);
        second=!crown&&enraged()?impact.add(look.normalize().scale(3)):null;
        entityData.set(CHARGE,crown?48:40);stagger=0;navigation.stop();
        playSound(ModContent.atmosphereSound(crown?"custodian_crown":"custodian_fracture"),1,.95f);
        announce(crown?"custodian_ring":"custodian_mark");return true;
    }
    private void announce(String key){if(level() instanceof ServerLevel w)for(var p:w.players())if(!p.isSpectator()&&distanceToSqr(p)<32*32)p.sendOverlayMessage(Component.translatable("message.tnd."+key));}
    private void circle(ServerLevel w,Vec3 c,double radius){for(int i=0;i<40;i++){double a=i*Math.PI/20;w.sendParticles(ParticleTypes.END_ROD,c.x+Math.cos(a)*radius,c.y+.12,c.z+Math.sin(a)*radius,1,0,0,0,0);}}
    private boolean inside(Vec3 p,Vec3 c,boolean crown){double dx=p.x-c.x,dz=p.z-c.z,d=dx*dx+dz*dz;return Math.abs(p.y-c.y)<2.5&&(crown?d>=2.5*2.5&&d<=49:d<=2.8*2.8);}
    @Override protected void customServerAiStep(ServerLevel world){
        if(home!=null&&tickCount%20==0&&world.hasChunk(home.getX()>>4,home.getZ()>>4)){
            if(!(world.getBlockEntity(home) instanceof VaultCoreEntity core)||!core.acceptsBoss(getUUID(),token)||home.distToCenterSqr(position())>48*48){discard();return;}
        }
        super.customServerAiStep(world);
        if(!enraged()&&getHealth()<=getMaxHealth()/2){entityData.set(ENRAGED,true);announce("custodian_phase");playSound(ModContent.atmosphereSound("custodian_awaken"),.8f,1.2f);}
        if(tickCount%10==0)updateBar(world);
        if(recovery>0)recovery--;
        if(exposed()>0){entityData.set(EXPOSED,exposed()-1);if(tickCount%8==0)world.sendParticles(ParticleTypes.ENCHANT,getX(),getY()+1.6,getZ(),6,.7,.7,.7,.05);}
        int n=charge();if(n==0||impact==null)return;
        if(n%4==0){circle(world,impact,ring?7:2.8);if(ring)circle(world,impact,2.5);if(second!=null)circle(world,second,2.8);}
        entityData.set(CHARGE,n-1);
        if(n==1){
            world.sendParticles(ParticleTypes.SONIC_BOOM,impact.x,impact.y+.7,impact.z,1,0,0,0,0);
            playSound(ModContent.atmosphereSound("custodian_impact"),1,1);
            for(var p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(24),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()))
                if((inside(p.position(),impact,ring)||second!=null&&inside(p.position(),second,false))&&hasLineOfSight(p))p.hurtServer(world,damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));
            openCore(60);impact=second=null;
        }
    }
    private void openCore(int ticks){entityData.set(CHARGE,0);entityData.set(EXPOSED,ticks);impact=second=null;recovery=ticks+30;stagger=0;announce("custodian_open");}
    @Override public boolean hurtServer(ServerLevel world,DamageSource source,float damage){
        boolean open=exposed()>0;boolean hurt=super.hurtServer(world,source,source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)?damage:damage*(open?1.4f:.55f));
        if(hurt&&charge()>0&&source.getEntity() instanceof LivingEntity){stagger+=damage;if(stagger>=18){openCore(80);playSound(SoundEvents.GLASS_BREAK,.8f,.7f);}}
        return hurt;
    }
    private void updateBar(ServerLevel world){
        bar.setName(Component.translatable(exposed()>0?"message.tnd.custodian_bar_open":"entity.tnd.fractured_custodian"));bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));
        var players=world.players().stream().filter(p->!p.isSpectator()&&distanceToSqr(p)<40*40).toList();
        for(var p:List.copyOf(bar.getPlayers()))if(!players.contains(p))bar.removePlayer(p);players.forEach(bar::addPlayer);
    }
    @Override public void die(DamageSource source){boolean wasDead=dead;super.die(source);if(!wasDead&&dead){bar.removeAllPlayers();if(home!=null&&level() instanceof ServerLevel w&&w.hasChunk(home.getX()>>4,home.getZ()>>4)&&w.getBlockEntity(home) instanceof VaultCoreEntity v)v.bossDefeated(getUUID(),token);}}
    @Override public void remove(RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
    @Override protected boolean shouldDropLoot(ServerLevel w){return false;}
    @Override protected int getBaseExperienceReward(ServerLevel w){return 0;}
    @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.ANCIENT_DEBRIS_HIT;}
    @Override protected SoundEvent getDeathSound(){return ModContent.atmosphereSound("custodian_fall");}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);if(home!=null){out.store("sanctuaryHome",BlockPos.CODEC,home);out.putString("custodianToken",token);}out.putBoolean("crownNext",nextRing);}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);home=in.read("sanctuaryHome",BlockPos.CODEC).orElse(null);token=in.getStringOr("custodianToken","");nextRing=in.getBooleanOr("crownNext",false);entityData.set(CHARGE,0);entityData.set(EXPOSED,0);recovery=60;}
}
