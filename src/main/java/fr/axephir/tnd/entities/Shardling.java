package fr.axephir.tnd.entities;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.familiar.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import java.util.UUID;

/** One unique symbiotic relic: hostile incubation subject, injured survivor, growing companion. */
public final class Shardling extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> DOWNED=SynchedEntityData.defineId(Shardling.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LEVEL=SynchedEntityData.defineId(Shardling.class,EntityDataSerializers.INT),BOND=SynchedEntityData.defineId(Shardling.class,EntityDataSerializers.INT),PULSE=SynchedEntityData.defineId(Shardling.class,EntityDataSerializers.INT);
    private BlockPos archiveAnchor;private long archiveUntil;
    public void holdArchive(BlockPos at,int ticks){archiveAnchor=at.immutable();archiveUntil=level().getGameTime()+ticks;}
    public void releaseArchive(){archiveAnchor=null;archiveUntil=0;}
    public boolean archiveHolding(){return archiveAnchor!=null&&archiveUntil>level().getGameTime()&&available()&&!isOrderedToSit()&&getTarget()==null&&getOwner()!=null&&distanceToSqr(getOwner())<80*80;}
    public void relicPulse(){entityData.set(PULSE,30);}
    private int protocols;public int protocols(){return protocols;}
    private int xp,guardCooldown,pulseCooldown;private boolean released;private UUID challenger;private BlockPos safe;
    public Shardling(EntityType<? extends Shardling> type,Level level){super(type,level);xpReward=0;setPersistenceRequired();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(DOWNED,false);b.define(LEVEL,1);b.define(BOND,0);b.define(PULSE,0);}
    public static AttributeSupplier.Builder attributes(){return createAnimalAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.ARMOR,0).add(Attributes.FOLLOW_RANGE,32);}
    public boolean downed(){return entityData.get(DOWNED);}public int familiarLevel(){return entityData.get(LEVEL);}public int familiarXp(){return xp;}public int bondTicks(){return entityData.get(BOND);}public int pulseTicks(){return entityData.get(PULSE);}
    public int evolution(){return stage(familiarLevel());}public static int stage(int level){return level>=20?2:level>=8?1:0;}public static int requiredXp(int level){return level>=30?0:60+25*level;}
    public boolean trial(){return released&&!isTame();}
    public void releaseFor(ServerPlayer player){released=true;challenger=player.getUUID();setTarget(player);setPersistenceRequired();safe=blockPosition();}
    @Override public void tame(Player player){
        if(!(player instanceof ServerPlayer p)||!FamiliarLedger.get(p.level().getServer()).claim(p,this))return;
        super.tame(player);released=true;setTarget(null);navigation.stop();setOrderedToSit(false);setInSittingPose(false);applyGrowth();FamiliarLedger.get(p.level().getServer()).snapshot(this);
    }
    private boolean authorized(){if(!isTame()||getOwnerReference()==null||!(level() instanceof ServerLevel w))return false;return FamiliarLedger.get(w.getServer()).owns(getOwnerReference().getUUID(),getUUID());}
    public boolean available(){return isTame()&&!downed()&&bondTicks()==0&&authorized();}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(1,new net.minecraft.world.entity.ai.goal.Goal(){
            {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));}
            public boolean canUse(){return archiveHolding();}
            public void tick(){if(archiveAnchor==null)return;if(distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(archiveAnchor))>2)getNavigation().moveTo(archiveAnchor.getX()+.5,archiveAnchor.getY(),archiveAnchor.getZ()+.5,1);else{getNavigation().stop();getLookControl().setLookAt(archiveAnchor.getX()+.5,archiveAnchor.getY()+1,archiveAnchor.getZ()-1);}}
        });
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,true));goalSelector.addGoal(3,new FollowOwnerGoal(this,1.15,7,2){
            private boolean here(){return available()&&getOwner()!=null&&getOwner().level()==level();}
            @Override public boolean canUse(){return here()&&super.canUse();}
            @Override public boolean canContinueToUse(){return here()&&super.canContinueToUse();}
        });
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.75));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));targetSelector.addGoal(2,new OwnerHurtTargetGoal(this));
    }
    @Override protected int getBaseExperienceReward(ServerLevel level){return 0;}
    @Override public boolean isFood(ItemStack stack){return false;}
    @Override public AgeableMob getBreedOffspring(ServerLevel w,AgeableMob other){return null;}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        var ledger=FamiliarLedger.get(p.level().getServer());
        if(isOwnedBy(p)&&player.isShiftKeyDown()){
            if(!ledger.pack(p,this)&&!ledger.owns(p.getUUID(),getUUID()))p.sendOverlayMessage(Component.translatable("familiar.tnd.one_only"));return InteractionResult.SUCCESS;
        }
        if(stack.is(fr.axephir.tnd.sanctuary.SanctuaryContent.MEMORY.get())){
            if(isOwnedBy(p)&&available()&&familiarLevel()>=20&&(protocols&8)==0){protocols|=8;applyGrowth();stack.consume(1,p);ledger.snapshot(this);p.level().sendParticles(ParticleTypes.ENCHANT,getX(),getEyeY(),getZ(),30,.4,.4,.4,.03);p.level().playSound(null,blockPosition(),ModContent.atmosphereSound("observatory_victory"),SoundSource.PLAYERS,.6f,1.2f);p.sendOverlayMessage(Component.translatable("observatory.tnd.familiar_memory"));}
            else p.sendOverlayMessage(Component.translatable("assault.tnd.protocol_refused"));return InteractionResult.SUCCESS;
        }
        for(var memory:fr.axephir.tnd.assault.AssaultContent.MEMORIES.entrySet())if(stack.is(memory.getValue().get())){
            int bit=1<<memory.getKey().ordinal();
            if(isOwnedBy(p)&&available()&&(protocols&bit)==0){protocols|=bit;applyGrowth();stack.consume(1,p);ledger.snapshot(this);p.level().playSound(null,blockPosition(),ModContent.atmosphereSound("familiar_bond"),SoundSource.PLAYERS,.7f,1.25f);p.level().sendParticles(ParticleTypes.ENCHANT,getX(),getEyeY(),getZ(),30,.4,.4,.4,.03);p.sendOverlayMessage(Component.translatable("assault.tnd.protocol_installed"));}
            else p.sendOverlayMessage(Component.translatable("assault.tnd.protocol_refused"));return InteractionResult.SUCCESS;
        }
        if(stack.is(FamiliarContent.NUTRIENT.get())){
            if(!isTame()){
                if(!trial()||!downed()){p.sendOverlayMessage(Component.translatable("familiar.tnd.defeat_first"));return InteractionResult.SUCCESS;}
                if(challenger!=null&&!challenger.equals(p.getUUID())){p.sendOverlayMessage(Component.translatable("familiar.tnd.other_trial"));return InteractionResult.SUCCESS;}
                if(!ledger.claim(p,this)){p.sendOverlayMessage(Component.translatable("familiar.tnd.one_only"));return InteractionResult.SUCCESS;}
                tame(p);recover();stack.consume(1,p);entityData.set(BOND,60);setOrderedToSit(true);setInSittingPose(true);navigation.stop();ledger.snapshot(this);
                p.sendSystemMessage(Component.translatable("familiar.tnd.bonded"));p.level().playSound(null,blockPosition(),ModContent.atmosphereSound("familiar_bond"),SoundSource.PLAYERS,.8f,.8f);return InteractionResult.SUCCESS;
            }
            if(isOwnedBy(p)&&(downed()||getHealth()<getMaxHealth())){recover();stack.consume(1,p);p.sendOverlayMessage(Component.translatable("familiar.tnd.revived"));ledger.snapshot(this);}else p.sendOverlayMessage(Component.translatable("familiar.tnd.healthy"));return InteractionResult.SUCCESS;
        }
        if(isOwnedBy(p)&&stack.isEmpty()){if(!ledger.claim(p,this)){p.sendOverlayMessage(Component.translatable("familiar.tnd.one_only"));return InteractionResult.SUCCESS;}ledger.snapshot(this);FamiliarMenu.open(p);return InteractionResult.SUCCESS;}
        if(stack.isEmpty())p.sendOverlayMessage(Component.translatable(isTame()?"familiar.tnd.foreign":"familiar.tnd.trial_hint"));
        return InteractionResult.SUCCESS;
    }
    public void recover(){entityData.set(DOWNED,false);setNoAi(false);setHealth(getMaxHealth());clearFire();setAirSupply(getMaxAirSupply());setOrderedToSit(false);setInSittingPose(false);setTarget(null);navigation.stop();invulnerableTime=40;}
    @Override public boolean hurtServer(ServerLevel w,DamageSource source,float amount){
        if(downed()||bondTicks()>0||isTame()&&source.getEntity() instanceof LivingEntity attacker&&isOwnedBy(attacker))return false;
        if(!isTame()&&trial()&&source.getEntity() instanceof ServerPlayer player)challenger=player.getUUID();
        return super.hurtServer(w,source,amount);
    }
    @Override public void die(DamageSource source){
        // Never run vanilla death: no loot, despawn, death XP or repeated resurrection reward.
        setHealth(1);dead=false;entityData.set(DOWNED,true);entityData.set(BOND,0);entityData.set(PULSE,0);setTarget(null);navigation.stop();setOrderedToSit(true);setInSittingPose(true);setNoAi(true);setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);clearFire();
        if(!isTame()&&source.getEntity() instanceof ServerPlayer p)challenger=p.getUUID();
        playSound(ModContent.atmosphereSound("familiar_down"),.55f,1);if(level() instanceof ServerLevel w){w.sendParticles(ParticleTypes.SMOKE,getX(),getY()+.2,getZ(),12,.3,.15,.3,.01);FamiliarLedger.get(w.getServer()).snapshot(this);}
    }
    @Override public boolean canAttack(LivingEntity target){
        if(target==null||downed()||bondTicks()>0||isOrderedToSit())return false;
        if(!isTame())return trial()&&target instanceof Player p&&!p.isCreative()&&!p.isSpectator()&&(challenger==null||challenger.equals(p.getUUID()))&&super.canAttack(target);
        return authorized()&&!(target instanceof Player)&&!(target instanceof TamableAnimal pet&&pet.isTame())&&super.canAttack(target);
    }
    public void awardExperience(int amount){
        if(!available()||amount<=0||familiarLevel()>=30)return;int old=familiarLevel(),oldStage=evolution();xp+=Math.min(amount,100);
        while(familiarLevel()<30&&xp>=requiredXp(familiarLevel())){xp-=requiredXp(familiarLevel());entityData.set(LEVEL,familiarLevel()+1);}if(familiarLevel()==30)xp=0;
        if(old!=familiarLevel()){float hp=getHealth();applyGrowth();setHealth(Math.min(getMaxHealth(),hp+4));
            if(level() instanceof ServerLevel w){w.sendParticles(ParticleTypes.END_ROD,getX(),getY()+.6,getZ(),evolution()!=oldStage?48:20,.6,.7,.6,.035);if(getOwner() instanceof ServerPlayer p)p.sendSystemMessage(Component.translatable(evolution()!=oldStage?"familiar.tnd.evolved":"familiar.tnd.level_up",getName(),familiarLevel()));}
            playSound(evolution()!=oldStage?ModContent.atmosphereSound("familiar_evolve"):SoundEvents.PLAYER_LEVELUP,.65f,evolution()!=oldStage?1.35f:1.7f);
        }if(level() instanceof ServerLevel w)FamiliarLedger.get(w.getServer()).snapshot(this);
    }
    private void applyGrowth(){int l=familiarLevel()-1,s=evolution();getAttribute(Attributes.MAX_HEALTH).setBaseValue(24+l*2+s*8+((protocols&1)!=0?6:0)+((protocols&8)!=0?8:0));getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3+l*.35+s*1.5+((protocols&8)!=0?2:0));getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.28+((protocols&2)!=0?.035:0));getAttribute(Attributes.ARMOR).setBaseValue(s*3+((protocols&4)!=0?2:0));getAttribute(Attributes.SCALE).setBaseValue(s==0?1:s==1?1.3:1.65);}
    public boolean toggleWaiting(){if(!available())return false;setOrderedToSit(!isOrderedToSit());setInSittingPose(isOrderedToSit());navigation.stop();setTarget(null);if(level() instanceof ServerLevel w)FamiliarLedger.get(w.getServer()).snapshot(this);return true;}
    @Override public void tick(){super.tick();
        if(!(level() instanceof ServerLevel w))return;
        if(isTame()&&tickCount%20==0){var ledger=FamiliarLedger.get(w.getServer());if(getOwner() instanceof ServerPlayer owner){if(!ledger.claim(owner,this)){setOrderedToSit(true);setInSittingPose(true);setTarget(null);}else ledger.applyPending(this);}
            if(getOwnerReference()!=null&&ledger.owns(getOwnerReference().getUUID(),getUUID())){var r=ledger.record(getOwnerReference().getUUID());if(r.getBooleanOr("Stored",false)){discard();return;}ledger.snapshot(this);}}
        if(onGround()&&getY()>w.getMinY()&&!isInWater()&&!isInLava())safe=blockPosition();
        if(getY()<w.getMinY()-8&&safe!=null){setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(safe));fallDistance=0;die(damageSources().fellOutOfWorld());}
        if(downed()){setNoAi(true);clearFire();setAirSupply(getMaxAirSupply());setTarget(null);navigation.stop();return;}
        if(bondTicks()>0){
            int t=bondTicks();entityData.set(BOND,t-1);setTarget(null);navigation.stop();
            if(t%3==0){double a=t*.26;w.sendParticles(ParticleTypes.END_ROD,getX()+Math.cos(a)*.8,getY()+.25+(60-t)/45.,getZ()+Math.sin(a)*.8,1,0,0,0,.015);}
            if(t==30)playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.7f,1.4f);
            if(t==1){setOrderedToSit(false);setInSittingPose(false);w.broadcastEntityEvent(this,(byte)7);playSound(SoundEvents.BEACON_ACTIVATE,.55f,1.4f);FamiliarLedger.get(w.getServer()).snapshot(this);}return;
        }
        if(!isTame()&&trial()&&getTarget()==null&&challenger!=null){var p=w.getServer().getPlayerList().getPlayer(challenger);if(p!=null&&p.level()==w&&distanceToSqr(p)<32*32&&canAttack(p))setTarget(p);}
        if(!available()||isOrderedToSit())return;
        guardCooldown=Math.max(0,guardCooldown-1);pulseCooldown=Math.max(0,pulseCooldown-1);
        if(familiarLevel()>=20&&tickCount%100==0&&getHealth()<getMaxHealth())heal(1);
        if(getOwner() instanceof ServerPlayer owner&&owner.level()==w&&distanceToSqr(owner)<144){
            if(familiarLevel()>=8&&guardCooldown==0&&owner.hurtTime>0){owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,0));guardCooldown=600;w.sendParticles(ParticleTypes.ENCHANT,owner.getX(),owner.getY()+1,owner.getZ(),20,.5,.7,.5,.02);playSound(SoundEvents.ENCHANTMENT_TABLE_USE,.4f,1.7f);}
            if(familiarLevel()>=3&&tickCount%200==0)survey(w,owner);
        }
        if(pulseTicks()>0){int t=pulseTicks();entityData.set(PULSE,t-1);if(t%4==0)for(int i=0;i<12;i++){double a=i*Math.PI/6;w.sendParticles(ParticleTypes.END_ROD,getX()+Math.cos(a)*3.5,getY()+.1,getZ()+Math.sin(a)*3.5,1,0,0,0,0);}
            if(t==1){int hit=0;for(var m:w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(3.5),m->m.isAlive()&&hasLineOfSight(m)&&canAttack(m))){m.hurtServer(w,damageSources().mobAttack(this),4+evolution()*2);if(++hit>=8)break;}playSound(ModContent.atmosphereSound("familiar_pulse"),.6f,1);}
        }else if(familiarLevel()>=12&&pulseCooldown==0&&getTarget() instanceof Monster m&&distanceToSqr(m)<20&&hasLineOfSight(m)){entityData.set(PULSE,20);pulseCooldown=160;}
    }
    @Override public void tryToTeleportToOwner(){if(available()&&getOwner()!=null&&getOwner().level()==level())super.tryToTeleportToOwner();}
    @Override public boolean shouldTryTeleportToOwner(){return available()&&getOwner()!=null&&getOwner().level()==level()&&super.shouldTryTeleportToOwner();}
    private void survey(ServerLevel w,ServerPlayer owner){BlockPos best=null;double d=Double.MAX_VALUE;
        for(var at:BlockPos.betweenClosed(blockPosition().offset(-6,-4,-6),blockPosition().offset(6,4,6))){if(!w.hasChunk(at.getX()>>4,at.getZ()>>4)||!w.getBlockState(at).is(ModContent.RESONANT_ORE.get()))continue;double n=at.distSqr(blockPosition());if(n<d){best=at.immutable();d=n;}}
        if(best==null)return;getLookControl().setLookAt(best.getX()+.5,best.getY()+.5,best.getZ()+.5);for(int i=1;i<=8;i++){double t=i/8.;w.sendParticles(owner,ParticleTypes.ENCHANT,false,false,getX()+(best.getX()+.5-getX())*t,getEyeY()+(best.getY()+.5-getEyeY())*t,getZ()+(best.getZ()+.5-getZ())*t,1,.04,.04,.04,0);}
    }
    @Override protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putInt("AssaultProtocols",protocols);o.putInt("FamiliarLevel",familiarLevel());o.putInt("FamiliarXp",xp);o.putBoolean("Injured",downed());o.putBoolean("Incubated",released);o.putInt("GuardCooldown",guardCooldown);o.putInt("PulseCooldown",pulseCooldown);if(challenger!=null)o.putString("Challenger",challenger.toString());if(safe!=null){o.putInt("SafeX",safe.getX());o.putInt("SafeY",safe.getY());o.putInt("SafeZ",safe.getZ());}}
    @Override protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);protocols=i.getIntOr("AssaultProtocols",0)&15;entityData.set(LEVEL,Math.clamp(i.getIntOr("FamiliarLevel",1),1,30));xp=Math.clamp(i.getIntOr("FamiliarXp",0),0,Math.max(0,requiredXp(familiarLevel())-1));entityData.set(DOWNED,i.getBooleanOr("Injured",false));released=i.getBooleanOr("Incubated",isTame());guardCooldown=Math.clamp(i.getIntOr("GuardCooldown",0),0,600);pulseCooldown=Math.clamp(i.getIntOr("PulseCooldown",0),0,160);try{challenger=UUID.fromString(i.getStringOr("Challenger",""));}catch(IllegalArgumentException ignored){challenger=null;}safe=new BlockPos(i.getIntOr("SafeX",getBlockX()),i.getIntOr("SafeY",getBlockY()),i.getIntOr("SafeZ",getBlockZ()));applyGrowth();setHealth(Math.clamp(i.getFloatOr("Health",getHealth()),1,getMaxHealth()));if(downed()){setNoAi(true);setHealth(1);setOrderedToSit(true);setInSittingPose(true);}entityData.set(BOND,0);entityData.set(PULSE,0);}
    @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.AMETHYST_BLOCK_HIT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.AMETHYST_BLOCK_BREAK;}
}
