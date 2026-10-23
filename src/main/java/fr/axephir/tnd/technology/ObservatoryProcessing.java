package fr.axephir.tnd.technology;
import fr.axephir.tnd.core.AwakeningContent;
import fr.axephir.tnd.sanctuary.SanctuaryContent;
import net.minecraft.world.item.*;
/** Explicit finite transformations: all catalysts, input counts and FE are committed with the output. */
public final class ObservatoryProcessing {
    public record Recipe(Item first,int firstCount,Item second,int secondCount,Item output,int count,int energy,int ticks){
        public boolean matches(ItemStack a,ItemStack b){return a.is(first)&&a.getCount()>=firstCount&&b.is(second)&&b.getCount()>=secondCount;}
    }
    public static Recipe recipe(Industry.Kind k){return switch(k){
        case REFINERY->new Recipe(AwakeningContent.RAW_AURALITE.get(),1,SanctuaryContent.SHARD.get(),1,SanctuaryContent.DUST.get(),3,6000,120);
        case CRUCIBLE->new Recipe(SanctuaryContent.DUST.get(),2,AwakeningContent.UMBRA_ALLOY.get(),1,SanctuaryContent.INGOT.get(),2,12000,180);
        case ASSEMBLER->new Recipe(SanctuaryContent.INGOT.get(),2,Industry.CIRCUIT.get(),1,SanctuaryContent.CIRCUIT.get(),2,24000,240);
        default->null;
    };}
    private ObservatoryProcessing(){}
}
