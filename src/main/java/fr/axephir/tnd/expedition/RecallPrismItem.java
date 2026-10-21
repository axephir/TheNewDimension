package fr.axephir.tnd.expedition;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.world.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/** Five-second stationary recall within Vestige. Fuel is charged only after successful safe arrival. */
public final class RecallPrismItem extends Item {
    public record Binding(BlockPos pos,UUID token){}
    private record Channel(Vec3 start,long tick,InteractionHand hand,Binding binding,int useTicks){}
    private static final Map<UUID,Channel> CHANNELS=new HashMap<>();
    public RecallPrismItem(Properties p){super(p);}
    public static Optional<Binding> binding(ItemStack stack){
        if(!stack.is(ModContent.RECALL_PRISM.get()))return Optional.empty();
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(tag.getIntOr("tnd_recall_schema",0)!=1)return Optional.empty();
        try{var pos=BlockPos.of(tag.getLongOr("tnd_anchor_pos",0));if(Math.abs((long)pos.getX())>29_999_984||Math.abs((long)pos.getZ())>29_999_984||pos.getY()<0||pos.getY()>255)return Optional.empty();
            return Optional.of(new Binding(pos,UUID.fromString(tag.getStringOr("tnd_anchor_token",""))));}catch(IllegalArgumentException e){return Optional.empty();}
    }
    private static void say(ServerPlayer p,String key){p.sendOverlayMessage(Component.translatable("message.tnd."+key));}
    public static boolean bind(ServerPlayer p,ItemStack stack,BlockPos pos){
        if(!ExpeditionService.inVestige(p)||!p.isAlive()||p.isSpectator()||!stack.is(ModContent.RECALL_PRISM.get())||pos.distToCenterSqr(p.position())>64
                ||!p.level().hasChunk(pos.getX()>>4,pos.getZ()>>4)||!(p.level().getBlockEntity(pos) instanceof ReturnAnchorEntity anchor))return false;
        anchor.setChanged();cancel(p);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putInt("tnd_recall_schema",1);tag.putLong("tnd_anchor_pos",pos.asLong());tag.putString("tnd_anchor_token",anchor.token().toString());});
        ExpeditionService.sound(p,SoundEvents.LODESTONE_COMPASS_LOCK,SoundSource.PLAYERS,.65f,1);say(p,"recall_bound");return true;
    }
    private static boolean fuel(ServerPlayer p){return p.isCreative()||p.getInventory().countItem(ModContent.ECHO_DUST.get())>=2;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(!(player instanceof ServerPlayer p)){player.startUsingItem(hand);return InteractionResult.CONSUME;}
        if(!ExpeditionService.inVestige(p)||p.isSpectator()||!p.isAlive()||p.isPassenger()){say(p,"recall_dimension");return InteractionResult.FAIL;}
        var binding=binding(stack);if(binding.isEmpty()){say(p,"recall_unbound");return InteractionResult.FAIL;}
        if(p.isUsingItem()||p.getCooldowns().isOnCooldown(stack))return InteractionResult.FAIL;
        if(!fuel(p)){say(p,"recall_fuel");return InteractionResult.FAIL;}
        if(p.hurtTime>0){say(p,"recall_interrupted");return InteractionResult.FAIL;}
        long now=p.level().getServer().overworld().getGameTime();CHANNELS.values().removeIf(c->now-c.tick()>200);
        if(CHANNELS.size()>=4096&&!CHANNELS.containsKey(p.getUUID()))return InteractionResult.FAIL;
        CHANNELS.put(p.getUUID(),new Channel(p.position(),now,hand,binding.get(),0));p.startUsingItem(hand);say(p,"recall_channel");ExpeditionService.sound(p,ModContent.atmosphereSound("anchor_depart"),SoundSource.PLAYERS,.35f,1.4f);return InteractionResult.CONSUME;
    }
    public static void cancel(ServerPlayer p){CHANNELS.remove(p.getUUID());}
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining){
        if(!(entity instanceof ServerPlayer p))return;var c=CHANNELS.get(p.getUUID());
        if(c==null||!ExpeditionService.inVestige(p)||p.hurtTime>0||p.position().distanceToSqr(c.start())>.09){cancel(p);p.stopUsingItem();say(p,"recall_interrupted");return;}
        CHANNELS.put(p.getUUID(),new Channel(c.start(),c.tick(),c.hand(),c.binding(),Math.min(100,c.useTicks()+1)));
        if(remaining%10==0)p.level().sendParticles(p,ParticleTypes.ENCHANT,false,false,p.getX(),p.getY()+1,p.getZ(),8,.5,.5,.5,.05);
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 100;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.BLOCK;}
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining){if(entity instanceof ServerPlayer p)cancel(p);return false;}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){if(entity instanceof ServerPlayer p)finishRecall(p,stack);return stack;}
    public static boolean finishRecall(ServerPlayer p,ItemStack stack){
        var c=CHANNELS.remove(p.getUUID());if(c==null)return false;
        if(c.useTicks()<100||p.getUseItem()!=stack||p.getUseItemRemainingTicks()>0||!p.isAlive()||!ExpeditionService.inVestige(p)||p.hurtTime>0||p.position().distanceToSqr(c.start())>.09||p.getItemInHand(c.hand())!=stack||!binding(stack).filter(c.binding()::equals).isPresent())return false;
        if(!fuel(p)){say(p,"recall_fuel");return false;}
        var world=p.level();var pos=c.binding().pos();
        if(!world.getWorldBorder().isWithinBounds(pos)){say(p,"recall_blocked");return false;}
        world.getChunkAt(pos);
        if(!(world.getBlockEntity(pos) instanceof ReturnAnchorEntity anchor)||!anchor.token().equals(c.binding().token())){say(p,"recall_missing");return false;}
        var arrival=PortalLanding.findArrival(world,pos,p);if(arrival.isEmpty()){say(p,"recall_blocked");return false;}
        var pets=world.getEntitiesOfClass(Shardling.class,p.getBoundingBox().inflate(6),pet->pet.isOwnedBy(p)&&!pet.isOrderedToSit()&&pet.isAlive());
        if(p.teleport(new TeleportTransition(world,Vec3.atBottomCenterOf(arrival.get()),Vec3.ZERO,p.getYRot(),p.getXRot(),TeleportTransition.DO_NOTHING))==null)return false;
        if(!p.isCreative()){int need=2;for(int i=0;i<p.getInventory().getContainerSize()&&need>0;i++){var item=p.getInventory().getItem(i);if(item.is(ModContent.ECHO_DUST.get())){int n=Math.min(need,item.getCount());item.shrink(n);need-=n;}}}
        int left=0;for(var pet:pets){var landing=CompanionTravel.findLanding(world,arrival.get(),pet);if(landing.isEmpty()||pet.teleport(new TeleportTransition(world,Vec3.atBottomCenterOf(landing.get()),Vec3.ZERO,pet.getYRot(),pet.getXRot(),TeleportTransition.DO_NOTHING))==null)left++;}
        p.getCooldowns().addCooldown(stack,1200);ExpeditionService.sound(p,SoundEvents.CHORUS_FRUIT_TELEPORT,SoundSource.PLAYERS,.65f,.8f);if(left>0)p.sendOverlayMessage(Component.translatable("message.tnd.companion_no_landing",left));else say(p,"recall_arrived");return true;
    }
}
