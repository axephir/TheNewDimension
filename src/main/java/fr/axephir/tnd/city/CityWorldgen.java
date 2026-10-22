package fr.axephir.tnd.city;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CityWorldgen {
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOMES=DeferredRegister.create(Registries.BIOME_SOURCE,"tnd");
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITIES=DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE,"tnd");
    static {BIOMES.register("metropolis",()->CityBiomeSource.CODEC);DENSITIES.register("city_blend",()->CityTerrain.CODEC);}
    public static void register(IEventBus bus){BIOMES.register(bus);DENSITIES.register(bus);}
    private CityWorldgen(){}
}
