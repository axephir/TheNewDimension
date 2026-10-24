package fr.axephir.tnd.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.expedition.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.world.*;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

final class FieldworkTests {
    private static void check(GameTestHelper h,boolean b,String message){h.assertTrue(b,Component.literal(message));}
    /** GameTestServer omits datapack dimensions. Install a real Vestige ServerLevel with a flat fixture generator, only in this disposable runner. */
    @SuppressWarnings("deprecation")
    static ServerLevel vestige(GameTestHelper h){
        var server=h.getLevel().getServer();var existing=server.getLevel(DevelopmentCommands.DIMENSION);if(existing!=null)return existing;
        try{
            LevelStem authored;
            try(var reader=server.getResourceManager().getResourceOrThrow(Identifier.fromNamespaceAndPath("tnd","dimension/vestige.json")).openAsReader()){
                authored=LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,server.registryAccess()),JsonParser.parseReader(reader)).getOrThrow();
            }
            var storage=LevelStorageSource.createDefault(Path.of("native-vestige-fixtures")).createAccess(UUID.randomUUID().toString());
            var stem=new LevelStem(authored.type(),h.getLevel().getChunkSource().getGenerator());
            var world=new ServerLevel(server,net.minecraft.util.Util.backgroundExecutor(),storage,new DerivedLevelData(server.getWorldData(),server.getWorldData().overworldData()),DevelopmentCommands.DIMENSION,stem,false,1234,List.of(),false);
            server.forgeGetWorldMap().put(DevelopmentCommands.DIMENSION,world);server.markWorldsDirty();
            NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->{if(e.getServer()==server)try{storage.close();}catch(java.io.IOException failure){throw new java.io.UncheckedIOException(failure);}});
            return world;
        }catch(Exception e){throw new IllegalStateException("Cannot create isolated native Vestige test level",e);}
    }
    static BlockPos center(GameTestHelper h,int lane){return new BlockPos(128+lane*48,120,128);}
    // Native fixture tickets can overlap at a chunk boundary. Release only tickets owned by this helper,
    // and retain them until every overlapping platform has finished using the chunk.
    private static final Map<ServerLevel,Map<ChunkPos,PlatformTicket>> PLATFORM_TICKETS=new WeakHashMap<>();
    private static final class PlatformTicket {final boolean owned;int users=1;PlatformTicket(boolean owned){this.owned=owned;}}
    static void platform(ServerLevel world,BlockPos at){
        for(int cx=(at.getX()-12)>>4;cx<=(at.getX()+12)>>4;cx++)for(int cz=(at.getZ()-12)>>4;cz<=(at.getZ()+12)>>4;cz++){var tickets=PLATFORM_TICKETS.computeIfAbsent(world,k->new HashMap<>());var key=new ChunkPos(cx,cz);var ticket=tickets.get(key);if(ticket==null)tickets.put(key,new PlatformTicket(world.setChunkForced(cx,cz,true)));else ticket.users++;}
        for(var p:BlockPos.betweenClosed(at.offset(-12,-1,-12),at.offset(12,4,12)))world.setBlock(p,p.getY()==at.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
    }
    static void release(ServerLevel world,BlockPos at){var tickets=PLATFORM_TICKETS.get(world);if(tickets==null)return;for(int cx=(at.getX()-12)>>4;cx<=(at.getX()+12)>>4;cx++)for(int cz=(at.getZ()-12)>>4;cz<=(at.getZ()+12)>>4;cz++){var key=new ChunkPos(cx,cz);var held=tickets.get(key);if(held!=null&&--held.users==0){tickets.remove(key);if(held.owned)world.setChunkForced(cx,cz,false);}}if(tickets.isEmpty())PLATFORM_TICKETS.remove(world);}
    static ServerPlayer player(GameTestHelper h,ServerLevel world,BlockPos at){
        var p=GameplayTests.player(h);p.teleport(new net.minecraft.world.level.portal.TeleportTransition(world,Vec3.atBottomCenterOf(at),Vec3.ZERO,0,0,net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));p.connection.markClientLoaded();p.hasChangedDimension();return p;
    }
    static void objectives(GameTestHelper h){
        var world=vestige(h);var at=center(h,0);platform(world,at);var p=player(h,world,at);var other=GameplayTests.player(h);
        try{
            for(var name:ExpeditionService.BIOMES){
                var key=ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("tnd",name));
                net.minecraft.server.commands.FillBiomeCommand.fill(world,at.offset(-4,-4,-4),at.offset(4,4,4),world.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key));
                p.tickCount=20;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            }
            check(h,p.getData(PlayerProgression.EXPEDITIONS).biomes()==63,"Native biome reads discover all six real biome IDs in the actual Vestige level");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.RESONANT_SHARD.get(),8));
            // A placed console remains excluded even when successfully repaired.
            var placed=at.offset(5,0,4);world.setBlock(placed,ModContent.ANCIENT_CONSOLE.get().defaultBlockState(),3);
            var chunk=world.getChunkAt(placed);chunk.setData(ProgressionEvents.PLACED,List.of(placed.asLong()));
            var before=p.getData(PlayerProgression.EXPEDITIONS);p.removeData(PlayerProgression.ACTIVITIES);AncientConsoleBlock.activate(p,placed);check(h,p.getData(PlayerProgression.EXPEDITIONS).equals(before),"Player-placed console repairs do not change field notes");
            for(int i=0;i<3;i++){
                var pos=at.offset(i*2-2,0,4);world.setBlock(pos,ModContent.ANCIENT_CONSOLE.get().defaultBlockState(),3);
                p.removeData(PlayerProgression.ACTIVITIES);check(h,AncientConsoleBlock.activate(p,pos),"A real natural console is repaired");
                p.removeData(PlayerProgression.ACTIVITIES);AncientConsoleBlock.activate(p,pos);
            }
            check(h,p.getData(PlayerProgression.EXPEDITIONS).consoles().size()==3,"Already lit consoles never count twice");
            p.getInventory().add(new ItemStack(ModContent.RESONANT_PICKAXE.get()));p.tickCount=20;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            check(h,p.getData(PlayerProgression.EXPEDITIONS).gear(),"Existing resonant tools qualify without requiring another craft");
            var menu=new ExpeditionMenu(91,p.getInventory());check(h,!menu.clickMenuButton(p,-1)&&!menu.clickMenuButton(p,99),"Forged objective indices are rejected");
            int fragments=p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get());check(h,menu.clickMenuButton(p,Expedition.SURVEY.ordinal()),"A complete objective can be claimed through its real menu");
            check(h,!menu.clickMenuButton(p,Expedition.SURVEY.ordinal())&&p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get())==fragments+2,"Repeated packets cannot duplicate the reward");
            check(h,!new ExpeditionMenu(92,other.getInventory()).clickMenuButton(other,Expedition.SURVEY.ordinal()),"Another player's discoveries do not unlock a reward");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,world.registryAccess());p.saveWithoutId(out);
            var clone=new ServerPlayer(world.getServer(),world,p.getGameProfile(),p.clientInformation());clone.load(TagValueInput.create(ProblemReporter.DISCARDING,world.registryAccess(),out.buildResult()));
            check(h,clone.getData(PlayerProgression.EXPEDITIONS).equals(p.getData(PlayerProgression.EXPEDITIONS)),"Full native player serialization retains notes and claimed flags");
            other.restoreFrom(p,false);check(h,other.getData(PlayerProgression.EXPEDITIONS).equals(p.getData(PlayerProgression.EXPEDITIONS)),"Native death restoration copies expedition progress");
        }finally{RecallPrismItem.cancel(p);GameplayTests.cleanup(p);GameplayTests.cleanup(other);release(world,at);}h.succeed();
    }
    static void actions(GameTestHelper h){
        var world=vestige(h);var at=center(h,1);platform(world,at);var p=player(h,world,at);
        var clock=world.dimensionType().defaultClock().orElseThrow();long old=world.getServer().clockManager().getTotalTicks(clock);
        try{
            // Explicitly set the actual independent clock, not the Overworld day.
            world.getServer().clockManager().setTotalTicks(clock,30000);
            for(int i=0;i<6;i++){var pos=at.offset(i-3,0,4);world.setBlock(pos,ModContent.ECHO_BLOOM.get().defaultBlockState().setValue(EchoBloomBlock.CHARGE,3),3);check(h,EchoBloomBlock.harvest(p,pos),"A mature nocturnal aggregate is actually harvested");check(h,!EchoBloomBlock.harvest(p,pos),"An emptied aggregate cannot immediately be harvested again");}
            check(h,p.getData(PlayerProgression.EXPEDITIONS).harvests().size()==6,"Six distinct harvest locations complete the objective");
            for(int i=0;i<4;i++){var mob=VestigeEntities.SENTINEL.get().create(world,EntitySpawnReason.EVENT);mob.setPos(at.getX()+4,at.getY(),at.getZ());mob.setNoAi(true);if(i==3)mob.bindTrial(at,"test-only");world.addFreshEntity(mob);mob.hurtServer(world,p.damageSources().playerAttack(p),10000);}
            check(h,p.getData(PlayerProgression.EXPEDITIONS).sentinels()==3,"Three free sentinels count while a trial guardian is excluded");
            var before=p.getData(PlayerProgression.EXPEDITIONS);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);ExpeditionService.observe(p);check(h,p.getData(PlayerProgression.EXPEDITIONS).equals(before),"Creative actions cannot complete survival objectives");
            check(h,!ExpeditionService.claim(p,Expedition.NIGHT_HARVEST),"Creative mode cannot claim even a previously completed survival objective");
        }finally{world.getServer().clockManager().setTotalTicks(clock,old);GameplayTests.cleanup(p);release(world,at);}h.succeed();
    }
    static void completion(GameTestHelper h){
        var p=GameplayTests.player(h);try{
            var s=ExpeditionProgress.empty();for(int i=0;i<6;i++)s=s.discover(i).harvest("sample"+i);for(int i=0;i<3;i++)s=s.console("console"+i).sentinel();s=s.observe().equip();p.setData(PlayerProgression.EXPEDITIONS,s);
            check(h,!ExpeditionService.claim(p,Expedition.CARTOGRAPHER),"The final trophy requires claiming the seven earlier rewards");
            for(var e:Expedition.values())if(e!=Expedition.CARTOGRAPHER)check(h,ExpeditionService.claim(p,e),"Each completed objective pays exactly once");
            check(h,ExpeditionService.claim(p,Expedition.CARTOGRAPHER)&&p.getInventory().countItem(ModContent.CARTOGRAPHER_TROPHY.get().asItem())==1,"The final native claim grants one real trophy block");
            check(h,!ExpeditionService.claim(p,Expedition.CARTOGRAPHER),"The final trophy is also finite");
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    private static ItemStack prism(ServerPlayer p,ServerLevel world,BlockPos anchor){
        world.setBlock(anchor,ModContent.RETURN_ANCHOR.get().defaultBlockState(),3);var stack=new ItemStack(ModContent.RECALL_PRISM.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);if(!RecallPrismItem.bind(p,stack,anchor))throw new IllegalStateException("Fixture anchor binding failed");return stack;
    }
    private static InteractionResult startRecall(GameTestHelper h,ServerPlayer p,ItemStack stack){
        // An EmbeddedChannel has no ServerConnectionListener network tick. Run the
        // ordinary native player tick that a connected client receives; never invoke
        // the item's callbacks directly or skip its use duration.
        h.onEachTick(p::doTick);return stack.getItem().use(p.level(),p,InteractionHand.MAIN_HAND);
    }
    static void recallSuccess(GameTestHelper h){
        var world=vestige(h);var at=center(h,2);platform(world,at);var p=player(h,world,at);var anchor=at.east(3);var stack=prism(p,world,anchor);
        var tag=world.getBlockEntity(anchor).saveWithFullMetadata(world.registryAccess());var restored=(ReturnAnchorEntity)BlockEntity.loadStatic(anchor,world.getBlockState(anchor),tag,world.registryAccess());
        check(h,restored.token().equals(RecallPrismItem.binding(stack).orElseThrow().token()),"Anchor identity and bound item survive native serialization");
        p.setPos(at.getX()-7.5,at.getY(),at.getZ()+.5);check(h,!stack.getItem().use(world,p,InteractionHand.MAIN_HAND).consumesAction()&&!p.isUsingItem(),"A bound prism cannot start without dust");p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        var pet=VestigeEntities.SHARDLING.get().create(world,EntitySpawnReason.EVENT);pet.setPos(p.getX()+1,p.getY(),p.getZ());pet.tame(p);pet.setNoAi(true);world.addFreshEntity(pet);
        check(h,startRecall(h,p,stack).consumesAction(),"A bound prism begins real native use");
        h.runAfterDelay(120,()->{try{
            check(h,p.distanceToSqr(Vec3.atBottomCenterOf(anchor.above()))<1,"Holding the item for five seconds actually recalls the player safely; position="+p.position()+", fuel="+p.getInventory().countItem(ModContent.ECHO_DUST.get())+", remaining="+p.getUseItemRemainingTicks());
            check(h,p.getInventory().countItem(ModContent.ECHO_DUST.get())==2&&p.getCooldowns().isOnCooldown(stack),"Only successful arrival consumes two dust and starts the native cooldown");
            check(h,pet.distanceToSqr(p)<25,"A nearby following familiar accompanies the recall");
        }finally{pet.discard();RecallPrismItem.cancel(p);GameplayTests.cleanup(p);release(world,at);}h.succeed();});
    }
    static void recallMovement(GameTestHelper h){
        var world=vestige(h);var at=center(h,3);platform(world,at);var p=player(h,world,at);var stack=prism(p,world,at.east(3));p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        startRecall(h,p,stack);p.setPos(p.getX()+1,p.getY(),p.getZ());
        h.runAfterDelay(120,()->{try{check(h,p.getX()<at.getX()+2&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4&&!p.getCooldowns().isOnCooldown(stack),"Moving interrupts recall without moving the player, charging fuel or starting cooldown");}finally{RecallPrismItem.cancel(p);GameplayTests.cleanup(p);release(world,at);}h.succeed();});
    }
    static void recallDamage(GameTestHelper h){
        var world=vestige(h);var at=center(h,4);platform(world,at);var p=player(h,world,at);var stack=prism(p,world,at.east(3));p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        startRecall(h,p,stack);p.hurtServer(world,p.damageSources().generic(),1);
        h.runAfterDelay(120,()->{try{check(h,p.distanceToSqr(Vec3.atBottomCenterOf(at))<1&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4,"Actual incoming damage interrupts the channel without consuming dust");}finally{RecallPrismItem.cancel(p);GameplayTests.cleanup(p);release(world,at);}h.succeed();});
    }
    static void recallBlocked(GameTestHelper h){
        var world=vestige(h);var at=center(h,5);platform(world,at);var p=player(h,world,at);var anchor=at.east(3);var stack=prism(p,world,anchor);p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        p.setPos(at.getX()-7.5,at.getY(),at.getZ()+.5);
        for(var pos:BlockPos.betweenClosed(anchor.offset(-3,0,-3),anchor.offset(3,3,3)))if(!pos.equals(anchor))world.setBlock(pos,Blocks.GOLD_BLOCK.defaultBlockState(),3);
        startRecall(h,p,stack);
        h.runAfterDelay(120,()->{try{check(h,p.getX()<at.getX()&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4,"A fully obstructed arrival preserves fuel and the source position");check(h,world.getBlockState(anchor.above()).is(Blocks.GOLD_BLOCK),"Recall never deletes the obstruction");}finally{RecallPrismItem.cancel(p);GameplayTests.cleanup(p);release(world,at);}h.succeed();});
    }
    static void recallReplaced(GameTestHelper h){
        var world=vestige(h);var at=center(h,6);platform(world,at);var p=player(h,world,at);var anchor=at.east(3);var stack=prism(p,world,anchor);p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        world.setBlock(anchor,Blocks.AIR.defaultBlockState(),3);world.setBlock(anchor,ModContent.RETURN_ANCHOR.get().defaultBlockState(),3);
        check(h,!((ReturnAnchorEntity)world.getBlockEntity(anchor)).token().equals(RecallPrismItem.binding(stack).orElseThrow().token()),"Replacing the block creates a new identity");startRecall(h,p,stack);
        h.runAfterDelay(120,()->{try{check(h,p.distanceToSqr(Vec3.atBottomCenterOf(at))<1&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4,"An old prism cannot recall to a replaced anchor");}finally{RecallPrismItem.cancel(p);GameplayTests.cleanup(p);release(world,at);}h.succeed();});
    }
    static void recallGuards(GameTestHelper h){
        var p=GameplayTests.player(h);var stack=new ItemStack(ModContent.RECALL_PRISM.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try{check(h,!RecallPrismItem.bind(p,stack,h.absolutePos(new BlockPos(2,1,2))),"Binding cannot happen outside Vestige");check(h,!stack.getItem().use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"A prism never bypasses dimensional progression from the Overworld");
            check(h,RecallPrismItem.binding(stack).isEmpty()&&!RecallPrismItem.finishRecall(p,stack),"Unbound and unsolicited finish attempts are rejected");
        }finally{GameplayTests.cleanup(p);}h.succeed();
    }
    static void observatory(GameTestHelper h){
        var world=h.getLevel();var template=world.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","observatory")).orElseThrow();var origin=h.absolutePos(new BlockPos(1,1,1));
        check(h,template.placeInWorld(world,origin,origin,new StructurePlaceSettings(),world.getRandom(),2),"Authored observatory places with native block entities");
        check(h,world.getBlockEntity(origin.offset(31,1,30)) instanceof SurveyTerminalEntity desk&&desk.natural(),"Generated survey desks carry natural-site authority");
        check(h,world.getBlockEntity(origin.offset(26,1,26)) instanceof ReturnAnchorEntity,"The observatory includes a functioning real return anchor");
        var pet=VestigeEntities.SHARDLING.get().create(world,EntitySpawnReason.EVENT);pet.setPos(Vec3.atBottomCenterOf(origin.offset(31,1,32)));world.addFreshEntity(pet);
        pet.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(80);
        h.runAfterDelay(2,()->{try{var path=pet.getNavigation().createPath(origin.offset(41,9,39),0);check(h,path!=null&&path.canReach(),"The upper observation gallery is reachable through the actual native stair geometry; path="+(path==null?"null":path.getEndNode()));}
        finally{pet.discard();}h.succeed();});
    }
    static void surveyAuthority(GameTestHelper h){
        var world=vestige(h);var at=center(h,7);platform(world,at);var p=player(h,world,at);
        try{
            world.setBlock(at.east(2),ModContent.SURVEY_TERMINAL.get().defaultBlockState(),3);var desk=(SurveyTerminalEntity)world.getBlockEntity(at.east(2));
            var hit=new BlockHitResult(Vec3.atCenterOf(desk.getBlockPos()),Direction.WEST,desk.getBlockPos(),false);
            desk.getBlockState().useWithoutItem(world,p,hit);check(h,!p.getData(PlayerProgression.EXPEDITIONS).observatory()&&p.containerMenu instanceof ExpeditionMenu,"Crafted desks open the actual menu but grant no natural discovery");p.closeContainer();
            var tag=desk.saveWithFullMetadata(world.registryAccess());tag.putInt("SurveySite",1);var natural=(SurveyTerminalEntity)BlockEntity.loadStatic(desk.getBlockPos(),desk.getBlockState(),tag,world.registryAccess());desk.setRemoved();natural.setLevel(world);world.setBlockEntity(natural);
            natural.getBlockState().useWithoutItem(world,p,hit);check(h,p.getData(PlayerProgression.EXPEDITIONS).observatory(),"Only a natural desk in Vestige records the observatory objective");p.closeContainer();
        }finally{GameplayTests.cleanup(p);release(world,at);}h.succeed();
    }
    private FieldworkTests(){}
}
