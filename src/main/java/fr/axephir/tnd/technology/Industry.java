package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.*;
import java.util.*;

/** The industrial tier uses NeoForge's transactional FE API, including other mods' adjacent machines. */
public final class Industry {
    public enum Kind {
        DYNAMO("coal_dynamo",64000), SOLAR("solar_controller",64000), BATTERY("industrial_accumulator",1000000),
        CRUSHER("electric_crusher",32000), FURNACE("electric_kiln",32000), QUARRY("industrial_quarry",128000),
        FARM("cultivator",32000), RANCH("rancher",32000), PASTURE("pastoral_station",32000), REPAIR("crystal_restorer",32000), REFINERY("mnemonic_refinery",96000), CRUCIBLE("nocturne_crucible",128000), ASSEMBLER("axiomatic_assembler",256000), SORTER("mnemonic_sorter",16000);
        public final String id; public final int capacity;
        Kind(String id,int capacity){this.id=id;this.capacity=capacity;}
        public boolean source(){return ordinal()<3;}
    }
    public static final Map<Kind,DeferredBlock<IndustrialBlock>> BLOCKS=new EnumMap<>(Kind.class);
    static { for(var k:Kind.values())BLOCKS.put(k,ModContent.BLOCKS.registerBlock(k.id,p->new IndustrialBlock(p,k),p->p.strength(5,12).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->s.getValue(IndustrialBlock.LIT)?6:0))); }
    public static final DeferredBlock<Block> FRAME=ModContent.BLOCKS.registerSimpleBlock("industrial_frame",p->p.strength(5,12).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final DeferredBlock<EnergyCableBlock> CABLE=ModContent.BLOCKS.registerBlock("energy_cable",EnergyCableBlock::new,p->p.strength(1.5f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> PANEL=ModContent.BLOCKS.registerSimpleBlock("solar_panel",p->p.strength(3,6).requiresCorrectToolForDrops().sound(SoundType.GLASS));
    public static final DeferredItem<Item> CIRCUIT=ModContent.ITEMS.registerSimpleItem("control_circuit"), COIL=ModContent.ITEMS.registerSimpleItem("copper_coil"), DRILL=ModContent.ITEMS.registerSimpleItem("diamond_drill_bit"),
            IRON_DUST=ModContent.ITEMS.registerSimpleItem("iron_grit"), COPPER_DUST=ModContent.ITEMS.registerSimpleItem("copper_grit"), GOLD_DUST=ModContent.ITEMS.registerSimpleItem("gold_grit");
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<IndustrialEntity>> ENTITY=MachineRegistry.ENTITIES.register("industrial_machine",()->new BlockEntityType<>(IndustrialEntity::new,BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<MenuType<?>,MenuType<IndustrialMenu>> MENU=ModContent.MENUS.register("industrial_machine",()->new MenuType<>(IndustrialMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){
        for(var k:Kind.values())ModContent.ITEMS.registerSimpleBlockItem(k.id,BLOCKS.get(k));
        ModContent.ITEMS.registerSimpleBlockItem("industrial_frame",FRAME);ModContent.ITEMS.registerSimpleBlockItem("energy_cable",CABLE);ModContent.ITEMS.registerSimpleBlockItem("solar_panel",PANEL);
        bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Energy.BLOCK,ENTITY.get(),(be,side)->be.port));
    }
    private Industry(){}
}
