package fr.axephir.tnd.domain;

/** Shared numbers used by server effects, UI descriptions and regression checks. */
public final class MasteryRules {
    public static Mastery partner(Mastery m) {
        return switch(m) {
            case MINING,CRAFTING->Mastery.ENGINEERING;case ENGINEERING->Mastery.MINING;
            case LOGGING->Mastery.BUILDING;case BUILDING->Mastery.LOGGING;
            case FARMING->Mastery.BREEDING;case BREEDING->Mastery.FARMING;
            case FISHING->Mastery.EXPLORATION;case EXPLORATION->Mastery.FISHING;
            case HUNTING->Mastery.RANGED;case RANGED->Mastery.HUNTING;
            case MELEE->Mastery.DEFENSE;case DEFENSE->Mastery.MELEE;
            case ALCHEMY->Mastery.ANCIENT_TECH;case ANCIENT_TECH->Mastery.ALCHEMY;
        };
    }
    public static boolean resourceYield(Mastery m) {
        return m==Mastery.MINING||m==Mastery.LOGGING||m==Mastery.FARMING||m==Mastery.FISHING||m==Mastery.HUNTING;
    }
    public static double xpFactor(Profile profile,Mastery m) {
        var p=profile.get(m);
        return 1+Experience.prestigeBonus(p.level())
                +(resourceYield(m)||m==Mastery.ENGINEERING?0:p.rank(Talent.YIELD)*0.02)
                +(m==Mastery.CRAFTING?p.rank(Talent.EFFICIENCY)*0.05:0)
                +Math.min(p.rank(Talent.SYNERGY),profile.get(partner(m)).rank(Talent.SYNERGY))*0.02;
    }
    public static int radius(MasteryProgress p){return 6+p.rank(Talent.PRECISION)*2+4*(p.rank(Talent.MINING_PROSPECTOR)+p.rank(Talent.HUNTING_TRACKER)+p.rank(Talent.ANCIENT_TECH_INTERPRETER));}
    private MasteryRules() {}
}
