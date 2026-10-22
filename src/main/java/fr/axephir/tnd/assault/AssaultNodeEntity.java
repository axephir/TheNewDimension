package fr.axephir.tnd.assault;

import fr.axephir.tnd.city.*;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.DevelopmentCommands;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Finite natural encounters. Successful insertions are saved; unloading never replenishes the guards or boss. */
public final class AssaultNodeEntity extends BlockEntity {
    private int spawned,blackout;private long lastTick=-1;
    public AssaultNodeEntity(BlockPos p,BlockState s){super(AssaultContent.NODE_ENTITY.get(),p,s);}
    public int spawned(){return spawned;}
    public void tick(){
        if(!(level instanceof ServerLevel w)||lastTick==w.getGameTime())return;lastTick=w.getGameTime();
        if(!w.dimension().equals(DevelopmentCommands.DIMENSION)||!w.getChunkAt(worldPosition).getData(CityRegions.GENERATED))return;
        if(blackout>0){blackout=Math.max(0,blackout-20);if(blackout==0)lighting(true);setChanged();}
        boolean roof=getBlockState().getValue(AssaultNodeBlock.SUMMIT);int limit=roof?1:4;
        if(spawned>=limit||w.getDifficulty()==Difficulty.PEACEFUL)return;
        ServerPlayer target=w.players().stream().filter(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&Math.abs(p.getY()-worldPosition.getY())<5&&p.position().distanceToSqr(Vec3.atCenterOf(worldPosition))<20*20).findFirst().orElse(null);
        if(target==null||w.getEntitiesOfClass(CityMob.class,new AABB(worldPosition).inflate(28,14,28)).size()>=16)return;
        var family=getBlockState().getValue(AssaultNodeBlock.FAMILY);
        var kind=roof?switch(family){case WARD->CityMob.Kind.MARSHAL;case SHADE->CityMob.Kind.PROWLER;case CHOIR->CityMob.Kind.SURGEON;}:
                spawned==3?CityMob.Kind.DRONE:spawned==1?CityMob.Kind.REAVER:CityMob.Kind.ENFORCER;
        for(int[] d:new int[][]{{4,0},{-4,0},{0,4},{0,-4},{4,4},{-4,-4}}){
            var at=worldPosition.offset(d[0],0,d[1]);if(!w.hasChunkAt(at)||!w.getBlockState(at.below()).isFaceSturdy(w,at.below(),Direction.UP))continue;
            var mob=CityMobs.TYPES.get(kind).get().create(w,EntitySpawnReason.EVENT);if(mob==null)return;mob.setPos(Vec3.atBottomCenterOf(at));
            if(!w.noCollision(mob)||!w.getFluidState(at).isEmpty()){mob.discard();continue;}
            mob.setPersistenceRequired();mob.setTarget(target);mob.bindPost(worldPosition);
            if(!w.addFreshEntity(mob)){mob.discard();return;}spawned++;setChanged();
            if(spawned==1){blackout=160;lighting(false);w.setBlock(worldPosition,getBlockState().setValue(AssaultNodeBlock.LIT,true),3);w.playSound(null,worldPosition,ModContent.atmosphereSound("city_alarm"),SoundSource.HOSTILE,.6f,roof?.65f:1.15f);}
            return;
        }
    }
    private void lighting(boolean lit){
        if(!(level instanceof ServerLevel w))return;
        for(var at:BlockPos.betweenClosed(worldPosition.offset(-12,0,-12),worldPosition.offset(12,6,12))){
            if(!w.hasChunkAt(at))continue;var state=w.getBlockState(at);
            if(state.is(AssaultContent.LAMP.get())){w.setBlock(at,state.setValue(AssaultLampBlock.LIT,lit),3);if(!lit)w.scheduleTick(at.immutable(),AssaultContent.LAMP.get(),160);}
        }
    }
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("spawned",spawned);out.putInt("blackout",blackout);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);spawned=Math.clamp(in.getIntOr("spawned",0),0,4);blackout=Math.clamp(in.getIntOr("blackout",0),0,160);}
}
