package fr.axephir.tnd.world;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Habitat placement runs after trees. All reads/writes remain in the originating chunk. */
public final class HabitatOreFeature extends Feature<NoneFeatureConfiguration> {
    public enum Habitat { LAVA, OCEAN, ROOTS }
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TheNewDimension.ID);
    public static final TagKey<Block> SUBSTRATES = BlockTags.create(Identifier.fromNamespaceAndPath(TheNewDimension.ID, "ore_substrates"));
    private static TagKey<net.minecraft.world.level.biome.Biome> biomeTag(String color) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheNewDimension.ID, color + "_ore"));
    }
    static {
        FEATURES.register("echo_bloom",EchoBloomFeature::new);
        FEATURES.register("red_habitat_ore", () -> new HabitatOreFeature(Habitat.LAVA));
        FEATURES.register("blue_habitat_ore", () -> new HabitatOreFeature(Habitat.OCEAN));
        FEATURES.register("green_habitat_ore", () -> new HabitatOreFeature(Habitat.ROOTS));
    }
    private final Habitat habitat;
    public HabitatOreFeature(Habitat habitat) { super(NoneFeatureConfiguration.CODEC); this.habitat = habitat; }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return generate(context.level(), context.origin(), context.random()) > 0;
    }
    public int generate(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int x0 = origin.getX() & ~15, z0 = origin.getZ() & ~15, placed = 0;
        java.util.List<BlockPos> exposedFloor = new java.util.ArrayList<>();
        String color = habitat == Habitat.LAVA ? "red" : habitat == Habitat.OCEAN ? "blue" : "green";
        for (int x = x0; x < x0 + 16; x++) for (int z = z0; z < z0 + 16; z++) {
            int top = Math.min(level.getMaxY()-1, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
            if (habitat == Habitat.LAVA) {
                // Sample a whole column, so both surface lakes and deep cave lakes qualify.
                for (int y = level.getMinY()+1; y < top; y++) {
                    BlockPos p = new BlockPos(x,y,z);
                    if (!eligible(level,p,habitat) || !level.getBiome(p).is(biomeTag(color))) continue;
                    exposedFloor.add(p);
                    // Stable spatial patches: no rarity roll capable of missing an entire lake.
                    if (patch(level.getSeed(),x,y,z,3,20)) { set(level,p,ModContent.RED_ORE.get()); placed++; }
                }
            } else if (habitat == Habitat.OCEAN) {
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR,x,z)-1;
                BlockPos p = new BlockPos(x,floor,z);
                if (eligible(level,p,habitat) && level.getBiome(p).is(biomeTag(color))) {
                    exposedFloor.add(p);
                    if (patch(level.getSeed()+139,x,floor,z,3,12)) {
                        set(level,p,ModContent.BLUE_ORE.get()); placed++;
                    }
                }
            } else {
                // WORLD_SURFACE includes leaves: descend to the terrain beneath each canopy.
                for (int y = top; y > Math.max(level.getMinY(),top-32); y--) {
                    BlockPos p = new BlockPos(x,y,z);
                    if (level.getBiome(p).is(biomeTag(color)) && eligible(level,p,habitat)
                            && random.nextInt(24)==0) { set(level,p,ModContent.GREEN_ORE.get()); placed++; break; }
                }
            }
        }
        // Even a lake spanning an unlucky patch cell gets a small exposed cluster.
        if (habitat != Habitat.ROOTS && placed == 0 && exposedFloor.size() >= 8) {
            BlockPos center = exposedFloor.get(exposedFloor.size()/2);
            for (BlockPos p : exposedFloor) if (p.distManhattan(center) <= 1) {
                set(level,p,habitat == Habitat.LAVA ? ModContent.RED_ORE.get() : ModContent.BLUE_ORE.get()); placed++;
            }
        }
        return placed;
    }
    private static boolean patch(long seed, int x, int y, int z, int cell, int percentage) {
        long h = Math.floorDiv(x,cell)*341873128712L + Math.floorDiv(z,cell)*132897987541L
                + Math.floorDiv(y,4)*42317861L + seed;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        return Math.floorMod(h ^ (h >>> 27),100) < percentage;
    }
    public static boolean eligible(WorldGenLevel level, BlockPos p, Habitat habitat) {
        if (!level.ensureCanWrite(p) || !level.getBlockState(p).is(SUBSTRATES)) return false;
        BlockPos above = p.above();
        if (habitat == Habitat.LAVA) return level.getFluidState(above).is(FluidTags.LAVA);
        if (habitat == Habitat.OCEAN) {
            if (!level.getFluidState(above).is(FluidTags.WATER)) return false;
            for (int dy = 2; dy <= 8; dy++) if (!level.getFluidState(p.above(dy)).is(FluidTags.WATER)) return false;
            return true;
        }
        BlockState head = level.getBlockState(above);
        if (!head.isAir()) return false; // visibly exposed, never under a log or a grass tuft.
        int lx = p.getX() & 15, lz = p.getZ() & 15;
        for (int[] d : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            if (lx+d[0]<0 || lx+d[0]>15 || lz+d[1]<0 || lz+d[1]>15) continue;
            BlockPos root = above.offset(d[0],0,d[1]);
            if (level.getBlockState(root).is(BlockTags.LOGS)
                    && level.getBlockState(root.above()).is(BlockTags.LOGS)) return true;
        }
        return false;
    }
    private static void set(WorldGenLevel level, BlockPos p, Block block) {
        level.setBlock(p,block.defaultBlockState(),2);
    }
}
