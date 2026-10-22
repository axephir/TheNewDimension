package fr.axephir.tnd.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TndConfig {
    public static final ModConfigSpec SERVER;
    public static final ModConfigSpec.IntValue EXTRACTOR_TICKS;
    public static final ModConfigSpec.IntValue MAX_CATCHUP;
    public static final ModConfigSpec.DoubleValue XP_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue DEVELOPMENT_COMMANDS;
    public static final ModConfigSpec.IntValue ACTIVE_ANCHORS,STORY_RADIUS;
    public static final ModConfigSpec.BooleanValue RARE_ANOMALIES;

    static {
        var server = new ModConfigSpec.Builder();
        server.push("progression");
        XP_MULTIPLIER = server.defineInRange("experienceMultiplier", 1.0, 0.0, 100.0);
        server.pop();
        server.push("machines");
        ACTIVE_ANCHORS=server.comment("Maximum powered 3x3-chunk factory districts across this server. Two per online owner.").defineInRange("activeFactoryAnchors",16,0,64);
        EXTRACTOR_TICKS = server.comment("Ticks per resource; no real-time production while server is stopped.")
                .defineInRange("extractorTicksPerItem", 1_200, 20, 72_000);
        MAX_CATCHUP = server.comment("Maximum unloaded catch-up in server ticks (default six hours).")
                .defineInRange("maxCatchupTicks", 432_000, 0, 1_728_000);
        server.pop();
        server.push("narrative");STORY_RADIUS=server.comment("Radius in chunks of the isolated personal final projection; source Overworld never modified.").defineInRange("projectionRadiusChunks",12,1,12);RARE_ANOMALIES=server.define("rarePerceptualEvents",true);server.pop();
        DEVELOPMENT_COMMANDS = server.comment("Operator-only dimension access for development.")
                .define("developmentCommands", true);
        SERVER = server.build();
    }

    private TndConfig() {}
}
