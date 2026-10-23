package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.familiar.FamiliarLedger;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Persistent shared cooldown, swept dodge collision and validated companion ownership. */
public final class RelicPowers {
    public record Activate() implements CustomPacketPayload {
        public static final Type<Activate> TYPE=new Type<>(Identifier.fromNamespaceAndPath("tnd","archive_relic"));
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Activate> CODEC=StreamCodec.unit(new Activate());
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void network(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(Activate.TYPE,Activate.CODEC,(packet,ctx)->{if(ctx.player() instanceof ServerPlayer p)activate(p);});}
    public static int remaining(ServerPlayer p){return (int)Math.clamp((p.getData(ArchiveContent.READY)-p.level().getServer().overworld().getGameTime()+19)/20,0,600);}
    public static boolean activate(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||p.isSleeping()||p.isPassenger()||!p.getData(ArchiveContent.UNLOCKED)||remaining(p)>0)return false;
        int choice=p.getData(ArchiveContent.SELECTION),seconds;
        if(choice==0){
            if(!p.onGround())return fail(p,"dodge_ground");
            Vec3 dir=p.getLookAngle().multiply(1,0,1).normalize();if(dir.lengthSqr()<.5)return false;Vec3 start=p.position(),end=start;
            for(int step=1;step<=16;step++){var next=start.add(dir.scale(step*.25));var feet=BlockPos.containing(next);
                if(!p.level().hasChunkAt(feet)||!p.level().getWorldBorder().isWithinBounds(feet)||!p.level().noCollision(p,p.getBoundingBox().move(next.subtract(start)))||!p.level().getBlockState(feet.below()).isFaceSturdy(p.level(),feet.below(),Direction.UP)||!p.level().getFluidState(feet).isEmpty()||!p.level().getFluidState(feet.above()).isEmpty())break;
                end=next;
            }
            if(end.distanceToSqr(start)<.5)return fail(p,"dodge_blocked");ArchiveScene.beam(p.level(),start.add(0,.3,0),end.add(0,.3,0),ParticleTypes.END_ROD);p.teleportTo(end.x,end.y,end.z);seconds=12;
        }else if(choice==1){p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,60,1));p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,60,1));seconds=30;
        }else if(choice==2){
            var pet=FamiliarLedger.get(p.level().getServer()).loaded(p.level().getServer(),p.getUUID());
            if(pet==null||pet.level()!=p.level()||!pet.available()||pet.isOrderedToSit()||pet.distanceToSqr(p)>48*48)return fail(p,"pet_needed");
            Vec3 safe=null;for(int[] offset:new int[][]{{2,0},{-2,0},{0,2},{0,-2}}){var at=p.blockPosition().offset(offset[0],0,offset[1]);var pos=Vec3.atBottomCenterOf(at);if(!p.level().hasChunkAt(at)||!p.level().getWorldBorder().isWithinBounds(at)||!p.level().getFluidState(at).isEmpty()||!p.level().getFluidState(at.above()).isEmpty()||!p.level().getBlockState(at.below()).isFaceSturdy(p.level(),at.below(),Direction.UP)||!p.level().noCollision(pet,pet.getBoundingBox().move(pos.subtract(pet.position()))))continue;
                if(p.level().clip(new ClipContext(p.getEyePosition(),pos.add(0,.8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)continue;safe=pos;break;}
            if(safe==null)return fail(p,"pet_space");
            var start=pet.position();pet.releaseArchive();pet.getNavigation().stop();pet.teleportTo(safe.x,safe.y,safe.z);pet.setDeltaMovement(Vec3.ZERO);pet.fallDistance=0;pet.relicPulse();pet.addEffect(new MobEffectInstance(MobEffects.STRENGTH,100,1));
            var target=p.level().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(10),m->m.isAlive()&&pet.hasLineOfSight(m)&&m.distanceToSqr(pet)<12*12).stream().min(java.util.Comparator.comparingDouble(m->m.distanceToSqr(pet))).orElse(null);if(target!=null){pet.setTarget(target);target.hurtServer(p.level(),p.level().damageSources().mobAttack(pet),10);}
            ArchiveScene.beam(p.level(),start.add(0,.6,0),safe.add(0,.6,0),ParticleTypes.ENCHANT);seconds=20;
        }else return false;
        p.setData(ArchiveContent.READY,p.level().getServer().overworld().getGameTime()+seconds*20L);p.level().sendParticles(ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),20,.5,.6,.5,.015);ArchiveScene.sound(p.level(),p.blockPosition(),"archive_relic",.55f,choice==0?1.3f:.9f);p.sendOverlayMessage(ArchiveScene.tr("relic_used",seconds));return true;
    }
    private static boolean fail(ServerPlayer p,String key){p.sendOverlayMessage(ArchiveScene.tr(key));return false;}
    private RelicPowers(){}
}
