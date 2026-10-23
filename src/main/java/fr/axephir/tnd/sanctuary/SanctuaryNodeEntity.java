package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** A finite, restartable objective. Missing entities never count as kills; tokens revoke abandoned attempts. */
public final class SanctuaryNodeEntity extends BlockEntity implements MenuProvider {
    private SanctuaryRoute.Site site;private int index=-1,spawned,killed,absent,traces,sequence,relays,securityDelay,partyScale;private Vec3 securityMark,securityOrigin;private boolean active;private String token="";
    boolean witness,bridgeWanted,circuitLatched;int bridgeStage,relicStage,shutterStage,gateLift,alignTicks,seenPhase=-1,scenePulse;
    boolean foundryAct,foundryLatched;int foundryCooling,foundryPulse,foundrySpan;
    public boolean foundry(){return foundryAct&&fr.axephir.tnd.domain.FoundryAct.contains(index);}
    public String encounterToken(){return token;}
    public void authoredFinish(){if(foundry())finish();}
    public BlockPos resonance(){if(foundry()&&index==12){var p=fr.axephir.tnd.domain.FoundryAct.resonance(Math.min(3,sequence));return local(p.x(),p.y(),p.z());}return pad(site,index,sequence);}
    public boolean enhanced(){return witness&&index>=0&&index<8;}
    public void initializeWitness(SanctuaryRoute.Site site,int index){initialize(site,index);witness=index>=0&&index<8;foundryAct=fr.axephir.tnd.domain.FoundryAct.contains(index);setChanged();}
    public BlockPos local(int x,int y,int z){return worldPosition.offset(x,y-1,z-18);}
    public int discoveries(Player player){return natural()&&level instanceof ServerLevel w?SanctuaryLedger.get(w.getServer()).discoveries(site.key(),player.getUUID()):0;}
    long siteKey(){return site.key();}
    void witnessFinish(){if(enhanced())finish();}
    public void sceneTick(){if(natural()&&level instanceof ServerLevel w){if(enhanced())WitnessScene.tick(this,w);if(foundry())FoundryScene.tick(this,w);}}
    private final Set<UUID> live=new HashSet<>(),participants=new HashSet<>();
    public SanctuaryNodeEntity(BlockPos p,BlockState s){super(SanctuaryContent.NODE_ENTITY.get(),p,s);}
    public void initialize(SanctuaryRoute.Site site,int index){this.site=site;this.index=index;setChanged();}
    public boolean natural(){return site!=null&&index>=0&&index<48&&worldPosition.equals(SanctuaryRegions.station(site,index))&&level!=null&&level.getBlockEntity(worldPosition)==this&&SanctuaryRegions.protectedAt(level,worldPosition);}
    public int index(){return Math.clamp(index,0,47);}public int spawned(){return spawned;}public int killed(){return killed;}public boolean active(){return active;}public int traces(){return traces;}public int sequence(){return sequence;}public int relays(){return relays;}
    public static BlockPos pad(SanctuaryRoute.Site site,int index,int sequence){int[][] points={{-42,11,-12},{0,1,-20},{42,11,12},{0,1,28}};var p=points[Math.floorMod(sequence+index/8,4)];return new BlockPos(site.x()+SanctuaryRoute.x(index)+p[0],SanctuaryRoute.FLOOR+p[1],site.z()+SanctuaryRoute.z(index)+p[2]);}
    public long mask(){return natural()&&level instanceof ServerLevel w?SanctuaryLedger.get(w.getServer()).mask(site.key()):0;}
    public boolean available(){return natural()&&SanctuaryRoute.available(mask(),index);}
    public int target(){return SanctuaryRoute.boss(index)?bossKinds().length:index%8==6?12:index%8==3?4:6;}
    public int[] tones(){return fr.axephir.tnd.domain.SanctuaryPuzzles.target(index());}
    public int dialTone(int dial){
        if(!natural()||index%8!=2||dial<0||dial>3)return -1;
        var at=new BlockPos(site.x()+SanctuaryRoute.x(index)-12+dial*8,SanctuaryRoute.FLOOR+2,site.z()+SanctuaryRoute.z(index)-14);
        return level.hasChunkAt(at)&&level.getBlockState(at).is(SanctuaryContent.DIAL.get())?level.getBlockState(at).getValue(SanctuaryDialBlock.TONE):-1;
    }
    public int claimState(Player player){if(!natural()||!SanctuaryRoute.boss(index)||!(level instanceof ServerLevel w))return 0;var ledger=SanctuaryLedger.get(w.getServer());return (ledger.rewards(site.key(),player.getUUID())&(1<<(index/8)))!=0?2:ledger.canClaim(site.key(),player.getUUID(),index/8)?1:0;}
    public boolean accepts(UUID id,String key){return natural()&&active&&token.equals(key)&&live.contains(id);}
    public void participant(ServerPlayer p){if(participants.size()<16&&inside(p))if(participants.add(p.getUUID()))setChanged();}
    private boolean inside(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&Math.abs(p.getY()-(SanctuaryRoute.FLOOR+1))<40&&Math.abs(p.getX()-(site.x()+SanctuaryRoute.x(index)))<49&&Math.abs(p.getZ()-(site.z()+SanctuaryRoute.z(index)))<49;}
    public void trace(ServerPlayer p,BlockPos at){
        if(!natural()||!available()&&!SanctuaryRoute.done(mask(),index)||index%8!=0&&index%8!=5||p.distanceToSqr(Vec3.atCenterOf(at))>64)return;
        int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index);int[][] offsets={{-24,8},{24,8},{0,-30}};
        for(int n=0;n<3;n++)if(at.equals(new BlockPos(cx+offsets[n][0],SanctuaryRoute.FLOOR+1,cz+offsets[n][1]))){
            String fragment=foundry()?"observatory.tnd.foundry."+(index%8==5?"echo.":"trace.")+n:"observatory.tnd."+(index%8==5?"echo.":"trace.")+(index/8)+"."+n;
            if(SanctuaryRoute.done(mask(),index)){p.sendOverlayMessage(Component.translatable(fragment));return;}
            boolean fresh=(traces&(1<<n))==0;traces|=1<<n;participant(p);setChanged();p.sendOverlayMessage(Component.translatable(fragment));
            if(fresh&&level instanceof ServerLevel w)w.playSound(null,at,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_relay"),SoundSource.BLOCKS,.45f,.8f+n*.15f);
            if(traces==7&&(!enhanced()||index!=5||circuitLatched)&&(!foundry()||foundryLatched))finish();return;
        }
    }
    public boolean action(ServerPlayer player,int action){
        if(!natural()||player.distanceToSqr(Vec3.atCenterOf(worldPosition))>64||player.isSpectator())return false;
        if(action==4&&available()&&index%8==2){int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index);for(int n=0;n<4;n++){var at=new BlockPos(cx-12+n*8,SanctuaryRoute.FLOOR+2,cz-14);if(level.hasChunkAt(at)&&level.getBlockState(at).is(SanctuaryContent.DIAL.get()))level.setBlock(at,SanctuaryContent.DIAL.get().defaultBlockState(),3);}player.sendOverlayMessage(Component.translatable("observatory.tnd.alignment_reset"));return true;}
        if(action==1)return claim(player);
        if(action>=100&&action<148){int dest=action-100;return SanctuaryRoute.done(mask(),dest)&&SanctuaryTravel.go(player,site,dest);}
        if(action==2)return SanctuaryTravel.go(player,site,-1);
        if(action==3){if(!active||!live.stream().allMatch(id->((ServerLevel)level).getEntity(id)==null))return false;reset();return true;}
        if(action!=0||!available())return false;participant(player);
        int slot=index%8;
        if(slot==1||slot==3||slot==6||slot==7){
            if(player.isCreative()){player.sendOverlayMessage(Component.translatable("observatory.tnd.survival_required"));return false;}
            if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){player.sendOverlayMessage(Component.translatable("observatory.tnd.difficulty_required"));return false;}
        }
        if(slot==0||slot==5){player.sendOverlayMessage(Component.translatable(foundry()&&!foundryLatched?"observatory.tnd.foundry.seek_valves":enhanced()&&index==5&&!circuitLatched?"observatory.tnd.witness.seek_circuit":"observatory.tnd.seek_traces",Integer.bitCount(traces)));player.closeContainer();return true;}
        if(slot==4){active=true;token=UUID.randomUUID().toString();absent=0;setChanged();player.closeContainer();return true;}
        if(slot==2){
            int[] wanted=tones();int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index);
            for(int i=0;i<4;i++){var p=new BlockPos(cx-12+i*8,SanctuaryRoute.FLOOR+2,cz-14);if(!level.hasChunkAt(p)||!level.getBlockState(p).is(SanctuaryContent.DIAL.get())||level.getBlockState(p).getValue(SanctuaryDialBlock.TONE)!=wanted[i]){player.sendOverlayMessage(Component.translatable("observatory.tnd.misaligned"));return false;}}
            finish();return true;
        }
        if(active)return false;beginEncounter();
        level.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_threshold"),SoundSource.HOSTILE,.65f,.8f);player.closeContainer();return true;
    }
    private void beginEncounter(){participants.clear();partyScale=0;active=true;token=UUID.randomUUID().toString();spawned=killed=absent=0;live.clear();setChanged();}
    private SanctuaryConstruct.Kind[] bossKinds(){return switch(Math.clamp(index/8,0,5)){
        case 0->new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.COLOSSUS};case 1->foundry()?new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.FOUNDER,SanctuaryConstruct.Kind.REGULATOR}:new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.LENSKEEPER};case 2->new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.SMELTER};case 3->new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.HORTULAN};case 4->new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.AXIOM,SanctuaryConstruct.Kind.ANTIPHON};default->new SanctuaryConstruct.Kind[]{SanctuaryConstruct.Kind.ARCHON};};}
    public void defeated(SanctuaryConstruct construct){
        if(!accepts(construct.getUUID(),construct.token()))return;live.remove(construct.getUUID());killed++;setChanged();if(killed>=target()&&spawned>=target()&&(index%8!=3||relays==15))finish();
    }
    public boolean disarm(ServerPlayer p,BlockPos at){
        if(!available()||index%8!=3||p.distanceToSqr(Vec3.atCenterOf(at))>64)return false;
        int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index);int[][] offsets={{-24,-24},{24,-24},{24,24},{-24,24}};
        for(int n=0;n<4;n++)if(at.equals(new BlockPos(cx+offsets[n][0],SanctuaryRoute.FLOOR+2,cz+offsets[n][1]))){
            if(!active)action(p,0);if((relays&(1<<n))!=0)return false;relays|=1<<n;participant(p);setChanged();level.setBlock(at,SanctuaryContent.RELAY.get().defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT,false),3);p.sendOverlayMessage(Component.translatable("observatory.tnd.relays",Integer.bitCount(relays)));
            if(relays==15&&killed>=target()&&spawned>=target())finish();return true;
        }return false;
    }
    private void security(ServerLevel w,java.util.List<ServerPlayer> players){
        if(index%8!=3||relays==15||players.isEmpty())return;
        if(securityMark!=null){
            for(int n=0;n<=12;n++){var beam=securityOrigin.lerp(securityMark.add(0,1,0),n/12.0);w.sendParticles(ParticleTypes.END_ROD,beam.x,beam.y,beam.z,1,0,0,0,0);}
            for(int n=0;n<20;n++){double a=n*Math.PI/10;w.sendParticles(ParticleTypes.END_ROD,securityMark.x+Math.cos(a)*2.4,securityMark.y+.1,securityMark.z+Math.sin(a)*2.4,1,0,0,0,0);}
            securityDelay-=20;if(securityDelay<=0){for(var p:players)if(p.position().subtract(securityMark).horizontalDistance()<2.4&&Math.abs(p.getY()-securityMark.y)<2.5&&w.clip(new net.minecraft.world.level.ClipContext(securityOrigin,p.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS)p.hurtServer(w,w.damageSources().magic(),8);securityMark=null;securityDelay=60;}return;
        }
        securityDelay-=20;if(securityDelay>0)return;
        int source=0;while(source<4&&(relays&(1<<source))!=0)source++;int[][] offsets={{-24,-24},{24,-24},{24,24},{-24,24}};securityOrigin=new Vec3(site.x()+SanctuaryRoute.x(index)+offsets[source][0]+.5,SanctuaryRoute.FLOOR+3.2,site.z()+SanctuaryRoute.z(index)+offsets[source][1]+.5);
        var target=players.get(Math.floorMod(w.getGameTime()/20,players.size()));securityMark=target.position();securityDelay=40;w.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_warning"),SoundSource.BLOCKS,.4f,1.4f);
    }
    private void reset(){if(level instanceof ServerLevel w)for(var id:live)if(w.getEntity(id) instanceof SanctuaryConstruct e)e.discard();live.clear();active=false;token="";spawned=killed=absent=relays=0;securityMark=null;securityDelay=0;
        if(level instanceof ServerLevel w&&site!=null&&index%8==3)for(int[] d:new int[][]{{-24,-24},{24,-24},{24,24},{-24,24}}){var at=new BlockPos(site.x()+SanctuaryRoute.x(index)+d[0],SanctuaryRoute.FLOOR+2,site.z()+SanctuaryRoute.z(index)+d[1]);if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.RELAY.get()))w.setBlock(at,SanctuaryContent.RELAY.get().defaultBlockState(),3);}setChanged();}
    private void finish(){
        if(!(level instanceof ServerLevel w)||!available())return;var ledger=SanctuaryLedger.get(w.getServer());
        if(!ledger.finish(site.key(),index))return;active=false;token="";if(enhanced())scenePulse=24;if(foundry())foundryPulse=20;setChanged();openGate();
        if(SanctuaryRoute.boss(index))for(var id:participants)ledger.eligible(site.key(),id,index/8);
        for(var p:w.players())if(inside(p)){if(index%8!=0&&index%8!=5)p.sendOverlayMessage(Component.translatable("observatory.tnd.complete",index+1));if(SanctuaryRoute.boss(index))claim(p);}
        w.sendParticles(ParticleTypes.END_ROD,worldPosition.getX()+.5,worldPosition.getY()+1.2,worldPosition.getZ()+.5,35,.6,1,.6,.025);
        w.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_victory"),SoundSource.BLOCKS,.7f,1);
    }
    public boolean claim(ServerPlayer p){if(!(level instanceof ServerLevel w)||!SanctuaryRoute.boss(index)||!SanctuaryLedger.get(w.getServer()).claim(site.key(),p.getUUID(),index/8))return false;
        give(p,new ItemStack(SanctuaryContent.CORE.get()));give(p,new ItemStack(SanctuaryContent.SHARD.get(),12+index/8*2));
        if(foundry()&&index==15)give(p,new ItemStack(SanctuaryContent.FOUNDRY_HEART.get()));if(index/8==3)give(p,new ItemStack(SanctuaryContent.MEMORY.get()));if(index==47){give(p,new ItemStack(SanctuaryContent.BLADE.get()));give(p,new ItemStack(SanctuaryContent.PICKAXE.get()));}return true;
    }
    private static void give(ServerPlayer p,ItemStack item){if(!p.getInventory().add(item))p.drop(item,false);}
    public void tick(){
        if(!(level instanceof ServerLevel w)||!natural())return;openGate();syncRelays(w);
        if(!active&&available()&&(index%8==1||index%8==3||index%8==6)&&w.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL){for(var p:w.players())if(inside(p)&&p.position().distanceToSqr(new Vec3(site.x()+SanctuaryRoute.x(index),SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(index)))<28*28){participant(p);beginEncounter();w.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_threshold"),SoundSource.HOSTILE,.6f,.8f);break;}}
        if(!active)return;
        var players=w.players().stream().filter(this::inside).toList();players.forEach(this::participant);
        if(players.isEmpty()||w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){absent+=20;if(absent>=200)reset();return;}absent=0;
        if(index%8==4){if(sequence>=4){if(!foundry()||foundryLatched)finish();return;}var target=resonance();w.sendParticles(ParticleTypes.END_ROD,target.getX()+.5,target.getY()+.3,target.getZ()+.5,5,.4,.15,.4,.01);for(var p:players)if(p.position().distanceToSqr(Vec3.atBottomCenterOf(target))<3){sequence++;setChanged();w.playSound(null,target,fr.axephir.tnd.core.ModContent.atmosphereSound("observatory_relay"),SoundSource.BLOCKS,.4f,.8f+sequence*.15f);if(sequence==4&&(!foundry()||foundryLatched))finish();break;}return;}
        security(w,players);
        if(index%8==6&&spawned>0&&spawned%4==0&&!live.isEmpty())return;
        int cap=SanctuaryRoute.boss(index)?2:index%8==6?4:6;
        if(spawned>=target()||live.size()>=cap)return;
        int n=spawned;var kind=SanctuaryRoute.boss(index)?bossKinds()[n]:foundry()?n%4==3?SanctuaryConstruct.Kind.FOUNDRY_MENDER:n%3==2?SanctuaryConstruct.Kind.FOUNDRY_ARBALIST:SanctuaryConstruct.Kind.FOUNDRY_WARDEN:n%4==3?SanctuaryConstruct.Kind.CANTOR:n%3==2?SanctuaryConstruct.Kind.LANCER:SanctuaryConstruct.Kind.WATCHER;
        int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index);int[][] offsets={{-14,-2},{14,-2},{0,-22},{0,6}};
        var at=new BlockPos(cx+offsets[n%4][0],SanctuaryRoute.FLOOR+1,cz+offsets[n%4][1]);if(!w.hasChunkAt(at))return;
        var mob=SanctuaryEntities.TYPES.get(kind).get().create(w,EntitySpawnReason.EVENT);if(mob==null)return;mob.setPos(Vec3.atBottomCenterOf(at));
        if(partyScale==0)partyScale=Math.min(4,players.size());mob.bind(worldPosition,token,partyScale,n);if(!w.noCollision(mob)){mob.discard();return;}
        mob.setTarget(players.get(n%players.size()));if(!w.addFreshEntity(mob)){mob.discard();return;}live.add(mob.getUUID());spawned++;setChanged();if(enhanced()&&index==7)WitnessScene.bossSpawn(this,mob,players);if(foundry()&&index==15)FoundryScene.bossSpawn(this,mob);if(index%8==6&&n%4==0)for(var p:players)p.sendOverlayMessage(Component.translatable("observatory.tnd.wave",n/4+1));
    }
    private void syncRelays(ServerLevel w){if(index%8!=3)return;int[][] points={{-24,-24},{24,-24},{24,24},{-24,24}};for(int n=0;n<4;n++){var at=new BlockPos(site.x()+SanctuaryRoute.x(index)+points[n][0],SanctuaryRoute.FLOOR+2,site.z()+SanctuaryRoute.z(index)+points[n][1]);if(!w.hasChunkAt(at))continue;var state=w.getBlockState(at);boolean lit=(relays&(1<<n))==0;if(state.is(SanctuaryContent.RELAY.get())&&state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)!=lit)w.setBlock(at,state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT,lit),3);}}
    private void openGate(){
        if(!(level instanceof ServerLevel w)||index<1||!SanctuaryRoute.done(mask(),index-1))return;
        int cx=site.x()+SanctuaryRoute.x(index),cz=site.z()+SanctuaryRoute.z(index),dx=Integer.signum(SanctuaryRoute.x(index-1)-SanctuaryRoute.x(index)),dz=Integer.signum(SanctuaryRoute.z(index-1)-SanctuaryRoute.z(index));
        int top=8;if(enhanced()||foundry()){if(gateLift<8){gateLift++;setChanged();if(gateLift==1)WitnessScene.sound(w,worldPosition,"witness_bridge",.45f,.8f);if(gateLift==8)WitnessScene.sound(w,worldPosition,"observatory_relay",.3f,.9f);}top=gateLift;}
        for(int a=-5;a<=5;a++)for(int y=1;y<=top;y++){var p=new BlockPos(cx+dx*48+(dz!=0?a:0),SanctuaryRoute.FLOOR+y,cz+dz*48+(dx!=0?a:0));if(w.hasChunkAt(p)&&w.getBlockState(p).is(SanctuaryContent.GATE.get()))w.setBlock(p,Blocks.AIR.defaultBlockState(),3);}
    }
    public Component getDisplayName(){return Component.translatable("observatory.tnd.title");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SanctuaryMenu(id,inv,this);}
    protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("index",index);out.putBoolean("foundryAct",foundryAct);out.putBoolean("foundryLatched",foundryLatched);out.putInt("foundryCooling",foundryCooling);out.putInt("foundryPulse",foundryPulse);out.putInt("foundrySpan",foundrySpan);out.putBoolean("witnessAct",witness);out.putBoolean("bridgeWanted",bridgeWanted);out.putInt("bridgeStage",bridgeStage);out.putInt("shutterStage",shutterStage);out.putInt("gateLift",gateLift);out.putBoolean("circuitLatched",circuitLatched);out.putInt("relicStage",relicStage);out.putInt("seenPhase",seenPhase);out.putInt("scenePulse",scenePulse);if(site!=null){out.putInt("siteX",site.x());out.putInt("siteZ",site.z());}out.putBoolean("active",active);out.putString("token",token);out.putInt("spawned",spawned);out.putInt("killed",killed);out.putInt("traces",traces);out.putInt("sequence",sequence);out.putInt("relays",relays);out.putInt("partyScale",partyScale);out.store("live",Codec.STRING.listOf(),live.stream().map(UUID::toString).toList());out.store("participants",Codec.STRING.listOf(),participants.stream().map(UUID::toString).toList());}
    protected void loadAdditional(ValueInput in){super.loadAdditional(in);index=in.getIntOr("index",-1);foundryAct=in.getBooleanOr("foundryAct",false);foundryLatched=in.getBooleanOr("foundryLatched",false);foundryCooling=Math.clamp(in.getIntOr("foundryCooling",0),0,360);foundryPulse=Math.clamp(in.getIntOr("foundryPulse",0),0,20);foundrySpan=Math.clamp(in.getIntOr("foundrySpan",0),0,10);witness=in.getBooleanOr("witnessAct",false);bridgeWanted=in.getBooleanOr("bridgeWanted",false);bridgeStage=Math.clamp(in.getIntOr("bridgeStage",0),0,10);shutterStage=Math.clamp(in.getIntOr("shutterStage",0),0,3);gateLift=Math.clamp(in.getIntOr("gateLift",0),0,8);circuitLatched=in.getBooleanOr("circuitLatched",false);relicStage=Math.clamp(in.getIntOr("relicStage",0),0,3);seenPhase=Math.clamp(in.getIntOr("seenPhase",-1),-1,2);scenePulse=Math.clamp(in.getIntOr("scenePulse",0),0,24);if(index>=0&&index<48)site=SanctuaryRoute.nearest(in.getIntOr("siteX",worldPosition.getX()),in.getIntOr("siteZ",worldPosition.getZ()));active=in.getBooleanOr("active",false);token=in.getStringOr("token","");spawned=Math.clamp(in.getIntOr("spawned",0),0,12);killed=Math.clamp(in.getIntOr("killed",0),0,spawned);traces=in.getIntOr("traces",0)&7;sequence=Math.clamp(in.getIntOr("sequence",0),0,4);relays=in.getIntOr("relays",0)&15;partyScale=Math.clamp(in.getIntOr("partyScale",0),0,4);live.clear();participants.clear();loadIds(in,"live",live,12);loadIds(in,"participants",participants,16);}
    private static void loadIds(ValueInput in,String key,Set<UUID> ids,int max){for(String raw:in.read(key,Codec.STRING.listOf()).orElse(List.of()).stream().limit(max).toList())try{ids.add(UUID.fromString(raw));}catch(IllegalArgumentException ignored){}}
}
