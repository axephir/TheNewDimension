package fr.axephir.tnd.domain;

import java.io.*;
import java.util.*;

/** Bounded server-only history for rewards, discovery and anti-repeat budgets. */
public record ActivityLedger(Map<String, Entry> entries) {
    public static final int CAPACITY=2048;
    public record Entry(long resetsAt,int count,long touched) {}
    public record Decision(boolean allowed,ActivityLedger ledger) {}
    public ActivityLedger {
        entries=Map.copyOf(entries);
        if(entries.size()>CAPACITY)throw new IllegalArgumentException("ledger too large");
        entries.forEach((key,e)->{
            if(key.isEmpty()||key.length()>192||e.resetsAt()<0||e.count()<1||e.touched()<0)
                throw new IllegalArgumentException("invalid activity record");
        });
    }
    public static ActivityLedger empty(){return new ActivityLedger(Map.of());}
    public Decision claim(String key,long now,long window,int limit) {
        if(now<0||window<1||limit<1)throw new IllegalArgumentException("invalid reward budget");
        Entry old=entries.get(key);
        if(old!=null&&now<old.resetsAt()&&old.count()>=limit)return new Decision(false,this);
        var next=new HashMap<>(entries);
        next.entrySet().removeIf(e->e.getValue().resetsAt()<=now);
        if(!next.containsKey(key)&&next.size()>=CAPACITY) {
            String oldest=next.entrySet().stream().min(Comparator
                    .<Map.Entry<String,Entry>>comparingLong(e->e.getValue().touched()).thenComparing(Map.Entry::getKey)).orElseThrow().getKey();
            next.remove(oldest);
        }
        long until=window>Long.MAX_VALUE-now?Long.MAX_VALUE:now+window;
        next.put(key,old!=null&&old.resetsAt()>now?new Entry(old.resetsAt(),old.count()+1,now):new Entry(until,1,now));
        return new Decision(true,new ActivityLedger(next));
    }
    public String encode() {
        try {
            var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);
            out.writeInt(1);out.writeInt(entries.size());
            for(var e:new TreeMap<>(entries).entrySet()) {
                out.writeUTF(e.getKey());out.writeLong(e.getValue().resetsAt());out.writeInt(e.getValue().count());out.writeLong(e.getValue().touched());
            }
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static ActivityLedger decode(String value) {
        if(value.length()>1_600_000)throw new IllegalArgumentException("ledger too large");
        try(var in=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(value)))) {
            if(in.readInt()!=1)throw new IOException("unknown activity schema");
            int count=in.readInt();if(count<0||count>CAPACITY)throw new IOException("invalid activity count");
            Map<String,Entry> map=new HashMap<>();
            for(int i=0;i<count;i++)if(map.put(in.readUTF(),new Entry(in.readLong(),in.readInt(),in.readLong()))!=null)throw new IOException("duplicate record");
            if(in.read()!=-1)throw new IOException("trailing activity data");
            return new ActivityLedger(map);
        }catch(IOException|IllegalArgumentException e){throw new IllegalArgumentException("invalid activity ledger; preserve player data",e);}
    }
}
