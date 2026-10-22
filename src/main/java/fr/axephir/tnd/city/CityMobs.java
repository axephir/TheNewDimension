package fr.axephir.tnd.city;
import fr.axephir.tnd.entities.VestigeEntities;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.DevelopmentCommands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import java.util.*;
public final class CityMobs {
    public static final Map<CityMob.Kind,DeferredHolder<EntityType<?>,EntityType<CityMob>>> TYPES=new EnumMap<>(CityMob.Kind.class);
    static {for(var k:CityMob.Kind.values())TYPES.put(k,VestigeEntities.TYPES.registerEntityType(k.id,(t,l)->new CityMob(t,l,k),MobCategory.MONSTER,b->b.sized(k==CityMob.Kind.DRONE?1.15f:.8f,k==CityMob.Kind.DRONE?1: k==CityMob.Kind.MARSHAL||k==CityMob.Kind.ENFORCER?2.5f:2.2f).clientTrackingRange(12)));}
    public static void register(IEventBus bus){
        TYPES.forEach((k,t)->ModContent.ITEMS.registerItem(k.id+"_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(t.get())));
        bus.addListener((EntityAttributeCreationEvent e)->TYPES.forEach((k,t)->e.put(t.get(),CityMob.attributes(k).build())));
        bus.addListener((RegisterSpawnPlacementsEvent e)->TYPES.forEach((k,t)->e.register(t.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,CityMobs::spawn,RegisterSpawnPlacementsEvent.Operation.REPLACE)));
    }
    public static boolean spawn(EntityType<CityMob> type,ServerLevelAccessor w,EntitySpawnReason reason,BlockPos p,net.minecraft.util.RandomSource r){
        if(TYPES.entrySet().stream().anyMatch(e->e.getKey().ordinal()>=3&&e.getValue().get()==type))return false;
        if(w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!w.getLevel().dimension().equals(DevelopmentCommands.DIMENSION)||!w.getBiome(p).is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd","neon_city"))))return false;
        return w.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.create(net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd","city_surfaces")))&&w.getLevel().getEntitiesOfClass(CityMob.class,new AABB(p).inflate(64)).size()<6;
    }
    private CityMobs(){}
}
