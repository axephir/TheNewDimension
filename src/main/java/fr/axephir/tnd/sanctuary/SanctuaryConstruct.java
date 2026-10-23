package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

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
import java.util.*;

/** Monumental constructs with snapshotted, visible attacks. No terrain damage and no unavoidable hits on pets. */
public final class SanctuaryConstruct extends Monster {
    public enum Kind {
        WATCHER("obsidian_watcher",.9f,2.5f,320,.29,14),LANCER("prismatic_lancer",1.1f,3.1f,240,.25,16),
        CANTOR("mnemonic_cantor",1.1f,3.3f,190,.24,8),
        FOUNDRY_WARDEN("foundry_warden",1.3f,3.5f,440,.25,20),FOUNDRY_ARBALIST("foundry_arbalist",1.1f,3.2f,320,.24,18),FOUNDRY_MENDER("foundry_mender",1.0f,2.8f,260,.27,10),
        FOUNDER("first_founder",4.0f,10.0f,3200,.20,34),REGULATOR("furnace_regulator",2.8f,7.0f,2600,.23,28),
        COLOSSUS("threshold_colossus",3.2f,7.8f,1800,.20,26),LENSKEEPER("keeper_of_lenses",2.8f,6.2f,2200,.22,24),
        SMELTER("silent_smelter",3.8f,8.8f,2800,.18,30),HORTULAN("petrified_hortulan",3.6f,7.6f,2600,.20,26),
        AXIOM("axiom_guardian",2.4f,6.4f,1900,.25,26),ANTIPHON("antiphon_guardian",2.4f,6.4f,1900,.23,24),ARCHON("first_silence_archon",4.4f,11.2f,5600,.19,32);
        public final String id;public final float width,height;public final double hp,speed,damage;
        Kind(String id,float width,float height,double hp,double speed,double damage){this.id=id;this.width=width;this.height=height;this.hp=hp;this.speed=speed;this.damage=damage;}
        public boolean foundryPair(){return this==FOUNDER||this==REGULATOR;}public boolean foundryGuard(){return this==FOUNDRY_WARDEN||this==FOUNDRY_ARBALIST||this==FOUNDRY_MENDER;}public boolean boss(){return !foundryGuard()&&this!=WATCHER&&this!=LANCER&&this!=CANTOR;}public boolean pair(){return this==AXIOM||this==ANTIPHON||foundryPair();}public boolean ranged(){return this==LANCER||this==CANTOR||this==LENSKEEPER||this==ANTIPHON||this==FOUNDRY_ARBALIST||this==FOUNDRY_MENDER||this==REGULATOR;}
    }
    private static final EntityDataAccessor<Integer> CHARGE=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.INT),EXPOSED=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.INT),PATTERN=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.INT),AWAKEN=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RITUAL=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.BOOLEAN);
    public boolean ritual(){return entityData.get(RITUAL);}
    private static final EntityDataAccessor<Float> RESERVE=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.FLOAT),TOTAL=SynchedEntityData.defineId(SanctuaryConstruct.class,EntityDataSerializers.FLOAT);
    private final Kind kind;private BlockPos home;private String token="";private Vec3 mark=Vec3.ZERO,origin=Vec3.ZERO,direction=new Vec3(0,0,1);private int cooldown=80,cycle;private float stagger,reserve,totalIntegrity;
    private final ServerBossEvent bar=new ServerBossEvent(UUID.randomUUID(),Component.empty(),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    public SanctuaryConstruct(EntityType<? extends SanctuaryConstruct> type,Level w,Kind kind){super(type,w);this.kind=kind;xpReward=kind.boss()?0:18;bar.setName(Component.translatable("entity.tnd."+kind.id));totalIntegrity=(float)kind.hp;reserve=Math.max(0,totalIntegrity-getMaxHealth());syncIntegrity();}
    public Kind kind(){return kind;}public int awakening(){return entityData.get(AWAKEN);}public String token(){return token;}public float integrity(){return getHealth()+(level().isClientSide()?entityData.get(RESERVE):reserve);}public float maxIntegrity(){return level().isClientSide()?entityData.get(TOTAL):totalIntegrity;}public int charge(){return entityData.get(CHARGE);}public int exposed(){return entityData.get(EXPOSED);}public int pattern(){return entityData.get(PATTERN);}public int phase(){return entityData.get(PHASE);}
    public static AttributeSupplier.Builder attributes(Kind k){return createMonsterAttributes().add(Attributes.MAX_HEALTH,Math.min(1024,k.hp)).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.ARMOR,k.boss()?16:8).add(Attributes.ARMOR_TOUGHNESS,k.boss()?8:2).add(Attributes.FOLLOW_RANGE,44).add(Attributes.KNOCKBACK_RESISTANCE,k.boss()?1:.6);}
    public void bind(BlockPos home,String token,int players,int offset){this.home=home.immutable();this.token=token;cycle=offset;cooldown=50+offset*35;setPersistenceRequired();boolean ceremonial=kind==Kind.COLOSSUS&&level().hasChunkAt(home)&&level().getBlockEntity(home) instanceof SanctuaryNodeEntity node&&node.enhanced();entityData.set(RITUAL,ceremonial);entityData.set(AWAKEN,ceremonial?160:kind.boss()?80+offset*10:0);if(kind.boss()){totalIntegrity=(float)(kind.hp*(1+.65*(Math.clamp(players,1,4)-1)));getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(1024,totalIntegrity));setHealth(getMaxHealth());reserve=totalIntegrity-getMaxHealth();syncIntegrity();}}
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(RITUAL,false);b.define(CHARGE,0);b.define(EXPOSED,0);b.define(PATTERN,0);b.define(PHASE,0);b.define(AWAKEN,0);b.define(RESERVE,0f);b.define(TOTAL,0f);}
    private boolean valid(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&(home==null||Math.abs(p.getX()-home.getX())<49&&Math.abs(p.getZ()-(home.getZ()-18))<49&&Math.abs(p.getY()-home.getY())<40);}
    protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            public boolean canUse(){return getTarget() instanceof Player p&&valid(p);}
            public boolean requiresUpdateEveryTick(){return true;}
            public void tick(){var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,22,22);
                if(charge()>0||exposed()>0){navigation.stop();return;}
                if(home!=null&&position().distanceToSqr(new Vec3(home.getX(),home.getY(),home.getZ()-18))>44*44){navigation.moveTo(home.getX(),home.getY(),home.getZ()-18,1);return;}
                double range=kind.ranged()?30:kind.boss()?20:6;
                if(distanceToSqr(t)>range*range||!hasLineOfSight(t))navigation.moveTo(t,1);
                else if(cooldown==0){navigation.stop();beginAttack(t);}
            }
        });
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private SanctuaryConstruct partner(ServerLevel w){if(!kind.pair())return null;return w.getEntitiesOfClass(SanctuaryConstruct.class,getBoundingBox().inflate(65),e->e!=this&&e.isAlive()&&e.kind.pair()&&e.token.equals(token)&&Objects.equals(e.home,home)).stream().findFirst().orElse(null);}
    public boolean beginAttack(LivingEntity target){
        if(awakening()>0||charge()>0||exposed()>0||!target.isAlive()||!hasLineOfSight(target)||distanceToSqr(target)>36*36)return false;
        if(level() instanceof ServerLevel w&&kind.pair()){var p=partner(w);if(p!=null&&p.charge()>(kind.foundryPair()?0:8)){cooldown=15;return false;}}
        origin=position();mark=target.position();direction=mark.subtract(origin).multiply(1,0,1).normalize();if(direction.lengthSqr()<.01)direction=new Vec3(0,0,1);
        int attack=kind.ranged()?2:cycle%2;
        if(kind==Kind.COLOSSUS&&ritual()&&phase()>0)attack=cycle%3==0?(phase()==2?4:3):cycle%2;
        if(kind==Kind.FOUNDER)attack=cycle%3==0?5:phase()==2?3:1;
        if(kind==Kind.REGULATOR)attack=cycle%2==0?6:phase()==2?4:0;
        if(kind==Kind.FOUNDRY_WARDEN)attack=cycle%2==0?0:1;
        if(kind==Kind.SMELTER)attack=cycle%3==0?3:cycle%2;
        if(kind==Kind.HORTULAN)attack=cycle%2==0?4:1;
        if(kind==Kind.AXIOM)attack=phase()==2?cycle%3==0?3:cycle%2:cycle%2==0?1:0;
        if(kind==Kind.ANTIPHON&&phase()==2)attack=cycle%2==0?2:4;
        if(kind==Kind.ARCHON)attack=cycle%5;
        entityData.set(PATTERN,attack);entityData.set(CHARGE,kind.boss()?Math.max(36,60-phase()*8):36);stagger=0;cycle++;
        if(kind.foundryPair()&&level() instanceof ServerLevel w)for(var p:w.players())if(valid(p)&&distanceToSqr(p)<50*50)p.sendOverlayMessage(Component.translatable("observatory.tnd.foundry.attack."+attack));
        if(ritual()&&level() instanceof ServerLevel w)for(var p:w.players())if(valid(p)&&distanceToSqr(p)<50*50)p.sendOverlayMessage(Component.translatable("observatory.tnd.witness.attack."+attack));
        playSound(fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_warning"),kind.boss()?1:.4f,.75f+attack*.12f);return true;
    }
    private void dot(ServerLevel w,double x,double y,double z){w.sendParticles(ParticleTypes.END_ROD,x,y+.12,z,1,0,0,0,0);}
    private void ring(ServerLevel w,Vec3 at,double r,int count){for(int n=0;n<count;n++){double a=n*Math.PI*2/count;dot(w,at.x+Math.cos(a)*r,at.y,at.z+Math.sin(a)*r);}}
    private void telegraph(ServerLevel w){
        switch(pattern()){
            case 5->{ring(w,mark,5,32);ring(w,mark,5.5,32);}
            case 6->{for(int n=0;n<=32;n+=2){var at=origin.add(direction.scale(n));var side=new Vec3(-direction.z,0,direction.x).scale(1.7);dot(w,at.x+side.x,mark.y,at.z+side.z);dot(w,at.x-side.x,mark.y,at.z-side.z);}}
            case 0->ring(w,mark,kind.boss()?4:2.5,kind.boss()?28:16);
            case 1->{ring(w,origin,7,24);ring(w,origin,17,40);}
            case 2->{for(int n=0;n<=28;n+=2){var at=origin.add(direction.scale(n));var side=new Vec3(-direction.z,0,direction.x).scale(1.3);dot(w,at.x+side.x,mark.y,at.z+side.z);dot(w,at.x-side.x,mark.y,at.z-side.z);}}
            case 3->{for(int n=-26;n<=26;n+=2){dot(w,origin.x+n,origin.y,origin.z-1.5);dot(w,origin.x+n,origin.y,origin.z+1.5);dot(w,origin.x-1.5,origin.y,origin.z+n);dot(w,origin.x+1.5,origin.y,origin.z+n);}}
            case 4->{ring(w,mark,3.5,20);ring(w,mark.add(8,0,0),3.5,20);ring(w,mark.add(-8,0,0),3.5,20);}
        }
    }
    public boolean threatens(Vec3 feet){
        var delta=feet.subtract(origin);double d=delta.horizontalDistance();
        return switch(pattern()){
            case 5->Math.abs(feet.y-mark.y)<2.4&&feet.subtract(mark).horizontalDistance()<=5;
            case 6->{double forward=delta.x*direction.x+delta.z*direction.z,side=Math.abs(delta.x*direction.z-delta.z*direction.x);yield forward>=0&&forward<=34&&side<=1.7&&Math.abs(feet.y-mark.y)<2.4;}
            case 0->Math.abs(feet.y-mark.y)<2.4&&feet.subtract(mark).horizontalDistance()<= (kind.boss()?4:2.5);
            case 1->Math.abs(feet.y-origin.y)<.9&&d>=7&&d<=17;
            case 2->{double forward=delta.x*direction.x+delta.z*direction.z,side=Math.abs(delta.x*direction.z-delta.z*direction.x);yield forward>=0&&forward<=30&&side<=1.3&&Math.abs(feet.y-mark.y)<2.4;}
            case 3->Math.abs(feet.y-origin.y)<1.2&&d<=28&&(Math.abs(delta.x)<=1.5||Math.abs(delta.z)<=1.5);
            case 4->Math.abs(feet.y-mark.y)<2.4&&(feet.subtract(mark).horizontalDistance()<=3.5||feet.subtract(mark.add(8,0,0)).horizontalDistance()<=3.5||feet.subtract(mark.add(-8,0,0)).horizontalDistance()<=3.5);
            default->false;
        };
    }
    protected void customServerAiStep(ServerLevel w){
        if(home!=null&&tickCount%20==0&&w.hasChunkAt(home)&&(!(w.getBlockEntity(home) instanceof SanctuaryNodeEntity node)||!node.accepts(getUUID(),token))){discard();return;}
        if(awakening()>0){entityData.set(AWAKEN,awakening()-1);navigation.stop();if(tickCount%8==0){ring(w,position(),kind.width+1,24);w.sendParticles(ParticleTypes.WHITE_ASH,getX(),getY()+.4,getZ(),12,kind.width,.3,kind.width,.02);}if(kind.boss()&&tickCount%10==0)updateBar(w);return;}
        super.customServerAiStep(w);if(cooldown>0)cooldown--;
        int hpPhase=integrity()<maxIntegrity()/3?2:integrity()<maxIntegrity()*2/3?1:0;if(kind.pair()&&home!=null&&w.hasChunkAt(home)&&w.getBlockEntity(home) instanceof SanctuaryNodeEntity node&&node.killed()>0)hpPhase=2;if(hpPhase>phase()){entityData.set(PHASE,hpPhase);if(ritual()&&home!=null&&w.hasChunkAt(home)&&w.getBlockEntity(home) instanceof SanctuaryNodeEntity node)WitnessScene.phase(node,this,hpPhase);cooldown=Math.max(cooldown,50);playSound(fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_core"),.8f,.7f+hpPhase*.15f);}
        if(exposed()>0)entityData.set(EXPOSED,exposed()-1);
        if(charge()>0){int n=charge();if(n%5==0)telegraph(w);entityData.set(CHARGE,n-1);if(n==1){
            if(kind==Kind.CANTOR||kind==Kind.FOUNDRY_MENDER){int healed=0;for(var ally:w.getEntitiesOfClass(SanctuaryConstruct.class,getBoundingBox().inflate(18),e->e!=this&&!e.kind.boss()&&Objects.equals(e.home,home)&&e.token.equals(token)&&e.getHealth()<e.getMaxHealth()&&hasLineOfSight(e))){ally.heal(kind==Kind.FOUNDRY_MENDER?28:18);if(kind==Kind.FOUNDRY_MENDER){for(int i=0;i<10;i++){var ray=position().add(0,1.5,0).lerp(ally.position().add(0,1.5,0),i/9.0);w.sendParticles(ParticleTypes.ELECTRIC_SPARK,ray.x,ray.y,ray.z,1,0,0,0,0);}}w.sendParticles(ParticleTypes.ENCHANT,ally.getX(),ally.getY()+1,ally.getZ(),6,.3,.4,.3,.01);if(++healed>=3)break;}}
            else for(var p:w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(38),this::valid))if(threatens(p.position())&&hasLineOfSight(p))p.hurtServer(w,damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));
            if(kind.foundryPair()&&(pattern()==5||pattern()==6)){
                var ally=partner(w);
                if(ally!=null&&ally.awakening()==0&&threatens(ally.position())&&hasLineOfSight(ally)){
                    ally.vent(120);ally.hurtServer(w,damageSources().mobAttack(this),90);
                    for(var p:w.players())if(valid(p))p.sendOverlayMessage(Component.translatable("observatory.tnd.foundry.crossed"));
                    w.sendParticles(ParticleTypes.ELECTRIC_SPARK,ally.getX(),ally.getY()+3,ally.getZ(),22,1.1,1.5,1.1,.05);
                }
            }
            w.sendParticles(ParticleTypes.SONIC_BOOM,mark.x,mark.y+.3,mark.z,1,0,0,0,0);playSound(fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_impact"),kind.boss()?1:.4f,.7f+pattern()*.1f);
            entityData.set(EXPOSED,kind.boss()?65:30);cooldown=kind.boss()?90:65;
        }}
        if(kind.boss()&&tickCount%10==0)updateBar(w);
    }
    private void updateBar(ServerLevel w){
        bar.setProgress(Math.clamp(integrity()/maxIntegrity(),0,1));bar.setColor(exposed()>0?BossEvent.BossBarColor.YELLOW:BossEvent.BossBarColor.BLUE);
        var near=w.players().stream().filter(p->!p.isSpectator()&&distanceToSqr(p)<64*64).toList();for(var p:List.copyOf(bar.getPlayers()))if(!near.contains(p))bar.removePlayer(p);near.forEach(bar::addPlayer);
    }
    public boolean vent(int ticks){if(awakening()>0||!isAlive())return false;entityData.set(CHARGE,0);entityData.set(EXPOSED,Math.clamp(ticks,1,140));cooldown=Math.max(cooldown,ticks+25);return true;}
    public boolean hurtServer(ServerLevel w,DamageSource source,float amount){
        if(awakening()>0&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return false;
        float factor=kind.boss()?(exposed()>0?1.35f:.65f):1;
        if(source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))factor=1;
        boolean success=super.hurtServer(w,source,amount*factor);
        if(success&&home!=null&&w.hasChunkAt(home)&&w.getBlockEntity(home) instanceof SanctuaryNodeEntity node){
            var attacker=source.getEntity();if(attacker instanceof ServerPlayer p)node.participant(p);
            if(attacker instanceof fr.axephir.tnd.entities.Shardling pet&&pet.getOwnerReference()!=null){var owner=w.getServer().getPlayerList().getPlayer(pet.getOwnerReference().getUUID());if(owner!=null)node.participant(owner);}
        }
        if(success&&charge()>0){stagger+=amount;if(stagger>=(kind.boss()?(kind==Kind.ARCHON?350:220):14)){entityData.set(CHARGE,0);entityData.set(EXPOSED,80);cooldown=100;playSound(SoundEvents.AMETHYST_CLUSTER_BREAK,.6f,.7f);}}
        return success;
    }
    public void die(DamageSource source){
        // Native max-health is capped at 1024. Extra integrity is carried by finite breakable core layers,
        // rather than changing the global attribute range for Minecraft or other mods.
        if(kind.boss()&&reserve>0&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)){
            float layer=Math.min(getMaxHealth(),reserve);reserve-=layer;setHealth(layer);syncIntegrity();entityData.set(CHARGE,0);entityData.set(EXPOSED,80);cooldown=110;
            playSound(fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_core"),1,.8f);return;
        }
        boolean before=dead;super.die(source);if(!before&&dead){bar.removeAllPlayers();if(home!=null&&level() instanceof ServerLevel w&&w.hasChunkAt(home)&&w.getBlockEntity(home) instanceof SanctuaryNodeEntity node)node.defeated(this);}}
    private void syncIntegrity(){entityData.set(RESERVE,reserve);entityData.set(TOTAL,totalIntegrity);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    public void remove(RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
    protected boolean shouldDropLoot(ServerLevel w){return !kind.boss();}
    protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState state){if(kind.boss()){if(tickCount%8==0)playSound(fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_impact"),.18f,.55f);}else super.playStepSound(p,state);}
    protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.NETHERITE_BLOCK_HIT;}
    protected SoundEvent getDeathSound(){return SoundEvents.RESPAWN_ANCHOR_DEPLETE.value();}
    protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);if(home!=null)out.store("observatoryHome",BlockPos.CODEC,home);out.putString("observatoryToken",token);out.putInt("cycle",cycle);out.putBoolean("witnessRitual",ritual());out.putInt("witnessAwakening",awakening());out.putFloat("reserveIntegrity",reserve);out.putFloat("totalIntegrity",totalIntegrity);}
    protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);home=in.read("observatoryHome",BlockPos.CODEC).orElse(null);token=in.getStringOr("observatoryToken","");cycle=Math.max(0,in.getIntOr("cycle",0));entityData.set(RITUAL,kind==Kind.COLOSSUS&&in.getBooleanOr("witnessRitual",false));entityData.set(AWAKEN,Math.clamp(in.getIntOr("witnessAwakening",0),0,ritual()?160:100));totalIntegrity=Math.clamp(in.getFloatOr("totalIntegrity",(float)kind.hp),1,20000);reserve=Math.clamp(in.getFloatOr("reserveIntegrity",Math.max(0,totalIntegrity-getMaxHealth())),0,Math.max(0,totalIntegrity-getHealth()));cooldown=80;entityData.set(CHARGE,0);entityData.set(EXPOSED,0);syncIntegrity();}
}
