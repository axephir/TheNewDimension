package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import java.util.stream.Stream;

public final class SanctuaryBiomeSource extends BiomeSource {
    public static final MapCodec<SanctuaryBiomeSource> CODEC=RecordCodecBuilder.mapCodec(i->i.group(BiomeSource.CODEC.fieldOf("outside").forGetter(s->s.outside),Biome.CODEC.fieldOf("sanctuary").forGetter(s->s.biome)).apply(i,SanctuaryBiomeSource::new));
    private final BiomeSource outside;private final Holder<Biome> biome;
    public SanctuaryBiomeSource(BiomeSource outside,Holder<Biome> biome){this.outside=outside;this.biome=biome;}
    @Override protected MapCodec<? extends BiomeSource> codec(){return CODEC;}
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes(){return Stream.concat(outside.possibleBiomes().stream(),Stream.of(biome));}
    @Override public Holder<Biome> getNoiseBiome(int x,int y,int z,Climate.Sampler sampler){return SanctuaryRegions.contains(x*4,z*4)?biome:outside.getNoiseBiome(x,y,z,sampler);}
}
