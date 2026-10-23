package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.world.item.Item;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
public final class SanctuaryCompassItem extends Item {
    public SanctuaryCompassItem(Properties p){super(p);}
    public InteractionResult use(Level w,Player player,InteractionHand hand){if(!w.isClientSide()){
        if(!w.dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION))player.sendOverlayMessage(Component.translatable("observatory.tnd.compass_elsewhere"));
        else{var site=SanctuaryRoute.nearest(player.getBlockX(),player.getBlockZ());player.sendSystemMessage(Component.translatable("observatory.tnd.compass",site.x()-640,SanctuaryRoute.FLOOR+1,site.z()-320));}
    }return InteractionResult.SUCCESS;}
}
