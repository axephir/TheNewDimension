package fr.axephir.tnd.progression;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.Mastery;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

/** Cosmetic distinctions never reset levels. Each physical milestone reward is issued once per player. */
public final class Prestige {
    public static final int[] LEVELS={100,250,500,1000};
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> SELECTION=PlayerProgression.TYPES.register("prestige_title",()->AttachmentType.builder(()->-1).serialize(Codec.intRange(-1,59).fieldOf("title")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> REWARDS=PlayerProgression.TYPES.register("prestige_rewards",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,15).fieldOf("claimed")).copyOnDeath().build());
    public static final java.util.List<DeferredBlock<net.minecraft.world.level.block.Block>> SEALS=new java.util.ArrayList<>();
    public static void bootstrap(){for(int i=0;i<4;i++){String id="prestige_seal_"+i;var b=ModContent.BLOCKS.registerSimpleBlock(id,p->p.strength(4,12).requiresCorrectToolForDrops().lightLevel(s->5).sound(net.minecraft.world.level.block.SoundType.AMETHYST));SEALS.add(b);ModContent.ITEMS.registerSimpleBlockItem(id,b);}}
    public static boolean select(ServerPlayer p,int mastery,int tier){if(mastery<0||mastery>=Mastery.values().length||tier<0||tier>=LEVELS.length||p.isSpectator()||p.getData(PlayerProgression.PROFILE).get(Mastery.values()[mastery]).level()<LEVELS[tier])return false;
        p.setData(SELECTION,mastery*4+tier);int claimed=p.getData(REWARDS);if((claimed&(1<<tier))==0){p.setData(REWARDS,claimed|(1<<tier));var reward=new ItemStack(SEALS.get(tier).get());if(!p.getInventory().add(reward))p.drop(reward,false);}return true;}
    private Prestige(){}
}
