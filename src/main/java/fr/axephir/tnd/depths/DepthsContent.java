package fr.axephir.tnd.depths;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
public final class DepthsContent{
 public static final DeferredBlock<DepthsNodeBlock> NODE=ModContent.BLOCKS.registerBlock("depths_terminal",DepthsNodeBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->10).sound(SoundType.METAL));
 public static final DeferredBlock<DepthsOreBlock> ORE=ModContent.BLOCKS.registerBlock("asterium_nodule",DepthsOreBlock::new,p->p.strength(-1,3600000).noLootTable().lightLevel(s->9).sound(SoundType.AMETHYST));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DepthsNodeEntity>> ENTITY=MachineRegistry.ENTITIES.register("depths_terminal",()->new BlockEntityType<>(DepthsNodeEntity::new,NODE.get()));
 public static final DeferredHolder<MenuType<?>,MenuType<DepthsMenu>> MENU=ModContent.MENUS.register("nacre_depths",()->new MenuType<>(DepthsMenu::new,FeatureFlags.DEFAULT_FLAGS));
 public static void register(IEventBus bus){ModContent.ITEMS.registerSimpleBlockItem("depths_terminal",NODE);ModContent.ITEMS.registerSimpleBlockItem("asterium_nodule",ORE);DepthsMigration.GENERATED.getId();}
 private DepthsContent(){}
}
