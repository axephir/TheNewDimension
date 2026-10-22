package fr.axephir.tnd.narrative;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;

/** Recovery index; journals and the player's own return marker are independent of this world index. */
public final class StoryRecords extends SavedData {
    private final Map<String,CompoundTag> records;
    public static final Codec<StoryRecords> CODEC=Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).xmap(StoryRecords::new,s->s.records);
    public static final SavedDataType<StoryRecords> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("tnd","convergence"),()->new StoryRecords(Map.of()),CODEC);
    private StoryRecords(Map<String,CompoundTag> input){records=new HashMap<>();input.forEach((k,v)->records.put(k,v.copy()));}
    public static StoryRecords get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    public CompoundTag record(UUID owner){return records.get(owner.toString());}
    public void reserve(UUID owner,UUID session,String marker,int slot){if(slot<0||slot>100000)throw new IllegalStateException("Story plot capacity reached");var tag=new CompoundTag();tag.putString("Session",session.toString());tag.putString("Return",marker);tag.putInt("Plot",slot);tag.putInt("Phase",1);records.put(owner.toString(),tag);setDirty();}
    public int nextSlot(){return records.values().stream().mapToInt(t->t.getIntOr("Plot",-1)).max().orElse(-1)+1;}
    public void phase(UUID owner,int phase){var r=record(owner);if(r!=null){r.putInt("Phase",phase);setDirty();}}
}
