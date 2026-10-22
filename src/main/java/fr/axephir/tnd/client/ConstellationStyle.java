package fr.axephir.tnd.client;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.*;

/** Small, deterministic pixel shapes: no background texture, particle entities or render targets. */
final class ConstellationStyle {
    static final int TEXT=0xffedf3fb,MUTED=0xff9aaec6,DIM=0xff63728b,CYAN=0xff8ee8df,GOLD=0xfff0cc86,LOCKED=0xff59647c;
    static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,0xff27334d);g.fill(x+1,y+1,x+w-1,y+h-1,0xff0d1424);
        g.fill(x+6,y,x+24,y+1,CYAN);g.fill(x+w-24,y+h-1,x+w-6,y+h,GOLD);
    }
    static void plate(GuiGraphicsExtractor g,int x,int y,int w,int h,int edge,int inside){
        g.fill(x+3,y,x+w-3,y+h,edge);g.fill(x,y+3,x+w,y+h-3,edge);
        g.fill(x+4,y+1,x+w-4,y+h-1,inside);g.fill(x+1,y+4,x+w-1,y+h-4,inside);
    }
    static void space(GuiGraphicsExtractor g,int x,int y,int w,int h,double seconds){
        g.fillGradient(x,y,x+w,y+h,0xff10192e,0xff080d19);
        // A low-contrast nebula built from broad pixel bands. Text and nodes remain stationary.
        for(int i=0;i<18;i++){
            int yy=y+h*i/18,spread=(int)(w*(.14+.18*Math.sin(i*.18)));
            int xx=x+w*2/3-spread/2;
            g.fillGradient(xx,yy,xx+spread,Math.min(y+h,yy+h/18+1),0x10194571,0x08194571);
            int left=x+w/5-(int)(spread*.7);
            g.fillGradient(left,yy,left+spread,Math.min(y+h,yy+h/18+1),0x103f285e,0x083f285e);
        }
        int count=Math.min(150,45+w*h/1300);
        for(int i=0;i<count;i++){
            long hash=(i*1103515245L+12345)&0x7fffffff;
            int sx=x+3+(int)(hash%Math.max(1,w-6));int sy=y+3+(int)((hash/853)%Math.max(1,h-6));
            int alpha=60+(int)((Math.sin(seconds*.45+i*2.1)+1)*24);
            int color=(alpha<<24)|(i%4==0?0xb4a6ed:0xb8d4e9);
            g.fill(sx,sy,sx+1,sy+1,color);
            if(i%17==0){g.fill(sx-1,sy,sx+2,sy+1,color);g.fill(sx,sy-1,sx+1,sy+2,color);}
        }
    }
    static void segment(GuiGraphicsExtractor g,int x1,int y1,int x2,int y2,int color){
        int left=Math.min(x1,x2),top=Math.min(y1,y2),right=Math.max(x1,x2),bottom=Math.max(y1,y2);
        g.fill(left-1,top-1,right+2,bottom+2,0xff080e1b);g.fill(left,top,right+1,bottom+1,color);
    }
    static ItemStack mastery(Mastery m){return new ItemStack(switch(m){
        case MINING->Items.IRON_PICKAXE;case LOGGING->Items.IRON_AXE;case FARMING->Items.WHEAT;
        case FISHING->Items.FISHING_ROD;case HUNTING->Items.BONE;case BREEDING->Items.WHEAT_SEEDS;
        case MELEE->Items.IRON_SWORD;case RANGED->Items.BOW;case DEFENSE->Items.SHIELD;
        case CRAFTING->Items.CRAFTING_TABLE;case BUILDING->Items.BRICKS;case EXPLORATION->Items.COMPASS;
        case ALCHEMY->Items.BREWING_STAND;case ENGINEERING->Items.REDSTONE;case ANCIENT_TECH->ModContent.RELIC_FRAGMENT.get();
    });}
    static ItemStack talent(Mastery m,Talent t){
        if(t==Talent.EFFICIENCY)return mastery(m);
        if(t==Talent.YIELD)return new ItemStack(switch(m){case MINING->Items.DIAMOND;case LOGGING->Items.OAK_LOG;case FARMING->Items.CARROT;case FISHING->Items.COD;case HUNTING->Items.LEATHER;default->Items.EXPERIENCE_BOTTLE;});
        if(t==Talent.INSIGHT)return new ItemStack(Items.ENDER_EYE);
        if(t==Talent.PRECISION)return new ItemStack(Items.SPYGLASS);
        if(t==Talent.SYNERGY)return new ItemStack(Items.AMETHYST_SHARD);
        return new ItemStack(switch(t){
            case MINING_PROSPECTOR->Items.RAW_GOLD;case MINING_SEISMIC->Items.GOLDEN_PICKAXE;
            case LOGGING_FORESTER->Items.OAK_SAPLING;case LOGGING_GROVE->Items.GOLDEN_AXE;
            case FARMING_SEEDKEEPER->Items.WHEAT_SEEDS;case FARMING_VERDANT->Items.BONE_MEAL;
            case FISHING_DIVER->Items.TURTLE_HELMET;case FISHING_CURRENT->Items.HEART_OF_THE_SEA;
            case HUNTING_TRACKER->Items.COMPASS;case HUNTING_FOCUS->Items.FEATHER;
            case BREEDING_CARETAKER->Items.GOLDEN_CARROT;case BREEDING_HERD->Items.GOLDEN_APPLE;
            case MELEE_DUELIST->Items.DIAMOND_SWORD;case MELEE_REPULSE->Items.WIND_CHARGE;
            case RANGED_MARKSMAN->Items.TARGET;case RANGED_PIN->Items.SPECTRAL_ARROW;
            case DEFENSE_BULWARK->Items.IRON_CHESTPLATE;case DEFENSE_AEGIS->Items.TOTEM_OF_UNDYING;
            case CRAFTING_ARTISAN->Items.ANVIL;case CRAFTING_INSPIRATION->Items.GRINDSTONE;
            case BUILDING_ARCHITECT->Items.SCAFFOLDING;case BUILDING_SCAFFOLD->Items.FEATHER;
            case EXPLORATION_WAYFARER->Items.LEATHER_BOOTS;case EXPLORATION_RETURN->Items.RABBIT_FOOT;
            case ALCHEMY_DISTILLER->Items.GLOWSTONE_DUST;case ALCHEMY_PURIFY->Items.HONEY_BOTTLE;
            case ENGINEERING_TUNER->Items.COMPARATOR;case ENGINEERING_OVERCLOCK->Items.REPEATER;
            case ANCIENT_TECH_INTERPRETER->ModContent.RELIC_FRAGMENT.get();case ANCIENT_TECH_RESONANCE->ModContent.COMPOSITE_CRYSTAL.get();
            default->throw new IllegalArgumentException("Unexpected talent icon: "+t);
        });
    }
    private ConstellationStyle(){}
}
