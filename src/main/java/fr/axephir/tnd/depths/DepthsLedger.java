package fr.axephir.tnd.depths;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;
/** Completion and personal extraction are committed before inventory insertion. */
public final class DepthsLedger extends SavedData{
 private final Map<String,CompoundTag> data;
 public static final Codec<DepthsLedger> CODEC=Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).xmap(DepthsLedger::new,l->l.data);
 public static final SavedDataType<DepthsLedger> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("tnd","nacre_depths"),()->new DepthsLedger(Map.of()),CODEC);
 private DepthsLedger(Map<String,CompoundTag> d){data=new HashMap<>();d.forEach((k,v)->data.put(k,v.copy()));}
 public static DepthsLedger get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(TYPE);}
 private CompoundTag site(long key){return data.computeIfAbsent("city/"+key,k->new CompoundTag());}
 public int mask(long key){var d=data.get("city/"+key);int raw=d==null?0:d.getIntOr("complete",0)&255;int n=0;while(n<8&&(raw&(1<<n))!=0)n++;return (1<<n)-1;}
 public boolean finish(long key,int i){if(i<0||i>7||mask(key)!=(1<<i)-1)return false;site(key).putInt("complete",(1<<(i+1))-1);setDirty();return true;}
 public void eligible(long key,UUID player,int i){var d=site(key);String p="eligible/"+player;d.putInt(p,d.getIntOr(p,0)|1<<i);setDirty();}
 public boolean canClaim(long key,UUID p,int i){var d=data.get("city/"+key);return i>=0&&i<8&&DepthsLayout.boss(i)&&(mask(key)&(1<<i))!=0&&d!=null&&(d.getIntOr("eligible/"+p,0)&(1<<i))!=0&&(d.getIntOr("claimed/"+p,0)&(1<<i))==0;}
 public boolean claim(long key,UUID p,int i){if(!canClaim(key,p,i))return false;var d=site(key);String k="claimed/"+p;d.putInt(k,d.getIntOr(k,0)|1<<i);setDirty();return true;}
}
