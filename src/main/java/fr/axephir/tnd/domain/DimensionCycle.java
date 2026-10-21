package fr.axephir.tnd.domain;

/** One real-time cycle is 48,000 server ticks at 20 TPS. */
public record DimensionCycle(long ticksPerCycle) {
    public DimensionCycle {
        if (ticksPerCycle < 2 || ticksPerCycle % 2 != 0) throw new IllegalArgumentException("even cycle required");
    }
    public boolean isDay(long tick) { return Math.floorMod(tick, ticksPerCycle) < ticksPerCycle / 2; }
    public long vanillaVisualTime(long tick) {
        return (long) (Math.floorMod(tick, ticksPerCycle) * (24_000.0 / ticksPerCycle));
    }
}
