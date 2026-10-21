package fr.axephir.tnd.progression;

import fr.axephir.tnd.core.TndConfig;
import fr.axephir.tnd.domain.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressionService {
    public static boolean claim(ServerPlayer p,String key,long window,int limit) {
        if(p.isCreative()||p.isSpectator())return false;
        var result=p.getData(PlayerProgression.ACTIVITIES).claim(key,Math.max(0,p.level().getServer().overworld().getGameTime()),window,limit);
        if(result.allowed())p.setData(PlayerProgression.ACTIVITIES,result.ledger());
        return result.allowed();
    }
    public static void award(ServerPlayer p,Mastery mastery,long base) {
        if(p.isCreative()||p.isSpectator()||base<=0)return;
        Profile before=p.getData(PlayerProgression.PROFILE);
        long amount=Math.round(base*TndConfig.XP_MULTIPLIER.get()*MasteryRules.xpFactor(before,mastery));
        if(amount<=0)return;
        Profile after=before.award(mastery,amount);p.setData(PlayerProgression.PROFILE,after);
        if(after.get(mastery).level()>before.get(mastery).level())
            p.sendOverlayMessage(Component.translatable("message.tnd.level_up",Component.translatable("mastery.tnd."+mastery.id()),after.get(mastery).level()));
    }
    private ProgressionService() {}
}
