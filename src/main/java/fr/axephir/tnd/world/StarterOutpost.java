package fr.axephir.tnd.world;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.progression.PlayerProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Only worlds that emitted CreateSpawnPosition are eligible: existing saves are untouched. */
public final class StarterOutpost {
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> REQUESTED = PlayerProgression.TYPES.register("outpost_requested",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("requested")).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> COMPLETE = PlayerProgression.TYPES.register("outpost_complete",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("complete")).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LOCATION = PlayerProgression.TYPES.register("outpost_location",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf("position")).build());

    @SubscribeEvent public void newWorld(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD)) level.setData(REQUESTED, true);
    }
    @SubscribeEvent public void firstPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.level();
        if (!level.dimension().equals(Level.OVERWORLD) || !level.getData(REQUESTED) || level.getData(COMPLETE)) return;
        BlockPos pos;
        long saved = level.getData(LOCATION);
        if (saved == 0) {
            double angle = Math.floorMod(level.getSeed(), 360) * Math.PI / 180;
            int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * 1000);
            int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * 1000);
            pos = new BlockPos(x, 0, z);
            level.setData(LOCATION, pos.asLong());
        } else pos = BlockPos.of(saved);
        var template = level.getServer().getStructureManager().get(Identifier.fromNamespaceAndPath("tnd", "outpost"));
        if (template.isPresent()) {
            var size = template.get().getSize();
            pos = OutpostTerrain.prepare(level,pos.getX(),pos.getZ(),size.getX(),size.getZ(),size.getY());
            level.setData(LOCATION,pos.asLong());
        }
        if (template.isPresent() && template.get().placeInWorld(level, pos, pos,
                new StructurePlaceSettings().setIgnoreEntities(true), level.getRandom(), 2)) {
            level.setData(COMPLETE, true);
        }
    }
}
