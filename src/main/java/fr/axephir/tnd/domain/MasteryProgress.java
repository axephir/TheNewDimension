package fr.axephir.tnd.domain;

import java.util.EnumMap;
import java.util.Map;

public record MasteryProgress(long experience, Map<Talent, Integer> talents) {
    public MasteryProgress {
        if (experience < 0) throw new IllegalArgumentException("negative experience");
        talents = Map.copyOf(talents);
        int spent = 0;
        for (var entry : talents.entrySet()) {
            int rank = entry.getValue();
            if (rank < 1 || rank > entry.getKey().maxRank())
                throw new IllegalArgumentException("invalid talent rank");
            Talent prerequisite = entry.getKey().prerequisite();
            if (prerequisite != null && talents.getOrDefault(prerequisite, 0) == 0)
                throw new IllegalArgumentException("missing prerequisite");
            spent += rank * entry.getKey().cost();
        }
        if (spent > Experience.earnedPoints(Experience.level(experience)))
            throw new IllegalArgumentException("overspent talent points");
    }

    public static MasteryProgress empty() { return new MasteryProgress(0, Map.of()); }
    public int level() { return Experience.level(experience); }
    public int rank(Talent talent) { return talents.getOrDefault(talent, 0); }
    public int points() {
        return Experience.earnedPoints(level()) - talents.entrySet().stream()
                .mapToInt(e -> e.getKey().cost() * e.getValue()).sum();
    }

    public MasteryProgress award(long amount) {
        return new MasteryProgress(Experience.add(experience, amount), talents);
    }

    public MasteryProgress learn(Talent talent) {
        if (points() < talent.cost() || rank(talent) >= talent.maxRank()
                || (talent.prerequisite() != null && rank(talent.prerequisite()) == 0))
            throw new IllegalStateException("talent unavailable");
        var result = new EnumMap<Talent, Integer>(Talent.class);
        result.putAll(talents);
        result.put(talent, rank(talent) + 1);
        return new MasteryProgress(experience, result);
    }
}
