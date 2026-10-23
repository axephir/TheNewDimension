package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.*;

public final class TemperingContent {
    public static final DeferredBlock<TemperingForgeBlock> BLOCK=ModContent.BLOCKS.registerBlock("mnemonic_tempering_forge",TemperingForgeBlock::new,p->p.strength(6,30).requiresCorrectToolForDrops().noOcclusion().sound(net.minecraft.world.level.block.SoundType.METAL).lightLevel(s->s.getValue(TemperingForgeBlock.LIT)?14:3));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TemperingForgeEntity>> ENTITY=MachineRegistry.ENTITIES.register("mnemonic_tempering_forge",()->new BlockEntityType<>(TemperingForgeEntity::new,BLOCK.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<TemperingMenu>> MENU=ModContent.MENUS.register("mnemonic_tempering_forge",()->new MenuType<>(TemperingMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){ModContent.ITEMS.registerSimpleBlockItem("mnemonic_tempering_forge",BLOCK);bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Energy.BLOCK,ENTITY.get(),(be,side)->be.port));}
    private TemperingContent(){}
}
