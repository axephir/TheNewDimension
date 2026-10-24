package fr.axephir.tnd.deepindustry;
import net.minecraft.world.item.*;
import fr.axephir.tnd.sanctuary.SanctuaryContent;
/** Explicit audited conversions: no tag-driven multiplication, no craft XP and no fuel feedback. */
public final class DeepRecipes {
    public record Recipe(Item first,int a,Item second,int b,Item result,int count,int energy,int ticks,int wear){public boolean matches(ItemStack x,ItemStack y){return x.is(first)&&x.getCount()>=a&&y.is(second)&&y.getCount()>=b;}}
    private static Item i(String n){return DeepIndustry.item(n);}
    public static java.util.List<Recipe> recipes(DeepIndustry.Kind k){return switch(k){
        case CENTRIFUGE->java.util.List.of(new Recipe(i("raw_ferronyte"),1,Items.SAND,1,i("ferronyte_grit"),3,6000,80,4),new Recipe(i("raw_thalassite"),1,Items.PRISMARINE_SHARD,1,i("thalassite_grit"),3,10000,100,6));
        case KILN->java.util.List.of(new Recipe(i("ferronyte_grit"),4,Items.COAL,1,i("ferronyte_ingot"),4,6000,60,4),new Recipe(i("thalassite_grit"),4,Items.QUARTZ,1,i("thalassite_ingot"),4,10000,80,6));
        case REPLICATOR->java.util.List.of(new Recipe(i("raw_asterium"),1,i("mineral_matrix"),1,i("asterium_ingot"),3,96000,320,40));
        case ASSEMBLER->java.util.List.of(new Recipe(Items.NETHERITE_SCRAP,1,SanctuaryContent.DUST.get(),4,i("mineral_matrix"),4,16000,120,10),new Recipe(i("ferronyte_ingot"),2,Items.QUARTZ,4,i("maintenance_kit"),4,6000,80,4),new Recipe(i("thalassite_ingot"),1,Items.COAL_BLOCK,2,i("resonant_fuel_cell"),1,24000,160,12));
        default->java.util.List.of();};}
    private DeepRecipes(){}
}
