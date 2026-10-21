package fr.axephir.tnd.expedition;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.world.DevelopmentCommands;
import java.util.*;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ExpeditionService {
    public static final List<String> BIOMES=List.of("silent_expanse","fractured_fields","hollow_shelves","glass_woodland","ashen_basin","obsidian_dunes");
    private static boolean eligible(ServerPlayer p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
    public static boolean inVestige(ServerPlayer p){return p.level().dimension().equals(DevelopmentCommands.DIMENSION);}
    private static void update(ServerPlayer p,UnaryOperator<ExpeditionProgress> action){
        if(!eligible(p))return;var before=p.getData(PlayerProgression.EXPEDITIONS);var after=action.apply(before);
        if(after.equals(before))return;p.setData(PlayerProgression.EXPEDITIONS,after);
        boolean newlyComplete=Arrays.stream(Expedition.values()).anyMatch(e->!before.complete(e)&&after.complete(e));
        if(newlyComplete){p.sendOverlayMessage(Component.translatable("message.tnd.expedition_ready"));sound(p,ModContent.atmosphereSound("expedition_complete"),SoundSource.PLAYERS,.5f,1.2f);}
    }
    public static void discover(ServerPlayer p,String biome){if(inVestige(p)){int index=BIOMES.indexOf(biome);if(index>=0)update(p,s->s.discover(index));}}
    public static void console(ServerPlayer p,BlockPos pos){if(inVestige(p))update(p,s->s.console(Long.toString(pos.asLong())));}
    public static void harvest(ServerPlayer p,BlockPos pos){if(inVestige(p))update(p,s->s.harvest(Long.toString(pos.asLong())));}
    public static void observe(ServerPlayer p){if(inVestige(p))update(p,ExpeditionProgress::observe);}
    @SubscribeEvent public void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0||!eligible(p))return;
        if(inVestige(p))p.level().getBiome(p.blockPosition()).unwrapKey().ifPresent(key->{if(key.identifier().getNamespace().equals("tnd"))discover(p,key.identifier().getPath());});
        if(!p.getData(PlayerProgression.EXPEDITIONS).gear())for(int i=0;i<p.getInventory().getContainerSize();i++){
            var item=p.getInventory().getItem(i);if(item.is(ModContent.RESONANT_PICKAXE.get())||item.is(ModContent.RESONANT_BLADE.get())||item.is(fr.axephir.tnd.core.AwakeningContent.UMBRA_PICKAXE.get())||item.is(fr.axephir.tnd.core.AwakeningContent.UMBRA_BLADE.get())||item.is(fr.axephir.tnd.core.AwakeningContent.HAMMER.get())){update(p,ExpeditionProgress::equip);break;}
        }
    }
    public static void sentinelKilled(ServerPlayer p){if(inVestige(p))update(p,ExpeditionProgress::sentinel);}
    @SubscribeEvent public void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)RecallPrismItem.cancel(p);}
    @SubscribeEvent public void changed(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)RecallPrismItem.cancel(p);}
    public static void sound(ServerPlayer p,SoundEvent sound,SoundSource category,float volume,float pitch){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(sound),category,p.getX(),p.getY(),p.getZ(),volume,pitch,p.getRandom().nextLong()));}
    public static Mastery mastery(Expedition e){return switch(e){case RESTORATION,GEAR->Mastery.ENGINEERING;case NIGHT_HARVEST,SENTINELS->Mastery.ANCIENT_TECH;default->Mastery.EXPLORATION;};}
    public static int xp(Expedition e){return switch(e){case SURVEY->60;case ATLAS->160;case RESTORATION->120;case NIGHT_HARVEST,SENTINELS,GEAR->80;case OBSERVATORY->100;case CARTOGRAPHER->240;};}
    public static List<ItemStack> rewards(Expedition e){return switch(e){
        case SURVEY->List.of(new ItemStack(ModContent.RELIC_FRAGMENT.get(),2),new ItemStack(ModContent.ECHO_DUST.get(),4));
        case ATLAS->List.of(new ItemStack(ModContent.TEMPERED_ALLOY.get(),4),new ItemStack(ModContent.ECHO_DUST.get(),16));
        case RESTORATION->List.of(new ItemStack(ModContent.STABILIZER_MODULE.get()));
        case NIGHT_HARVEST,SENTINELS->List.of(new ItemStack(ModContent.ECHO_DUST.get(),8));
        case OBSERVATORY->List.of(new ItemStack(ModContent.RELIC_FRAGMENT.get(),4),new ItemStack(ModContent.COMPOSITE_CRYSTAL.get()));
        case GEAR->List.of(new ItemStack(ModContent.ECHO_DUST.get(),6),new ItemStack(ModContent.RESONANT_SHARD.get(),4));
        case CARTOGRAPHER->List.of(new ItemStack(ModContent.CARTOGRAPHER_TROPHY.get()),new ItemStack(ModContent.TEMPERED_ALLOY.get(),8));
    };}
    public static boolean claim(ServerPlayer p,Expedition mission){
        if(!eligible(p))return false;var state=p.getData(PlayerProgression.EXPEDITIONS);
        if(!state.complete(mission)||state.claimed(mission))return false;
        p.setData(PlayerProgression.EXPEDITIONS,state.claim(mission));
        for(var reward:rewards(mission))if(!p.getInventory().add(reward))p.drop(reward,false);
        ProgressionService.award(p,mastery(mission),xp(mission));
        sound(p,SoundEvents.PLAYER_LEVELUP,SoundSource.PLAYERS,.35f,1.4f);
        p.sendOverlayMessage(Component.translatable("message.tnd.expedition_claimed",Component.translatable("expedition.tnd."+mission.id())));return true;
    }
}
