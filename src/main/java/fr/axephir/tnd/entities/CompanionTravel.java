package fr.axephir.tnd.entities;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Capture before a successful player transfer; move only loaded, owned, following pets. */
public final class CompanionTravel {
    private record Pending(ResourceKey<Level> source,ResourceKey<Level> target,List<UUID> pets,long tick){}
    private final Map<UUID,Pending> pending=new HashMap<>();
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void departing(EntityTravelToDimensionEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||p.level().dimension().equals(e.getDimension()))return;
        long now=p.level().getServer().overworld().getGameTime();
        pending.values().removeIf(old->now-old.tick()>40);
        if(pending.size()>=4096)pending.clear();
        var pets=p.level().getEntitiesOfClass(Shardling.class,p.getBoundingBox().inflate(6),pet->pet.isOwnedBy(p)&&!pet.isOrderedToSit()&&!pet.downed()&&pet.available()&&pet.isAlive());
        pending.put(p.getUUID(),new Pending(p.level().dimension(),e.getDimension(),pets.stream().map(Shardling::getUUID).toList(),now));
    }
    @SubscribeEvent
    public void arrived(PlayerEvent.PlayerChangedDimensionEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var transfer=pending.remove(p.getUUID());if(transfer==null||!transfer.source().equals(e.getFrom())||!transfer.target().equals(e.getTo()))return;
        if(p.level().getServer().overworld().getGameTime()-transfer.tick()>40)return;
        var source=p.level().getServer().getLevel(transfer.source());if(source==null)return;
        // PlayerChangedDimension fires before a cold destination necessarily finishes
        // loading. Load the single arrival chunk, as the portal itself does, without
        // installing any persistent ticket or loading remote exploration chunks.
        if(!transfer.pets().isEmpty())p.level().getChunkAt(p.blockPosition());
        int left=0;
        for(UUID uuid:transfer.pets()) {
            if(!(source.getEntity(uuid) instanceof Shardling pet)||!pet.isAlive()||pet.isOrderedToSit()
                    ||pet.downed()||!pet.available()||pet.getOwnerReference()==null||!pet.getOwnerReference().matches(p))continue;
            var pos=findLanding(p.level(),p.blockPosition(),pet);
            if(pos.isEmpty()||pet.teleport(new TeleportTransition(p.level(),Vec3.atBottomCenterOf(pos.get()),Vec3.ZERO,pet.getYRot(),pet.getXRot(),TeleportTransition.DO_NOTHING))==null)left++;
        }
        if(left>0)p.sendOverlayMessage(Component.translatable("message.tnd.companion_no_landing",left));
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){pending.remove(e.getEntity().getUUID());}
    public static Optional<BlockPos> findLanding(ServerLevel level,BlockPos center,Shardling pet) {
        for(int r=1;r<=3;r++)for(int dy:new int[]{0,1,-1,2,-2})for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
            var pos=center.offset(dx,dy,dz);
            if(pos.getY()<=level.getMinY()||pos.getY()+2>level.getMaxY()||!level.hasChunk(pos.getX()>>4,pos.getZ()>>4))continue;
            if(!level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)
                    ||!level.getFluidState(pos).isEmpty()||!level.getFluidState(pos.above()).isEmpty())continue;
            var box=pet.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(pet.position()));
            if(level.noCollision(pet,box))return Optional.of(pos);
        }
        return Optional.empty();
    }
}
