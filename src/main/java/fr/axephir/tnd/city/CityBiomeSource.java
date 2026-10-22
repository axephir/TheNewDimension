package fr.axephir.tnd.city;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import java.util.stream.Stream;

/** A whole contiguous circular metropolis; the six original climates remain outside its footprint. */
public final class CityBiomeSource extends BiomeSource {
    public static final MapCodec<CityBiomeSource> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            BiomeSource.CODEC.fieldOf("outside").forGetter(s->s.outside),Biome.CODEC.fieldOf("city").forGetter(s->s.city)).apply(i,CityBiomeSource::new));
    private final BiomeSource outside;private final Holder<Biome> city;
    public CityBiomeSource(BiomeSource outside,Holder<Biome> city){this.outside=outside;this.city=city;}
    @Override protected MapCodec<? extends BiomeSource> codec(){return CODEC;}
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes(){return Stream.concat(outside.possibleBiomes().stream(),Stream.of(city));}
    @Override public Holder<Biome> getNoiseBiome(int x,int y,int z,Climate.Sampler sampler){return CityRegions.contains(x*4,z*4)?city:outside.getNoiseBiome(x,y,z,sampler);}
}
