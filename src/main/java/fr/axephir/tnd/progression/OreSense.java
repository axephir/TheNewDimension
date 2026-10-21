package fr.axephir.tnd.progression;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.technology.ExtractorBlockEntity;
import fr.axephir.tnd.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

public final class OreSense {
    /** Bounded loaded-only search. One cooldown across all categories, owned by the server. */
    public static int scan(ServerPlayer player,Mastery mastery) {
        var progress=player.getData(PlayerProgression.PROFILE).get(mastery);
        if(progress.rank(Talent.INSIGHT)==0) {player.sendOverlayMessage(Component.translatable("message.tnd.sense_locked"));return 0;}
        long now=player.level().getServer().overworld().getGameTime();
        if(now<player.getData(PlayerProgression.SENSE_NEXT_TICK)) {
            player.sendOverlayMessage(Component.translatable("message.tnd.sense_cooldown"));return 0;
        }
        player.setData(PlayerProgression.SENSE_NEXT_TICK,now+100);
        int radius=MasteryRules.radius(progress);
        BlockPos origin=player.blockPosition(),best=null;
        double distance=Double.POSITIVE_INFINITY;
        boolean creatures=switch(mastery){case HUNTING,BREEDING,MELEE,RANGED,DEFENSE->true;default->false;};
        if(creatures) {
            for(var entity:player.level().getEntities(player,new AABB(origin).inflate(radius),e->switch(mastery){
                case HUNTING->e instanceof Animal a&&a.isAlive()&&!(a instanceof fr.axephir.tnd.entities.Shardling)&&!(a instanceof net.minecraft.world.entity.TamableAnimal pet&&pet.isTame());
                case BREEDING->e instanceof Animal a&&a.isAlive()&&!a.isBaby()&&!(a instanceof fr.axephir.tnd.entities.Shardling);
                default->e instanceof Enemy&&e.isAlive();
            })) {
                double d=entity.blockPosition().distSqr(origin);
                if(d<distance){distance=d;best=entity.blockPosition();}
            }
        } else for(BlockPos pos:BlockPos.betweenClosed(origin.offset(-radius,-radius,-radius),origin.offset(radius,radius,radius))) {
            if(!player.level().hasChunk(pos.getX()>>4,pos.getZ()>>4))continue;
            var state=player.level().getBlockState(pos);
            double d=pos.distSqr(origin);
            if(d<distance&&matches(player,pos,state,mastery)){best=pos.immutable();distance=d;}
        }
        if(best==null){player.sendOverlayMessage(Component.translatable("message.tnd.sense_empty"));return 0;}
        int dx=best.getX()-origin.getX(),dz=best.getZ()-origin.getZ();
        String direction=Math.abs(dx)<2&&Math.abs(dz)<2?"level":Math.abs(dx)>Math.abs(dz)?dx>0?"east":"west":dz>0?"south":"north";
        player.sendOverlayMessage(Component.translatable("message.tnd.sense_found_direction",Math.round(Math.sqrt(distance)),
                Component.translatable("direction.tnd."+direction),Component.translatable("direction.tnd."+(best.getY()>origin.getY()+2?"above":best.getY()<origin.getY()-2?"below":"level"))));
        Vec3 eye=player.getEyePosition(),end=Vec3.atCenterOf(best);
        // Direction markers are sent only to the caster, never through a chunk load.
        Vec3 delta=end.subtract(eye);
        for(int i=1;i<=12;i++) {
            Vec3 point=eye.add(delta.scale(i/12.0));
            player.level().sendParticles(player,ParticleTypes.ENCHANT,false,false,point.x,point.y,point.z,2,.07,.07,.07,0);
        }
        return 1;
    }
    private static boolean matches(ServerPlayer p,BlockPos pos,BlockState s,Mastery m) {
        return switch(m) {
            case MINING->ProgressionEvents.isOre(s);
            case LOGGING->s.is(BlockTags.LOGS);
            case FARMING->ProgressionEvents.matureCrop(s);
            case FISHING->s.is(Blocks.WATER)&&p.level().getBlockState(pos.below(2)).is(Blocks.WATER);
            case CRAFTING->s.is(Blocks.CRAFTING_TABLE)||s.is(Blocks.FURNACE)||s.is(Blocks.SMITHING_TABLE);
            case BUILDING->s.is(Blocks.STONECUTTER)||s.is(Blocks.CRAFTING_TABLE)||s.is(BlockTags.LOGS);
            case EXPLORATION->s.getBlock() instanceof ChestBlock||s.is(ModContent.ANCIENT_CONSOLE.get());
            case ALCHEMY->s.is(Blocks.BREWING_STAND)||s.is(Blocks.NETHER_WART);
            case ENGINEERING->p.level().getBlockEntity(pos) instanceof ExtractorBlockEntity;
            case ANCIENT_TECH->s.getBlock() instanceof AncientConsoleBlock||s.getBlock() instanceof InscriptionBlock||s.is(ModContent.ECHO_BLOOM.get());
            default->false;
        };
    }
    private OreSense(){}
}
