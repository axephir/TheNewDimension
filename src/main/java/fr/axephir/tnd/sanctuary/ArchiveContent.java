package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.progression.PlayerProgression;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

public final class ArchiveContent {
    public static final DeferredBlock<ArchiveDeviceBlock> MIRROR=ModContent.BLOCKS.registerBlock("archive_mirror",p->new ArchiveDeviceBlock(p,false),p->p.strength(-1,3600000).noLootTable().noOcclusion().lightLevel(s->s.getValue(ArchiveDeviceBlock.LIT)?14:6));
    public static final DeferredBlock<ArchiveDeviceBlock> RELAY=ModContent.BLOCKS.registerBlock("archive_anchor",p->new ArchiveDeviceBlock(p,true),p->p.strength(-1,3600000).noLootTable().noOcclusion().lightLevel(s->s.getValue(ArchiveDeviceBlock.LIT)?14:5));
    public static final DeferredItem<ArchiveLensItem> LENS=ModContent.ITEMS.registerItem("silent_archive_lens",ArchiveLensItem::new,p->p.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    public static final DeferredHolder<MenuType<?>,MenuType<RelicMenu>> MENU=ModContent.MENUS.register("relic_attunement",()->new MenuType<>(RelicMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> UNLOCKED=PlayerProgression.TYPES.register("archive_relic_unlocked",()->AttachmentType.builder(()->false).serialize(Codec.BOOL.fieldOf("unlocked")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> SELECTION=PlayerProgression.TYPES.register("archive_relic_selection",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,2).fieldOf("selection")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> READY=PlayerProgression.TYPES.register("archive_relic_ready",()->AttachmentType.builder(()->0L).serialize(Codec.LONG.fieldOf("ready")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.LONG).build());
    public static void register(IEventBus bus){
        ModContent.ITEMS.registerSimpleBlockItem("archive_mirror",MIRROR);ModContent.ITEMS.registerSimpleBlockItem("archive_anchor",RELAY);
        bus.addListener(RelicPowers::network);
        for(String cue:java.util.List.of("archive_turn","archive_memory","archive_atlas","archive_awakening","archive_relic"))ModContent.ATMOSPHERE_SOUNDS.put(cue,ModContent.SOUNDS.register(cue,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd",cue))));
    }
    private ArchiveContent(){}
}
