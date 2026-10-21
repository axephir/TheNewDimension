package fr.axephir.tnd.domain;

import java.util.Locale;

/** Stable save identifiers. Renaming an enum entry requires a save migration. */
public enum Mastery {
    MINING, LOGGING, FARMING, FISHING, HUNTING, BREEDING, MELEE, RANGED,
    DEFENSE, CRAFTING, BUILDING, EXPLORATION, ALCHEMY, ENGINEERING, ANCIENT_TECH;

    public String id() { return name().toLowerCase(Locale.ROOT); }
}
