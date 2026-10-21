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
        if(after.get(mastery).level()>before.get(mastery).level()){
            celebrate(p);
            p.sendOverlayMessage(Component.translatable("message.tnd.level_up",Component.translatable("mastery.tnd."+mastery.id()),after.get(mastery).level()));
        }
    }
    private static void celebrate(ServerPlayer p){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
            fr.axephir.tnd.core.ModContent.ATMOSPHERE_SOUNDS.get("mastery_ascend"),net.minecraft.sounds.SoundSource.PLAYERS,
            p.getX(),p.getY(),p.getZ(),.65f,1,p.getRandom().nextLong()));
        for(int i=0;i<16;i++){
            double angle=i*Math.PI/8;
            p.level().sendParticles(p,net.minecraft.core.particles.ParticleTypes.END_ROD,false,false,
                p.getX()+Math.cos(angle)*.65,p.getY()+.25+i*.08,p.getZ()+Math.sin(angle)*.65,1,0,.08,0,.015);
        }
        p.level().sendParticles(p,net.minecraft.core.particles.ParticleTypes.ENCHANT,false,false,p.getX(),p.getY()+1,p.getZ(),24,.5,.6,.5,.12);
    }
    private ProgressionService() {}
}
