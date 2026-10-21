package fr.axephir.tnd.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AtmosphereOptions {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue VOLUME;
    public static final ModConfigSpec.BooleanValue HUD,ANIMATIONS;
    static{
        var b=new ModConfigSpec.Builder();b.push("atmosphere");
        ENABLED=b.comment("Additional night/indoor layers and distant cues in Vestige. Biome loops use Minecraft's Ambient slider.").define("additionalLayers",true);
        VOLUME=b.comment("Gain for the additional local layers and spatial cues. All ambience also follows Minecraft's Ambient slider.").defineInRange("additionalVolume",.85,0,1);
        b.pop();b.push("interface");
        HUD=b.comment("Biome discovery cards and health/attack feedback for targeted Vestige creatures.").define("immersiveHud",true);
        ANIMATIONS=b.comment("Subtle animated panel accents and eased progress bars. Disable for reduced motion.").define("animations",true);
        b.pop();SPEC=b.build();
    }
    private AtmosphereOptions(){}
}
