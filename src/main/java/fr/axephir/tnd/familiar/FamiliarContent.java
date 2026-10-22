package fr.axephir.tnd.familiar;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class FamiliarContent {
    public static final DeferredItem<Item> NUTRIENT=ModContent.ITEMS.registerSimpleItem("incubator_nutrient");
    public static final DeferredItem<FamiliarCapsuleItem> CAPSULE=ModContent.ITEMS.registerItem("familiar_capsule",FamiliarCapsuleItem::new,p->p.stacksTo(1).fireResistant());
    public static final DeferredBlock<IncubationComputerBlock> COMPUTER=ModContent.BLOCKS.registerBlock("incubation_computer",IncubationComputerBlock::new,p->p.strength(-1,3600000).noLootTable().sound(SoundType.METAL).lightLevel(s->9));
    public static final DeferredBlock<Block> POD=ModContent.BLOCKS.registerSimpleBlock("incubation_capsule",p->p.strength(-1,3600000).noLootTable().sound(SoundType.METAL).lightLevel(s->7));
    public static final DeferredBlock<net.minecraft.world.level.block.TransparentBlock> GLASS=ModContent.BLOCKS.registerBlock("incubation_glass",TransparentBlock::new,p->p.strength(-1,3600000).noLootTable().noOcclusion().sound(SoundType.GLASS));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<IncubationComputerEntity>> COMPUTER_ENTITY=MachineRegistry.ENTITIES.register("incubation_computer",()->new BlockEntityType<>(IncubationComputerEntity::new,COMPUTER.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<FamiliarMenu>> MENU=ModContent.MENUS.register("familiar",()->new MenuType<>(FamiliarMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){
        ModContent.ITEMS.registerSimpleBlockItem("city_dome",fr.axephir.tnd.city.CityContent.DOME);ModContent.ITEMS.registerSimpleBlockItem("city_dome_rib",fr.axephir.tnd.city.CityContent.DOME_RIB);
        ModContent.ITEMS.registerSimpleBlockItem("incubation_computer",COMPUTER);ModContent.ITEMS.registerSimpleBlockItem("incubation_capsule",POD);ModContent.ITEMS.registerSimpleBlockItem("incubation_glass",GLASS);
        bus.addListener(FamiliarNetworking::register);
        for(String name:java.util.List.of("incubator_start","incubator_release","familiar_bond","familiar_evolve","familiar_down","familiar_pulse"))ModContent.ATMOSPHERE_SOUNDS.put(name,ModContent.SOUNDS.register(name,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd",name))));
    }
    private FamiliarContent(){}
}
