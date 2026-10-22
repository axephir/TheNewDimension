package fr.axephir.tnd.narrative;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Spectator inventory clicks stay forbidden by vanilla; only these bounded narrative choices can bypass that restriction. */
public final class ConvergenceNetworking {
    public record Choice(int menu,int action) implements CustomPacketPayload {
        public static final Type<Choice> TYPE=new Type<>(Identifier.fromNamespaceAndPath("tnd","convergence_choice"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Choice> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Choice::menu,ByteBufCodecs.VAR_INT,Choice::action,Choice::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(Choice.TYPE,Choice.CODEC,(packet,ctx)->{
        if(!(ctx.player() instanceof ServerPlayer p))return;
        if(packet.menu()==-1&&packet.action()==3&&Convergence.inEcho(p)&&p.getData(Convergence.PHASE)==2){Convergence.finishCinema(p);return;}
        if(p.containerMenu instanceof ConvergenceMenu menu&&menu.containerId==packet.menu())menu.select(p,packet.action());
    });}
    private ConvergenceNetworking(){}
}
