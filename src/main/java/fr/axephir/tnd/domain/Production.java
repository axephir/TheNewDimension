package fr.axephir.tnd.domain;

/** Catch-up uses server game ticks, never wall-clock time, and never forces chunks to load. */
public final class Production {
    public record State(long lastTick, long progressTicks, int fuel, int output) {
        public State {
            if (lastTick < 0 || progressTicks < 0 || fuel < 0 || output < 0)
                throw new IllegalArgumentException("negative machine state");
        }
    }
    public record Rules(int ticksPerItem, int fuelPerItem, int capacity, long maxCatchupTicks) {
        public Rules {
            if (ticksPerItem < 1 || fuelPerItem < 1 || capacity < 1 || maxCatchupTicks < 0)
                throw new IllegalArgumentException("invalid production rules");
        }
    }
    private Production() {}

    public static State advance(State state, Rules rules, long now) {
        if (now < 0) throw new IllegalArgumentException("negative clock");
        if (now < state.lastTick()) return new State(now, 0, state.fuel(), state.output());
        long elapsed = Math.min(now - state.lastTick(), rules.maxCatchupTicks());
        if (state.output() >= rules.capacity() || state.fuel() < rules.fuelPerItem())
            return new State(now, 0, state.fuel(), state.output());
        long progress = Experience.add(elapsed, Math.min(state.progressTicks(), rules.ticksPerItem() - 1));
        long requested = progress / rules.ticksPerItem();
        int available = Math.min(rules.capacity() - state.output(), state.fuel() / rules.fuelPerItem());
        int completed = (int) Math.min(requested, available);
        int fuel = state.fuel() - completed * rules.fuelPerItem();
        int output = state.output() + completed;
        // Discard backlog when blocked: inserting fuel later must not produce past work.
        long remainder = output == rules.capacity() || fuel < rules.fuelPerItem()
                ? 0 : progress % rules.ticksPerItem();
        return new State(now, remainder, fuel, output);
    }
}
