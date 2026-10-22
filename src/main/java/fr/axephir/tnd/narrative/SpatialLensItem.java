package fr.axephir.tnd.narrative;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.PortalLanding;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Earned perception and safe local spatial steps. A reissued lens revokes old copies; cooldown is shared by item type. */
public final class SpatialLensItem extends Item {
    public SpatialLensItem(Properties p){super(p);}
    public static boolean reissue(ServerPlayer p){
        if(!p.getData(Convergence.FINISHED)||present(p)||p.getInventory().getFreeSlot()<0)return false;
        String key=UUID.randomUUID().toString();var item=new ItemStack(ConvergenceContent.LENS.get());var tag=new CompoundTag();tag.putString("Owner",p.getUUID().toString());tag.putString("Key",key);item.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        p.setData(Convergence.LENS_KEY,key);return p.getInventory().add(item);
    }
    public static boolean present(ServerPlayer p){for(int n=0;n<p.getInventory().getContainerSize();n++)if(valid(p,p.getInventory().getItem(n)))return true;return false;}
    public static boolean valid(ServerPlayer p,ItemStack lens){var tag=lens.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return lens.is(ConvergenceContent.LENS.get())&&p.getData(Convergence.FINISHED)&&tag.getStringOr("Owner","").equals(p.getUUID().toString())&&!p.getData(Convergence.LENS_KEY).isEmpty()&&tag.getStringOr("Key","").equals(p.getData(Convergence.LENS_KEY));}
    @Override public InteractionResult use(Level w,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)activate(p,player.getItemInHand(hand),player.isShiftKeyDown());return InteractionResult.SUCCESS;}
    public static boolean activate(ServerPlayer p,ItemStack lens,boolean step){
        if(!valid(p,lens)){p.sendOverlayMessage(Component.translatable("story.tnd.lens_inactive"));return false;}
        if(p.isSpectator()||Convergence.inEcho(p)||p.getCooldowns().isOnCooldown(lens))return false;
        var w=p.level();if(step){
            java.util.Optional<BlockPos> target=java.util.Optional.empty();
            for(int distance=8;distance>=2;distance--){var aim=BlockPos.containing(p.position().add(p.getLookAngle().scale(distance)));boolean loaded=true;
                for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1})if(!w.hasChunk((aim.getX()+dx)>>4,(aim.getZ()+dz)>>4))loaded=false;
                if(loaded&&Vec3.atBottomCenterOf(aim).distanceTo(p.position())<=8.01&&PortalLanding.isSafe(w,aim,p)){target=java.util.Optional.of(aim);break;}}
            if(target.isEmpty()){p.sendOverlayMessage(Component.translatable("story.tnd.step_blocked"));return false;}
            Vec3 old=p.position();p.teleportTo(target.get().getX()+.5,target.get().getY(),target.get().getZ()+.5);p.fallDistance=0;w.sendParticles(ParticleTypes.END_ROD,old.x,old.y+.8,old.z,14,.3,.6,.3,.02);p.getCooldowns().addCooldown(lens,800);
        }else{p.setData(Convergence.PERCEPTION,w.getGameTime()+300);p.getCooldowns().addCooldown(lens,600);p.sendOverlayMessage(Component.translatable("story.tnd.perception"));}
        w.playSound(null,p.blockPosition(),ModContent.atmosphereSound("archive_read"),SoundSource.PLAYERS,.6f,.65f);return true;
    }
}
