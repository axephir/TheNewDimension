package fr.axephir.tnd.narrative;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ConvergenceContent {
    public static final DeferredBlock<ConvergenceTerminalBlock> RESTORER=ModContent.BLOCKS.registerBlock("spatial_restorer",p->new ConvergenceTerminalBlock(p,false),p->p.strength(8,30).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->7));
    public static final DeferredBlock<ConvergenceTerminalBlock> REACTOR=ModContent.BLOCKS.registerBlock("convergence_reactor",p->new ConvergenceTerminalBlock(p,true),p->p.strength(8,30).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s->10));
    public static final DeferredBlock<PhaseLatticeBlock> LATTICE=ModContent.BLOCKS.registerBlock("phase_lattice",PhaseLatticeBlock::new,p->p.strength(-1,3600000).noCollision().noOcclusion().noLootTable());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<PhaseLatticeEntity>> LATTICE_ENTITY=fr.axephir.tnd.technology.MachineRegistry.ENTITIES.register("phase_lattice",()->new net.minecraft.world.level.block.entity.BlockEntityType<>(PhaseLatticeEntity::new,LATTICE.get()));
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<ConvergenceTerminalEntity>> TERMINAL_ENTITY=fr.axephir.tnd.technology.MachineRegistry.ENTITIES.register("convergence_terminal",()->new net.minecraft.world.level.block.entity.BlockEntityType<>(ConvergenceTerminalEntity::new,RESTORER.get(),REACTOR.get()));
    public static final DeferredItem<SpatialLensItem> LENS=ModContent.ITEMS.registerItem("spatial_lens",SpatialLensItem::new,p->p.stacksTo(1).fireResistant());
    public static final DeferredHolder<MenuType<?>,MenuType<ConvergenceMenu>> MENU=ModContent.MENUS.register("convergence",()->new MenuType<>(ConvergenceMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent e)->e.register(Convergence.CAMERA_TICKETS));ModContent.ITEMS.registerSimpleBlockItem("spatial_restorer",RESTORER);ModContent.ITEMS.registerSimpleBlockItem("convergence_reactor",REACTOR);Convergence.bootstrap();RareAnomalies.bootstrap();bus.addListener(ConvergenceNetworking::register);}
    private ConvergenceContent(){}
}
