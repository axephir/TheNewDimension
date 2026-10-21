package fr.axephir.tnd.technology;

import net.minecraft.world.item.*;

public enum ExtractionMode {
    IRON(Items.RAW_IRON,1),COPPER(Items.RAW_COPPER,1),GOLD(Items.RAW_GOLD,3),REDSTONE(Items.REDSTONE,2),DIAMOND(Items.DIAMOND,8);
    public final Item output;
    public final int timeFactor;
    ExtractionMode(Item output,int timeFactor){this.output=output;this.timeFactor=timeFactor;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
}
