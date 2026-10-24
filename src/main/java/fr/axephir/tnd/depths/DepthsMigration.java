package fr.axephir.tnd.depths;
import com.mojang.serialization.Codec;
import fr.axephir.tnd.city.CityRegions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import java.util.*;
/** Safe, loaded-chunk retrofit. A chunk containing player work is preserved and flagged, never force-loaded. */
public final class DepthsMigration{
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> GENERATED=fr.axephir.tnd.progression.PlayerProgression.TYPES.register("nacre_depths_chunk",()->AttachmentType.builder(()->false).serialize(Codec.BOOL.fieldOf("depths")).build());
 private final Map<ServerLevel,LinkedHashSet<ChunkPos>> pending=new WeakHashMap<>();
 @SubscribeEvent public void load(ChunkEvent.Load e){if(!(e.getLevel() instanceof ServerLevel w)||!w.dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION)||!e.getChunk().getData(CityRegions.GENERATED)||e.getChunk().getData(GENERATED))return;var p=e.getChunk().getPos();var r=CityRegions.nearest(p.getMiddleBlockX(),p.getMiddleBlockZ());if(DepthsArchitecture.relevant(r,p.getMinBlockX(),p.getMinBlockZ()))pending.computeIfAbsent(w,k->new LinkedHashSet<>()).add(p);}
 @SubscribeEvent public void tick(LevelTickEvent.Post e){if(!(e.getLevel() instanceof ServerLevel w)||w.getGameTime()%5!=0)return;var q=pending.get(w);if(q==null||q.isEmpty())return;var it=q.iterator();var p=it.next();it.remove();var chunk=w.getChunkSource().getChunkNow(p.x(),p.z());if(chunk==null||chunk.getData(GENERATED))return;
  if(safe(w,p.getMinBlockX(),p.getMinBlockZ())){DepthsArchitecture.generate(w,p.getMinBlockX(),p.getMinBlockZ());chunk.setData(GENERATED,true);}else{chunk.setData(GENERATED,true);org.slf4j.LoggerFactory.getLogger(DepthsMigration.class).warn("Nacre depths retrofit preserved occupied chunk {}, {}",p.x(),p.z());}}
 public static boolean safe(ServerLevel w,int x0,int z0){var r=CityRegions.nearest(x0+8,z0+8);for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++)for(int y=14;y<=99;y++){if(DepthsArchitecture.state(r,x,y,z)==null)continue;var p=new BlockPos(x,y,z);if(w.getBlockEntity(p)!=null)return false;var s=w.getBlockState(p);if(s.isAir()||s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(Blocks.WATER)||s.is(Blocks.LAVA)||s.is(Blocks.GRAVEL)||s.is(Blocks.SAND)||s.is(Blocks.DIRT)||s.is(Blocks.CLAY)||s.is(Blocks.BEDROCK))continue;var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock());if(id.getNamespace().equals("tnd")&&(id.getPath().equals("ashstone")||id.getPath().equals("nightslag")||id.getPath().equals("veined_nightslag")||id.getPath().equals("city_asphalt")||id.getPath().equals("city_tiles")))continue;return false;}return true;}
}
