package fr.axephir.tnd.city;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** City-wide protection covers vanilla furniture as well as mod blocks. Doors, containers and terminals still work. */
public final class CityShield {
    private static boolean locked(net.minecraft.world.level.LevelAccessor w,BlockPos p){return w instanceof Level l&&CityRegions.protectedAt(l,p);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public void mining(BreakBlockEvent e){if(locked(e.getLevel(),e.getPos())){e.setCanceled(true);e.setNotifyClient(true);if(!e.getLevel().isClientSide())e.getPlayer().sendOverlayMessage(Component.translatable("city.tnd.protected"));}}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public void placement(BlockEvent.EntityPlaceEvent e){if(locked(e.getLevel(),e.getPos())||e instanceof BlockEvent.EntityMultiPlaceEvent multi&&multi.getReplacedBlockSnapshots().stream().anyMatch(s->locked(e.getLevel(),s.getPos())))e.setCanceled(true);}
    @SubscribeEvent public void explosion(ExplosionEvent.Detonate e){e.getAffectedBlocks().removeIf(p->locked(e.getLevel(),p));}
    @SubscribeEvent public void fire(BlockEvent.FluidPlaceBlockEvent e){if(locked(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void tools(BlockEvent.BlockToolModificationEvent e){if(locked(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void piston(PistonEvent.Pre e){
        for(int n=-1;n<=13;n++)if(locked(e.getLevel(),e.getPos().relative(e.getDirection(),n))){e.setCanceled(true);return;}
        var resolver=e.getStructureHelper();if(resolver!=null&&resolver.resolve())for(var p:java.util.stream.Stream.concat(resolver.getToPush().stream(),resolver.getToDestroy().stream()).toList())if(locked(e.getLevel(),p)||locked(e.getLevel(),p.relative(e.getDirection()))){e.setCanceled(true);return;}
    }
    @SubscribeEvent public void entityBreak(net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent e){if(locked(e.getEntity().level(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void grief(EntityMobGriefingEvent e){var mob=e.getEntity();if(mob.level().dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION)&&CityRegions.nearest(mob.getBlockX(),mob.getBlockZ()).distance(mob.getX(),mob.getZ())<=CityRegions.RADIUS+32)e.setCanGrief(false);}
    @SubscribeEvent public void use(PlayerInteractEvent.RightClickBlock e){
        if(!locked(e.getLevel(),e.getPos()))return;var item=e.getItemStack();
        if(item.getItem() instanceof net.minecraft.world.item.BucketItem||item.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)||item.is(net.minecraft.world.item.Items.FIRE_CHARGE))e.setCanceled(true);
    }
}
