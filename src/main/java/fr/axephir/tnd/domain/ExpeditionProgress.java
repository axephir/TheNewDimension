package fr.axephir.tnd.domain;

import java.io.*;
import java.util.*;

/** Immutable bounded field notes. Only confirmed server actions may create this state. */
public record ExpeditionProgress(int biomes,Set<String> consoles,Set<String> harvests,int sentinels,
                                 boolean observatory,boolean gear,int claimed) {
    public ExpeditionProgress {
        consoles=bounded(consoles,3);harvests=bounded(harvests,6);
        if(biomes<0||biomes>63||sentinels<0||sentinels>3||claimed<0||claimed>255)throw new IllegalArgumentException("invalid expedition counters");
        for(var mission:Expedition.values())if((claimed&(1<<mission.ordinal()))!=0&&count(mission,biomes,consoles.size(),harvests.size(),sentinels,observatory,gear,claimed)<mission.goal)
            throw new IllegalArgumentException("claimed incomplete expedition");
    }
    private static Set<String> bounded(Set<String> set,int maximum){
        if(set.size()>maximum||set.stream().anyMatch(s->s==null||s.isEmpty()||s.length()>96))throw new IllegalArgumentException("invalid field notes");
        return Set.copyOf(set);
    }
    public static ExpeditionProgress empty(){return new ExpeditionProgress(0,Set.of(),Set.of(),0,false,false,0);}
    private static int count(Expedition e,int b,int c,int h,int s,boolean o,boolean g,int claims){return switch(e){
        case SURVEY,ATLAS->Math.min(e.goal,Integer.bitCount(b));case RESTORATION->c;case NIGHT_HARVEST->h;
        case SENTINELS->s;case OBSERVATORY->o?1:0;case GEAR->g?1:0;case CARTOGRAPHER->Integer.bitCount(claims&127);
    };}
    public int progress(Expedition e){return count(e,biomes,consoles.size(),harvests.size(),sentinels,observatory,gear,claimed);}
    public boolean claimed(Expedition e){return (claimed&(1<<e.ordinal()))!=0;}
    public boolean complete(Expedition e){return progress(e)>=e.goal;}
    public ExpeditionProgress discover(int index){if(index<0||index>5)throw new IllegalArgumentException("unknown biome");return new ExpeditionProgress(biomes|(1<<index),consoles,harvests,sentinels,observatory,gear,claimed);}
    private static Set<String> add(Set<String> old,String key,int maximum){if(old.size()>=maximum||old.contains(key))return old;var next=new HashSet<>(old);next.add(key);return next;}
    public ExpeditionProgress console(String key){return new ExpeditionProgress(biomes,add(consoles,key,3),harvests,sentinels,observatory,gear,claimed);}
    public ExpeditionProgress harvest(String key){return new ExpeditionProgress(biomes,consoles,add(harvests,key,6),sentinels,observatory,gear,claimed);}
    public ExpeditionProgress sentinel(){return new ExpeditionProgress(biomes,consoles,harvests,Math.min(3,sentinels+1),observatory,gear,claimed);}
    public ExpeditionProgress observe(){return new ExpeditionProgress(biomes,consoles,harvests,sentinels,true,gear,claimed);}
    public ExpeditionProgress equip(){return new ExpeditionProgress(biomes,consoles,harvests,sentinels,observatory,true,claimed);}
    public ExpeditionProgress claim(Expedition e){if(claimed(e)||!complete(e))throw new IllegalStateException("unavailable expedition reward");return new ExpeditionProgress(biomes,consoles,harvests,sentinels,observatory,gear,claimed|(1<<e.ordinal()));}
    public String encode(){
        try{var bytes=new ByteArrayOutputStream();try(var out=new DataOutputStream(bytes)){
            out.writeInt(1);out.writeInt(biomes);out.writeInt(sentinels);out.writeBoolean(observatory);out.writeBoolean(gear);out.writeInt(claimed);
            for(var set:List.of(consoles,harvests)){out.writeInt(set.size());for(var key:new TreeSet<>(set))out.writeUTF(key);}
        }return Base64.getEncoder().encodeToString(bytes.toByteArray());}catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static ExpeditionProgress decode(String encoded){
        if(encoded.length()>4096)throw new IllegalArgumentException("field notes too large");
        try(var in=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))){
            if(in.readInt()!=1)throw new IOException("unsupported field notes schema");
            int b=in.readInt(),s=in.readInt();boolean o=in.readBoolean(),g=in.readBoolean();int c=in.readInt();
            var notes=new ArrayList<Set<String>>();
            for(int maximum:new int[]{3,6}){int size=in.readInt();if(size<0||size>maximum)throw new IOException("too many samples");var set=new HashSet<String>();for(int i=0;i<size;i++)if(!set.add(in.readUTF()))throw new IOException("duplicate sample");notes.add(set);}
            if(in.read()!=-1)throw new IOException("trailing field notes");return new ExpeditionProgress(b,notes.get(0),notes.get(1),s,o,g,c);
        }catch(IOException|IllegalArgumentException e){throw new IllegalArgumentException("invalid field notes; preserve original player data",e);}
    }
}
