package fr.axephir.tnd.city;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.progression.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Personal, persistent discoveries; reading a copied terminal never duplicates rewards. */
public final class Codex {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> NOTES=PlayerProgression.TYPES.register("nacre_archives",()->AttachmentType.builder(()->0)
            .serialize(Codec.intRange(0,511).fieldOf("records")).copyOnDeath().sync((holder,receiver)->holder==receiver,ByteBufCodecs.VAR_INT).build());
    public static void bootstrap(){}
    public static void collect(ServerPlayer p,int entry){
        if(entry<0||entry>7)return;int old=p.getData(NOTES),bit=1<<entry;
        if((old&bit)==0){p.setData(NOTES,old|bit);if(!p.isCreative())ProgressionService.award(p,fr.axephir.tnd.domain.Mastery.ANCIENT_TECH,12);
            p.sendOverlayMessage(Component.translatable("codex.tnd.discovered",Component.translatable("codex.tnd.title."+entry),Integer.bitCount((old|bit)&255)));
            p.level().playSound(null,p.blockPosition(),fr.axephir.tnd.core.ModContent.atmosphereSound("archive_read"),net.minecraft.sounds.SoundSource.PLAYERS,.5f,1);
        }
    }
    public static void open(ServerPlayer p,int entry){p.openMenu(new SimpleMenuProvider((id,inv,player)->new CodexMenu(id,inv,p,entry),Component.translatable("codex.tnd.name")));}
    private Codex(){}
}
