package fr.axephir.tnd.narrative;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Only personal resources are restored. Source blocks, machines, chunks, other players and familiar ownership are never rewritten. */
public final class EchoReference {
    public record State(UUID owner,UUID session,ResourceKey<Level> source,Vec3 position,float yaw,float pitch,GameType mode,BlockPos machine,int slot){}
    public static CompoundTag capture(ServerPlayer p,UUID session,BlockPos machine,int slot){
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,p.registryAccess());out.putInt("Schema",1);out.putBoolean("Flying",p.getAbilities().flying);out.putFloat("FlightSpeed",p.getAbilities().getFlyingSpeed());out.putFloat("WalkSpeed",p.getAbilities().getWalkingSpeed());out.putFloat("Absorption",p.getAbsorptionAmount());var effects=out.list("Effects",net.minecraft.world.effect.MobEffectInstance.CODEC);for(var effect:p.getActiveEffects())effects.add(new net.minecraft.world.effect.MobEffectInstance(effect));out.putString("Owner",p.getUUID().toString());out.putString("Session",session.toString());out.putString("Dimension",p.level().dimension().identifier().toString());
        out.putDouble("X",p.getX());out.putDouble("Y",p.getY());out.putDouble("Z",p.getZ());out.putFloat("Yaw",p.getYRot());out.putFloat("Pitch",p.getXRot());out.putInt("Mode",p.gameMode.getGameModeForPlayer().getId());out.putInt("Plot",slot);out.putInt("MachineX",machine.getX());out.putInt("MachineY",machine.getY());out.putInt("MachineZ",machine.getZ());
        out.putInt("Selected",p.getInventory().getSelectedSlot());out.putInt("Experience",p.totalExperience);out.putInt("ExperienceLevel",p.experienceLevel);out.putFloat("ExperienceProgress",p.experienceProgress);out.putFloat("Health",p.getHealth());
        var main=out.list("Main",ItemStackWithSlot.CODEC);for(int n=0;n<p.getInventory().getContainerSize();n++)if(!p.getInventory().getItem(n).isEmpty())main.add(new ItemStackWithSlot(n,p.getInventory().getItem(n).copy()));
        p.getEnderChestInventory().storeAsSlots(out.list("Ender",ItemStackWithSlot.CODEC));p.getFoodData().addAdditionalSaveData(out.child("Food"));return out.buildResult();
    }
    public static State validate(CompoundTag tag,UUID owner,UUID session){
        if(tag.getIntOr("Schema",1)!=1)throw new IllegalArgumentException("Unsupported story reference schema");
        if(!tag.getStringOr("Owner","").equals(owner.toString())||!tag.getStringOr("Session","").equals(session.toString()))throw new IllegalArgumentException("Story reference identity mismatch");
        var pos=new Vec3(tag.getDoubleOr("X",Double.NaN),tag.getDoubleOr("Y",Double.NaN),tag.getDoubleOr("Z",Double.NaN));int mode=tag.getIntOr("Mode",-1),slot=tag.getIntOr("Plot",-1);
        if(!Double.isFinite(pos.x)||!Double.isFinite(pos.y)||!Double.isFinite(pos.z)||Math.abs(pos.x)>29999000||Math.abs(pos.z)>29999000||pos.y< -2048||pos.y>2048||mode<0||mode>3||slot<0||slot>100000)throw new IllegalArgumentException("Story reference outside supported bounds");
        if(!Float.isFinite(tag.getFloatOr("Yaw",0))||!Float.isFinite(tag.getFloatOr("Pitch",0)))throw new IllegalArgumentException("Non-finite story orientation");
        var dimension=ResourceKey.create(Registries.DIMENSION,Identifier.parse(tag.getStringOr("Dimension","")));if(dimension.equals(Convergence.ECHO))throw new IllegalArgumentException("Echo cannot be its own reference world");
        return new State(owner,session,dimension,pos,tag.getFloatOr("Yaw",0),tag.getFloatOr("Pitch",0),GameType.byId(mode),new BlockPos(tag.getIntOr("MachineX",0),tag.getIntOr("MachineY",0),tag.getIntOr("MachineZ",0)),slot);
    }
    public static void restoreResources(ServerPlayer p,CompoundTag tag){
        // Fully decode and validate both containers before changing a single slot.
        var input=TagValueInput.create(ProblemReporter.DISCARDING,p.registryAccess(),tag);var main=new ArrayList<ItemStackWithSlot>();var ender=new ArrayList<ItemStackWithSlot>();var mainSlots=new HashSet<Integer>();var enderSlots=new HashSet<Integer>();
        for(var entry:input.listOrEmpty("Main",ItemStackWithSlot.CODEC)){if(!entry.isValidInContainer(p.getInventory().getContainerSize())||!mainSlots.add(entry.slot()))throw new IllegalArgumentException("Invalid reference inventory");main.add(entry);}
        for(var entry:input.listOrEmpty("Ender",ItemStackWithSlot.CODEC)){if(!entry.isValidInContainer(27)||!enderSlots.add(entry.slot()))throw new IllegalArgumentException("Invalid reference ender inventory");ender.add(entry);}
        if(main.size()!=tag.getListOrEmpty("Main").size()||ender.size()!=tag.getListOrEmpty("Ender").size())throw new IllegalArgumentException("A reference item could not be decoded; retaining native resources");
        for(int i=0;i<p.getInventory().getContainerSize();i++)p.getInventory().setItem(i,ItemStack.EMPTY);for(int i=0;i<27;i++)p.getEnderChestInventory().setItem(i,ItemStack.EMPTY);
        for(var entry:main)p.getInventory().setItem(entry.slot(),entry.stack().copy());for(var entry:ender)p.getEnderChestInventory().setItem(entry.slot(),entry.stack().copy());
        p.getInventory().setSelectedSlot(Math.clamp(tag.getIntOr("Selected",0),0,8));p.getInventory().setChanged();p.totalExperience=Math.max(0,tag.getIntOr("Experience",0));p.experienceLevel=Math.max(0,tag.getIntOr("ExperienceLevel",0));p.experienceProgress=Math.clamp(tag.getFloatOr("ExperienceProgress",0),0,1);
        p.removeAllEffects();for(var effect:input.listOrEmpty("Effects",net.minecraft.world.effect.MobEffectInstance.CODEC))p.addEffect(effect);p.setAbsorptionAmount(Math.max(0,tag.getFloatOr("Absorption",0)));
        p.getAbilities().setFlyingSpeed(Math.clamp(tag.getFloatOr("FlightSpeed",.05f),.01f,1));p.getAbilities().setWalkingSpeed(Math.clamp(tag.getFloatOr("WalkSpeed",.1f),.01f,1));
        p.getFoodData().readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING,p.registryAccess(),tag.getCompoundOrEmpty("Food")));p.setHealth(Math.clamp(tag.getFloatOr("Health",20),1,p.getMaxHealth()));p.inventoryMenu.broadcastChanges();
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(p.experienceProgress,p.totalExperience,p.experienceLevel));
    }
    private EchoReference(){}
}
