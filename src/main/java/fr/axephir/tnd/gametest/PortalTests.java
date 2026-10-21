package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.PortalLanding;
import fr.axephir.tnd.world.PortalControllerBlock;
import fr.axephir.tnd.world.PortalControllerEntity;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PortalTests {
    public static void register(DeferredRegister<Consumer<GameTestHelper>> functions) {
        functions.register("portal_terrain", () -> PortalTests::terrain);
        functions.register("portal_arrival", () -> PortalTests::arrival);
        functions.register("portal_no_space", () -> PortalTests::noSpace);
        functions.register("portal_round_trip", () -> PortalTests::roundTrip);
    }

    private static void terrain(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(2, 0, 2)).offset(1024, 0, 1024);
        // Empty center, elevated terrain at a footprint edge: the old center-only
        // height calculation proposes Y=96 and rejects this legitimate location.
        for (int y = 90; y <= 120; y++)
            level.setBlock(new BlockPos(center.getX() + 3, y, center.getZ()), Blocks.STONE.defaultBlockState(), 3);
        var result = PortalLanding.findBuildSite(level, center.getX(), center.getZ());
        helper.assertTrue(result.isPresent(), Component.literal("Uneven terrain must not prevent portal activation"));
        helper.assertTrue(result.get().getY() > 121, Component.literal("The whole platform must sit above surrounding terrain"));
        helper.assertTrue(level.getBlockState(new BlockPos(center.getX() + 3, 120, center.getZ())).is(Blocks.STONE),
                Component.literal("Existing terrain must remain intact"));
        helper.succeed();
    }

    private static void arrival(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos controller = helper.absolutePos(new BlockPos(2, 1, 2));
        for (BlockPos pos : BlockPos.betweenClosed(controller.offset(-3, -1, -3), controller.offset(3, -1, 3)))
            level.setBlock(pos, ModContent.PORTAL_FRAME.get().defaultBlockState(), 3);
        level.setBlock(controller, ModContent.PORTAL_CONTROLLER.get().defaultBlockState(), 3);
        level.setBlock(controller.above(2), Blocks.STONE.defaultBlockState(), 3);
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        var result = PortalLanding.findArrival(level, controller, player);
        helper.assertTrue(result.isPresent(), Component.literal("Use a clear adjacent landing cell when the controller headroom is obstructed"));
        helper.assertTrue(!result.get().equals(controller.above()), Component.literal("Never place the player inside the obstruction"));
        helper.assertTrue(level.getBlockState(controller.above(2)).is(Blocks.STONE), Component.literal("Do not delete the player's obstruction"));
        for (BlockPos pos : BlockPos.betweenClosed(controller.offset(-3, 0, -3), controller.offset(3, 3, 3)))
            if (!pos.equals(controller)) level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(PortalLanding.findArrival(level, controller, player).isEmpty(),
                Component.literal("A fully obstructed platform must refuse unsafe travel"));
        for (BlockPos pos : BlockPos.betweenClosed(controller.offset(-3, 0, -3), controller.offset(3, 3, 3)))
            if (!pos.equals(controller)) level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        helper.assertTrue(PortalLanding.findArrival(level, controller, player).isEmpty(),
                Component.literal("A flooded platform must refuse unsafe travel"));
        helper.succeed();
    }

    private static void noSpace(GameTestHelper helper) {
        var source = helper.getLevel();
        var target = source.getServer().getLevel(Level.END);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.setBlock(new BlockPos(2, 1, 2), ModContent.PORTAL_CONTROLLER.get());
        var portal = (PortalControllerEntity) source.getBlockEntity(pos);
        BlockPos obstruction = new BlockPos(pos.getX() + 3, target.getMaxY(), pos.getZ());
        target.setBlock(obstruction, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        var crystals = new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(), 2);
        helper.assertTrue(!portal.activateTo(target, crystals), Component.literal("Activation must fail when no platform fits inside build height"));
        helper.assertTrue(crystals.getCount() == 2, Component.literal("Failed activation must not consume crystals"));
        helper.assertTrue(portal.destination() == null && !source.getBlockState(pos).getValue(PortalControllerBlock.ACTIVE),
                Component.literal("Failed activation must leave the source portal inactive"));
        helper.assertTrue(target.getBlockState(obstruction).is(Blocks.GOLD_BLOCK), Component.literal("Never overwrite existing constructions"));
        helper.succeed();
    }

    @SuppressWarnings("removal") // Native helper supplies the connected server player needed for real dimension transport.
    private static void roundTrip(GameTestHelper helper) {
        var source = helper.getLevel();
        var target = source.getServer().getLevel(Level.END);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        for (BlockPos floor : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, -1, 2)))
            source.setBlock(floor, ModContent.ANCIENT_ALLOY.get().defaultBlockState(), 3);
        helper.setBlock(new BlockPos(2, 1, 2), ModContent.PORTAL_CONTROLLER.get());
        var portal = (PortalControllerEntity) source.getBlockEntity(pos);
        var crystals = new ItemStack(ModContent.COMPOSITE_CRYSTAL.get(), 2);
        helper.assertTrue(portal.activateTo(target, crystals), Component.literal("An unvisited destination must activate on the first attempt"));
        helper.assertTrue(crystals.getCount() == 1, Component.literal("Successful activation consumes exactly one crystal"));
        var destination = portal.destination();
        helper.assertTrue(destination != null && destination.dimension().equals(Level.END), Component.literal("Save the real destination dimension"));
        helper.assertTrue(!portal.activateTo(target, crystals) && crystals.getCount() == 1,
                Component.literal("Repeated activation must not charge another crystal"));
        var partner = (PortalControllerEntity) target.getBlockEntity(destination.pos());
        helper.assertTrue(partner != null && GlobalPos.of(source.dimension(), pos).equals(partner.destination()),
                Component.literal("The return portal must point back to the original source"));
        // Read both block entities through Minecraft's actual save/load codecs.
        var restoredSource = BlockEntity.loadStatic(pos, source.getBlockState(pos), portal.saveWithFullMetadata(source.registryAccess()), source.registryAccess());
        var restoredTarget = BlockEntity.loadStatic(destination.pos(), target.getBlockState(destination.pos()),
                partner.saveWithFullMetadata(target.registryAccess()), target.registryAccess());
        helper.assertTrue(restoredSource instanceof PortalControllerEntity && restoredTarget instanceof PortalControllerEntity,
                Component.literal("Both portal entities must reload from their saved data"));
        source.setBlockEntity(restoredSource);
        target.setBlockEntity(restoredTarget);
        portal = (PortalControllerEntity) restoredSource;
        partner = (PortalControllerEntity) restoredTarget;
        helper.assertTrue(destination.equals(portal.destination()), Component.literal("Reloading must preserve the destination"));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            helper.assertTrue(portal.travel(player) && player.level() == target,
                    Component.literal("A player must traverse the activated portal into the destination level"));
            helper.assertTrue(partner.travel(player) && player.level() == source,
                    Component.literal("The same player must return through the persisted paired portal"));
            helper.assertTrue(player.blockPosition().distSqr(pos) <= 16,
                    Component.literal("Return to a safe cell near the original controller"));
            helper.assertTrue(crystals.getCount() == 1, Component.literal("Travelling in either direction must remain free"));
        } finally { source.getServer().getPlayerList().remove(player); }
        helper.succeed();
    }
    private PortalTests() {}
}
