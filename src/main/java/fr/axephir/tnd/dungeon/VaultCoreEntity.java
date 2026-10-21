package fr.axephir.tnd.dungeon;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.Mastery;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.progression.ProgressionService;
import fr.axephir.tnd.technology.MachineRegistry;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.BossEvent;

/** One finite shared reward per natural vault. Missing entities never count as kills. */
public final class VaultCoreEntity extends BlockEntity {
    private boolean natural,claimed,bossCleared,bossRewardClaimed;
    private UUID bossId;
    private String bossToken="";
    private int bossAbsent,bossMissing,bossElapsed;
    private int completed,active=-1,absentTicks,missingTicks,elapsed;
    private String token="";
    private final Set<UUID> guardians=new HashSet<>();
    private final ServerBossEvent bar=new ServerBossEvent(UUID.randomUUID(),Component.empty(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    public VaultCoreEntity(BlockPos p,BlockState s){super(MachineRegistry.VAULT_ENTITY.get(),p,s);}
    public int completed(){return completed;}
    public int active(){return active;}
    public Set<UUID> guardians(){return Set.copyOf(guardians);}
    public String token(){return token;}
    public boolean accepts(UUID id,String key){return natural&&active>=0&&token.equals(key)&&guardians.contains(id);}
    public BlockPos sealPos(int index){return worldPosition.offset(VaultBlock.offset(getBlockState(),index));}
    private void say(ServerPlayer p,String key,Object...args){p.sendOverlayMessage(Component.translatable("message.tnd."+key,args));}
    private boolean near(ServerPlayer p,BlockPos pos){return p.level()==level&&!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(pos))<36;}
    public boolean start(ServerPlayer p,int index){
        if(!(level instanceof ServerLevel world)||index<0||index>2||!near(p,sealPos(index)))return false;
        if(!natural){say(p,"vault_unbound");return false;}
        if(completed==3||index<completed){say(p,"vault_seal_done");return false;}
        if(active>=0){say(p,"vault_busy");return false;}
        if(index!=completed){say(p,"vault_order",completed+1);return false;}
        if(world.getDifficulty()==Difficulty.PEACEFUL){say(p,"vault_peaceful");return false;}
        if(!world.getBlockState(sealPos(index)).is(ModContent.VAULT_SEAL.get()))return false;
        ItemStack fuel=ItemStack.EMPTY;
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModContent.ECHO_DUST.get())){fuel=p.getInventory().getItem(i);break;}
        if(!p.isCreative()&&fuel.isEmpty()){say(p,"vault_fuel");return false;}
        String next=UUID.randomUUID().toString();var batch=new ArrayList<RiftSentinel>();
        for(int i=0;i<=index;i++){
            var local=VaultBlock.turn(getBlockState(),(i-1)*3,4);
            var at=sealPos(index).offset(local);
            if(!world.hasChunk(at.getX()>>4,at.getZ()>>4)){say(p,"vault_blocked");return false;}
            var mob=VestigeEntities.SENTINEL.get().create(world,EntitySpawnReason.EVENT);
            if(mob==null)return false;
            mob.setPos(Vec3.atBottomCenterOf(at));mob.bindTrial(worldPosition,next);
            if(!world.getBlockState(at.below()).isFaceSturdy(world,at.below(),Direction.UP)
                    ||!world.getFluidState(at).isEmpty()||!world.getFluidState(at.above()).isEmpty()||!world.noCollision(mob)){
                say(p,"vault_blocked");return false;
            }
            batch.add(mob);
        }
        for(var mob:batch)if(!world.addFreshEntity(mob)){batch.forEach(Entity::discard);say(p,"vault_blocked");return false;}
        token=next;active=index;guardians.clear();batch.forEach(m->guardians.add(m.getUUID()));
        elapsed=absentTicks=missingTicks=0;
        if(!p.isCreative())fuel.shrink(1);
        setChanged();say(p,"vault_start",index+1,index+1);
        world.playSound(null,sealPos(index),SoundEvents.BEACON_ACTIVATE,SoundSource.BLOCKS,.8f,.7f);
        updateBar(world);return true;
    }
    public void defeated(UUID id,String key){
        if(!(level instanceof ServerLevel world)||!accepts(id,key))return;
        guardians.remove(id);setChanged();
        if(guardians.isEmpty()){
            completed++;active=-1;token="";bar.removeAllPlayers();
            var pos=sealPos(completed-1);var state=world.getBlockState(pos);
            if(state.is(ModContent.VAULT_SEAL.get()))world.setBlock(pos,state.setValue(VaultBlock.LIT,true),3);
            world.playSound(null,pos,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,1,1.2f);
            for(var p:world.players())if(p.distanceToSqr(Vec3.atCenterOf(worldPosition))<54*54)say(p,"vault_clear",completed);
        }
    }
    public boolean claim(ServerPlayer p){
        if(!(level instanceof ServerLevel)||!near(p,worldPosition))return false;
        if(!natural){say(p,"vault_unbound");return false;}
        if(completed<3){say(p,"vault_progress",completed);return false;}
        if(claimed){say(p,"vault_empty");return false;}
        claimed=true;setChanged();
        for(var stack:List.of(new ItemStack(ModContent.RESONANCE_HEART.get()),new ItemStack(ModContent.RELIC_FRAGMENT.get(),4),new ItemStack(ModContent.ECHO_DUST.get(),8)))
            if(!p.getInventory().add(stack))p.drop(stack,false);
        if(!p.isCreative()){ProgressionService.award(p,Mastery.ANCIENT_TECH,120);ProgressionService.award(p,Mastery.EXPLORATION,80);}
        say(p,"vault_reward");return true;
    }
    public boolean bossCleared(){return bossCleared;}
    public UUID bossId(){return bossId;}
    public boolean acceptsBoss(UUID id,String key){return natural&&!bossCleared&&id.equals(bossId)&&!bossToken.isEmpty()&&bossToken.equals(key);}
    public void interact(ServerPlayer p){
        if(claimed&&bossCleared&&!bossRewardClaimed){claimBoss(p);return;}
        if(completed==3&&(p.isShiftKeyDown()||claimed&&p.getMainHandItem().is(ModContent.ECHO_DUST.get()))){summonBoss(p);return;}
        if(!claimed){claim(p);return;}
        if(bossCleared&&!bossRewardClaimed){claimBoss(p);return;}
        say(p,bossRewardClaimed?"custodian_empty":"custodian_hint");
    }
    public boolean summonBoss(ServerPlayer p){
        if(!(level instanceof ServerLevel world)||!near(p,worldPosition))return false;
        if(!natural){say(p,"vault_unbound");return false;}
        if(completed<3){say(p,"vault_progress",completed);return false;}
        if(bossCleared){say(p,"custodian_empty");return false;}
        if(bossId!=null||active>=0){say(p,"vault_busy");return false;}
        if(world.getDifficulty()==Difficulty.PEACEFUL){say(p,"vault_peaceful");return false;}
        // Count split stacks as well; mutation happens only after successful entity insertion.
        if(!p.isCreative()&&p.getInventory().countItem(ModContent.ECHO_DUST.get())<2){say(p,"custodian_fuel");return false;}
        var at=worldPosition.offset(VaultBlock.turn(getBlockState(),0,7));
        if(!world.hasChunk(at.getX()>>4,at.getZ()>>4)){say(p,"vault_blocked");return false;}
        var mob=VestigeEntities.CUSTODIAN.get().create(world,EntitySpawnReason.EVENT);if(mob==null)return false;
        String key=UUID.randomUUID().toString();mob.bind(worldPosition,key);mob.setPos(Vec3.atBottomCenterOf(at));
        if(!world.getBlockState(at.below()).isFaceSturdy(world,at.below(),Direction.UP)||!world.noCollision(mob)
                ||!BlockPos.betweenClosedStream(mob.getBoundingBox()).allMatch(b->world.getFluidState(b).isEmpty())||!world.addFreshEntity(mob)){
            mob.discard();say(p,"vault_blocked");return false;
        }
        bossId=mob.getUUID();bossToken=key;bossAbsent=bossMissing=bossElapsed=0;
        if(!p.isCreative()){int needed=2;for(int i=0;i<p.getInventory().getContainerSize()&&needed>0;i++){var item=p.getInventory().getItem(i);if(item.is(ModContent.ECHO_DUST.get())){int n=Math.min(needed,item.getCount());item.shrink(n);needed-=n;}}}
        setChanged();say(p,"custodian_start");world.playSound(null,at,ModContent.atmosphereSound("custodian_awaken"),SoundSource.HOSTILE,1,.8f);return true;
    }
    public void bossDefeated(UUID id,String key){
        if(!acceptsBoss(id,key))return;
        bossCleared=true;bossId=null;bossToken="";setChanged();
        if(level instanceof ServerLevel world){
            var state=world.getBlockState(worldPosition);if(state.is(ModContent.VAULT_CORE.get()))world.setBlock(worldPosition,state.setValue(VaultBlock.LIT,true),3);
            for(var p:world.players())if(p.distanceToSqr(Vec3.atCenterOf(worldPosition))<54*54)say(p,"custodian_victory");
        }
    }
    public boolean claimBoss(ServerPlayer p){
        if(!near(p,worldPosition)||!natural||!bossCleared||bossRewardClaimed)return false;
        bossRewardClaimed=true;setChanged();
        for(var stack:List.of(new ItemStack(ModContent.INERTIAL_MATRIX.get()),new ItemStack(ModContent.TEMPERED_ALLOY.get(),6)))if(!p.getInventory().add(stack))p.drop(stack,false);
        if(!p.isCreative()){ProgressionService.award(p,Mastery.ANCIENT_TECH,240);ProgressionService.award(p,Mastery.ENGINEERING,120);}
        say(p,"custodian_reward");return true;
    }
    private void tickBoss(ServerLevel world){
        if(bossId==null)return;
        boolean present=world.players().stream().anyMatch(p->!p.isSpectator()&&p.isAlive()&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<54*54);
        bossAbsent=present?0:bossAbsent+20;bossElapsed+=20;
        bossMissing=world.getEntity(bossId) instanceof FracturedCustodian?0:bossMissing+20;
        if(bossAbsent>=200||bossMissing>=200||bossElapsed>=12000||world.getDifficulty()==Difficulty.PEACEFUL)abortBoss();
        else setChanged();
    }
    private void abortBoss(){
        if(level instanceof ServerLevel world&&bossId!=null&&world.getEntity(bossId) instanceof FracturedCustodian mob&&mob.trialToken().equals(bossToken))mob.discard();
        bossId=null;bossToken="";bossAbsent=bossMissing=bossElapsed=0;setChanged();
    }
    public void tickEncounter(){
        if(!(level instanceof ServerLevel world))return;
        tickBoss(world);if(active<0)return;
        boolean present=world.players().stream().anyMatch(p->!p.isSpectator()&&p.isAlive()&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<54*54);
        absentTicks=present?0:absentTicks+20;elapsed+=20;
        boolean missing=guardians.stream().anyMatch(id->!(world.getEntity(id) instanceof RiftSentinel));
        missingTicks=missing?missingTicks+20:0;
        if(absentTicks>=200||missingTicks>=200||elapsed>=6000||world.getDifficulty()==Difficulty.PEACEFUL){abort();return;}
        updateBar(world);
    }
    private void updateBar(ServerLevel world){
        bar.setName(Component.translatable("message.tnd.vault_bar",active+1,guardians.size()));bar.setProgress(guardians.size()/(float)(active+1));
        var nearby=world.players().stream().filter(p->!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<54*54).toList();
        for(var p:List.copyOf(bar.getPlayers()))if(!nearby.contains(p))bar.removePlayer(p);
        nearby.forEach(bar::addPlayer);
    }
    public void abort(){
        abortBoss();
        if(level instanceof ServerLevel world)for(var id:guardians)if(world.getEntity(id) instanceof RiftSentinel mob&&mob.trialToken().equals(token))mob.discard();
        guardians.clear();active=-1;token="";bar.removeAllPlayers();setChanged();
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){abort();super.preRemoveSideEffects(pos,state);}
    @Override public void setRemoved(){bar.removeAllPlayers();super.setRemoved();}
    @Override protected void saveAdditional(ValueOutput out){
        super.saveAdditional(out);out.putInt("TrialSite",natural?1:0);out.putInt("completed",completed);out.putInt("active",active);
        out.putBoolean("claimed",claimed);out.putString("trialToken",token);out.putInt("elapsed",elapsed);
        out.putBoolean("bossCleared",bossCleared);out.putBoolean("bossRewardClaimed",bossRewardClaimed);
        if(bossId!=null)out.putString("bossId",bossId.toString());out.putString("bossToken",bossToken);out.putInt("bossElapsed",bossElapsed);
        out.store("guardians",Codec.STRING.listOf(),guardians.stream().map(UUID::toString).toList());
    }
    @Override protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);natural=in.getIntOr("TrialSite",0)==1;completed=Math.clamp(in.getIntOr("completed",0),0,3);
        active=in.getIntOr("active",-1);claimed=in.getBooleanOr("claimed",false);token=in.getStringOr("trialToken","");elapsed=Math.clamp(in.getIntOr("elapsed",0),0,6000);
        guardians.clear();for(var id:in.read("guardians",Codec.STRING.listOf()).orElse(List.of()).stream().limit(3).toList())try{guardians.add(UUID.fromString(id));}catch(IllegalArgumentException ignored){}
        if(active!=completed||active>2||guardians.isEmpty()||token.isEmpty()){active=-1;guardians.clear();token="";}
        bossCleared=in.getBooleanOr("bossCleared",false);bossRewardClaimed=in.getBooleanOr("bossRewardClaimed",false);
        bossId=null;try{bossId=UUID.fromString(in.getStringOr("bossId",""));}catch(IllegalArgumentException ignored){}
        bossToken=in.getStringOr("bossToken","");bossElapsed=Math.clamp(in.getIntOr("bossElapsed",0),0,12000);bossAbsent=bossMissing=0;
        if(!natural||completed<3||bossCleared||bossToken.isEmpty()){bossId=null;bossToken="";}
        absentTicks=missingTicks=0;
    }
}
