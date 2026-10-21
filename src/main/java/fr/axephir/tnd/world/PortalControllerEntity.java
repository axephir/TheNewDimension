package fr.axephir.tnd.world;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.technology.MachineRegistry;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** One shared, permanent pair. Controllers and their destination are saved in chunk data. */
public final class PortalControllerEntity extends BlockEntity {
    private GlobalPos destination;
    private long chargeStart;
    private final java.util.LinkedHashMap<java.util.UUID,Long> travellers=new java.util.LinkedHashMap<>();
    public PortalControllerEntity(BlockPos pos, BlockState state) { super(MachineRegistry.PORTAL_ENTITY.get(), pos, state); }
    public GlobalPos destination() { return destination; }
    public long chargeStart(){return chargeStart;}
    public void charging(boolean charging){if(level==null)return;chargeStart=level.getGameTime();var s=getBlockState();level.setBlock(worldPosition,s.setValue(PortalControllerBlock.CHARGING,charging),3);setChanged();level.sendBlockUpdated(worldPosition,s,getBlockState(),3);}
    public void updateLink(){
        if(!(level instanceof ServerLevel))return;
        var monitor=level.dimension()==Level.OVERWORLD?fr.axephir.tnd.city.PortalMonitorEntity.findFor(level,worldPosition):null;
        var s=getBlockState();
        // Recover the orientation of existing, already saved 0.10 frames too.
        Direction orientation=s.getValue(PortalControllerBlock.FACING);
        for(var front:Direction.Plane.HORIZONTAL)for(int depth:new int[]{1,2}){
            var back=worldPosition.relative(front.getOpposite(),depth);var side=front.getClockWise();
            var left=back.relative(side,2);var right=back.relative(side.getOpposite(),2);
            if(level.hasChunkAt(left)&&level.hasChunkAt(right)&&level.getBlockState(left).is(ModContent.PORTAL_FRAME.get())&&level.getBlockState(right).is(ModContent.PORTAL_FRAME.get())&&(level.getBlockState(back.above(5)).is(ModContent.PORTAL_FRAME.get())||level.getBlockState(back.above(6)).is(ModContent.PORTAL_FRAME.get())))orientation=front;
        }
        boolean online=s.getValue(PortalControllerBlock.ACTIVE)&&(level.dimension()!=Level.OVERWORLD||monitor!=null);
        boolean charging=monitor!=null&&monitor.charging();
        if(s.getValue(PortalControllerBlock.ONLINE)!=online||s.getValue(PortalControllerBlock.CHARGING)!=charging||s.getValue(PortalControllerBlock.FACING)!=orientation)level.setBlock(worldPosition,s.setValue(PortalControllerBlock.ONLINE,online).setValue(PortalControllerBlock.CHARGING,charging).setValue(PortalControllerBlock.FACING,orientation),3);
    }

    public void activate(ServerPlayer player, ItemStack crystal) {
        if (!(level instanceof ServerLevel source) || getBlockState().getValue(PortalControllerBlock.ACTIVE)) return;
        if (!source.dimension().equals(Level.OVERWORLD)) return;
        var monitor=fr.axephir.tnd.city.PortalMonitorEntity.findFor(source,worldPosition);
        if(monitor==null){player.sendOverlayMessage(Component.translatable("message.tnd.monitor_required"));return;}
        if(monitor.isEmpty()&&crystal.is(ModContent.COMPOSITE_CRYSTAL.get())&&!crystal.isEmpty())monitor.setItem(0,crystal.split(1));
        fr.axephir.tnd.city.Codex.collect(player,0);player.openMenu(monitor);
    }

    /** Establish a shared pair on the server; target selection stays with the caller. */
    public boolean activateTo(ServerLevel target, ItemStack crystal) {
        if (!(level instanceof ServerLevel source) || !source.dimension().equals(Level.OVERWORLD)
                || source == target || source.getServer() != target.getServer()
                || getBlockState().getValue(PortalControllerBlock.ACTIVE)
                || !crystal.is(ModContent.COMPOSITE_CRYSTAL.get()) || crystal.isEmpty()) return false;
        var monitor=fr.axephir.tnd.city.PortalMonitorEntity.findFor(source,worldPosition);
        if(monitor==null||monitor.energy.getAmountAsInt()<fr.axephir.tnd.city.PortalMonitorEntity.COST)return false;
        int x = worldPosition.getX(), z = worldPosition.getZ();
        var site = PortalLanding.findBuildSite(target, x, z);
        if (site.isEmpty()) return false;
        BlockPos partnerPos = site.get();
        for (BlockPos pos : BlockPos.betweenClosed(partnerPos.offset(-3, -1, -3), partnerPos.offset(3, -1, 3)))
            target.setBlock(pos, ModContent.PORTAL_FRAME.get().defaultBlockState(), 3);
        // A sheltered arrival shrine fits entirely inside the volume already checked as empty.
        // The central landing and the four approaches remain clear for players and pets.
        for (int dx : new int[]{-3,3}) for (int dz : new int[]{-3,3}) {
            for (int dy=0;dy<=5;dy++) target.setBlock(partnerPos.offset(dx,dy,dz),
                    (dy==4 ? ModContent.RESONANT_LAMP.get() : ModContent.ANCIENT_PILLAR.get()).defaultBlockState(),3);
        }
        for (int dx=-3;dx<=3;dx++) for (int dz=-3;dz<=3;dz++)
            target.setBlock(partnerPos.offset(dx,6,dz),ModContent.CUT_ALLOY.get().defaultBlockState(),3);
        for (int dx=-2;dx<=2;dx++) for (int dy=0;dy<=5;dy++)
            if (Math.abs(dx)==2 || dy==5) target.setBlock(partnerPos.offset(dx,dy,2),ModContent.PORTAL_FRAME.get().defaultBlockState(),3);
        target.setBlock(partnerPos, ModContent.PORTAL_CONTROLLER.get().defaultBlockState().setValue(PortalControllerBlock.ACTIVE, true).setValue(PortalControllerBlock.ONLINE,true), 3);
        if (!(target.getBlockEntity(partnerPos) instanceof PortalControllerEntity partner)) return false;
        partner.destination = GlobalPos.of(source.dimension(), worldPosition);
        partner.setChanged();
        destination = GlobalPos.of(target.dimension(), partnerPos);
        source.setBlock(worldPosition, getBlockState().setValue(PortalControllerBlock.ACTIVE, true).setValue(PortalControllerBlock.CHARGING,false).setValue(PortalControllerBlock.ONLINE,true), 3);
        setChanged();
        crystal.shrink(1);
        monitor.energy.set(monitor.energy.getAmountAsInt()-fr.axephir.tnd.city.PortalMonitorEntity.COST);
        return true;
    }

    public boolean travel(ServerPlayer player) {
        if (!(level instanceof ServerLevel source))return false;
        if(destination==null||!getBlockState().getValue(PortalControllerBlock.ACTIVE)){player.sendOverlayMessage(Component.translatable("message.tnd.monitor_required"));return false;}
        if(source.dimension()==Level.OVERWORLD&&fr.axephir.tnd.city.PortalMonitorEntity.findFor(source,worldPosition)==null){player.sendOverlayMessage(Component.translatable("message.tnd.monitor_required"));return false;}
        if(source.getGameTime()<travellers.getOrDefault(player.getUUID(),-100L)+30)return false;
        ServerLevel target = source.getServer().getLevel(destination.dimension());
        if (target == null) return false;
        BlockPos controller = destination.pos();
        // Loading one destination chunk is necessary for transport, never permanent chunk loading.
        target.getChunkAt(controller);
        if (!(target.getBlockEntity(controller) instanceof PortalControllerEntity)) {
            player.sendOverlayMessage(Component.translatable("message.tnd.portal_unavailable")); return false;
        }
        var landing = PortalLanding.findArrival(target, controller, player);
        if (landing.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.tnd.portal_blocked")); return false;
        }
        BlockPos arrival = landing.get();
        var pets = source.getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(6),
                pet -> !(pet instanceof fr.axephir.tnd.entities.Shardling) && pet.isOwnedBy(player) && !pet.isInSittingPose());
        var transition = new TeleportTransition(target, Vec3.atBottomCenterOf(arrival), Vec3.ZERO,
                player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING);
        if (player.teleport(transition) == null) return false;
        travellers.put(player.getUUID(),source.getGameTime());if(travellers.size()>64)travellers.remove(travellers.keySet().iterator().next());
        target.playSound(null,arrival,ModContent.atmosphereSound("gate_transit"),net.minecraft.sounds.SoundSource.PLAYERS,.65f,1);
        target.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,player.getX(),player.getY()+1,player.getZ(),30,.5,.8,.5,.08);
        for (var pet : pets) {
            var petLanding = PortalLanding.findArrival(target, controller, pet);
            if (petLanding.isPresent()) pet.teleport(new TeleportTransition(target, Vec3.atBottomCenterOf(petLanding.get()), Vec3.ZERO,
                    pet.getYRot(), pet.getXRot(), TeleportTransition.DO_NOTHING));
        }
        return true;
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (destination != null) output.store("destination", GlobalPos.CODEC, destination);
        output.putLong("ChargeStart",chargeStart);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); destination = input.read("destination", GlobalPos.CODEC).orElse(null);chargeStart=input.getLongOr("ChargeStart",0);
    }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
}
