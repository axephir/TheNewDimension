package fr.axephir.tnd.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.progression.*;
import fr.axephir.tnd.world.VestigeLandscapeFeature;
import java.util.*;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.*;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;

final class AtmosphereTests {
    private static void check(GameTestHelper h,boolean ok,String reason){h.assertTrue(ok,Component.literal(reason));}
    static void biomes(GameTestHelper h){
        var world=h.getLevel();var registries=world.registryAccess();LevelStem stem;
        try(var reader=world.getServer().getResourceManager().getResourceOrThrow(Identifier.fromNamespaceAndPath("tnd","dimension/vestige.json")).openAsReader()){
            stem=LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,registries),JsonParser.parseReader(reader)).getOrThrow();
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
        var random=RandomState.create(registries,ResourceKey.create(Registries.NOISE_SETTINGS,Identifier.fromNamespaceAndPath("tnd","vestige")),987654L);
        Set<String> found=new HashSet<>();
        for(int x=-8192;x<=8192&&found.size()<6;x+=256)for(int z=-8192;z<=8192&&found.size()<6;z+=256)
            found.add(stem.generator().getBiomeSource().getNoiseBiome(x>>2,24,z>>2,random.sampler()).unwrapKey().orElseThrow().identifier().getPath());
        check(h,found.equals(Set.of("silent_expanse","fractured_fields","hollow_shelves","glass_woodland","ashen_basin","obsidian_dunes")),"All six biomes must actually be reachable through the real climate sampler: "+found);
        for(int x=-2048;x<=2048;x+=128){
            int height=stem.generator().getBaseHeight(x,-x,Heightmap.Types.WORLD_SURFACE,net.minecraft.world.level.LevelHeightAccessor.create(0,256),random);
            check(h,height>5&&height<=208,"New terrain must retain safe headroom for structures and crystals");
        }
        h.succeed();
    }
    static void landscapes(GameTestHelper h){
        var world=h.getLevel();var center=h.absolutePos(new BlockPos(16,1,16));int x0=center.getX()&~15,z0=center.getZ()&~15;
        for(String biome:List.of("glass_woodland","ashen_basin","obsidian_dunes")){
            var key=ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("tnd",biome));
            var origin=new BlockPos(x0,120,z0);
            var fill=FillBiomeCommand.fill(world,origin.offset(0,-4,0),origin.offset(15,30,15),world.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key));
            check(h,fill.left().isPresent(),"Native biome replacement prepares the feature fixture");
            for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++)for(int y=117;y<=145;y++)
                world.setBlock(new BlockPos(x,y,z),(y<=120?ModContent.ASHSTONE.get():Blocks.AIR).defaultBlockState(),2);
            var protectedBlock=new BlockPos(x0+1,121,z0+1);world.setBlock(protectedBlock,Blocks.GOLD_BLOCK.defaultBlockState(),2);
            Heightmap.primeHeightmaps(world.getChunk(x0>>4,z0>>4),EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG));
            int made=new VestigeLandscapeFeature().generate(world,origin,RandomSource.create(14728));
            check(h,made>0,"The real "+biome+" feature must produce visible landscape elements");
            check(h,world.getBlockState(protectedBlock).is(Blocks.GOLD_BLOCK),"Landscape decoration cannot erase a construction");
            boolean crystal=false,water=false,fossil=false,dark=false;
            for(var p:BlockPos.betweenClosed(origin.offset(0,-3,0),origin.offset(15,25,15))){
                var s=world.getBlockState(p);crystal|=s.is(ModContent.PALE_CRYSTAL.get());water|=s.is(Blocks.WATER);
                fossil|=s.is(ModContent.FOSSIL_COLUMN.get());dark|=s.is(ModContent.VITREOUS_STONE.get());
            }
            check(h,biome.equals("ashen_basin")?water&&fossil:crystal&&dark,"Each biome needs its own tangible landforms: "+biome);
            if(biome.equals("ashen_basin"))for(var p:BlockPos.betweenClosed(origin.offset(0,-2,0),origin.offset(15,-1,15)))if(world.getBlockState(p).is(Blocks.WATER)){
                for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST,Direction.DOWN))
                    check(h,!world.getBlockState(p.relative(dir)).isAir(),"Generated basins must have closed sides and a floor");
            }
        }
        h.succeed();
    }
    private static List<Object> packets(EmbeddedChannel channel){
        channel.runPendingTasks();var result=new ArrayList<Object>();Object p;
        while((p=channel.readOutbound())!=null){result.add(p);io.netty.util.ReferenceCountUtil.release(p);}return result;
    }
    static void celebration(GameTestHelper h){
        var p=GameplayTests.player(h);var other=GameplayTests.player(h);p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new BlockPos(2,1,2))));
        var channel=(EmbeddedChannel)p.connection.getConnection().channel();var otherChannel=(EmbeddedChannel)other.connection.getConnection().channel();
        try{
            packets(channel);packets(otherChannel);
            for(var mastery:Mastery.values()){
                ProgressionService.award(p,mastery,1);check(h,packets(channel).stream().noneMatch(x->x instanceof ClientboundSoundPacket||x instanceof ClientboundLevelParticlesPacket),"Sub-level XP must remain quiet");
                ProgressionService.award(p,mastery,100);var sent=packets(channel);
                check(h,sent.stream().filter(x->x instanceof ClientboundSoundPacket).count()==1,"Exactly one sound per genuine level increase in "+mastery);
                check(h,sent.stream().filter(x->x instanceof ClientboundLevelParticlesPacket).count()==17,"A real level sends its ring and particle burst");
            }
            check(h,packets(otherChannel).stream().noneMatch(x->x instanceof ClientboundSoundPacket||x instanceof ClientboundLevelParticlesPacket),"Celebrations target their owner and do not spam nearby players");
            p.setData(PlayerProgression.PROFILE,Profile.empty());check(h,packets(channel).stream().noneMatch(x->x instanceof ClientboundSoundPacket),"Profile synchronization itself cannot replay level sounds");
        }finally{GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();
    }
    private AtmosphereTests(){}
}
