package fr.axephir.tnd.domain;

import java.util.*;

/** Dependency-free regression runner; every assertion throws even without -ea. */
public final class CoreTests {
    private static int assertions;
    public static void main(String[] args) {
        progression(); profiles(); production(); cycle(); activities(); masteryRules(); expeditions(); sanctuary();
        System.out.println("PASS: " + assertions + " assertions (progression, saves, production, cycle, activity ledger, mastery rules, expeditions, sanctuary route)");
    }
    private static void check(boolean value, String reason) {
        assertions++;
        if (!value) throw new AssertionError(reason);
    }
    private static void rejects(Runnable action) {
        assertions++;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("expected rejection");
    }
    private static void expeditions(){
        var empty=ExpeditionProgress.empty();rejects(()->empty.claim(Expedition.SURVEY));
        check(ExpeditionProgress.decode(empty.encode()).equals(empty),"Empty notes round-trip for old players");
        var s=empty;
        for(int i=0;i<300;i++){
            s=s.discover(i%6).console("console"+(i%3)).harvest("bloom"+(i%6)).sentinel().observe().equip();
            check(ExpeditionProgress.decode(s.encode()).equals(s),"Field notes retain counters and unique locations");
            check(ExpeditionProgress.decode(s.encode()).encode().equals(s.encode()),"Field notes have stable encoding");
            check(s.consoles().size()<=3&&s.harvests().size()<=6&&s.sentinels()<=3,"All collected state is bounded");
        }
        var ready=s;rejects(()->ready.claim(Expedition.CARTOGRAPHER));
        for(var mission:Expedition.values())if(mission!=Expedition.CARTOGRAPHER){s=s.claim(mission);var claimed=s;rejects(()->claimed.claim(mission));}
        check(s.complete(Expedition.CARTOGRAPHER),"Final objective requires the previous seven claims");s=s.claim(Expedition.CARTOGRAPHER);
        check(s.claimed()==255&&ExpeditionProgress.decode(s.encode()).equals(s),"All claimed rewards persist");
        rejects(()->new ExpeditionProgress(64,Set.of(),Set.of(),0,false,false,0));
        rejects(()->new ExpeditionProgress(0,Set.of(),Set.of(),4,false,false,0));
        rejects(()->new ExpeditionProgress(0,Set.of(),Set.of(),0,false,false,1));
        rejects(()->ExpeditionProgress.decode("bad encoded notes!"));rejects(()->ExpeditionProgress.decode("a".repeat(4097)));
        var raw=Base64.getDecoder().decode(s.encode());raw[3]=2;rejects(()->ExpeditionProgress.decode(Base64.getEncoder().encodeToString(raw)));
        rejects(()->empty.console("x".repeat(97)));rejects(()->empty.discover(-1));rejects(()->empty.discover(6));
    }
    private static void sanctuary(){
        long mask=0;var visited=new HashSet<String>();
        for(int i=0;i<48;i++){int x=SanctuaryRoute.x(i),z=SanctuaryRoute.z(i);check(visited.add(x+":"+z),"Every chamber has a unique position");check(SanctuaryRoute.indexAt(x,z)==i,"Negative local coordinates map back to the correct station");check(SanctuaryRoute.available(mask,i),"The next objective is available");
            for(int n=i+1;n<48;n++)check(!SanctuaryRoute.available(mask,n),"No route can skip unseen chambers");
            if(i>0)check(Math.abs(x-SanctuaryRoute.x(i-1))+Math.abs(z-SanctuaryRoute.z(i-1))==128,"One straight main route joins each pair of chambers");
            mask=SanctuaryRoute.complete(mask,i);check(SanctuaryRoute.reached(mask)==i+1,"Reached count follows the contiguous completed prefix");check(SanctuaryRoute.complete(mask,i)==mask,"Completion is idempotent");}
        check(mask==SanctuaryRoute.FULL,"The route uses exactly 48 persistent bits");check(!SanctuaryRoute.available(1L<<6,7),"A corrupt isolated predecessor cannot bypass the route");
        for(int x=-100000;x<100000;x+=997){var site=SanctuaryRoute.nearest(x,-x);check(site.x()==site.gridX()*6144+3072&&site.z()==site.gridZ()*6144+3072,"Signed grid placement has stable boundaries");check(site.contains(site.x(),site.z()),"Every site contains its own origin");check(!site.contains(site.x()+641,site.z()),"The reserved footprint has exact bounds");}
        for(int act=0;act<6;act++){int station=act*8+2;boolean reachable=false;int[] target=SanctuaryPuzzles.target(station);
            for(int code=0;code<256;code++){int[] state=new int[4];int digits=code;for(int d=0;d<4;d++){int count=digits%4;digits/=4;for(int n=0;n<count;n++)state=SanctuaryPuzzles.turn(state,station,d);}if(Arrays.equals(state,target))reachable=true;}
            check(reachable,"Each coupled dial target is reachable without guessing an impossible state");
            int[] state={0,1,2,3},original=state.clone();for(int n=0;n<4;n++)state=SanctuaryPuzzles.turn(state,station,2);check(Arrays.equals(state,original),"Every dial mechanism can be reversed in four turns");}
        rejects(()->SanctuaryPuzzles.turn(new int[]{0,0,0},2,0));rejects(()->SanctuaryPuzzles.turn(new int[]{0,0,4,0},2,0));
        rejects(()->SanctuaryRoute.x(-1));rejects(()->SanctuaryRoute.z(48));
    }
    private static void progression() {
        check(Mastery.values().length == 15, "fifteen independent masteries");
        check(Experience.level(0) == 1, "initial level");
        for (int level = 2; level < 10_000; level++) {
            long threshold = Experience.threshold(level);
            check(Experience.level(threshold - 1) == level - 1, "lower boundary " + level);
            check(Experience.level(threshold) == level, "exact boundary " + level);
        }
        check(Experience.earnedPoints(100) == 99, "main progression");
        check(Experience.earnedPoints(109) == 99, "prestige interval");
        check(Experience.earnedPoints(110) == 100, "prestige point");
        check(Experience.level(Long.MAX_VALUE) > 1_000_000, "no level 100 cap");
        check(Experience.add(Long.MAX_VALUE - 2, 10) == Long.MAX_VALUE, "overflow safe XP");
        check(Experience.prestigeBonus(1_000_000) < 0.05, "bounded prestige");
        rejects(() -> Experience.add(1, -1));
        rejects(() -> Experience.level(-1));
        var p = MasteryProgress.empty();
        rejects(() -> MasteryProgress.empty().learn(Talent.EFFICIENCY));
        p = p.award(Experience.threshold(100));
        rejects(() -> MasteryProgress.empty().award(Experience.threshold(100)).learn(Talent.INSIGHT));
        for (var talent : Talent.values())
            if(!talent.specialized())
            for (int rank = 0; rank < talent.maxRank(); rank++) p = p.learn(talent);
        check(p.points() == 44, "all talents obtainable without exclusivity");
        MasteryProgress full = p;
        rejects(() -> full.learn(Talent.YIELD));
        check(Profile.empty().award(Mastery.MINING, 250).get(Mastery.LOGGING).experience() == 0,
                "independent progression");
    }
    private static void profiles() {
        var p = Profile.empty();
        for (var m : Mastery.values()) p = p.award(m, Experience.threshold(110));
        p = p.with(Mastery.MINING, p.get(Mastery.MINING).learn(Talent.EFFICIENCY).learn(Talent.INSIGHT));
        check(Profile.decode(p.encode()).equals(p), "save roundtrip including talents");
        check(Profile.decode(p.encode()).encode().equals(p.encode()), "deterministic encoding");
        check(Profile.decode(Profile.empty().encode()).equals(Profile.empty()), "empty save");
        rejects(() -> Profile.decode("not base64!"));
        rejects(() -> Profile.decode("A".repeat(16_385)));
        byte[] raw = Base64.getDecoder().decode(p.encode());
        raw[3] = 99;
        rejects(() -> Profile.decode(Base64.getEncoder().encodeToString(raw)));
        rejects(() -> new MasteryProgress(0, Map.of(Talent.YIELD, 1)));
        rejects(() -> new MasteryProgress(0, Map.of(Talent.EFFICIENCY, 1)));
        var specialized=MasteryProgress.empty().award(Experience.threshold(100)).learn(Talent.EFFICIENCY).learn(Talent.INSIGHT).learn(Talent.MINING_PROSPECTOR);
        rejects(()->Profile.empty().with(Mastery.FISHING,specialized));
        rejects(()->MasteryProgress.empty().award(Experience.threshold(24)).learn(Talent.EFFICIENCY).learn(Talent.INSIGHT).learn(Talent.MINING_PROSPECTOR));
        check(Profile.decode(Profile.empty().with(Mastery.MINING,specialized).encode()).get(Mastery.MINING).rank(Talent.MINING_PROSPECTOR)==1,"specialization roundtrip");
    }
    private static void production() {
        var rules = new Production.Rules(100, 2, 64, 432_000);
        var state = new Production.State(0, 0, 100, 0);
        var next = Production.advance(state, rules, 1_050);
        check(next.output() == 10 && next.fuel() == 80 && next.progressTicks() == 50, "catch-up accounting");
        check(Production.advance(next, rules, 1_050).equals(next), "repeated catch-up is idempotent");
        var full = Production.advance(state, rules, Long.MAX_VALUE);
        check(full.output() == 50 && full.fuel() == 0 && full.progressTicks() == 0, "fuel limits absence production");
        var capped = Production.advance(new Production.State(0, 0, 1_000, 63), rules, 1_000);
        check(capped.output() == 64 && capped.fuel() == 998, "inventory limits production");
        check(capped.progressTicks() == 0, "no stored backlog at capacity");
        var noFuel = Production.advance(new Production.State(0, 90, 0, 0), rules, 10_000);
        check(noFuel.output() == 0 && noFuel.progressTicks() == 0, "no retroactive fuel credit");
        check(Production.advance(new Production.State(10_000, 90, 8, 0), rules, 1).progressTicks() == 0,
                "clock rollback never grants production");
        var limit = Production.advance(new Production.State(0, 0, 1_000_000, 0),
                new Production.Rules(100, 1, 1_000_000, 1_000), 50_000);
        check(limit.output() == 10, "bounded catch-up workload");
        Random random = new Random(843124);
        for (int i = 0; i < 10_000; i++) {
            int fuel = random.nextInt(128), output = random.nextInt(65);
            var input = new Production.State(100, random.nextInt(100), fuel, output);
            var actual = Production.advance(input, rules, 100 + random.nextInt(50_000));
            check(actual.output() >= output && actual.output() <= 64, "output invariant");
            check(actual.fuel() >= 0 && actual.fuel() <= fuel, "fuel invariant");
            check((actual.output() - output) * 2 == fuel - actual.fuel(), "resource conservation");
        }
        // A reload must match continuous ticking while resources remain available.
        var repeated = state;
        for (int tick = 1; tick <= 3_000; tick++) repeated = Production.advance(repeated, rules, tick);
        check(repeated.equals(Production.advance(state, rules, 3_000)), "loaded/unloaded equivalence");
    }
    private static void activities() {
        var ledger=ActivityLedger.empty();
        for(int i=0;i<8;i++) {
            var claim=ledger.claim("craft/iron",100+i,24000,8);
            check(claim.allowed(),"budget admits distinct successful crafts");ledger=claim.ledger();
        }
        check(!ledger.claim("craft/iron",23999,24000,8).allowed(),"taking result repeatedly is bounded");
        check(ledger.claim("craft/copper",110,24000,8).allowed(),"independent activity budgets");
        check(ledger.claim("craft/iron",24100,24000,8).allowed(),"budget resets at original deadline");
        ledger=ledger.claim("region/negative",200,Long.MAX_VALUE,1).ledger();
        check(!ledger.claim("region/negative",90000,Long.MAX_VALUE,1).allowed(),"revisiting a territory does not award again");
        check(ActivityLedger.decode(ledger.encode()).equals(ledger),"reward history survives save/reload");
        for(int i=0;i<ActivityLedger.CAPACITY+2;i++)ledger=ledger.claim("site/"+i,1000+i,Long.MAX_VALUE,1).ledger();
        check(ledger.entries().size()==ActivityLedger.CAPACITY,"history is bounded during long expeditions");
        check(ledger.entries().containsKey("site/2049"),"newest discoveries are retained");
        check(ActivityLedger.decode(ledger.encode()).equals(ledger),"full history roundtrip");
        rejects(()->ActivityLedger.decode("invalid!"));
        rejects(()->ActivityLedger.empty().claim("",0,1,1));
        rejects(()->ActivityLedger.empty().claim("test",-1,1,1));
        rejects(()->ActivityLedger.empty().claim("test",0,0,1));
        rejects(()->ActivityLedger.empty().claim("test",0,1,0));
    }
    private static void masteryRules() {
        var p=Profile.empty();
        for(var m:Mastery.values()) {
            check(MasteryRules.partner(m)!=m,"synergy involves another activity");
            check(MasteryRules.xpFactor(p,m)==1,"no hidden initial XP multiplier");
            var progress=MasteryProgress.empty().award(Experience.threshold(100));
            for(var t:Talent.available(m))for(int i=0;i<t.maxRank();i++)progress=progress.learn(t);
            p=p.with(m,progress);
            check(MasteryRules.radius(progress)>=16&&MasteryRules.radius(progress)<=20,"specialist insight remains bounded");
            check(progress.points()==20,"all seven talents remain attainable before prestige");
            check(Talent.available(m).size()==7,"two exclusive branches per mastery");
            final var own=progress;
            rejects(()->Profile.empty().with(m,own.learn(Talent.EFFICIENCY)));
        }
        for(var m:Mastery.values())check(MasteryRules.xpFactor(p,m)>1&&MasteryRules.xpFactor(p,m)<2,"useful bounded XP effects for every mastery");
    }
    private static void cycle() {
        var cycle = new DimensionCycle(48_000);
        check(cycle.isDay(0) && cycle.isDay(23_999), "twenty-minute day");
        check(!cycle.isDay(24_000) && !cycle.isDay(47_999), "twenty-minute night");
        check(cycle.isDay(48_000), "cycle rollover");
        check(cycle.vanillaVisualTime(24_000) == 12_000, "visual phase");
        rejects(() -> new DimensionCycle(7));
    }
}
