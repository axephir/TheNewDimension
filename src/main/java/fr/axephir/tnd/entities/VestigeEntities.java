package fr.axephir.tnd.entities;

import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;

public final class VestigeEntities {
    public static final DeferredRegister.Entities TYPES=DeferredRegister.createEntities(TheNewDimension.ID);
    public static final DeferredHolder<EntityType<?>,EntityType<Shardling>> SHARDLING=TYPES.registerEntityType("shardling",Shardling::new,MobCategory.CREATURE,
            b->b.sized(.65f,.65f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>,EntityType<RiftSentinel>> SENTINEL=TYPES.registerEntityType("rift_sentinel",RiftSentinel::new,MobCategory.MONSTER,
            b->b.sized(.95f,1.45f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>,EntityType<FracturedCustodian>> CUSTODIAN=TYPES.registerEntityType("fractured_custodian",FracturedCustodian::new,MobCategory.MONSTER,
            b->b.sized(1.8f,3.2f).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>,EntityType<AwakenedMob>> LITHOGUARD=TYPES.registerEntityType("lithoguard",(type,level)->new AwakenedMob(type,level,true),MobCategory.MONSTER,b->b.sized(1.8f,3.4f).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>,EntityType<AwakenedMob>> STALKER=TYPES.registerEntityType("glass_stalker",(type,level)->new AwakenedMob(type,level,false),MobCategory.MONSTER,b->b.sized(1.3f,1.25f).clientTrackingRange(10));
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(VestigeEntities::attributes);
        bus.addListener(VestigeEntities::placements);
    }
    private static void attributes(EntityAttributeCreationEvent e) {
        e.put(LITHOGUARD.get(),AwakenedMob.attributes(true).build());e.put(STALKER.get(),AwakenedMob.attributes(false).build());
        e.put(CUSTODIAN.get(),FracturedCustodian.attributes().build());e.put(SHARDLING.get(),Shardling.attributes().build());e.put(SENTINEL.get(),RiftSentinel.attributes().build());
    }
    private static void placements(RegisterSpawnPlacementsEvent e) {
        e.register(LITHOGUARD.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,VestigeEntities::awakeningSpawn,RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(STALKER.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,VestigeEntities::awakeningSpawn,RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(SHARDLING.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VestigeEntities::scoutSpawn,RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(SENTINEL.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VestigeEntities::sentinelSpawn,RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    public static boolean habitat(ServerLevelAccessor level,BlockPos pos) {
        return level.getLevel().dimension().equals(DevelopmentCommands.DIMENSION)&&level.canSeeSky(pos)
                &&level.getBlockState(pos.below()).is(VestigeLandscapeFeature.SURFACES);
    }
    public static boolean scoutSpawn(EntityType<Shardling> type,ServerLevelAccessor level,EntitySpawnReason reason,BlockPos pos,RandomSource random) {
        return reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.CHUNK_GENERATION;
    }
    public static boolean sentinelSpawn(EntityType<RiftSentinel> type,ServerLevelAccessor level,EntitySpawnReason reason,BlockPos pos,RandomSource random) {
        return level.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL&&habitat(level,pos)
                &&VestigeTime.isNight(level.getLevel())&&level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)<7
                &&random.nextInt(48)==0&&level.getLevel().getEntitiesOfClass(RiftSentinel.class,new AABB(pos).inflate(96)).size()<2;
    }
    public static boolean awakeningSpawn(EntityType<AwakenedMob> type,ServerLevelAccessor level,EntitySpawnReason reason,BlockPos pos,RandomSource random){
        if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!habitat(level,pos)||level.getBrightness(LightLayer.BLOCK,pos)>6)return false;
        boolean heavy=type==LITHOGUARD.get();String biome=level.getBiome(pos).unwrapKey().map(k->k.identifier().getPath()).orElse("");
        if(heavy&&!VestigeTime.isNight(level.getLevel()))return false;
        if(!heavy&&!java.util.Set.of("glass_woodland","hollow_shelves","fractured_fields").contains(biome))return false;
        return random.nextInt(heavy?20:12)==0&&level.getLevel().getEntitiesOfClass(AwakenedMob.class,new AABB(pos).inflate(80),m->m.heavy()==heavy).size()<(heavy?1:3);
    }
    private VestigeEntities(){}
}
