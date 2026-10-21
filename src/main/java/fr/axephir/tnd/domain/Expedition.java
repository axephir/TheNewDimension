package fr.axephir.tnd.domain;

/** Fixed, finite objectives. Ordinals are persistent IDs; append, never reorder. */
public enum Expedition {
    SURVEY(3), ATLAS(6), RESTORATION(3), NIGHT_HARVEST(6), SENTINELS(3), OBSERVATORY(1), GEAR(1), CARTOGRAPHER(7);
    public final int goal;
    Expedition(int goal){this.goal=goal;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
}
