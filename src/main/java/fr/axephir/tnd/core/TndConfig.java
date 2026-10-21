package fr.axephir.tnd.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TndConfig {
    public static final ModConfigSpec SERVER;
    public static final ModConfigSpec.IntValue EXTRACTOR_TICKS;
    public static final ModConfigSpec.IntValue MAX_CATCHUP;
    public static final ModConfigSpec.DoubleValue XP_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue DEVELOPMENT_COMMANDS;

    static {
        var server = new ModConfigSpec.Builder();
        server.push("progression");
        XP_MULTIPLIER = server.defineInRange("experienceMultiplier", 1.0, 0.0, 100.0);
        server.pop();
        server.push("machines");
        EXTRACTOR_TICKS = server.comment("Ticks per resource; no real-time production while server is stopped.")
                .defineInRange("extractorTicksPerItem", 1_200, 20, 72_000);
        MAX_CATCHUP = server.comment("Maximum unloaded catch-up in server ticks (default six hours).")
                .defineInRange("maxCatchupTicks", 432_000, 0, 1_728_000);
        server.pop();
        DEVELOPMENT_COMMANDS = server.comment("Operator-only dimension access for development.")
                .define("developmentCommands", true);
        SERVER = server.build();
    }

    private TndConfig() {}
}
