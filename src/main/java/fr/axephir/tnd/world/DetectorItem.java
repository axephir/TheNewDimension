package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class DetectorItem extends Item {
    public DetectorItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            // Both products come from one consumed detector, including in creative mode.
            stack.shrink(1);
            give(player, new ItemStack(ModContent.LOCATOR_CORE.get()));
            give(player, new ItemStack(ModContent.COMPOSITE_CRYSTAL.get()));
            return InteractionResult.SUCCESS;
        }
        if (!level.dimension().equals(Level.OVERWORLD)) {
            player.sendOverlayMessage(Component.translatable("message.tnd.detector_dimension"));
            return InteractionResult.SUCCESS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.SUCCESS;
        player.getCooldowns().addCooldown(stack, 200);
        BlockPos found = serverPlayer.level().findNearestMapStructure(
                TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("tnd", "portals")),
                player.blockPosition(), 1024, false);
        if (found == null) {
            player.sendOverlayMessage(Component.translatable("message.tnd.detector_silent"));
            return InteractionResult.SUCCESS;
        }
        double distance = Math.hypot((double) found.getX() - player.getX(), (double) found.getZ() - player.getZ());
        if (distance <= 128) player.sendOverlayMessage(Component.translatable("message.tnd.detector_exact", found.getX(), found.getZ()));
        else {
            double angle = Math.atan2(found.getZ() - player.getZ(), found.getX() - player.getX());
            int direction = Math.floorMod((int) Math.round(angle / (Math.PI / 4)), 8);
            String[] directions = {"east", "southeast", "south", "southwest", "west", "northwest", "north", "northeast"};
            player.sendOverlayMessage(Component.translatable("message.tnd.detector_direction",
                    Component.translatable("direction.tnd." + directions[direction]),
                    distance < 1024 ? Component.translatable("message.tnd.signal_strong") : Component.translatable("message.tnd.signal_faint")));
        }
        return InteractionResult.SUCCESS;
    }
    private static void give(Player player, ItemStack result) { if (!player.addItem(result)) player.drop(result, false); }
}
