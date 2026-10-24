package fr.axephir.tnd.depths;
import com.mojang.serialization.Codec;
import fr.axephir.tnd.city.CityRegions;
import fr.axephir.tnd.deepindustry.DeepIndustry;
import fr.axephir.tnd.sanctuary.*;
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
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Bounded combat waves. Only witnessed, native deaths advance the encounter; unloaded actors never count. */
public final class DepthsNodeEntity extends BlockEntity implements MenuProvider{
 private String token="";private int spawned,killed,absent,wavePause;private boolean active;private final Set<UUID> live=new HashSet<>(),participants=new HashSet<>();
 public DepthsNodeEntity(BlockPos p,BlockState s){super(DepthsContent.ENTITY.get(),p,s);}
 public CityRegions.Region region(){return CityRegions.nearest(worldPosition.getX(),worldPosition.getZ());}
 public int index(){return DepthsLayout.atNode(region(),worldPosition);}public int spawned(){return spawned;}public int killed(){return killed;}public boolean active(){return active;}
 public boolean natural(){return index()>=0&&level!=null&&level.dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION)&&level.getBlockEntity(worldPosition)==this&&CityRegions.protectedAt(level,worldPosition);}
 public int mask(){return natural()&&level instanceof ServerLevel w?DepthsLedger.get(w.getServer()).mask(DepthsLayout.key(region())):0;}
 public boolean available(){return natural()&&mask()==(1<<index())-1;}
 public boolean accepts(UUID id,String t){return natural()&&active&&token.equals(t)&&live.contains(id);}
 private boolean inside(Player p){var c=worldPosition.offset(0,0,-18);return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.level()==level&&Math.abs(p.getX()-c.getX())<47&&Math.abs(p.getZ()-c.getZ())<47&&Math.abs(p.getY()-c.getY())<38;}
 public void participant(ServerPlayer p){if(participants.size()<16&&inside(p)&&participants.add(p.getUUID()))setChanged();}
 public void defeated(SanctuaryConstruct e){if(!accepts(e.getUUID(),e.token()))return;live.remove(e.getUUID());killed++;setChanged();if(killed==DepthsLayout.target(index()))finish();else if(live.isEmpty())wavePause=3;}
 public boolean claim(ServerPlayer p){if(!natural()||!inside(p)||!(level instanceof ServerLevel w)||!DepthsLedger.get(w.getServer()).claim(DepthsLayout.key(region()),p.getUUID(),index())){p.sendOverlayMessage(Component.translatable("depths.tnd.claim_locked"));return false;}
  give(p,new ItemStack(DeepIndustry.item("raw_asterium"),3+index()/2));give(p,new ItemStack(DeepIndustry.item("sundered_drive")));give(p,new ItemStack(SanctuaryContent.SHARD.get(),12));sound("observatory_reliquary",.6f,1);return true;}
 private static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
 private void sound(String id,float volume,float pitch){if(level instanceof ServerLevel w)w.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound(id.equals("observatory_reliquary")?"witness_reliquary":id),SoundSource.HOSTILE,volume,pitch);}
 public boolean action(ServerPlayer p,int action){if(!natural()||p.isSpectator()||p.distanceToSqr(Vec3.atCenterOf(worldPosition))>64)return false;
  if(action==1)return claim(p);
  if(action==2){if(!active||!(level instanceof ServerLevel w)||!live.stream().allMatch(id->w.getEntity(id)==null))return false;reset();return true;}
  if(action!=0||!available()||active||!inside(p)||level.getDifficulty()==Difficulty.PEACEFUL)return false;start();participant(p);p.closeContainer();return true;}
 private void start(){active=true;spawned=killed=absent=wavePause=0;live.clear();participants.clear();token=UUID.randomUUID().toString();sound("observatory_threshold",.8f,.65f);setChanged();}
 private void reset(){if(level instanceof ServerLevel w)for(var id:live)if(w.getEntity(id) instanceof SanctuaryConstruct e)e.discard();active=false;live.clear();participants.clear();token="";spawned=killed=absent=wavePause=0;setChanged();}
 private void finish(){if(!(level instanceof ServerLevel w)||!available())return;var ledger=DepthsLedger.get(w.getServer());long k=DepthsLayout.key(region());if(!ledger.finish(k,index()))return;active=false;token="";if(DepthsLayout.boss(index()))for(var id:participants)ledger.eligible(k,id,index());sound("observatory_victory",.8f,.8f);w.sendParticles(ParticleTypes.END_ROD,worldPosition.getX()+.5,worldPosition.getY()+2,worldPosition.getZ()+.5,45,2,2,2,.03);for(var p:w.players())if(inside(p))p.sendOverlayMessage(Component.translatable("depths.tnd.cleared",index()+1));setChanged();openGate();}
 public void tick(){if(!(level instanceof ServerLevel w)||!natural())return;openGate();var players=w.players().stream().filter(this::inside).toList();
  if(!active){if(available()&&!players.isEmpty()&&w.getDifficulty()!=Difficulty.PEACEFUL&&players.stream().anyMatch(p->p.position().distanceToSqr(Vec3.atBottomCenterOf(worldPosition.offset(0,0,-18)))<32*32)){start();players.forEach(this::participant);}return;}
  if(players.isEmpty()||w.getDifficulty()==Difficulty.PEACEFUL){absent+=20;if(absent>=200)reset();return;}absent=0;players.forEach(this::participant);
  if(wavePause>0){wavePause--;return;}int target=DepthsLayout.target(index());if(spawned>=target||live.size()>=12||spawned%12==0&&!live.isEmpty())return;
  for(int attempt=0;attempt<4&&spawned<target&&live.size()<12;attempt++){
   boolean boss=DepthsLayout.boss(index())&&spawned==24;SanctuaryConstruct.Kind kind=boss?switch(index()){case 1->SanctuaryConstruct.Kind.DEPTHS_BASTION;case 3->SanctuaryConstruct.Kind.DEPTHS_LENS;case 5->SanctuaryConstruct.Kind.DEPTHS_REAPER;default->SanctuaryConstruct.Kind.DEPTHS_SOVEREIGN;}:spawned%4==3?SanctuaryConstruct.Kind.DEPTHS_RIFLE:spawned%3==2?SanctuaryConstruct.Kind.DEPTHS_HUNTER:SanctuaryConstruct.Kind.DEPTHS_SENTINEL;
   int n=spawned%12;double a=n*Math.PI/6;var centre=worldPosition.offset(0,0,-18);var at=new BlockPos(centre.getX()+(boss?0:(int)(Math.cos(a)*24)),centre.getY(),centre.getZ()+(boss?-8:(int)(Math.sin(a)*24)));if(!w.hasChunkAt(at))break;
   var mob=SanctuaryEntities.TYPES.get(kind).get().create(w,EntitySpawnReason.EVENT);if(mob==null)break;mob.setPos(Vec3.atBottomCenterOf(at));mob.bind(worldPosition,token,Math.min(4,players.size()),boss?0:n);mob.setTarget(players.get(n%players.size()));if(!w.noCollision(mob)||!w.addFreshEntity(mob)){mob.discard();break;}live.add(mob.getUUID());spawned++;setChanged();
  }
 }
 private void openGate(){int i=index();if(i<0||(mask()&(1<<i))==0)return;var exit=DepthsLayout.exit(region(),i);boolean z=i==3;for(int a=-5;a<=5;a++)for(int y=0;y<10;y++){var p=exit.offset(z?a:0,y,z?0:a);if(level.hasChunkAt(p)&&level.getBlockState(p).is(SanctuaryContent.GATE.get()))level.setBlock(p,Blocks.AIR.defaultBlockState(),3);}}
 public Component getDisplayName(){return Component.translatable("depths.tnd.title");}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new DepthsMenu(id,inv,this);}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putString("token",token);out.putBoolean("active",active);out.putInt("spawned",spawned);out.putInt("killed",killed);out.store("live",Codec.STRING.listOf(),live.stream().map(UUID::toString).toList());out.store("participants",Codec.STRING.listOf(),participants.stream().map(UUID::toString).toList());}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);token=in.getStringOr("token","");active=in.getBooleanOr("active",false);spawned=Math.clamp(in.getIntOr("spawned",0),0,48);killed=Math.clamp(in.getIntOr("killed",0),0,spawned);live.clear();participants.clear();loadIds(in,"live",live,12);loadIds(in,"participants",participants,16);}
 private static void loadIds(ValueInput in,String k,Set<UUID> ids,int max){for(var s:in.read(k,Codec.STRING.listOf()).orElse(List.of()).stream().limit(max).toList())try{ids.add(UUID.fromString(s));}catch(IllegalArgumentException ignored){}}
}
