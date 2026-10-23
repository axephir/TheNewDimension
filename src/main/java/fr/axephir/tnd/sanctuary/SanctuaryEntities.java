package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import fr.axephir.tnd.entities.VestigeEntities;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
import java.util.*;
public final class SanctuaryEntities {
    public static final Map<SanctuaryConstruct.Kind,DeferredHolder<EntityType<?>,EntityType<SanctuaryConstruct>>> TYPES=new EnumMap<>(SanctuaryConstruct.Kind.class);
    static{for(var k:SanctuaryConstruct.Kind.values())TYPES.put(k,VestigeEntities.TYPES.registerEntityType(k.id,(t,w)->new SanctuaryConstruct(t,w,k),MobCategory.MONSTER,b->b.sized(k.width,k.height).clientTrackingRange(16)));}
    public static void register(IEventBus bus){
        TYPES.forEach((k,t)->ModContent.ITEMS.registerItem(k.id+"_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(t.get())));
        bus.addListener((EntityAttributeCreationEvent e)->TYPES.forEach((k,t)->e.put(t.get(),SanctuaryConstruct.attributes(k).build())));
    }
    private SanctuaryEntities(){}
}
