package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.core.TndConfig;
import fr.axephir.tnd.progression.MasteryMenu;
import fr.axephir.tnd.progression.OreSense;
import fr.axephir.tnd.domain.Mastery;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class DevelopmentCommands {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath("tnd", "vestige"));

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tnd")
                .then(senseCommands())
                .then(Commands.literal("masteries").executes(ctx -> {
                    ctx.getSource().getPlayerOrException().openMenu(new SimpleMenuProvider(
                            MasteryMenu::new, Component.translatable("gui.tnd.masteries")));
                    return 1;
                }))
                .then(Commands.literal("enter").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> travel(ctx.getSource().getPlayerOrException(), DIMENSION)))
                .then(Commands.literal("leave").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> travel(ctx.getSource().getPlayerOrException(), Level.OVERWORLD))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> senseCommands() {
        var command=Commands.literal("sense");
        for(Mastery mastery:Mastery.values())command.then(Commands.literal(mastery.id()).executes(ctx->OreSense.scan(ctx.getSource().getPlayerOrException(),mastery)));
        return command;
    }

    private static int travel(ServerPlayer player, ResourceKey<Level> destination) {
        if (!TndConfig.DEVELOPMENT_COMMANDS.getAsBoolean()) return 0;
        ServerLevel level = player.level().getServer().getLevel(destination);
        if (level == null) { player.sendSystemMessage(Component.translatable("message.tnd.dimension_missing")); return 0; }
        // Test landing zones are confined to this mod's dimension; do not overwrite the Overworld.
        BlockPos target;
        if (destination.equals(DIMENSION)) {
            var site = PortalLanding.findBuildSite(level,0,0);
            if (site.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.tnd.portal_no_site"));
                return 0;
            }
            target=site.get();
            for (int x=-2;x<=2;x++) for(int z=-2;z<=2;z++)
                level.setBlock(target.offset(x,-1,z),ModContent.ANCIENT_ALLOY.get().defaultBlockState(),3);
        } else {
            level.getChunk(0,0);
            target=new BlockPos(0,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0),0);
            if (!level.getFluidState(target.below()).isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.tnd.unsafe_return"));
                return 0;
            }
        }
        player.teleport(new TeleportTransition(level, Vec3.atBottomCenterOf(target), Vec3.ZERO,
                player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
        return 1;
    }
    private DevelopmentCommands() {}
}
