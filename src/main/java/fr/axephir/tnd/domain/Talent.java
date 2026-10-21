package fr.axephir.tnd.domain;

/** Persistent talent identities; effects and names depend on the selected mastery. */
public enum Talent {
    EFFICIENCY(5, 1, null),
    YIELD(5, 2, EFFICIENCY),
    INSIGHT(1, 5, EFFICIENCY),
    PRECISION(5, 3, INSIGHT),
    SYNERGY(5, 4, YIELD);

    private final int maxRank;
    private final int cost;
    private final Talent prerequisite;

    Talent(int maxRank, int cost, Talent prerequisite) {
        this.maxRank = maxRank;
        this.cost = cost;
        this.prerequisite = prerequisite;
    }

    public int maxRank() { return maxRank; }
    public int cost() { return cost; }
    public Talent prerequisite() { return prerequisite; }
}
