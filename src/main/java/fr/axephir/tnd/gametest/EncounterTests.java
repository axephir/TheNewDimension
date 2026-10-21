package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.*;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.world.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

public final class EncounterTests {
    private static void check(GameTestHelper h,boolean ok,String reason){h.assertTrue(ok,Component.literal(reason));}
    private static Shardling scout(ServerLevel level,BlockPos at) {
        var pet=VestigeEntities.SHARDLING.get().create(level,EntitySpawnReason.COMMAND);
        pet.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);level.addFreshEntity(pet);return pet;
    }
    private static RiftSentinel sentinel(ServerLevel level,BlockPos at) {
        var mob=VestigeEntities.SENTINEL.get().create(level,EntitySpawnReason.COMMAND);
        mob.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);level.addFreshEntity(mob);return mob;
    }
    public static void bond(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var pet=scout(level,h.absolutePos(new BlockPos(2,1,2)));
        try {
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(),2));
            pet.mobInteract(p,InteractionHand.MAIN_HAND);
            check(h,pet.getExperienceReward(level,p)==0,"Hunting a familiar never produces vanilla XP");
            check(h,pet.isTame()&&pet.isOwnedBy(p),"A composite crystal establishes the actual persisted owner");
            check(h,p.getMainHandItem().getCount()==1,"One bond consumes exactly one composite crystal");
            pet.setHealth(16);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.RESONANT_SHARD.get(),2));
            pet.mobInteract(p,InteractionHand.MAIN_HAND);
            check(h,pet.getHealth()==24&&p.getMainHandItem().getCount()==1,"Owner heals eight HP with one resonant shard");
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);pet.mobInteract(p,InteractionHand.MAIN_HAND);
            check(h,pet.isOrderedToSit()&&pet.isInSittingPose(),"Empty hand orders this familiar to wait");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());pet.saveWithoutId(out);
            var restored=(Shardling)EntityType.create(VestigeEntities.SHARDLING.get(),TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,EntitySpawnReason.LOAD).orElseThrow();
            check(h,restored.isTame()&&restored.getOwnerReference().matches(p)&&restored.isOrderedToSit(),"Native save/load preserves owner and wait order");
            check(h,restored.getHealth()==24&&!restored.canAttack(p),"Restored familiar preserves health and cannot attack its owner");
            var foreign=VestigeEntities.SHARDLING.get().create(level,EntitySpawnReason.COMMAND);foreign.setTame(true,false);foreign.setOwnerReference(EntityReference.of(java.util.UUID.randomUUID()));
            restored.setOrderedToSit(false);
            check(h,!restored.canAttack(foreign),"A companion cannot attack another player's tamed pet");
            pet.mobInteract(p,InteractionHand.MAIN_HAND);check(h,!pet.isOrderedToSit(),"The same interaction resumes following");
        }finally{pet.discard();GameplayTests.cleanup(p);}
        h.succeed();
    }
    public static void follow(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));for(var cell:BlockPos.betweenClosed(at.offset(-6,-1,-6),at.offset(6,2,6)))level.setBlock(cell,cell.getY()==at.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
        p.setPos(at.getX()+5.5,at.getY(),at.getZ()+.5);
        var pet=scout(level,at.west(5));pet.tame(p);
        double before=pet.distanceToSqr(p);
        // Stay clearly above the 7-block start threshold and below vanilla's
        // 12-block teleport threshold: random idle motion must not decide the test.
        check(h,before>49&&before<144,"The fixture requires actual ground navigation");
        h.runAfterDelay(90,()->{
            try{check(h,pet.distanceToSqr(p)<before/2,"The familiar must physically navigate toward its owner");}
            finally{pet.discard();GameplayTests.cleanup(p);}h.succeed();
        });
    }
    private static void platform(ServerLevel level,BlockPos at) {
        level.getChunkAt(at);
        for(var p:BlockPos.betweenClosed(at.offset(-4,-1,-4),at.offset(4,2,4)))level.setBlock(p,p.getY()==at.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
    }
    public static void travel(GameTestHelper h) {
        var p=GameplayTests.player(h);var source=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));
        platform(source,at);p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);
        var pet=scout(source,at.east(2));pet.tame(p);var uuid=pet.getUUID();
        var waiting=scout(source,at.west(2));waiting.tame(p);waiting.setOrderedToSit(true);
        travelStep(h,p,uuid,waiting,at,0,new java.util.HashSet<ServerLevel>());
    }
    private static void travelStep(GameTestHelper h,ServerPlayer p,java.util.UUID uuid,Shardling waiting,BlockPos at,int step,java.util.Set<ServerLevel> tickets) {
        var dimensions=java.util.List.of(Level.END,Level.NETHER,Level.OVERWORLD);
        var source=p.level();var target=source.getServer().getLevel(dimensions.get(step));
        var landing=new BlockPos(at.getX(),80,at.getZ());platform(target,landing);
        check(h,p.teleport(new TeleportTransition(target,Vec3.atBottomCenterOf(landing),Vec3.ZERO,0,0,TeleportTransition.DO_NOTHING))!=null,"Native player transfer succeeds");
        p.connection.markClientLoaded();p.hasChangedDimension();
        // As in coldArrival, the embedded client cannot acknowledge destination
        // chunk batches. Maintain visibility AFTER the real transfer, without moving
        // a pet left behind or changing the production transport implementation.
        if(target.setChunkForced(landing.getX()>>4,landing.getZ()>>4,true))tickets.add(target);
        h.runAfterDelay(20,()->{
            try {
                check(h,target.getEntity(uuid) instanceof Shardling moved&&moved.getOwnerReference().matches(p),"Companion actually arrives in "+dimensions.get(step).identifier());
                var arrived=(Shardling)target.getEntity(uuid);
                check(h,arrived.distanceToSqr(p)<25,"Companion arrives safely near its owner");
                check(h,source.getEntity(uuid)==null,"Transfer leaves no duplicate in the source level");
                check(h,waiting.level()==h.getLevel()&&waiting.isAlive(),"A waiting companion stays at departure");
                if(step+1<dimensions.size())travelStep(h,p,uuid,waiting,at,step+1,tickets);
                else {cleanupTravel(p,uuid,waiting,at,tickets);h.succeed();}
            }catch(Throwable failure) {
                cleanupTravel(p,uuid,waiting,at,tickets);throw failure;
            }
        });
    }
    private static void cleanupTravel(ServerPlayer p,java.util.UUID uuid,Shardling waiting,BlockPos at,java.util.Set<ServerLevel> tickets){
        for(var world:p.level().getServer().getAllLevels())if(world.getEntity(uuid) instanceof Shardling pet)pet.discard();
        waiting.discard();GameplayTests.cleanup(p);
        for(var world:tickets)world.setChunkForced(at.getX()>>4,at.getZ()>>4,false);
    }
    public static void coldArrival(GameTestHelper h) {
        var p=GameplayTests.player(h);var source=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));platform(source,at);
        p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);var pet=scout(source,at.east(2));pet.tame(p);var uuid=pet.getUUID();
        var target=source.getServer().getLevel(Level.NETHER);var landing=new BlockPos(10248,128,10248);
        // The Nether roof is naturally solid. Do not preload it by preparing a platform.
        check(h,!target.hasChunk(landing.getX()>>4,landing.getZ()>>4),"The regression fixture starts with an unloaded arrival chunk");
        p.teleport(new TeleportTransition(target,Vec3.atBottomCenterOf(landing),Vec3.ZERO,0,0,TeleportTransition.DO_NOTHING));
        p.connection.markClientLoaded();p.hasChangedDimension();
        // The embedded client never acknowledges destination chunk batches. Keep the
        // destination ticking only AFTER the cold transfer has happened. This cannot
        // rescue a failed transfer: a pet left at the source will still fail below.
        target.setChunkForced(landing.getX()>>4,landing.getZ()>>4,true);
        h.runAfterDelay(20,()->{
            try {
                check(h,target.getEntity(uuid) instanceof Shardling moved&&moved.getOwnerReference().matches(p),"Companion must reach an initially unloaded destination; source alive="+pet.isAlive()+", loaded="+target.hasChunk(landing.getX()>>4,landing.getZ()>>4)+", floor="+target.getBlockState(landing.below())+", landing="+CompanionTravel.findLanding(target,landing,pet));
                check(h,source.getEntity(uuid)==null,"Cold arrival leaves no duplicate behind");
                target.getEntity(uuid).discard();
            }finally{if(source.getEntity(uuid)!=null)source.getEntity(uuid).discard();GameplayTests.cleanup(p);target.setChunkForced(landing.getX()>>4,landing.getZ()>>4,false);}
            h.succeed();
        });
    }
    public static void pulseDodge(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));platform(level,at);
        var mob=sentinel(level,at);p.setPos(at.getX()+3.5,at.getY(),at.getZ()+.5);
        mob.hurtServer(level,p.damageSources().playerAttack(p),1);
        check(h,mob.beginPulse(p)&&mob.windup()==32,"A pulse must announce its impact before doing damage");
        float health=p.getHealth();p.setPos(at.getX()-3.5,at.getY(),at.getZ()+.5);
        h.runAfterDelay(36,()->{
            try{check(h,p.getHealth()==health,"Leaving the marked impact zone avoids damage");check(h,mob.windup()==0,"Windup finishes on the server");}
            finally{mob.discard();GameplayTests.cleanup(p);}h.succeed();
        });
    }
    public static void pulseHit(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));platform(level,at);
        var mob=sentinel(level,at);p.setPos(at.getX()+3.5,at.getY(),at.getZ()+.5);
        mob.hurtServer(level,p.damageSources().playerAttack(p),1);check(h,mob.beginPulse(p),"An in-range target starts the telegraphed pulse");
        float health=p.getHealth();
        h.runAfterDelay(36,()->{
            try{check(h,p.getHealth()<=health-4.9f,"Remaining inside the marked zone must actually inflict the announced five damage");}
            finally{mob.discard();GameplayTests.cleanup(p);}h.succeed();
        });
    }
    public static void pulseInterrupt(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(16,1,16));platform(level,at);
        var mob=sentinel(level,at);p.setPos(at.getX()+3.5,at.getY(),at.getZ()+.5);mob.setNoAi(true);
        try {
            check(h,mob.beginPulse(p),"A sentinel can prepare an in-range pulse");
            check(h,mob.hurtServer(level,p.damageSources().playerAttack(p),1),"A real player attack reaches the sentinel");
            check(h,mob.windup()==0&&!mob.beginPulse(p),"Damage interrupts the charge and starts a real recovery window");
        }finally{mob.discard();GameplayTests.cleanup(p);}
        h.succeed();
    }
    public static void lens(GameTestHelper h) {
        var p=GameplayTests.player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(2,1,2));p.setPos(at.getX(),at.getY(),at.getZ());
        var mob=sentinel(level,at.east(3));mob.setNoAi(true);var lens=new ItemStack(ModContent.ECHO_LENS.get());
        try {
            check(h,EchoLensItem.reveal(p,lens)==0&&!mob.hasEffect(MobEffects.GLOWING),"An empty prism cannot reveal without dust");
            p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),2));
            check(h,EchoLensItem.reveal(p,lens)>=1&&mob.hasEffect(MobEffects.GLOWING),"One dust reveals nearby hostile creatures using the native glowing effect");
            check(h,p.getInventory().countItem(ModContent.ECHO_DUST.get())==1,"One activation consumes one dust");
            check(h,EchoLensItem.reveal(p,lens)==0&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==1,"Cooldown cannot consume or reveal a second time");
            check(h,!VestigeEntities.habitat(level,at),"Natural creatures cannot spawn in the Overworld");
            level.setBlock(at,ModContent.ECHO_BLOOM.get().defaultBlockState().setValue(EchoBloomBlock.CHARGE,3),3);
            check(h,!EchoBloomBlock.harvest(p,at)&&level.getBlockState(at).getValue(EchoBloomBlock.CHARGE)==3,"An Overworld aggregate never grants nocturnal resources");
        }finally{mob.discard();GameplayTests.cleanup(p);}
        h.succeed();
    }
    public static void deposits(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(16,1,16));
        int x0=center.getX()&~15,z0=center.getZ()&~15;
        for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++)level.setBlock(new BlockPos(x,120,z),ModContent.ASHSTONE.get().defaultBlockState(),3);
        var protectedBlock=new BlockPos(x0+8,121,z0+8);level.setBlock(protectedBlock,Blocks.GOLD_BLOCK.defaultBlockState(),3);
        net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(level.getChunk(x0>>4,z0>>4),java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
        var feature=new EchoBloomFeature();var random=net.minecraft.util.RandomSource.create(14237);
        boolean made=false;for(int i=0;i<32;i++)made|=feature.generate(level,new BlockPos(x0,120,z0),random);
        check(h,made,"The real feature creates surface echo aggregates");
        int count=0;
        for(var pos:BlockPos.betweenClosed(new BlockPos(x0,121,z0),new BlockPos(x0+15,122,z0+15)))if(level.getBlockState(pos).is(ModContent.ECHO_BLOOM.get())) {
            count++;check(h,pos.getY()==121&&level.getBlockState(pos.below()).is(ModContent.ASHSTONE.get()),"Deposits remain exposed on natural ashstone");
        }
        check(h,count>0&&level.getBlockState(protectedBlock).is(Blocks.GOLD_BLOCK),"Generating deposits preserves existing construction");
        h.succeed();
    }
    private EncounterTests(){}
}
