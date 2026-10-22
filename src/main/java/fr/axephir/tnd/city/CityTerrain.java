package fr.axephir.tnd.city;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Smooth transition outside the shield; entire city plateau shares an exact, saved spatial footprint. */
public record CityTerrain() implements DensityFunction {
    public static final MapCodec<CityTerrain> CODEC=MapCodec.unit(new CityTerrain());
    @Override public double compute(FunctionContext c){return CityRegions.blend(c.blockX(),c.blockZ());}
    @Override public void fillArray(double[] out,ContextProvider c){c.fillAllDirectly(out,this);}
    @Override public DensityFunction mapChildren(Visitor v){return this;}
    @Override public double minValue(){return 0;}
    @Override public double maxValue(){return 1;}
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec(){return KeyDispatchDataCodec.of(CODEC);}
}
