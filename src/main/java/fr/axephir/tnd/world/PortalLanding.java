package fr.axephir.tnd.world;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/** Placement and arrival checks shared by activation and transport. */
public final class PortalLanding {
    public static final int RADIUS = 3;
    private PortalLanding() {}

    public static Optional<BlockPos> findBuildSite(ServerLevel level, int x, int z) {
        if (!withinBorder(level, x, z)) return Optional.empty();
        loadFootprint(level, x, z);
        int highest = level.getMinY();
        for (int dx = -RADIUS; dx <= RADIUS; dx++)
            for (int dz = -RADIUS; dz <= RADIUS; dz++)
                highest = Math.max(highest, level.getHeight(Heightmap.Types.WORLD_SURFACE, x + dx, z + dz));
        int y = Math.max(96, highest + 4);
        if (y - 1 < level.getMinY() || y + 7 > level.getMaxY()) return Optional.empty();
        BlockPos center = new BlockPos(x, y, z);
        // A final check protects structures and blocks placed above the terrain too.
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -1, -RADIUS), center.offset(RADIUS, 7, RADIUS)))
            if (!level.getBlockState(pos).isAir()) return Optional.empty();
        return Optional.of(center);
    }

    public static Optional<BlockPos> findArrival(ServerLevel level, BlockPos controller, Entity entity) {
        if (!withinBorder(level, controller.getX(), controller.getZ())) return Optional.empty();
        loadFootprint(level, controller.getX(), controller.getZ());
        var state=level.getBlockState(controller);
        if(state.hasProperty(PortalControllerBlock.FACING)){
            var approach=controller.relative(state.getValue(PortalControllerBlock.FACING),2);
            if(isSafe(level,approach,entity))return Optional.of(approach);
        }
        if (isSafe(level, controller.above(), entity)) return Optional.of(controller.above());
        // The generated platform is one block below the controller. Search it
        // before refusing transport; never clear a player's blocks on arrival.
        for (int radius = 1; radius <= RADIUS; radius++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                        BlockPos candidate = controller.offset(dx, dy, dz);
                        if (isSafe(level, candidate, entity)) return Optional.of(candidate);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet, Entity entity) {
        var dimensions = entity.getDimensions(Pose.STANDING);
        double halfWidth = dimensions.width() / 2.0;
        AABB box = new AABB(feet.getX() + 0.5 - halfWidth, feet.getY(), feet.getZ() + 0.5 - halfWidth,
                feet.getX() + 0.5 + halfWidth, feet.getY() + dimensions.height(), feet.getZ() + 0.5 + halfWidth);
        if (box.minY < level.getMinY() || box.maxY > level.getMaxY() + 1
                || !level.getWorldBorder().isWithinBounds(box)) return false;
        var floor = level.getBlockState(feet.below());
        if (!floor.isFaceSturdy(level, feet.below(), Direction.UP) || floor.is(Blocks.MAGMA_BLOCK)
                || floor.is(Blocks.CACTUS) || floor.is(Blocks.CAMPFIRE) || floor.is(Blocks.SOUL_CAMPFIRE)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX - 1.0E-5, box.maxY - 1.0E-5, box.maxZ - 1.0E-5))) {
            var state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || state.is(BlockTags.FIRE)
                    || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SWEET_BERRY_BUSH)) return false;
        }
        return level.noCollision(entity, box);
    }

    private static boolean withinBorder(ServerLevel level, int x, int z) {
        return level.getWorldBorder().isWithinBounds(new BlockPos(x - RADIUS, 0, z - RADIUS))
                && level.getWorldBorder().isWithinBounds(new BlockPos(x + RADIUS, 0, z + RADIUS));
    }

    private static void loadFootprint(ServerLevel level, int x, int z) {
        // At most four chunks for a 7x7 footprint. getHeight() returns minY for
        // unloaded chunks, so loading must precede every heightmap lookup.
        for (int cx = (x - RADIUS) >> 4; cx <= (x + RADIUS) >> 4; cx++)
            for (int cz = (z - RADIUS) >> 4; cz <= (z + RADIUS) >> 4; cz++) level.getChunk(cx, cz);
    }
}
