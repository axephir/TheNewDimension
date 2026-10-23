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
    private static boolean locked(net.minecraft.world.level.LevelAccessor w,BlockPos p){return w instanceof Level l&&(CityRegions.protectedAt(l,p)||fr.axephir.tnd.sanctuary.SanctuaryRegions.protectedAt(l,p));}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public void mining(BreakBlockEvent e){
        if(!locked(e.getLevel(),e.getPos()))return;
        e.setCanceled(true);e.setNotifyClient(true);
        if(!e.getLevel().isClientSide()){
            boolean sanctuary=e.getLevel() instanceof Level level&&fr.axephir.tnd.sanctuary.SanctuaryRegions.protectedAt(level,e.getPos());
            e.getPlayer().sendOverlayMessage(Component.translatable(sanctuary?"observatory.tnd.protected":"city.tnd.protected"));
        }
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public void placement(BlockEvent.EntityPlaceEvent e){if(locked(e.getLevel(),e.getPos())||e instanceof BlockEvent.EntityMultiPlaceEvent multi&&multi.getReplacedBlockSnapshots().stream().anyMatch(s->locked(e.getLevel(),s.getPos())))e.setCanceled(true);}
    @SubscribeEvent public void explosion(ExplosionEvent.Detonate e){e.getAffectedBlocks().removeIf(p->locked(e.getLevel(),p));}
    @SubscribeEvent public void fire(BlockEvent.FluidPlaceBlockEvent e){if(locked(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void tools(BlockEvent.BlockToolModificationEvent e){if(locked(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void piston(PistonEvent.Pre e){
        for(int n=-1;n<=13;n++)if(locked(e.getLevel(),e.getPos().relative(e.getDirection(),n))){e.setCanceled(true);return;}
        if(e.getPistonMoveType()==PistonEvent.PistonMoveType.RETRACT&&e.getLevel() instanceof Level l&&nearProtectedPull(l,e.getPos())){e.setCanceled(true);return;}
        var resolver=e.getStructureHelper();if(resolver!=null&&resolver.resolve())for(var p:java.util.stream.Stream.concat(resolver.getToPush().stream(),resolver.getToDestroy().stream()).toList())if(locked(e.getLevel(),p)||locked(e.getLevel(),p.relative(resolver.getPushDirection()))){e.setCanceled(true);return;}
    }
    /** Retraction is announced before its head is removed, so the native resolver cannot yet see sticky side branches. */
    private static boolean nearProtectedPull(Level w,BlockPos at){
        if(!w.dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION))return false;
        int minX=at.getX()-14,maxX=at.getX()+14,minZ=at.getZ()-14,maxZ=at.getZ()+14;var region=CityRegions.nearest(at.getX(),at.getZ());
        // Exact closest points cover the circular footprint, including a thin intersection at the edge of a chunk.
        for(int cx=minX>>4;cx<=maxX>>4;cx++)for(int cz=minZ>>4;cz<=maxZ>>4;cz++)if(w.hasChunk(cx,cz)){
            int x=Math.clamp(region.x(),Math.max(minX,cx*16),Math.min(maxX,cx*16+15)),z=Math.clamp(region.z(),Math.max(minZ,cz*16),Math.min(maxZ,cz*16+15));
            if(CityRegions.protectedAt(w,new BlockPos(x,at.getY(),z)))return true;
            for(int xx=Math.max(minX,cx*16);xx<=Math.min(maxX,cx*16+15);xx++)for(int zz=Math.max(minZ,cz*16);zz<=Math.min(maxZ,cz*16+15);zz++)if(fr.axephir.tnd.sanctuary.SanctuaryRegions.protectedAt(w,new BlockPos(xx,at.getY(),zz)))return true;
        }
        // Older city biomes are stored in four-block cells and may predate the new circular generation marker.
        for(int x=minX;x<=maxX;x+=4)for(int z=minZ;z<=maxZ;z+=4)for(int y=at.getY()-14;y<=at.getY()+14;y+=4)if(CityRegions.protectedAt(w,new BlockPos(x,y,z)))return true;
        return false;
    }
    @SubscribeEvent public void entityBreak(net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent e){if(locked(e.getEntity().level(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public void grief(EntityMobGriefingEvent e){var mob=e.getEntity();if(mob.level().dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION)&&(CityRegions.nearest(mob.getBlockX(),mob.getBlockZ()).distance(mob.getX(),mob.getZ())<=CityRegions.RADIUS+32||fr.axephir.tnd.sanctuary.SanctuaryRegions.protectedAt(mob.level(),mob.blockPosition())))e.setCanGrief(false);}
    @SubscribeEvent public void use(PlayerInteractEvent.RightClickBlock e){
        if(!locked(e.getLevel(),e.getPos()))return;var item=e.getItemStack();
        if(item.getItem() instanceof net.minecraft.world.item.BucketItem||item.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)||item.is(net.minecraft.world.item.Items.FIRE_CHARGE))e.setCanceled(true);
    }
}
