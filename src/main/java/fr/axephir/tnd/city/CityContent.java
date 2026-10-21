package fr.axephir.tnd.city;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
import java.util.*;

/** The first gate is buildable using only Overworld/Nether materials; no Vestige prerequisite cycle. */
public final class CityContent {
    public static final Map<String,DeferredBlock<Block>> DECOR=new LinkedHashMap<>();
    static {for(String name:List.of("city_asphalt","carbon_panel","city_tiles","reinforced_panel","circuit_panel","vent_panel","neon_cyan","neon_magenta","neon_amber","relay_coil","nacre_memorial")){
        int light=name.startsWith("neon_")?13:name.equals("nacre_memorial")?8:0;
        DECOR.put(name,ModContent.BLOCKS.registerSimpleBlock(name,p->p.strength(4,10).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->light)));
    }}
    public static final DeferredBlock<PortalMonitorBlock> MONITOR=ModContent.BLOCKS.registerBlock("portal_monitor",PortalMonitorBlock::new,p->p.strength(5,12).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->s.getValue(PortalMonitorBlock.LIT)?9:1));
    public static final DeferredBlock<ArchiveTerminalBlock> ARCHIVE=ModContent.BLOCKS.registerBlock("memory_terminal",ArchiveTerminalBlock::new,p->p.strength(-1,3600000).noLootTable().sound(SoundType.METAL).lightLevel(s->7));
    public static final DeferredItem<Item> PROCESSOR=ModContent.ITEMS.registerSimpleItem("security_processor"),FILAMENT=ModContent.ITEMS.registerSimpleItem("ion_filament");
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PortalMonitorEntity>> MONITOR_ENTITY=MachineRegistry.ENTITIES.register("portal_monitor",()->new BlockEntityType<>(PortalMonitorEntity::new,MONITOR.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<PortalMonitorMenu>> MONITOR_MENU=ModContent.MENUS.register("portal_monitor",()->new MenuType<>(PortalMonitorMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>,MenuType<CodexMenu>> CODEX_MENU=ModContent.MENUS.register("archives",()->new MenuType<>(CodexMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){
        DECOR.forEach((name,b)->ModContent.ITEMS.registerSimpleBlockItem(name,b));ModContent.ITEMS.registerSimpleBlockItem("portal_monitor",MONITOR);ModContent.ITEMS.registerSimpleBlockItem("memory_terminal",ARCHIVE);
        Codex.bootstrap();CityMobs.register(bus);
        bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Energy.BLOCK,MONITOR_ENTITY.get(),(be,side)->be.port));
        for(String name:List.of("gate_charge","gate_open","gate_transit","gate_hum","city_drone","city_alarm","city_footstep","city_laser","city_dash","city_impact","archive_read","nacre_ambience"))
            ModContent.ATMOSPHERE_SOUNDS.put(name,ModContent.SOUNDS.register(name,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd",name))));
    }
    private CityContent(){}
}
