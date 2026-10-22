package fr.axephir.tnd.familiar;

import fr.axephir.tnd.entities.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** The computer persists the released subject. Reloading, repeated clicks or a second player cannot duplicate it. */
public final class IncubationComputerEntity extends BlockEntity {
    private boolean opened;private int opening;private UUID challenger,subject;
    public IncubationComputerEntity(BlockPos p,BlockState s){super(FamiliarContent.COMPUTER_ENTITY.get(),p,s);}
    public boolean opened(){return opened;}public int opening(){return opening;}
    public boolean start(ServerPlayer p){
        if(level!=p.level()||p.distanceToSqr(Vec3.atCenterOf(worldPosition))>64)return false;
        if(opened||opening>0){p.sendOverlayMessage(Component.translatable(opened?"familiar.tnd.released":"familiar.tnd.opening"));return false;}
        if(FamiliarLedger.get(p.level().getServer()).record(p.getUUID())!=null){p.sendOverlayMessage(Component.translatable("familiar.tnd.one_only"));return false;}
        // Only a complete physical chamber can be activated. A copied bare computer has no subject.
        var center=worldPosition.offset(0,0,6);
        if(!level.getBlockState(center.below()).is(FamiliarContent.POD.get())||!level.getBlockState(center.above()).is(FamiliarContent.POD.get())){p.sendOverlayMessage(Component.translatable("familiar.tnd.missing_pod"));return false;}
        challenger=p.getUUID();opening=80;setChanged();p.sendSystemMessage(Component.translatable("familiar.tnd.warning"));
        level.playSound(null,worldPosition,fr.axephir.tnd.core.ModContent.atmosphereSound("incubator_start"),SoundSource.BLOCKS,.7f,.6f);return true;
    }
    public void tick(){
        if(!(level instanceof ServerLevel w)||opening<=0)return;
        var p=w.getServer().getPlayerList().getPlayer(challenger);
        if(p==null||p.level()!=w||p.distanceToSqr(Vec3.atCenterOf(worldPosition))>32*32){opening=0;challenger=null;setChanged();return;}
        if(--opening%10==0){var at=worldPosition.offset(0,0,6);w.sendParticles(ParticleTypes.ELECTRIC_SPARK,at.getX()+.5,at.getY()+1,at.getZ()+.5,12,1.4,1.7,1.4,.01);}
        if(opening!=0)return;
        if(FamiliarLedger.get(w.getServer()).record(p.getUUID())!=null){p.sendOverlayMessage(Component.translatable("familiar.tnd.one_only"));setChanged();return;}
        var at=worldPosition.offset(0,0,6);var pet=VestigeEntities.SHARDLING.get().create(w,EntitySpawnReason.EVENT);pet.setPos(Vec3.atBottomCenterOf(at));
        // Clear only authored capsule components, never player blocks; reject any genuine obstruction first.
        var area=pet.getBoundingBox();for(var cell:BlockPos.betweenClosed(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ))){var s=w.getBlockState(cell);if(!s.isAir()&&!s.is(FamiliarContent.POD.get())&&!s.is(fr.axephir.tnd.city.CityContent.DECOR.get("relay_coil").get())){p.sendOverlayMessage(Component.translatable("familiar.tnd.deploy_blocked"));setChanged();return;}}
        for(int y=0;y<=2;y++){var cell=at.above(y);if(w.getBlockState(cell).is(FamiliarContent.POD.get())||w.getBlockState(cell).is(fr.axephir.tnd.city.CityContent.DECOR.get("relay_coil").get()))w.setBlock(cell,Blocks.AIR.defaultBlockState(),3);}
        pet.releaseFor(p);if(!w.addFreshEntity(pet)){setChanged();return;}
        for(int dx=-1;dx<=1;dx++)for(int y=0;y<=3;y++){var cell=at.offset(dx,y,-2);if(w.getBlockState(cell).is(FamiliarContent.GLASS.get()))w.setBlock(cell,Blocks.AIR.defaultBlockState(),3);}
        subject=pet.getUUID();opened=true;setChanged();w.playSound(null,at,fr.axephir.tnd.core.ModContent.atmosphereSound("incubator_release"),SoundSource.BLOCKS,.9f,.6f);p.sendOverlayMessage(Component.translatable("familiar.tnd.defend"));
    }
    @Override protected void saveAdditional(ValueOutput o){super.saveAdditional(o);o.putBoolean("Released",opened);if(subject!=null)o.putString("Subject",subject.toString());}
    @Override protected void loadAdditional(ValueInput i){super.loadAdditional(i);opened=i.getBooleanOr("Released",false);opening=0;challenger=null;try{subject=UUID.fromString(i.getStringOr("Subject",""));}catch(IllegalArgumentException ignored){subject=null;}}
}
