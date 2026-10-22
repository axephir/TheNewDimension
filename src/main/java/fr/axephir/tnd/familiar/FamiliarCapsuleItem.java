package fr.axephir.tnd.familiar;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public final class FamiliarCapsuleItem extends Item {
    public FamiliarCapsuleItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getPlayer() instanceof ServerPlayer p){var at=c.getClickedPos().relative(c.getClickedFace());
            boolean ok=FamiliarLedger.get(p.level().getServer()).deploy(p,c.getItemInHand(),Vec3.atBottomCenterOf(at));
            if(!ok)p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("familiar.tnd.deploy_blocked"));
        }return InteractionResult.SUCCESS;
    }
}
