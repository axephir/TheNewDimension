package fr.axephir.tnd.domain;

import java.util.Arrays;
import java.util.List;

/** Saved names are stable. The original five foundations remain compatible with earlier profiles. */
public enum Talent {
    EFFICIENCY(5, 1, null),
    YIELD(5, 2, EFFICIENCY),
    INSIGHT(1, 5, EFFICIENCY),
    PRECISION(5, 3, INSIGHT),
    SYNERGY(5, 4, YIELD),
    MINING_PROSPECTOR(Mastery.MINING, 25, INSIGHT),
    MINING_SEISMIC(Mastery.MINING, 50, MINING_PROSPECTOR),
    LOGGING_FORESTER(Mastery.LOGGING, 25, INSIGHT),
    LOGGING_GROVE(Mastery.LOGGING, 50, LOGGING_FORESTER),
    FARMING_SEEDKEEPER(Mastery.FARMING, 25, INSIGHT),
    FARMING_VERDANT(Mastery.FARMING, 50, FARMING_SEEDKEEPER),
    FISHING_DIVER(Mastery.FISHING, 25, INSIGHT),
    FISHING_CURRENT(Mastery.FISHING, 50, FISHING_DIVER),
    HUNTING_TRACKER(Mastery.HUNTING, 25, INSIGHT),
    HUNTING_FOCUS(Mastery.HUNTING, 50, HUNTING_TRACKER),
    BREEDING_CARETAKER(Mastery.BREEDING, 25, INSIGHT),
    BREEDING_HERD(Mastery.BREEDING, 50, BREEDING_CARETAKER),
    MELEE_DUELIST(Mastery.MELEE, 25, INSIGHT),
    MELEE_REPULSE(Mastery.MELEE, 50, MELEE_DUELIST),
    RANGED_MARKSMAN(Mastery.RANGED, 25, INSIGHT),
    RANGED_PIN(Mastery.RANGED, 50, RANGED_MARKSMAN),
    DEFENSE_BULWARK(Mastery.DEFENSE, 25, INSIGHT),
    DEFENSE_AEGIS(Mastery.DEFENSE, 50, DEFENSE_BULWARK),
    CRAFTING_ARTISAN(Mastery.CRAFTING, 25, INSIGHT),
    CRAFTING_INSPIRATION(Mastery.CRAFTING, 50, CRAFTING_ARTISAN),
    BUILDING_ARCHITECT(Mastery.BUILDING, 25, INSIGHT),
    BUILDING_SCAFFOLD(Mastery.BUILDING, 50, BUILDING_ARCHITECT),
    EXPLORATION_WAYFARER(Mastery.EXPLORATION, 25, INSIGHT),
    EXPLORATION_RETURN(Mastery.EXPLORATION, 50, EXPLORATION_WAYFARER),
    ALCHEMY_DISTILLER(Mastery.ALCHEMY, 25, INSIGHT),
    ALCHEMY_PURIFY(Mastery.ALCHEMY, 50, ALCHEMY_DISTILLER),
    ENGINEERING_TUNER(Mastery.ENGINEERING, 25, INSIGHT),
    ENGINEERING_OVERCLOCK(Mastery.ENGINEERING, 50, ENGINEERING_TUNER),
    ANCIENT_TECH_INTERPRETER(Mastery.ANCIENT_TECH, 25, INSIGHT),
    ANCIENT_TECH_RESONANCE(Mastery.ANCIENT_TECH, 50, ANCIENT_TECH_INTERPRETER);

    private final int maxRank, cost, minimumLevel;
    private final Talent prerequisite;
    private final Mastery owner;
    Talent(int maxRank,int cost,Talent prerequisite) {
        this.maxRank=maxRank;this.cost=cost;this.prerequisite=prerequisite;minimumLevel=1;owner=null;
    }
    Talent(Mastery owner,int level,Talent prerequisite) {
        maxRank=1;cost=12;this.owner=owner;minimumLevel=level;this.prerequisite=prerequisite;
    }
    public int maxRank(){return maxRank;}
    public int cost(){return cost;}
    public int minimumLevel(){return minimumLevel;}
    public Talent prerequisite(){return prerequisite;}
    public boolean availableFor(Mastery mastery){return owner==null||owner==mastery;}
    public boolean specialized(){return owner!=null;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public static List<Talent> available(Mastery mastery){return Arrays.stream(values()).filter(t->t.availableFor(mastery)).toList();}
    public static Talent passive(Mastery mastery){return available(mastery).get(5);}
    public static Talent ability(Mastery mastery){return available(mastery).get(6);}
}
