package fr.axephir.tnd.narrative;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.progression.PlayerProgression;
import fr.axephir.tnd.world.DevelopmentCommands;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Rare private perceptual events. No doors, player buildings, inventories, enemies or source terrain are modified. */
public final class RareAnomalies {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> NEXT=PlayerProgression.TYPES.register("next_anomaly",()->AttachmentType.builder(()->0L).serialize(Codec.LONG.fieldOf("tick")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> SEQUENCE=PlayerProgression.TYPES.register("anomaly_sequence",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,Integer.MAX_VALUE).fieldOf("sequence")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    public static void bootstrap(){}
    @SubscribeEvent public void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%200!=0||p.isCreative()||p.isSpectator()||!p.level().dimension().equals(DevelopmentCommands.DIMENSION)||!fr.axephir.tnd.core.TndConfig.RARE_ANOMALIES.get()||p.hurtTime>0)return;
        long now=p.level().getServer().overworld().getGameTime(),next=p.getData(NEXT);
        if(next==0){p.setData(NEXT,now+12000+p.getRandom().nextInt(24001));return;}
        if(now<next||p.containerMenu!=p.inventoryMenu)return;
        trigger(p);p.setData(NEXT,now+36000+p.getRandom().nextInt(84001));
    }
    public static void trigger(ServerPlayer p){int n=p.getData(SEQUENCE);p.setData(SEQUENCE,n==Integer.MAX_VALUE?1:n+1);}
}
