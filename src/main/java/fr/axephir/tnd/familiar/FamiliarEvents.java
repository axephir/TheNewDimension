package fr.axephir.tnd.familiar;

import fr.axephir.tnd.entities.Shardling;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class FamiliarEvents {
    @SubscribeEvent public void victory(LivingDeathEvent e){
        if(!(e.getEntity() instanceof Monster target))return;
        var killer=e.getSource().getEntity();Shardling pet=null;
        if(killer instanceof Shardling s)pet=s;
        else if(killer instanceof ServerPlayer p)pet=FamiliarLedger.get(p.level().getServer()).loaded(p.level().getServer(),p.getUUID());
        if(pet==null||pet.level()!=target.level()||!pet.available()||pet.isOrderedToSit()||pet.distanceToSqr(target)>24*24)return;
        pet.awardExperience(Math.clamp(12+Math.round(target.getMaxHealth()/4),15,60));
    }
}
