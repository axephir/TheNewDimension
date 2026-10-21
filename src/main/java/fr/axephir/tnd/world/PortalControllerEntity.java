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
    public PortalControllerEntity(BlockPos pos, BlockState state) { super(MachineRegistry.PORTAL_ENTITY.get(), pos, state); }
    public GlobalPos destination() { return destination; }

    public void activate(ServerPlayer player, ItemStack crystal) {
        if (!(level instanceof ServerLevel source) || getBlockState().getValue(PortalControllerBlock.ACTIVE)) return;
        if (!source.dimension().equals(Level.OVERWORLD)) return;
        ServerLevel target = source.getServer().getLevel(DevelopmentCommands.DIMENSION);
        if (target == null) { player.sendOverlayMessage(Component.translatable("message.tnd.dimension_missing")); return; }
        if (activateTo(target, crystal)) player.sendSystemMessage(Component.translatable("message.tnd.portal_active"));
        else player.sendOverlayMessage(Component.translatable("message.tnd.portal_no_site"));
    }

    /** Establish a shared pair on the server; target selection stays with the caller. */
    public boolean activateTo(ServerLevel target, ItemStack crystal) {
        if (!(level instanceof ServerLevel source) || !source.dimension().equals(Level.OVERWORLD)
                || source == target || source.getServer() != target.getServer()
                || getBlockState().getValue(PortalControllerBlock.ACTIVE)
                || !crystal.is(ModContent.COMPOSITE_CRYSTAL.get()) || crystal.isEmpty()) return false;
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
        target.setBlock(partnerPos, ModContent.PORTAL_CONTROLLER.get().defaultBlockState().setValue(PortalControllerBlock.ACTIVE, true), 3);
        if (!(target.getBlockEntity(partnerPos) instanceof PortalControllerEntity partner)) return false;
        partner.destination = GlobalPos.of(source.dimension(), worldPosition);
        partner.setChanged();
        destination = GlobalPos.of(target.dimension(), partnerPos);
        source.setBlock(worldPosition, getBlockState().setValue(PortalControllerBlock.ACTIVE, true), 3);
        setChanged();
        crystal.shrink(1);
        return true;
    }

    public boolean travel(ServerPlayer player) {
        if (!(level instanceof ServerLevel source) || destination == null || !getBlockState().getValue(PortalControllerBlock.ACTIVE)) return false;
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
                pet -> pet.isOwnedBy(player) && !pet.isInSittingPose());
        var transition = new TeleportTransition(target, Vec3.atBottomCenterOf(arrival), Vec3.ZERO,
                player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING);
        if (player.teleport(transition) == null) return false;
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
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); destination = input.read("destination", GlobalPos.CODEC).orElse(null);
    }
}
