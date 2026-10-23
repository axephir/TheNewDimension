package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
public record SanctuaryBlend() implements DensityFunction {
    public static final MapCodec<SanctuaryBlend> CODEC=MapCodec.unit(new SanctuaryBlend());
    public double compute(FunctionContext c){var s=SanctuaryRoute.nearest(c.blockX(),c.blockZ());double d=Math.max(Math.abs(c.blockX()-s.x())-SanctuaryRoute.HALF_X,Math.abs(c.blockZ()-s.z())-SanctuaryRoute.HALF_Z);return Math.clamp((96-d)/96,0,1);}
    public void fillArray(double[] out,ContextProvider c){c.fillAllDirectly(out,this);}public DensityFunction mapChildren(Visitor v){return this;}public double minValue(){return 0;}public double maxValue(){return 1;}public KeyDispatchDataCodec<? extends DensityFunction> codec(){return KeyDispatchDataCodec.of(CODEC);}
}
