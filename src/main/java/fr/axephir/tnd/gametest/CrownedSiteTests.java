package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.AwakeningContent;
import fr.axephir.tnd.expedition.ReturnAnchorEntity;
import fr.axephir.tnd.world.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import java.util.*;

final class CrownedSiteTests {
    private static void check(GameTestHelper h,boolean b,String s){h.assertTrue(b,Component.literal(s));}
    static void generation(GameTestHelper h){
        var w=h.getLevel();var registry=w.registryAccess().lookupOrThrow(Registries.STRUCTURE);var ctx=StructurePieceSerializationContext.fromLevel(w);
        var generator=w.getChunkSource().getGenerator();var random=w.getChunkSource().randomState();
        for(String name:List.of("outpost","portal_site","relay","archive","foundry","resonance_vault","observatory","aureate_citadel","echo_conservatory")){
            var holder=registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("tnd",name)));
            check(h,holder.value() instanceof CrownedSiteStructure,name+" uses the mountain structure codec");
            var cp=new ChunkPos(500,500);
            var start=holder.value().generate(holder,w.dimension(),w.registryAccess(),generator,generator.getBiomeSource(),random,w.getServer().getStructureManager(),1234,cp,0,w,b->true);
            check(h,start.isValid()&&start.getPieces().size()==1,name+" produces one atomic building/mountain piece");
            var piece=(CrownedSitePiece)start.getPieces().getFirst();
            check(h,piece.getBoundingBox().maxY()<w.getMaxY(),name+" fits below the dimension ceiling");
            var saved=start.createTag(ctx,cp);var restored=StructureStart.loadStaticStart(ctx,saved,1234);
            check(h,restored!=null&&restored.isValid()&&restored.getPieces().getFirst() instanceof CrownedSitePiece,name+" survives native structure-start serialization");
            var copy=(CrownedSitePiece)restored.getPieces().getFirst();
            check(h,copy.buildingOrigin().equals(piece.buildingOrigin())&&copy.siteRotation()==piece.siteRotation()&&copy.ascent().equals(piece.ascent()),name+" keeps the exact summit, rotation and deterministic ascent after reload");
        }
        // Also exercise the actual authored noise generator and its 256-block ceiling.
        try(var reader=w.getServer().getResourceManager().getResourceOrThrow(Identifier.fromNamespaceAndPath("tnd","dimension/vestige.json")).openAsReader()){
            var stem=net.minecraft.world.level.dimension.LevelStem.CODEC.parse(net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,w.registryAccess()),com.google.gson.JsonParser.parseReader(reader)).getOrThrow();
            var terrain=stem.generator();var noise=RandomState.create(w.registryAccess(),ResourceKey.create(Registries.NOISE_SETTINGS,Identifier.fromNamespaceAndPath("tnd","vestige")),987654);
            var bounds=LevelHeightAccessor.create(0,256);
            for(var name:List.of("relay","archive","foundry","resonance_vault","observatory","aureate_citadel","echo_conservatory")){
                var holder=registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("tnd",name)));boolean found=false;
                for(int n=0;n<24&&!found;n++){
                    var start=holder.value().generate(holder,DevelopmentCommands.DIMENSION,w.registryAccess(),terrain,terrain.getBiomeSource(),noise,w.getServer().getStructureManager(),987654,new ChunkPos(n*5,n*-3),0,bounds,b->true);
                    if(start.isValid()){found=true;check(h,start.getBoundingBox().maxY()<254&&start.getBoundingBox().minY()>0,name+" fits in real Vestige terrain");}
                }
                check(h,found,name+" finds an eligible summit in authored Vestige terrain");
            }
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        h.succeed();
    }
    static void mountain(GameTestHelper h){
        var w=FieldworkTests.vestige(h);var center=new BlockPos(8192,108,8192);var manager=w.getServer().getStructureManager();
        var piece=new CrownedSitePiece(manager,Identifier.fromNamespaceAndPath("tnd","aureate_citadel"),center,60,w.getMinY()+1,Rotation.CLOCKWISE_90);
        place(h,w,piece);
        check(h,w.getBlockState(center.below()).is(AwakeningContent.NIGHTSLAG.get())||w.getBlockState(center.below()).is(AwakeningContent.VEINED_NIGHTSLAG.get()),"Largest building has solid mineral foundations");
        // Real authored return anchor must survive the rotation on top of the mountain.
        var anchor=piece.buildingOrigin().offset(47,1,84);
        var pivot=piece.buildingOrigin().offset(47,0,47);
        anchor=pivot.offset(anchor.subtract(pivot).rotate(Rotation.CLOCKWISE_90));
        check(h,w.getBlockEntity(anchor) instanceof ReturnAnchorEntity,"Authored block entities remain functional on a rotated summit");
        var floors=piece.ascent();int clear=0;
        for(var p:floors)if(w.getBlockState(p).is(AwakeningContent.DECOR.get("umbra_tiles").get())||w.getBlockState(p).is(AwakeningContent.STAIRS.get("umbra_tiles").get())){
            if(w.isEmptyBlock(p.above())&&w.isEmptyBlock(p.above(2)))clear++;
            for(int dy=1;dy<=8;dy++)check(h,!w.isEmptyBlock(p.below(dy)),"Ascent has continuous support below its paving, including the summit approach");
        }
        check(h,clear>floors.size()*.9,"A five-block-wide ascent retains two clear blocks of walking height");
        int crowns=0;for(int x=-85;x<=85;x++)for(int z=-85;z<=85;z++){
            var s=w.getBlockState(center.offset(x,10,z));if(s.is(AwakeningContent.NIGHTSLAG.get())||s.is(AwakeningContent.VEINED_NIGHTSLAG.get()))crowns++;
        }
        check(h,crowns>60,"A real ring of tall mineral needles crowns the summit");
        var start=floors.stream().min(java.util.Comparator.comparingInt(BlockPos::getY)).orElseThrow().above();
        var target=pivot.offset(new BlockPos(0,1,-40).rotate(Rotation.CLOCKWISE_90));
        var walker=net.minecraft.world.entity.EntityTypes.ZOMBIE.create(w,net.minecraft.world.entity.EntitySpawnReason.EVENT);
        walker.setNoAi(true);walker.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(start));walker.setOnGround(true);walker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(256);w.addFreshEntity(walker);
        try{
            var nav=(net.minecraft.world.entity.ai.navigation.GroundPathNavigation)walker.getNavigation();nav.setCanOpenDoors(true);nav.setRequiredPathLength(320);nav.setMaxVisitedNodesMultiplier(32);
            var route=nav.createPath(target,0);check(h,route!=null&&route.canReach(),"Native walking navigation can climb the complete mountain to the citadel entrance; end="+(route==null?"null":route.getEndNode()));
        }finally{walker.discard();}h.succeed();
    }
    static void starter(GameTestHelper h){
        var w=h.getLevel();var template=w.getServer().getStructureManager().getOrCreate(Identifier.fromNamespaceAndPath("tnd","outpost"));
        var pos=OutpostTerrain.crowned(w,16384,16384,template).orElseThrow();
        check(h,w.getBlockEntity(pos.offset(24,1,43)) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"The first-world outpost has its working cache on the summit");
        check(h,w.getBlockState(pos.offset(24,-1,24)).is(AwakeningContent.NIGHTSLAG.get())||w.getBlockState(pos.offset(24,-1,24)).is(AwakeningContent.VEINED_NIGHTSLAG.get()),"The manually seeded Overworld outpost uses the same solid mountain");h.succeed();
    }
    private static void place(GameTestHelper h,ServerLevel w,CrownedSitePiece p){
        var bb=p.getBoundingBox();var random=RandomSource.create(44);
        var last=new ChunkPos(bb.maxX()>>4,bb.maxZ()>>4);var marker=new BlockPos(last.getMinBlockX()+2,p.summit().getY(),last.getMinBlockZ()+2);
        w.getChunkAt(marker);w.setBlock(marker,Blocks.BEDROCK.defaultBlockState(),2);
        boolean first=true;
        for(int x=bb.minX()>>4;x<=bb.maxX()>>4;x++)for(int z=bb.minZ()>>4;z<=bb.maxZ()>>4;z++){
            var cp=new ChunkPos(x,z);w.getChunk(x,z);
            var clip=new BoundingBox(cp.getMinBlockX(),w.getMinY(),cp.getMinBlockZ(),cp.getMaxBlockX(),w.getMaxY()-1,cp.getMaxBlockZ());
            p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),random,clip,cp,p.summit());
            if(first){check(h,w.getBlockState(marker).is(Blocks.BEDROCK),"Generating one chunk cannot write into another chunk");first=false;}
        }
    }
    private CrownedSiteTests(){}
}
