package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import fr.axephir.tnd.familiar.FamiliarLedger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** Companion hints show a nearby unread trace without solving it or revealing a distant objective. */
public final class SanctuaryFamiliar {
    @SubscribeEvent public void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event){
        var stack=event.getItemStack();
        String key=stack.is(ArchiveContent.LENS.get())?"archive.relic_use":stack.is(SanctuaryContent.MEMORY.get())?"memory_use":stack.is(SanctuaryContent.CORE.get())?"core_use":stack.is(SanctuaryContent.COMPASS.get())?"compass_use":null;
        if(key!=null)event.getToolTip().add(net.minecraft.network.chat.Component.translatable("observatory.tnd."+key).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @SubscribeEvent public void tick(PlayerTickEvent.Post event){if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)||p.tickCount%100!=0||!SanctuaryRegions.protectedAt(p.level(),p.blockPosition()))return;
        var pet=FamiliarLedger.get(p.level().getServer()).loaded(p.level().getServer(),p.getUUID());if(pet==null||pet.level()!=p.level()||!pet.available()||pet.isOrderedToSit()||pet.getTarget()!=null||pet.distanceToSqr(p)>24*24)return;
        var site=SanctuaryRoute.nearest(p.getBlockX(),p.getBlockZ());int index=SanctuaryRoute.reached(SanctuaryLedger.get(p.level().getServer()).mask(site.key()));if(index>=48||index%8!=0&&index%8!=5)return;
        var at=SanctuaryRegions.station(site,index);if(!p.level().hasChunkAt(at)||!(p.level().getBlockEntity(at) instanceof SanctuaryNodeEntity node))return;
        int[][] places={{-24,8},{24,8},{0,-30}};double range=(pet.protocols()&8)!=0?36:22;
        for(int n=0;n<3;n++)if((node.traces()&(1<<n))==0){var point=new Vec3(site.x()+SanctuaryRoute.x(index)+places[n][0]+.5,SanctuaryRoute.FLOOR+2.2,site.z()+SanctuaryRoute.z(index)+places[n][1]+.5);if(pet.position().distanceToSqr(point)>range*range)continue;
            pet.getLookControl().setLookAt(point.x,point.y,point.z);p.level().sendParticles(ParticleTypes.ENCHANT,point.x,point.y,point.z,8,.3,.4,.3,.01);return;
        }
    }
}
