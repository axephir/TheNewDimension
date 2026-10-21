package fr.axephir.tnd.world;

import fr.axephir.tnd.domain.DimensionCycle;
import net.minecraft.world.level.Level;

/** Read the dimension's real clock, including /time changes and clock-rate changes. */
public final class VestigeTime {
    private static final DimensionCycle CYCLE=new DimensionCycle(48_000);
    public static boolean isNight(Level level) {
        return level.dimension().equals(DevelopmentCommands.DIMENSION)&&!CYCLE.isDay(level.getDefaultClockTime());
    }
    private VestigeTime(){}
}
