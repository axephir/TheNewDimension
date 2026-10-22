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

/** Contextual perceptions; only doors inside newly authored, protected city chunks may open. */
public final class RareAnomalies {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> NEXT=PlayerProgression.TYPES.register("next_anomaly",()->AttachmentType.builder(()->0L).serialize(Codec.LONG.fieldOf("tick")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> SEQUENCE=PlayerProgression.TYPES.register("anomaly_sequence",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,Integer.MAX_VALUE).fieldOf("sequence")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> VARIANT=PlayerProgression.TYPES.register("anomaly_variant",()->AttachmentType.builder(()->0).sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<net.minecraft.core.BlockPos>> ORIGIN=PlayerProgression.TYPES.register("anomaly_origin",()->AttachmentType.builder(()->net.minecraft.core.BlockPos.ZERO).sync((h,p)->h==p,net.minecraft.core.BlockPos.STREAM_CODEC).build());
    public static void bootstrap(){}
    public static int contextualVariant(ServerPlayer p){
        var origin=p.blockPosition();
        // A door or dormant machine must already exist in loaded surroundings. The event never places one.
        for(var at:net.minecraft.core.BlockPos.betweenClosed(origin.offset(-7,-2,-7),origin.offset(7,3,7))){
            if(!p.level().hasChunkAt(at))continue;var state=p.level().getBlockState(at);
            if(state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock){p.setData(ORIGIN,at.immutable());return 2;}
            if(state.getBlock() instanceof fr.axephir.tnd.technology.IndustrialBlock||state.is(fr.axephir.tnd.core.ModContent.ANCIENT_CONSOLE.get())){p.setData(ORIGIN,at.immutable());return 3;}
        }
        p.setData(ORIGIN,net.minecraft.core.BlockPos.containing(p.position().subtract(p.getLookAngle().scale(9))));return p.getRandom().nextInt(2);
    }
    @SubscribeEvent public void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%200!=0||p.isCreative()||p.isSpectator()||!p.level().dimension().equals(DevelopmentCommands.DIMENSION)||!fr.axephir.tnd.core.TndConfig.RARE_ANOMALIES.get()||p.hurtTime>0)return;
        long now=p.level().getServer().overworld().getGameTime(),next=p.getData(NEXT);
        if(next==0){p.setData(NEXT,now+12000+p.getRandom().nextInt(24001));return;}
        if(now<next||p.containerMenu!=p.inventoryMenu)return;
        trigger(p);p.setData(NEXT,now+36000+p.getRandom().nextInt(84001));
    }
    public static boolean stirDoor(ServerPlayer p){
        var at=p.getData(ORIGIN);var w=p.level();
        if(!w.dimension().equals(DevelopmentCommands.DIMENSION)||!w.hasChunkAt(at)||!w.getChunkAt(at).getData(fr.axephir.tnd.city.CityRegions.GENERATED)||!fr.axephir.tnd.city.CityRegions.protectedAt(w,at))return false;
        var state=w.getBlockState(at);
        if(!(state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door)||state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)||state.getValue(net.minecraft.world.level.block.DoorBlock.POWERED))return false;
        if(state.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER){at=at.below();state=w.getBlockState(at);if(!state.is(door))return false;}
        // Open only. Never close a doorway onto a player or interfere with redstone. Players retain ordinary use.
        door.setOpen(null,w,state,at,true);return true;
    }
    public static void trigger(ServerPlayer p){int variant=contextualVariant(p);p.setData(VARIANT,variant);if(variant==2)stirDoor(p);int n=p.getData(SEQUENCE);p.setData(SEQUENCE,n==Integer.MAX_VALUE?1:n+1);}
}
