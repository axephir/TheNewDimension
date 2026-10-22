package fr.axephir.tnd.narrative;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.city.*;
import fr.axephir.tnd.core.*;
import fr.axephir.tnd.progression.PlayerProgression;
import fr.axephir.tnd.technology.Industry;
import fr.axephir.tnd.world.PortalLanding;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.io.IOException;
import java.util.*;

/** Personal narrative projections. The real Overworld is never a destruction target, backup destination or restoration output. */
public final class Convergence {
    public static final ResourceKey<Level> ECHO=ResourceKey.create(Registries.DIMENSION,Identifier.fromNamespaceAndPath("tnd","aftermath"));
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<String>> RETURN=PlayerProgression.TYPES.register("echo_return",()->AttachmentType.builder(()->"").serialize(Codec.STRING.fieldOf("reference")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> PHASE=PlayerProgression.TYPES.register("echo_phase",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,6).fieldOf("phase")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> FINISHED=PlayerProgression.TYPES.register("convergence_finished",()->AttachmentType.builder(()->false).serialize(Codec.BOOL.fieldOf("finished")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<String>> LENS_KEY=PlayerProgression.TYPES.register("spatial_lens_key",()->AttachmentType.builder(()->"").serialize(Codec.STRING.fieldOf("key")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> PERCEPTION=PlayerProgression.TYPES.register("spatial_perception",()->AttachmentType.builder(()->0L).serialize(Codec.LONG.fieldOf("until")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_LONG).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<BlockPos>> SIGNAL=PlayerProgression.TYPES.register("echo_signal",()->AttachmentType.builder(()->BlockPos.ZERO).serialize(BlockPos.CODEC.fieldOf("position")).copyOnDeath().sync((h,p)->h==p,BlockPos.STREAM_CODEC).build());
    private static final Map<MinecraftServer,Map<UUID,Session>> SESSIONS=new WeakHashMap<>();
    public static final net.neoforged.neoforge.common.world.chunk.TicketController CAMERA_TICKETS=new net.neoforged.neoforge.common.world.chunk.TicketController(Identifier.fromNamespaceAndPath("tnd","projection_camera"),(w,h)->{for(var owner:List.copyOf(h.getEntityTickets().keySet()))h.removeAllTickets(owner);for(var at:List.copyOf(h.getBlockTickets().keySet()))h.removeAllTickets(at);});
    private static final Set<UUID> RETURNING=new HashSet<>();
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("tnd/convergence");
    private static final class Session {
        CompoundTag reference;EchoReference.State state;final EchoJournal journal;final EchoCopyTask copy;final BlockPos reactor;
        long since;ArmorStand camera;int phase=1;boolean hinted,cameraTickets;
        Session(CompoundTag ref,EchoReference.State state,EchoJournal journal,EchoCopyTask copy,BlockPos reactor,long now){reference=ref.copy();this.state=state;this.journal=journal;this.copy=copy;this.reactor=reactor.immutable();since=now;}
    }
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> TRACES=PlayerProgression.TYPES.register("echo_traces",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,15).fieldOf("heard")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    private static final Map<UUID,Long> INSPECTION_NEXT=new HashMap<>();
    public static void bootstrap(){}
    /** The client supplies no position. Only the owner's ray into their completed personal projection is accepted. */
    public static boolean inspect(ServerPlayer p){
        var s=session(p);if(s==null||!inEcho(p)||(s.phase!=3&&s.phase!=4)||p.containerMenu!=p.inventoryMenu)return false;
        long now=p.level().getServer().overworld().getGameTime();if(now<INSPECTION_NEXT.getOrDefault(p.getUUID(),0L))return false;
        var hit=p.pick(6,1,false);if(!(hit instanceof net.minecraft.world.phys.BlockHitResult block)||hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return false;
        var at=block.getBlockPos();var center=s.copy.mapped(s.state.machine());int limit=s.copy.radius()*16+15;
        if(Math.abs(at.getX()-center.getX())>limit||Math.abs(at.getZ()-center.getZ())>limit||!p.level().hasChunkAt(at))return false;
        var state=p.level().getBlockState(at);int type=at.equals(center)?3:state.getBlock() instanceof net.minecraft.world.level.block.EntityBlock?1:net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("tnd")?0:2;
        INSPECTION_NEXT.put(p.getUUID(),now+30);p.setData(TRACES,p.getData(TRACES)|(1<<type));
        p.sendOverlayMessage(Component.translatable("echo.tnd.trace."+type));fr.axephir.tnd.expedition.ExpeditionService.sound(p,ModContent.atmosphereSound(type==3?"spatial_chime":"hollow_resonance"),SoundSource.AMBIENT,.3f,.65f+type*.12f);
        p.level().sendParticles(p,net.minecraft.core.particles.ParticleTypes.ENCHANT,false,false,at.getX()+.5,at.getY()+.5,at.getZ()+.5,12,.35,.35,.35,0);
        if(type==3)ConvergenceMenu.open(p,center,false);return true;
    }
    public static boolean inEcho(net.minecraft.world.entity.Entity p){return p.level().dimension().equals(ECHO);}
    private static Map<UUID,Session> sessions(MinecraftServer server){return SESSIONS.computeIfAbsent(server,s->new HashMap<>());}
    private static Session session(ServerPlayer p){return sessions(p.level().getServer()).get(p.getUUID());}
    public static int progress(ServerPlayer p){var s=session(p);return s==null?0:s.copy.progress();}
    public static boolean assembled(ServerLevel w,BlockPos p,boolean reactor){
        int r=reactor?2:1;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(reactor?Math.max(Math.abs(x),Math.abs(z))==r:x!=0||z!=0){var at=p.offset(x,-1,z);if(!w.hasChunkAt(at)||!w.getBlockState(at).is(Industry.FRAME.get()))return false;}
        for(int x:new int[]{-r,r})for(int z:new int[]{-r,r}){
            if(reactor)for(int y=0;y<3;y++){var at=p.offset(x,y,z);if(!w.hasChunkAt(at)||!w.getBlockState(at).is(CityContent.DECOR.get("carbon_panel").get()))return false;}
            var at=p.offset(x,reactor?3:0,z);if(!w.hasChunkAt(at)||!w.getBlockState(at).is(CityContent.DECOR.get("relay_coil").get()))return false;
        }return true;
    }
    public static BlockPos restorer(ServerLevel w,BlockPos reactor){
        BlockPos best=null;double distance=145;for(var p:BlockPos.betweenClosed(reactor.offset(-12,-4,-12),reactor.offset(12,4,12)))if(p.distSqr(reactor)<=144&&w.hasChunkAt(p)&&w.getBlockState(p).is(ConvergenceContent.RESTORER.get())&&assembled(w,p,false)&&p.distSqr(reactor)<distance){best=p.immutable();distance=p.distSqr(reactor);}return best;
    }
    /** Status is entirely server-derived. No client can supply a reference, world, footprint, inventory or scene owner. */
    public static int readiness(ServerPlayer p,BlockPos reactor){
        if(p.getData(FINISHED))return 6;if(p.getData(PHASE)==1)return 5;
        if(!p.level().dimension().equals(Level.OVERWORLD)||p.isSpectator())return 1;
        if((p.getData(Codex.NOTES)&255)!=255)return 2;
        if(!p.level().getBlockState(reactor).is(ConvergenceContent.REACTOR.get())||!assembled(p.level(),reactor,true))return 3;
        if(restorer(p.level(),reactor)==null)return 4;return 0;
    }
    public static boolean begin(ServerPlayer p,BlockPos reactor,int radius){
        var source=p.level();var server=source.getServer();if(!p.isAlive()||p.distanceToSqr(Vec3.atCenterOf(reactor))>64||readiness(p,reactor)!=0||radius<0||radius>12)return false;
        var echo=server.getLevel(ECHO);if(echo==null||echo==source||source.getMinY()<echo.getMinY()||source.getMaxY()>echo.getMaxY())return false;
        if(sessions(server).values().stream().anyMatch(s->s.phase==1)){p.sendOverlayMessage(Component.translatable("story.tnd.busy"));return false;}
        BlockPos machine=restorer(source,reactor);if(machine==null)return false;UUID token=UUID.randomUUID();int slot=StoryRecords.get(server).nextSlot();
        // A damaged world index must not reuse a projection that still exists on disk.
        boolean unused=false;
        for(int attempts=0;attempts<1024&&slot<=100000;attempts++,slot++){if(EchoJournal.plotReserved(server,slot))continue;var chunk=echo.getChunk(EchoCopyTask.plotX(slot)>>4,EchoCopyTask.plotZ(slot)>>4);if(Arrays.stream(chunk.getSections()).allMatch(net.minecraft.world.level.chunk.LevelChunkSection::hasOnlyAir)){unused=true;break;}}
        if(!unused||slot>100000)return false;
        var reference=EchoReference.capture(p,token,machine,slot);var state=EchoReference.validate(reference,p.getUUID(),token);var journal=new EchoJournal(server,p.getUUID(),token);
        try{EchoJournal.reservePlot(server,slot,p.getUUID(),token);journal.prepare(reference);}catch(IOException|RuntimeException failure){LOG.error("Cannot verify both story references; player and source world unchanged",failure);p.sendSystemMessage(Component.translatable("story.tnd.prepare_failed"));return false;}
        String marker=encode(state);StoryRecords.get(server).reserve(p.getUUID(),token,marker,slot);p.setData(RETURN,marker);p.setData(PHASE,1);
        sessions(server).put(p.getUUID(),new Session(reference,state,journal,new EchoCopyTask(source,echo,machine,slot,radius),reactor,source.getGameTime()));
        server.getPlayerList().saveAll();return true;
    }
    @SubscribeEvent public void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;var s=session(p);if(s==null)return;var server=p.level().getServer();long now=server.overworld().getGameTime();
        try{
            if(s.phase==1){
                if(!p.isAlive()||p.level().dimension()!=s.state.source()||p.distanceToSqr(Vec3.atCenterOf(s.reactor))>32*32){cancel(p);return;}
                s.copy.step();if(!s.copy.finished())return;
                var echo=server.getLevel(ECHO);if(echo==null||!p.level().getBlockState(s.state.machine()).is(ConvergenceContent.RESTORER.get())||!assembled(p.level(),s.state.machine(),false)||!p.level().getBlockState(s.reactor).is(ConvergenceContent.REACTOR.get())||!assembled(p.level(),s.reactor,true)){cancel(p);p.sendSystemMessage(Component.translatable("story.tnd.prepare_failed"));return;}p.closeContainer();p.stopRiding();
                // Preparation is interactive. Refresh both references at the exact transition so moving items during preparation cannot duplicate them.
                var fresh=EchoReference.capture(p,s.state.session(),s.state.machine(),s.state.slot());s.journal.prepare(fresh);s.reference=fresh;s.state=EchoReference.validate(fresh,p.getUUID(),s.state.session());
                p.setData(RETURN,encode(s.state));StoryRecords.get(server).reserve(p.getUUID(),s.state.session(),encode(s.state),s.state.slot());
                s.journal.mark("entering");server.getPlayerList().saveAll();p.setGameMode(GameType.SPECTATOR);
                var at=Vec3.atCenterOf(s.copy.mapped(s.state.machine())).add(cameraRadius(s),36,0);
                if(p.teleport(new TeleportTransition(echo,at,Vec3.ZERO,90,35,TeleportTransition.DO_NOTHING))==null){p.setGameMode(s.state.mode());cancel(p);return;}
                s.phase=2;s.since=now;p.setData(PHASE,2);p.setData(SIGNAL,s.copy.mapped(s.state.machine()));StoryRecords.get(server).phase(p.getUUID(),2);s.journal.mark("exploring");cameraTickets(p,s,true);
                var camera=EntityTypes.ARMOR_STAND.create(echo,EntitySpawnReason.EVENT);camera.setInvisible(true);camera.setNoGravity(true);camera.setInvulnerable(true);camera.addTag("tnd_echo_camera");camera.setPos(at);camera.setYRot(90);camera.setYHeadRot(90);camera.setXRot(35);
                echo.addFreshEntity(camera);s.camera=camera;p.setCamera(camera);return;
            }
            if(!inEcho(p)){if(p.getData(PHASE)>=2&&p.getData(PHASE)<=5)recover(p);return;}
            if(s.phase==2){
                if(now-s.since>=160||p.isShiftKeyDown()||s.camera==null||p.getCamera()!=s.camera){finishCinema(p);return;}
                if((now-s.since)%10==0)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCameraPacket(s.camera));
                var center=Vec3.atCenterOf(s.copy.mapped(s.state.machine()));double angle=(now-s.since)*.0055,r=cameraRadius(s);var pos=center.add(Math.cos(angle)*r,36+Math.sin(angle)*5,Math.sin(angle)*r);
                var delta=center.subtract(pos);s.camera.setPos(pos);s.camera.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));s.camera.setYHeadRot(s.camera.getYRot());s.camera.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));
                if((now-s.since)%18==0){var impact=s.copy.cinematicImpact((int)((now-s.since)/18-1));p.level().sendParticles(p,net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,false,false,impact.getX()+.5,impact.getY()+.5,impact.getZ()+.5,1,0,0,0,0);p.level().playSound(null,impact,ModContent.atmosphereSound("city_impact"),SoundSource.AMBIENT,.9f,.6f);}
            }else if(s.phase==3||s.phase==4){
                if(p.getCamera()!=p)p.setCamera(p);
                if(!s.hinted&&now-s.since>=2400){s.hinted=true;s.phase=4;p.setData(PHASE,4);StoryRecords.get(server).phase(p.getUUID(),4);fr.axephir.tnd.expedition.ExpeditionService.sound(p,ModContent.atmosphereSound("low_signal"),SoundSource.AMBIENT,.8f,.75f);p.sendSystemMessage(Component.translatable("story.tnd.signal"));}
                // The unobserved boundary is a spatial fold, never a corridor into somebody else's projection.
                var center=s.copy.mapped(s.state.machine());int limit=s.copy.radius()*16+7;
                if(Math.abs(p.getX()-center.getX())>limit||Math.abs(p.getZ()-center.getZ())>limit||p.getY()<p.level().getMinY()-8)p.teleportTo(center.getX()+.5,center.getY()+4,center.getZ()+.5);
            }else if(s.phase==5&&now-s.since>=30)recover(p);
        }catch(IOException|RuntimeException failure){LOG.error("Interrupted personal projection; preserving both reference journals and original world",failure);if(inEcho(p))recover(p);else cancel(p);}
    }
    private static double cameraRadius(Session s){return Math.min(52,s.copy.radius()*16+4);}
    private static void cameraTickets(ServerPlayer p,Session s,boolean add){
        var echo=p.level().getServer().getLevel(ECHO);if(echo==null||!add&&!s.cameraTickets)return;var center=s.copy.mapped(s.state.machine());
        // Spectators do not keep camera entities ticking. Cover the orbit for eight seconds only; never ticket the source world here.
        int reach=(int)Math.ceil(cameraRadius(s)/16);for(int x=-reach;x<=reach;x++)for(int z=-reach;z<=reach;z++)CAMERA_TICKETS.forceChunk(echo,p.getUUID(),(center.getX()>>4)+x,(center.getZ()>>4)+z,add,false);s.cameraTickets=add;
    }
    public static void finishCinema(ServerPlayer p){var s=session(p);if(s==null||s.phase!=2)return;
        var point=s.camera==null?p.position():s.camera.position();float yaw=s.camera==null?p.getYRot():s.camera.getViewYRot(1),pitch=s.camera==null?p.getXRot():s.camera.getViewXRot(1);
        p.setCamera(p);p.setYRot(yaw);p.setXRot(pitch);p.teleportTo(point.x,point.y,point.z);if(s.camera!=null){s.camera.discard();s.camera=null;}cameraTickets(p,s,false);
        s.phase=3;s.since=p.level().getServer().overworld().getGameTime();p.setData(PHASE,3);StoryRecords.get(p.level().getServer()).phase(p.getUUID(),3);p.sendSystemMessage(Component.translatable("story.tnd.detached"));}
    public static boolean requestRestoration(ServerPlayer p){var s=session(p);if(s==null||!inEcho(p)||s.phase<3||s.phase>4)return false;s.phase=5;s.since=p.level().getServer().overworld().getGameTime();p.setData(PHASE,5);p.closeContainer();return true;}
    public static boolean cancel(ServerPlayer p){var s=sessions(p.level().getServer()).remove(p.getUUID());if(s==null||inEcho(p)){if(s!=null)sessions(p.level().getServer()).put(p.getUUID(),s);return false;}p.setData(RETURN,"");p.setData(PHASE,p.getData(FINISHED)?6:0);StoryRecords.get(p.level().getServer()).phase(p.getUUID(),0);try{s.journal.mark("canceled-before-entry");}catch(IOException failure){LOG.warn("Reference journal retained after cancellation",failure);}return true;}
    public static boolean recover(ServerPlayer p){
        var server=p.level().getServer();var s=session(p);String marker=p.getData(RETURN);if(marker.isEmpty()){var record=StoryRecords.get(server).record(p.getUUID());if(record!=null&&record.getIntOr("Phase",0)>0&&record.getIntOr("Phase",0)<6)marker=record.getStringOr("Return","");}
        EchoReference.State state=null;
        if(!marker.isEmpty())try{state=decode(marker,p.getUUID());}catch(RuntimeException bad){LOG.warn("Invalid return header; searching retained independent journals",bad);}
        if(state==null&&inEcho(p))state=EchoJournal.discover(server,p);
        if(state==null)return false;
        if(!inEcho(p)&&(p.getData(PHASE)<=1||p.getData(PHASE)==6)){if(p.isSpectator()&&state.mode()!=GameType.SPECTATOR)p.setGameMode(state.mode());p.setData(RETURN,"");p.setData(PHASE,p.getData(FINISHED)?6:0);StoryRecords.get(server).phase(p.getUUID(),0);sessions(server).remove(p.getUUID());return true;}
        var journal=new EchoJournal(server,p.getUUID(),state.session());CompoundTag reference;
        try{reference=journal.read();EchoReference.validate(reference,p.getUUID(),state.session());}catch(IOException|RuntimeException failure){
            LOG.error("Disk reference unavailable; retaining journals and the untouched native spectator inventory",failure);reference=s==null?null:s.reference.copy();
        }
        var source=server.getLevel(state.source());if(source==null)return false;var feet=BlockPos.containing(state.position());var landing=PortalLanding.findArrival(source,feet.below(),p);
        if(landing.isEmpty())for(int r:new int[]{8,16,24,32}){for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}){int x=feet.getX()+d[0]*r,z=feet.getZ()+d[1]*r;source.getChunk(x>>4,z>>4);int y=source.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);landing=PortalLanding.findArrival(source,new BlockPos(x,y-1,z),p);if(landing.isPresent())break;}if(landing.isPresent())break;}
        if(landing.isEmpty()){if(s!=null&&inEcho(p)){s.phase=4;s.hinted=true;p.setData(PHASE,4);StoryRecords.get(server).phase(p.getUUID(),4);}p.sendSystemMessage(Component.translatable("story.tnd.return_blocked"));return false;}
        p.setData(PHASE,5);StoryRecords.get(server).phase(p.getUUID(),5);p.setCamera(p);if(s!=null){if(s.camera!=null)s.camera.discard();cameraTickets(p,s,false);}RETURNING.add(p.getUUID());
        try{if(p.teleport(new TeleportTransition(source,Vec3.atBottomCenterOf(landing.get()),Vec3.ZERO,state.yaw(),state.pitch(),TeleportTransition.DO_NOTHING))==null)return false;
            if(reference!=null)EchoReference.restoreResources(p,reference);p.setGameMode(state.mode());if(reference!=null)p.getAbilities().flying=p.getAbilities().mayfly&&reference.getBooleanOr("Flying",false);p.onUpdateAbilities();p.clearFire();p.fallDistance=0;p.setAirSupply(p.getMaxAirSupply());
            p.setData(FINISHED,true);p.setData(PHASE,6);p.setData(RETURN,"");StoryRecords.get(server).phase(p.getUUID(),6);sessions(server).remove(p.getUUID());server.getPlayerList().saveAll();
            try{journal.mark("returned-and-player-saved");}catch(IOException failure){LOG.warn("Return succeeded; keeping all reference files after final journal write failure",failure);}
            p.sendSystemMessage(Component.translatable("story.tnd.revelation"));return true;
        }finally{RETURNING.remove(p.getUUID());}
    }
    public static BlockPos echoMachine(ServerPlayer p){var s=session(p);if(s!=null)return s.copy.mapped(s.state.machine());try{var state=decode(p.getData(RETURN),p.getUUID());return new BlockPos(EchoCopyTask.plotX(state.slot())+(state.machine().getX()&15),state.machine().getY(),EchoCopyTask.plotZ(state.slot())+(state.machine().getZ()&15));}catch(RuntimeException e){return p.blockPosition();}}
    @SubscribeEvent public void orphanCamera(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(!e.getLevel().isClientSide()&&e.loadedFromDisk()&&e.getEntity().entityTags().contains("tnd_echo_camera"))e.setCanceled(true);}
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&(inEcho(p)||!p.getData(RETURN).isEmpty()))recover(p);}
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p&&!p.getData(RETURN).isEmpty()&&p.getData(PHASE)>=2&&p.getData(PHASE)<=5)recover(p);}
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var s=session(p);if(s!=null&&s.phase==1)cancel(p);else if(s!=null){cameraTickets(p,s,false);if(s.camera!=null){p.setCamera(p);s.camera.discard();}sessions(p.level().getServer()).remove(p.getUUID());}}}
    @SubscribeEvent public void stop(ServerStoppedEvent e){SESSIONS.remove(e.getServer());RETURNING.clear();INSPECTION_NEXT.clear();}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void damage(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&inEcho(p))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void travel(EntityTravelToDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p&&inEcho(p)&&!e.getDimension().equals(p.level().dimension())&&!RETURNING.contains(p.getUUID())){e.setCanceled(true);p.sendOverlayMessage(Component.translatable("story.tnd.folded_exit"));}}
    private static String encode(EchoReference.State s){return s.session()+";"+s.source().identifier()+";"+s.position().x+";"+s.position().y+";"+s.position().z+";"+s.yaw()+";"+s.pitch()+";"+s.mode().getId()+";"+s.slot()+";"+s.machine().getX()+";"+s.machine().getY()+";"+s.machine().getZ();}
    private static EchoReference.State decode(String marker,UUID owner){String[] f=marker.split(";",-1);if(f.length!=12||marker.length()>1024)throw new IllegalArgumentException("Invalid return header");var tag=new CompoundTag();tag.putString("Owner",owner.toString());tag.putString("Session",UUID.fromString(f[0]).toString());tag.putString("Dimension",f[1]);tag.putDouble("X",Double.parseDouble(f[2]));tag.putDouble("Y",Double.parseDouble(f[3]));tag.putDouble("Z",Double.parseDouble(f[4]));tag.putFloat("Yaw",Float.parseFloat(f[5]));tag.putFloat("Pitch",Float.parseFloat(f[6]));tag.putInt("Mode",Integer.parseInt(f[7]));tag.putInt("Plot",Integer.parseInt(f[8]));tag.putInt("MachineX",Integer.parseInt(f[9]));tag.putInt("MachineY",Integer.parseInt(f[10]));tag.putInt("MachineZ",Integer.parseInt(f[11]));return EchoReference.validate(tag,owner,UUID.fromString(f[0]));}
}
