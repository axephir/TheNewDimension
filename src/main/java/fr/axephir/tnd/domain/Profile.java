package fr.axephir.tnd.domain;

import java.io.*;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/** Immutable, versioned player state; malformed saves are rejected, never reset silently. */
public record Profile(Map<Mastery, MasteryProgress> masteries) {
    private static final int SCHEMA = 1;

    public Profile {
        masteries = Map.copyOf(masteries);
        masteries.forEach((mastery,progress)->progress.talents().keySet().forEach(talent->{
            if(!talent.availableFor(mastery))throw new IllegalArgumentException("talent belongs to another mastery");
        }));
    }
    public static Profile empty() { return new Profile(Map.of()); }
    public MasteryProgress get(Mastery mastery) {
        return masteries.getOrDefault(mastery, MasteryProgress.empty());
    }
    public Profile with(Mastery mastery, MasteryProgress value) {
        var map = new EnumMap<Mastery, MasteryProgress>(Mastery.class);
        map.putAll(masteries);
        map.put(mastery, value);
        return new Profile(map);
    }
    public Profile award(Mastery mastery, long amount) { return with(mastery, get(mastery).award(amount)); }

    public String encode() {
        try {
            var bytes = new ByteArrayOutputStream();
            try (var out = new DataOutputStream(bytes)) {
                out.writeInt(SCHEMA);
                out.writeInt(masteries.size());
                // Enum order makes identical profiles produce byte-identical saves.
                for (var mastery : Mastery.values()) {
                    if (!masteries.containsKey(mastery)) continue;
                    var progress = get(mastery);
                    out.writeUTF(mastery.name());
                    out.writeLong(progress.experience());
                    out.writeInt(progress.talents().size());
                    for (var talent : Talent.values()) {
                        if (progress.rank(talent) == 0) continue;
                        out.writeUTF(talent.name());
                        out.writeInt(progress.rank(talent));
                    }
                }
            }
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException impossible) { throw new UncheckedIOException(impossible); }
    }

    public static Profile decode(String encoded) {
        if (encoded.length() > 16_384) throw new IllegalArgumentException("profile too large");
        try (var in = new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            if (in.readInt() != SCHEMA) throw new IOException("unsupported profile schema");
            int count = in.readInt();
            if (count < 0 || count > Mastery.values().length) throw new IOException("invalid mastery count");
            var map = new EnumMap<Mastery, MasteryProgress>(Mastery.class);
            for (int i = 0; i < count; i++) {
                var mastery = Mastery.valueOf(in.readUTF());
                long xp = in.readLong();
                int ranks = in.readInt();
                if (ranks < 0 || ranks > Talent.values().length) throw new IOException("invalid talent count");
                var talents = new EnumMap<Talent, Integer>(Talent.class);
                for (int j = 0; j < ranks; j++)
                    if (talents.put(Talent.valueOf(in.readUTF()), in.readInt()) != null)
                        throw new IOException("duplicate talent");
                if (map.put(mastery, new MasteryProgress(xp, talents)) != null)
                    throw new IOException("duplicate mastery");
            }
            if (in.read() != -1) throw new IOException("trailing profile data");
            return new Profile(map);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid profile; preserve original player data for recovery", e);
        }
    }
}
