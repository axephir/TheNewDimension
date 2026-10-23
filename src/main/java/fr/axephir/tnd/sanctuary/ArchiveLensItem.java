package fr.axephir.tnd.sanctuary;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
public final class ArchiveLensItem extends Item {
    public ArchiveLensItem(Properties p){super(p);}
    public InteractionResult use(Level w,Player player,InteractionHand hand){if(player instanceof ServerPlayer p){if(!p.getData(ArchiveContent.UNLOCKED)){p.setData(ArchiveContent.UNLOCKED,true);p.getItemInHand(hand).shrink(1);ArchiveScene.sound(p.level(),p.blockPosition(),"archive_relic",.7f,1);p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),32,.5,.8,.5,.02);}RelicMenu.open(p);}return InteractionResult.SUCCESS;}
}
