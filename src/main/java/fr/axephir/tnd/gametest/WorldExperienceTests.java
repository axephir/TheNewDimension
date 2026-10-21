package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.HabitatOreFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

final class WorldExperienceTests {
    static void habitatOres(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos corner=h.absolutePos(new BlockPos(0,1,0));
        int x0=(corner.getX()+15)&~15,z0=(corner.getZ()+15)&~15,y=corner.getY()+2;
        BlockPos origin=new BlockPos(x0,y,z0);
        // Real configured habitat feature, in a biome modified with the normal engine API.
        h.setBiome(Biomes.DEEP_OCEAN);
        // GameTest surrounds its template with a barrier roof; remove it above this fixture.
        for (int x=0;x<16;x++) for (int z=0;z<16;z++)
            for (int dy=0;dy<30;dy++) level.setBlock(origin.offset(x,dy,z),Blocks.AIR.defaultBlockState(),2);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            level.setBlock(origin.offset(x,0,z),Blocks.GRAVEL.defaultBlockState(),2);
            for(int dy=1;dy<=12;dy++)level.setBlock(origin.offset(x,dy,z),Blocks.WATER.defaultBlockState(),2);
        }
        var ocean=new HabitatOreFeature(HabitatOreFeature.Habitat.OCEAN);
        int blue=ocean.generate(level,origin,RandomSource.create(17));
        h.assertTrue(blue>0,Component.literal("Deep ocean floor must have visible blue ore; height="+level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR,x0+8,z0+8)+" expected="+(y+1)+" biome="+level.getBiome(origin.offset(8,0,8)).unwrapKey()+" eligible="+HabitatOreFeature.eligible(level,origin.offset(8,0,8),HabitatOreFeature.Habitat.OCEAN)));
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            BlockPos p=origin.offset(x,0,z);
            if(level.getBlockState(p).is(ModContent.BLUE_ORE.get()))
                h.assertTrue(level.getBlockState(p.above()).is(Blocks.WATER),Component.literal("Blue ore must touch water directly"));
        }
        BlockPos buried=origin.offset(2,0,2);
        level.setBlock(buried,Blocks.STONE.defaultBlockState(),2);
        level.setBlock(buried.above(),Blocks.STONE.defaultBlockState(),2);
        h.assertTrue(!HabitatOreFeature.eligible(level,buried,HabitatOreFeature.Habitat.OCEAN),Component.literal("Buried ore positions must be rejected"));
        h.setBiome(Biomes.PLAINS);
        // Clear the complete water fixture before introducing lava. Vanilla onPlace
        // would otherwise solidify it against water still in the next column.
        for (int x=0;x<16;x++) for (int z=0;z<16;z++)
            for (int dy=1;dy<=12;dy++) level.setBlock(origin.offset(x,dy,z),Blocks.AIR.defaultBlockState(),2);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            level.setBlock(origin.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
            for(int dy=1;dy<=12;dy++)level.setBlock(origin.offset(x,dy,z),(dy<4?Blocks.LAVA:Blocks.AIR).defaultBlockState(),2);
        }
        int red=new HabitatOreFeature(HabitatOreFeature.Habitat.LAVA).generate(level,origin,RandomSource.create(17));
        h.assertTrue(red>0,Component.literal("A substantial lava lake must always expose red ore"));
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            BlockPos p=origin.offset(x,0,z);
            if(level.getBlockState(p).is(ModContent.RED_ORE.get()))
                h.assertTrue(level.getBlockState(p.above()).is(Blocks.LAVA),Component.literal("Red ore must be in the first solid layer below lava"));
        }
        h.setBiome(Biomes.FOREST);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            level.setBlock(origin.offset(x,0,z),Blocks.DIRT.defaultBlockState(),2);
            for(int dy=1;dy<=12;dy++)level.setBlock(origin.offset(x,dy,z),Blocks.AIR.defaultBlockState(),2);
            if(x%3==1&&z%3==1)for(int dy=1;dy<=4;dy++)level.setBlock(origin.offset(x,dy,z),Blocks.OAK_LOG.defaultBlockState(),2);
        }
        int green=new HabitatOreFeature(HabitatOreFeature.Habitat.ROOTS).generate(level,origin,RandomSource.create(1234));
        h.assertTrue(green>0,Component.literal("A forest must contain some discoverable root ore"));
        h.assertTrue(green<40,Component.literal("Root ore should stay uncommon"));
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            BlockPos p=origin.offset(x,0,z);
            if(level.getBlockState(p).is(ModContent.GREEN_ORE.get()))
                h.assertTrue(level.isEmptyBlock(p.above()),Component.literal("Root ore must be visible from above"));
        }
        h.succeed();
    }
    static void outpostTerrain(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos p=h.absolutePos(new BlockPos(3,2,3));
        for(int x=0;x<12;x++)for(int z=0;z<12;z++) {
            for(int dy=1;dy<26;dy++)level.setBlock(p.offset(x,dy,z),Blocks.AIR.defaultBlockState(),2);
            level.setBlock(p.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
        }
        level.setBlock(p.offset(5,1,5),Blocks.STONE.defaultBlockState(),2);
        level.setBlock(p.offset(5,2,5),Blocks.STONE.defaultBlockState(),2);
        for(int dy=1;dy<=9;dy++)level.setBlock(p.offset(7,dy,7),Blocks.OAK_LOG.defaultBlockState(),2);
        BlockPos anchor=fr.axephir.tnd.world.OutpostTerrain.prepare(level,p.getX(),p.getZ(),12,12,35);
        h.assertTrue(anchor.getY()==p.getY()+2,Component.literal("Outpost height must follow the ground, not tree tops"));
        h.assertTrue(level.getBlockState(anchor.offset(2,-1,2)).is(ModContent.MOSSY_ASHSTONE_BRICKS.get()),Component.literal("A slope must receive a solid retaining foundation"));
        for(int x=0;x<12;x++)for(int z=0;z<12;z++)
            for(int dy=1;dy<=6;dy++)level.setBlock(p.offset(x,dy,z),Blocks.WATER.defaultBlockState(),2);
        BlockPos coastal=fr.axephir.tnd.world.OutpostTerrain.prepare(level,p.getX(),p.getZ(),12,12,35);
        h.assertTrue(coastal.getY()==p.getY()+6,Component.literal("Water must not be treated as foliage: build above the sea, not on its bed"));
        h.assertTrue(level.getBlockState(coastal.offset(2,-1,2)).is(ModContent.MOSSY_ASHSTONE_BRICKS.get()),Component.literal("The coastal foundation must replace water with solid support"));
        h.succeed();
    }
    static void buildings(GameTestHelper h) {
        var manager=h.getLevel().getServer().getStructureManager();
        for(String name:new String[]{"outpost","portal_site","relay","archive","foundry"}) {
            var template=manager.get(Identifier.fromNamespaceAndPath("tnd",name)).orElseThrow();
            h.assertTrue(template.getSize().getX()>=41&&template.getSize().getZ()>=41,Component.literal(name+" must be a substantial building"));
            var settings=new StructurePlaceSettings();
            var lamps=template.filterBlocks(BlockPos.ZERO,settings,ModContent.RESONANT_LAMP.get());
            h.assertTrue(lamps.size()>=8,Component.literal(name+" must have functional interior lighting"));
            var chests=template.filterBlocks(BlockPos.ZERO,settings,Blocks.CHEST);
            h.assertTrue(!chests.isEmpty(),Component.literal(name+" must have furnished storage"));
            for(var chest:chests) h.assertTrue(chest.nbt()!=null&&chest.nbt().contains("LootTable"),Component.literal("Storage must contain authored loot"));
        }
        // Place the real starter template to detect missing blocks, invalid properties or broken block entities.
        var outpost=manager.get(Identifier.fromNamespaceAndPath("tnd","outpost")).orElseThrow();
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        h.assertTrue(outpost.placeInWorld(h.getLevel(),pos,pos,new StructurePlaceSettings().setIgnoreEntities(true),h.getLevel().getRandom(),2),Component.literal("Starter outpost must place in the game engine"));
        h.assertTrue(h.getLevel().getBlockEntity(pos.offset(24,1,43)) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,Component.literal("Expedition cache must be a real working container"));
        h.succeed();
    }
    private WorldExperienceTests() {}
}
