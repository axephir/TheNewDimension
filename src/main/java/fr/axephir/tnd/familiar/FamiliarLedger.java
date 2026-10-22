package fr.axephir.tnd.familiar;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.entities.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Authoritative cross-dimension bond. Capsules carry a revocable ticket, never a clonable creature payload. */
public final class FamiliarLedger extends SavedData {
    private final Map<String,CompoundTag> bonds;
    public static final Codec<FamiliarLedger> CODEC=Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).xmap(FamiliarLedger::new,l->l.bonds);
    public static final SavedDataType<FamiliarLedger> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("tnd","familiar_bonds"),()->new FamiliarLedger(Map.of()),CODEC);
    private FamiliarLedger(Map<String,CompoundTag> data){bonds=new HashMap<>();data.forEach((k,v)->bonds.put(k,v.copy()));}
    public static FamiliarLedger get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    public CompoundTag record(UUID owner){return bonds.get(owner.toString());}
    public boolean owns(UUID owner,UUID pet){var r=record(owner);return r!=null&&r.getStringOr("PetId","").equals(pet.toString());}
    public boolean claim(ServerPlayer owner,Shardling pet){
        var old=record(owner.getUUID());if(old!=null&&!owns(owner.getUUID(),pet.getUUID()))return false;
        if(old==null){old=new CompoundTag();old.putString("PetId",pet.getUUID().toString());old.putBoolean("Stored",false);bonds.put(owner.getUUID().toString(),old);setDirty();}return true;
    }
    public Shardling loaded(MinecraftServer server,UUID owner){
        var r=record(owner);if(r==null||r.getBooleanOr("Stored",false))return null;
        UUID id;try{id=UUID.fromString(r.getStringOr("PetId",""));}catch(IllegalArgumentException e){return null;}
        for(var w:server.getAllLevels())if(w.getEntity(id) instanceof Shardling pet&&pet.isAlive()&&pet.getOwnerReference()!=null&&pet.getOwnerReference().getUUID().equals(owner))return pet;
        return null;
    }
    public void snapshot(Shardling pet){
        if(pet.getOwnerReference()==null)return;var r=record(pet.getOwnerReference().getUUID());
        if(r==null||!r.getStringOr("PetId","").equals(pet.getUUID().toString())||r.getBooleanOr("Stored",false))return;
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,pet.registryAccess());pet.saveWithoutId(out);
        r.put("Pet",out.buildResult());r.putString("Dimension",pet.level().dimension().identifier().toString());r.putInt("X",pet.getBlockX());r.putInt("Y",pet.getBlockY());r.putInt("Z",pet.getBlockZ());
        r.putInt("Protocols",pet.protocols());r.putString("Name",pet.getName().getString());r.putInt("Level",pet.familiarLevel());r.putInt("Xp",pet.familiarXp());r.putInt("Health",pet.downed()?0:Math.round(pet.getHealth()*10));r.putInt("MaxHealth",Math.round(pet.getMaxHealth()*10));r.putBoolean("Downed",pet.downed());r.putBoolean("Waiting",pet.isOrderedToSit());setDirty();
    }
    public ItemStack ticket(UUID owner){
        var r=record(owner);if(r==null||!r.getBooleanOr("Stored",false))return ItemStack.EMPTY;
        var item=new ItemStack(FamiliarContent.CAPSULE.get());var tag=new CompoundTag();tag.putString("Owner",owner.toString());tag.putString("PetId",r.getStringOr("PetId",""));tag.putString("Ticket",r.getStringOr("Ticket",""));
        item.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));item.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.translatable("familiar.tnd.capsule_name",r.getStringOr("Name","")));return item;
    }
    public boolean pack(ServerPlayer p,Shardling pet){
        if(pet.level()!=p.level()||!pet.isOwnedBy(p)||pet.distanceToSqr(p)>36||!claim(p,pet)||pet.bondTicks()>0)return false;
        // Reserve inventory space first; no lost pet on a full inventory and no item thrown into a hazard.
        if(p.getInventory().getFreeSlot()<0){p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("familiar.tnd.inventory_full"));return false;}
        snapshot(pet);var r=record(p.getUUID());r.putBoolean("Stored",true);r.putString("Ticket",UUID.randomUUID().toString());setDirty();
        var item=ticket(p.getUUID());if(!p.getInventory().add(item)){r.putBoolean("Stored",false);r.remove("Ticket");return false;}
        pet.discard();p.level().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,net.minecraft.sounds.SoundSource.PLAYERS,.45f,1.5f);return true;
    }
    public boolean deploy(ServerPlayer p,ItemStack item,Vec3 pos){
        var tag=item.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var r=record(p.getUUID());
        if(!item.is(FamiliarContent.CAPSULE.get())||r==null||!r.getBooleanOr("Stored",false)||!tag.getStringOr("Owner","").equals(p.getUUID().toString())||!tag.getStringOr("Ticket","-").equals(r.getStringOr("Ticket",""))||!tag.getStringOr("PetId","").equals(r.getStringOr("PetId","")))return false;
        if(p.position().distanceToSqr(pos)>64||!p.level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(pos)))return false;
        var pet=(Shardling)EntityType.create(VestigeEntities.SHARDLING.get(),TagValueInput.create(ProblemReporter.DISCARDING,p.registryAccess(),r.getCompoundOrEmpty("Pet")),p.level(),EntitySpawnReason.LOAD).orElse(null);
        if(pet==null||!pet.getUUID().toString().equals(r.getStringOr("PetId","")))return false;
        pet.setPos(pos);pet.setDeltaMovement(Vec3.ZERO);pet.setOrderedToSit(pet.downed());pet.setInSittingPose(pet.downed());pet.fallDistance=0;pet.clearFire();
        var feet=pet.blockPosition();var floor=p.level().getBlockState(feet.below());
        if(floor.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||floor.is(net.minecraft.world.level.block.Blocks.CACTUS)||floor.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)||floor.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)||floor.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW))return false;
        if(!p.level().getBlockState(feet.below()).isFaceSturdy(p.level(),feet.below(),net.minecraft.core.Direction.UP)||!p.level().getFluidState(feet).isEmpty()||!p.level().getFluidState(feet.above()).isEmpty()||!p.level().noCollision(pet,pet.getBoundingBox()))return false;
        for(var w:p.level().getServer().getAllLevels())if(w.getEntity(pet.getUUID())!=null)return false;
        if(!p.level().addFreshEntity(pet))return false;
        r.putBoolean("Stored",false);r.remove("Ticket");item.shrink(1);snapshot(pet);return true;
    }
    public boolean rename(ServerPlayer p,String text){
        String name=text.strip().replaceAll("[\\p{Cntrl}§]","");if(name.isEmpty()||name.codePointCount(0,name.length())>24)return false;
        var r=record(p.getUUID());if(r==null)return false;var pet=loaded(p.level().getServer(),p.getUUID());
        if(pet!=null){pet.setCustomName(net.minecraft.network.chat.Component.literal(name));snapshot(pet);}else{
            r.putString("PendingName",name);r.putString("Name",name);
            // The capsule payload receives the same validated native component on deployment.
            if(r.getBooleanOr("Stored",false)){
                var restored=(Shardling)EntityType.create(VestigeEntities.SHARDLING.get(),TagValueInput.create(ProblemReporter.DISCARDING,p.registryAccess(),r.getCompoundOrEmpty("Pet")),p.level(),EntitySpawnReason.LOAD).orElse(null);
                if(restored==null)return false;restored.setCustomName(net.minecraft.network.chat.Component.literal(name));var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,p.registryAccess());restored.saveWithoutId(out);r.put("Pet",out.buildResult());
            }setDirty();
        }return true;
    }
    public void applyPending(Shardling pet){if(pet.getOwnerReference()==null)return;var r=record(pet.getOwnerReference().getUUID());if(r!=null&&owns(pet.getOwnerReference().getUUID(),pet.getUUID())&&r.contains("PendingName")){pet.setCustomName(net.minecraft.network.chat.Component.literal(r.getStringOr("PendingName","")));r.remove("PendingName");setDirty();}}
}
