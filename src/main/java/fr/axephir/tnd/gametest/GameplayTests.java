package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.*;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.technology.*;
import fr.axephir.tnd.world.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Real registries, attachments, inventories, menu validation and NeoForge event bus. */
@SuppressWarnings("removal")
public final class GameplayTests {
    private static void check(GameTestHelper h,boolean ok,String reason){h.assertTrue(ok,Component.literal(reason));}
    private static ServerPlayer player(GameTestHelper h) {
        // Vanilla's helper hardcodes gameMode() to CREATIVE even after setGameMode.
        // Use the same embedded connection with an ordinary survival ServerPlayer.
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(UUID.randomUUID(),"tnd-test"),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,cookie);
        p.setGameMode(GameType.SURVIVAL);return p;
    }
    private static void cleanup(ServerPlayer p){p.level().getServer().getPlayerList().remove(p);}
    public static void masteries(GameTestHelper h) {
        var p=player(h);var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,1,2));
        try {
            for(var state:List.of(Blocks.IRON_ORE.defaultBlockState(),Blocks.OAK_LOG.defaultBlockState(),
                    Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),ModContent.RESONANT_ORE.get().defaultBlockState())) {
                var drop=new ItemEntity(level,pos.getX(),pos.getY(),pos.getZ(),new ItemStack(Items.WHEAT));
                NeoForge.EVENT_BUS.post(new BlockDropsEvent(level,pos,state,null,new ArrayList<>(List.of(drop)),p,ItemStack.EMPTY));
            }
            NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(p,new ItemStack(ModContent.EFFICIENCY_MODULE.get()),new SimpleContainer(9)));
            var snapshot=BlockSnapshot.create(level.dimension(),level,pos);
            level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
            NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(snapshot,Blocks.STONE.defaultBlockState(),p));
            p.tickCount=20;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            NeoForge.EVENT_BUS.post(new PlayerBrewedPotionEvent(p,new ItemStack(Items.POTION)));
            NeoForge.EVENT_BUS.post(new ItemFishedEvent(List.of(new ItemStack(Items.COD)),1,new FishingHook(p,level,0,0)));
            var cow=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);
            var other=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);
            var child=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);
            cow.setInLove(p);NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(cow,other,child));
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(cow,p.damageSources().playerAttack(p)));
            var zombie=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(zombie,p.damageSources().playerAttack(p)));
            var arrow=EntityTypes.ARROW.create(level,EntitySpawnReason.COMMAND);arrow.setOwner(p);
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(zombie,p.damageSources().arrow(arrow,p)));
            NeoForge.EVENT_BUS.post(new LivingShieldBlockEvent(p,new DamageContainer(p.damageSources().mobAttack(zombie),5),5,true));
            for(var m:Mastery.values())check(h,p.getData(PlayerProgression.PROFILE).get(m).experience()>0,"Successful action must award "+m);
            long before=p.getData(PlayerProgression.PROFILE).get(Mastery.BUILDING).experience();
            NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(snapshot,Blocks.STONE.defaultBlockState(),p));
            check(h,p.getData(PlayerProgression.PROFILE).get(Mastery.BUILDING).experience()==before,"Replacing the same building cell grants no XP");
            var saved=p.getData(PlayerProgression.PROFILE);
            p.setGameMode(GameType.CREATIVE);
            NeoForge.EVENT_BUS.post(new PlayerBrewedPotionEvent(p,new ItemStack(Items.POTION)));
            check(h,p.getData(PlayerProgression.PROFILE).equals(saved),"Creative actions do not farm progression");
        }finally{cleanup(p);}
        h.succeed();
    }
    public static void console(GameTestHelper h) {
        var p=player(h);var level=h.getLevel();var at=h.absolutePos(new BlockPos(2,1,2));
        try {
            level.setBlock(at,ModContent.ANCIENT_CONSOLE.get().defaultBlockState(),3);
            level.setBlock(at.east(2),MachineRegistry.EXTRACTOR.get().defaultBlockState(),3);
            var machine=(ExtractorBlockEntity)level.getBlockEntity(at.east(2));
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.RESONANT_SHARD.get(),2));
            check(h,AncientConsoleBlock.activate(p,at),"Repair an ancient console using a resonant shard");
            check(h,p.getMainHandItem().getCount()==1,"Only one shard is consumed");
            check(h,level.getBlockState(at).getValue(AncientConsoleBlock.LIT),"Repaired state persists as a real block property");
            int fragments=p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get());
            check(h,fragments>=1&&fragments<=2,"Natural consoles grant one bounded relic reward");
            check(h,machine.relicCalibration()==15,"Console improves a nearby machine without consuming it");
            long xp=p.getData(PlayerProgression.PROFILE).get(Mastery.ANCIENT_TECH).experience();
            p.setData(PlayerProgression.ACTIVITIES,ActivityLedger.empty());
            AncientConsoleBlock.activate(p,at);
            check(h,p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get())==fragments,"An active console cannot mint more fragments");
            check(h,p.getMainHandItem().getCount()==1,"Restudying an active console is free");
            check(h,p.getData(PlayerProgression.PROFILE).get(Mastery.ANCIENT_TECH).experience()-xp<=25,"Even a missing old reward ledger cannot repeat the repair XP");
            var restored=BlockEntity.loadStatic(at.east(2),machine.getBlockState(),machine.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
            check(h,restored instanceof ExtractorBlockEntity m&&m.relicCalibration()==15,"Relic calibration survives native save codecs");
            p.setData(PlayerProgression.ACTIVITIES,ActivityLedger.empty());
            var snapshot=BlockSnapshot.create(level.dimension(),level,at);
            level.setBlock(at,ModContent.ANCIENT_CONSOLE.get().defaultBlockState(),3);
            NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(snapshot,Blocks.STONE.defaultBlockState(),p));
            AncientConsoleBlock.activate(p,at);
            check(h,p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get())==fragments,"Player-crafted consoles cannot farm relic fragments");
        }finally{cleanup(p);}
        h.succeed();
    }
    public static void extractionModes(GameTestHelper h) {
        var level=h.getLevel();var at=h.absolutePos(new BlockPos(2,1,2));
        level.setBlock(at,MachineRegistry.EXTRACTOR.get().defaultBlockState(),3);
        var machine=(ExtractorBlockEntity)level.getBlockEntity(at);
        check(h,!machine.selectMode(ExtractionMode.DIAMOND),"Diamond extraction requires a stabilizer");
        for(var mode:ExtractionMode.values()) {
            machine.clearContent();machine.setItem(1,new ItemStack(ModContent.STABILIZER_MODULE.get()));
            check(h,machine.selectMode(mode),"Switch to "+mode);
            machine.setItem(0,new ItemStack(Items.COAL,4));
            var saved=machine.saveWithFullMetadata(level.registryAccess());
            saved.putLong("lastTick",Math.max(0,level.getGameTime()-1));saved.putLong("progress",machine.periodTicks());
            var restored=(ExtractorBlockEntity)BlockEntity.loadStatic(at,machine.getBlockState(),saved,level.registryAccess());
            level.setBlockEntity(restored);machine=restored;machine.settle();
            check(h,machine.mode()==mode&&machine.getItem(2).is(mode.output)&&machine.getItem(2).getCount()==1,"Saved mode produces only its selected resource: "+mode);
            check(h,machine.getItem(0).getCount()==3,"Each resource consumes exactly one coal");
            check(h,!machine.selectMode(mode==ExtractionMode.IRON?ExtractionMode.COPPER:ExtractionMode.IRON),"Never change or convert an occupied output slot");
        }
        machine.removeItem(2,64);machine.removeItem(1,1);
        var saved=machine.saveWithFullMetadata(level.registryAccess());saved.putLong("progress",machine.periodTicks()*2L);
        var restored=(ExtractorBlockEntity)BlockEntity.loadStatic(at,machine.getBlockState(),saved,level.registryAccess());
        level.setBlockEntity(restored);restored.settle();
        check(h,restored.getItem(2).isEmpty(),"Removing the stabilizer suspends diamond production including saved catch-up");
        var p=player(h);
        try {
            var menu=new ExtractorMenu(1,p.getInventory(),restored,restored.data);
            check(h,!menu.clickMenuButton(p,-1)&&!menu.clickMenuButton(p,500),"Server rejects invalid machine button IDs");
        }finally{cleanup(p);}
        h.succeed();
    }
    public static void talentMenus(GameTestHelper h) {
        var p=player(h);var at=h.absolutePos(new BlockPos(2,1,2));
        try {
            var profile=Profile.empty();for(var m:Mastery.values())profile=profile.award(m,Experience.threshold(20));
            p.setData(PlayerProgression.PROFILE,profile);
            var menu=new MasteryMenu(1,p.getInventory());
            for(var m:Mastery.values()) {
                check(h,!menu.clickMenuButton(p,m.ordinal()*5+Talent.INSIGHT.ordinal()),"Insight requires its prerequisite for "+m);
                check(h,menu.clickMenuButton(p,m.ordinal()*5),"Every mastery accepts its first useful talent: "+m);
                check(h,menu.clickMenuButton(p,m.ordinal()*5+Talent.INSIGHT.ordinal()),"Every mastery unlocks Insight: "+m);
            }
            check(h,!menu.clickMenuButton(p,-1)&&!menu.clickMenuButton(p,99)&&!menu.clickMenuButton(p,1015),"Unknown client buttons cannot mutate a profile");
            p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);
            h.getLevel().setBlock(at.east(2),Blocks.DIAMOND_ORE.defaultBlockState(),3);
            check(h,OreSense.scan(p,Mastery.MINING)==1,"Insight finds a nearby loaded target");
            check(h,OreSense.scan(p,Mastery.LOGGING)==0,"Changing category cannot bypass the shared cooldown");
        }finally{cleanup(p);}
        h.succeed();
    }
    public static void talentEffects(GameTestHelper h) {
        var p=player(h);var level=h.getLevel();
        try {
            var progress=MasteryProgress.empty().award(Experience.threshold(100));
            for(var t:Talent.values())for(int i=0;i<t.maxRank();i++)progress=progress.learn(t);
            var profile=Profile.empty();for(var m:Mastery.values())profile=profile.with(m,progress);
            p.setData(PlayerProgression.PROFILE,profile);
            var zombie=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);
            var attack=new LivingIncomingDamageEvent(zombie,new DamageContainer(p.damageSources().playerAttack(p),10));
            NeoForge.EVENT_BUS.post(attack);check(h,Math.abs(attack.getAmount()-11.5)<.01,"Melee talents increase real mob damage by 15 percent");
            var defense=new LivingIncomingDamageEvent(p,new DamageContainer(p.damageSources().mobAttack(zombie),10));
            NeoForge.EVENT_BUS.post(defense);check(h,Math.abs(defense.getAmount()-9)<.01,"Defense talents reduce real mob damage by 10 percent");
            var cow=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);var other=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);
            var child=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);float health=child.getMaxHealth();cow.setInLove(p);
            NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(cow,other,child));
            check(h,Math.abs(child.getMaxHealth()-health*1.2)<.01,"Bred offspring gain persistent bonus health");
            p.tickCount=20;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STONE));
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            check(h,p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE)>5,"Builder gains reach while holding a block");
            check(h,p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)>.107,"Explorer gains movement speed");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FISHING_ROD));NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            check(h,Math.abs(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.LUCK)-1)<.01,"Angler gains luck while holding a rod");
            check(h,Math.abs(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE)-4.5)<.01,"Putting away blocks removes the reach modifier");
            var contents=new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.SWIFTNESS);
            ItemStack potion=new ItemStack(Items.POTION);potion.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,contents);
            for(var effect:contents.getAllEffects())p.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
            int duration=p.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getDuration();
            NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(p,potion,0,new ItemStack(Items.GLASS_BOTTLE)));
            check(h,p.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getDuration()==duration*5/4,"Alchemy extends consumed beneficial potions by 25 percent");
            p.setGameMode(GameType.CREATIVE);NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
            check(h,p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.LUCK)==0,"Creative mode removes survival attribute modifiers");
        }finally{cleanup(p);}
        h.succeed();
    }
    private GameplayTests(){}
}
