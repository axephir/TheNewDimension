package fr.axephir.tnd.progression;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.core.*;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.*;

/** Awards are server-side and only follow successful manual harvests. */
public final class ProgressionEvents {
    @SubscribeEvent
    public void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative()) return;
        String item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getCrafting().getItem()).toString();
        if(!ProgressionService.claim(player,"craft/"+item,24000,8))return;
        ProgressionService.award(player,Mastery.CRAFTING,8+Math.min(16,event.getCrafting().getCount()));
        boolean machine=event.getCrafting().is(MachineRegistry.EXTRACTOR.get().asItem())
                || event.getCrafting().is(ModContent.EFFICIENCY_MODULE.get()) || event.getCrafting().is(ModContent.STABILIZER_MODULE.get());
        if(machine)ProgressionService.award(player,Mastery.ENGINEERING,40);
    }
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<Long>>> PLACED =
            PlayerProgression.TYPES.register("placed_resources", () -> AttachmentType.<List<Long>>builder(() -> List.of())
                    .serialize(Codec.LONG.listOf().fieldOf("positions")).build());

    public static boolean isOre(BlockState state) {
        return state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)
                || state.is(ModContent.BLUE_ORE.get()) || state.is(ModContent.RED_ORE.get())
                || state.is(ModContent.GREEN_ORE.get()) || state.is(ModContent.RESONANT_ORE.get());
    }

    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public void placed(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockState state = event.getPlacedBlock();
        if(!player.isCreative()&&!state.getCollisionShape(player.level(),event.getPos()).isEmpty()) {
            BlockPos p=event.getPos();
            String cell=player.level().dimension().identifier()+"/"+Math.floorDiv(p.getX(),4)+"/"+Math.floorDiv(p.getY(),4)+"/"+Math.floorDiv(p.getZ(),4);
            if(ProgressionService.claim(player,"build/"+cell,Long.MAX_VALUE,1))ProgressionService.award(player,Mastery.BUILDING,8);
        }
        if (!isOre(state) && !state.is(BlockTags.LOGS) && !state.is(ModContent.ANCIENT_CONSOLE.get())
                && !(state.getBlock() instanceof fr.axephir.tnd.world.InscriptionBlock)) return;
        var chunk = player.level().getChunkAt(event.getPos());
        var positions = new HashSet<>(chunk.getData(PLACED));
        positions.add(event.getPos().asLong());
        chunk.setData(PLACED, List.copyOf(positions));
    }

    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public void harvested(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || player.isCreative()
                || event.getDrops().isEmpty() || event.isCanceled()) return;
        var chunk = event.getLevel().getChunkAt(event.getPos());
        var positions = new HashSet<>(chunk.getData(PLACED));
        if (positions.remove(event.getPos().asLong())) {
            chunk.setData(PLACED, List.copyOf(positions));
            return;
        }
        Mastery mastery = event.getState().is(BlockTags.LOGS) ? Mastery.LOGGING
                : isOre(event.getState()) ? Mastery.MINING : matureCrop(event.getState()) ? Mastery.FARMING : null;
        if (mastery == null) return;
        Profile profile = player.getData(PlayerProgression.PROFILE);
        ProgressionService.award(player,mastery,mastery==Mastery.MINING?20:8);
        if(event.getState().is(ModContent.RESONANT_ORE.get()))ProgressionService.award(player,Mastery.ANCIENT_TECH,6);
        // Bonus respects vanilla drops (including enchantments), but is capped at one item per harvest.
        double chance = profile.get(mastery).rank(Talent.YIELD) * 0.02;
        if (player.getRandom().nextDouble() < chance) {
            var first = event.getDrops().getFirst();
            var extra = first.getItem().copyWithCount(1);
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(event.getLevel(),
                    first.getX(), first.getY(), first.getZ(), extra));
        }
    }

    public static boolean playerPlaced(net.minecraft.world.level.Level level,BlockPos pos) {
        return level.getChunkAt(pos).getData(PLACED).contains(pos.asLong());
    }
    public static boolean matureCrop(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && crop.isMaxAge(state)
                || state.is(net.minecraft.world.level.block.Blocks.NETHER_WART)&&state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE)==3
                || state.is(net.minecraft.world.level.block.Blocks.COCOA)&&state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE)==2;
    }
    @SubscribeEvent
    public void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        var item=event.getItemStack();
        String key=item.is(ModContent.RELIC_FRAGMENT.get())?"relic":item.is(ModContent.STABILIZER_MODULE.get())?"stabilizer":item.is(ModContent.EFFICIENCY_MODULE.get())?"efficiency":null;
        if(key!=null)event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.tnd."+key).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @SubscribeEvent
    public void speed(PlayerEvent.BreakSpeed event) {
        Mastery mastery = event.getState().is(BlockTags.LOGS) ? Mastery.LOGGING
                : isOre(event.getState()) ? Mastery.MINING : matureCrop(event.getState()) ? Mastery.FARMING : null;
        if (mastery == null) return;
        var progress = event.getEntity().getData(PlayerProgression.PROFILE).get(mastery);
        var engineering = event.getEntity().getData(PlayerProgression.PROFILE).get(MasteryRules.partner(mastery));
        double multiplier = 1 + progress.rank(Talent.EFFICIENCY) * 0.05 + Experience.prestigeBonus(progress.level())
                + Math.min(progress.rank(Talent.SYNERGY), engineering.rank(Talent.SYNERGY)) * 0.02;
        event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
    }
}
