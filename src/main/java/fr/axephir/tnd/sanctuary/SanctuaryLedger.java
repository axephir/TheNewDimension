package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;

/** Durable shared route with personal reward masks. Only real objective completion advances a station. */
public final class SanctuaryLedger extends SavedData {
    private final Map<String,CompoundTag> records;
    public static final Codec<SanctuaryLedger> CODEC=Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).xmap(SanctuaryLedger::new,l->l.records);
    public static final SavedDataType<SanctuaryLedger> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("tnd","observatory_routes"),()->new SanctuaryLedger(Map.of()),CODEC);
    private SanctuaryLedger(Map<String,CompoundTag> data){records=new HashMap<>();data.forEach((k,v)->records.put(k,v.copy()));}
    public static SanctuaryLedger get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    private CompoundTag region(long key){return records.computeIfAbsent("site/"+key,k->new CompoundTag());}
    public long mask(long key){var r=records.get("site/"+key);if(r==null)return 0;long raw=r.getLongOr("complete",0)&SanctuaryRoute.FULL;return (1L<<SanctuaryRoute.reached(raw))-1;}
    public boolean finish(long key,int index){long old=mask(key),next=SanctuaryRoute.complete(old,index);if(next==old)return false;region(key).putLong("complete",next);setDirty();return true;}
    public void eligible(long key,UUID player,int act){if(act>=0&&act<6){var r=region(key);String k="eligible/"+player;r.putInt(k,r.getIntOr(k,0)|(1<<act));setDirty();}}
    public boolean canClaim(long key,UUID player,int act){if(act<0||act>=6||!SanctuaryRoute.done(mask(key),act*8+7))return false;var r=records.get("site/"+key);return r!=null&&(r.getIntOr("eligible/"+player,0)&(1<<act))!=0&&(r.getIntOr("reward/"+player,0)&(1<<act))==0;}
    public boolean claim(long key,UUID player,int act){if(!canClaim(key,player,act))return false;var r=region(key);String name="reward/"+player;r.putInt(name,r.getIntOr(name,0)|1<<act);setDirty();return true;}
    public int discoveries(long key,UUID player){var r=records.get("site/"+key);return r==null?0:r.getIntOr("discoveries/"+player,0)&7;}
    public boolean discover(long key,UUID player,int secret){if(secret<0||secret>2||(discoveries(key,player)&(1<<secret))!=0)return false;region(key).putInt("discoveries/"+player,discoveries(key,player)|(1<<secret));setDirty();return true;}
    public int rewards(long key,UUID player){var r=records.get("site/"+key);return r==null?0:r.getIntOr("reward/"+player,0)&63;}
}
