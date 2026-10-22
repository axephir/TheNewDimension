package fr.axephir.tnd.familiar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class FamiliarNetworking {
    public record Rename(int menu,String name) implements CustomPacketPayload {
        public static final Type<Rename> TYPE=new Type<>(Identifier.fromNamespaceAndPath("tnd","familiar_rename"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Rename> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Rename::menu,ByteBufCodecs.stringUtf8(96),Rename::name,Rename::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(Rename.TYPE,Rename.CODEC,(packet,ctx)->{
        if(ctx.player() instanceof ServerPlayer p&&p.containerMenu instanceof FamiliarMenu menu&&menu.containerId==packet.menu())menu.rename(p,packet.name());
    });}
    private FamiliarNetworking(){}
}
