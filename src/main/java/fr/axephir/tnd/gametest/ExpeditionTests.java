package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.dungeon.VaultCoreEntity;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.progression.PlayerProgression;
import fr.axephir.tnd.technology.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;

final class ExpeditionTests {
    private static void check(GameTestHelper h,boolean b,String s){h.assertTrue(b,Component.literal(s));}
    private static FracturedCustodian boss(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(16,1,16));
        for(var pos:BlockPos.betweenClosed(at.offset(-12,-1,-12),at.offset(12,-1,12)))h.getLevel().setBlock(pos,ModContent.ASHSTONE_TILES.get().defaultBlockState(),3);
        var b=VestigeEntities.CUSTODIAN.get().create(h.getLevel(),EntitySpawnReason.EVENT);b.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);h.getLevel().addFreshEntity(b);return b;
    }
    static void dodge(GameTestHelper h){
        var b=boss(h);var p=GameplayTests.player(h);p.setPos(b.getX()+4,b.getY(),b.getZ());
        check(h,b.beginAttack(p,false)&&b.charge()==40,"A fracture snapshots its impact with a two-second warning");
        float hp=p.getHealth();p.setPos(b.getX()-4,b.getY(),b.getZ());
        h.runAfterDelay(44,()->{try{check(h,p.getHealth()==hp&&b.exposed()>0,"Moving away avoids the fracture and opens the boss core");}finally{b.discard();GameplayTests.cleanup(p);}h.succeed();});
    }
    static void hit(GameTestHelper h){
        var b=boss(h);var p=GameplayTests.player(h);p.setPos(b.getX()+4,b.getY(),b.getZ());
        var pet=VestigeEntities.SHARDLING.get().create(h.getLevel(),EntitySpawnReason.EVENT);pet.setPos(p.position());pet.setNoAi(true);h.getLevel().addFreshEntity(pet);
        check(h,b.beginAttack(p,false),"A fracture starts");float hp=p.getHealth(),petHp=pet.getHealth();
        h.runAfterDelay(44,()->{try{check(h,p.getHealth()<=hp-6.9f,"Remaining in the actual marked circle inflicts seven damage");check(h,pet.getHealth()==petHp,"The boss area attack never harms pets");float old=b.getHealth();b.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),10);check(h,old-b.getHealth()>13.9f,"An exposed core takes the announced increased damage");}finally{pet.discard();b.discard();GameplayTests.cleanup(p);}h.succeed();});
    }
    static void crown(GameTestHelper h){
        var b=boss(h);var center=GameplayTests.player(h);var ring=GameplayTests.player(h);var outside=GameplayTests.player(h);
        center.setPos(b.getX()+1.9,b.getY(),b.getZ());ring.setPos(b.getX()+4,b.getY(),b.getZ());outside.setPos(b.getX()+9,b.getY(),b.getZ());
        check(h,b.beginAttack(ring,true)&&b.charge()==48,"A crown leaves 2.4 seconds to reach either safe area");
        float centerHp=center.getHealth(),ringHp=ring.getHealth(),outsideHp=outside.getHealth();
        h.runAfterDelay(52,()->{try{check(h,center.getHealth()==centerHp&&outside.getHealth()==outsideHp,"Both the inner refuge and outer region are safe");check(h,ring.getHealth()<=ringHp-6.9f,"Only the announced annulus deals actual damage");}finally{b.discard();GameplayTests.cleanup(center);GameplayTests.cleanup(ring);GameplayTests.cleanup(outside);}h.succeed();});
    }
    static void stagger(GameTestHelper h){
        var b=boss(h);var p=GameplayTests.player(h);p.setPos(b.getX()+4,b.getY(),b.getZ());b.setNoAi(true);
        try{check(h,b.beginAttack(p,false),"Charge starts before player retaliation");float before=b.getHealth();b.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),20);
            check(h,b.charge()==0&&b.exposed()==80,"Eighteen raw damage interrupts the cast and exposes the core for four seconds");
            check(h,before-b.getHealth()>10.9f&&before-b.getHealth()<11.1f,"The closed shell reduces damage to 55 percent");
            check(h,b.getExperienceReward(h.getLevel(),p)==0,"Boss instances cannot farm native XP");
        }finally{b.discard();GameplayTests.cleanup(p);}h.succeed();
    }
    static void phase(GameTestHelper h){
        var b=boss(h);var p=GameplayTests.player(h);p.setPos(b.getX()+4,b.getY(),b.getZ());p.setXRot(90);b.setHealth(60);
        h.runAfterDelay(3,()->{
            check(h,b.enraged()&&b.beginAttack(p,false),"Half health activates the second pattern without increasing base damage");
            float hp=p.getHealth();p.setPos(p.getX(),p.getY(),p.getZ()+3);
            h.runAfterDelay(44,()->{try{check(h,p.getHealth()<=hp-6.9f,"The second marked circle remains distinct even when the target looks vertically");}finally{b.discard();GameplayTests.cleanup(p);}h.succeed();});
        });
    }
    private static VaultCoreEntity vault(GameTestHelper h){
        var w=h.getLevel();var origin=h.absolutePos(new BlockPos(1,1,1));
        var t=w.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","resonance_vault")).orElseThrow();t.placeInWorld(w,origin,origin,new StructurePlaceSettings(),w.getRandom(),2);
        var v=(VaultCoreEntity)w.getBlockEntity(origin.offset(31,1,30));
        // Load a legitimate legacy 0.6 completed sanctuary, with no new boss fields.
        var tag=v.saveWithFullMetadata(w.registryAccess());tag.putInt("completed",3);tag.putInt("active",-1);tag.putBoolean("claimed",true);
        var restored=(VaultCoreEntity)BlockEntity.loadStatic(v.getBlockPos(),v.getBlockState(),tag,w.registryAccess());v.setRemoved();restored.setLevel(w);w.setBlockEntity(restored);return restored;
    }
    static void bossProgress(GameTestHelper h){
        var v=vault(h);var p=GameplayTests.player(h);var other=GameplayTests.player(h);var at=v.getBlockPos();p.setPos(at.getX()+.5,at.getY(),at.getZ()-1);other.setPos(p.position());
        try{
            check(h,!v.summonBoss(p)&&v.bossId()==null,"A legacy completed vault cannot summon without fuel");p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
            var spawn=at.offset(0,0,7);h.getLevel().setBlock(spawn,Blocks.GOLD_BLOCK.defaultBlockState(),3);
            check(h,!v.summonBoss(p)&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4&&h.getLevel().getBlockState(spawn).is(Blocks.GOLD_BLOCK),"An obstructed boss spawn preserves both construction and fuel");h.getLevel().setBlock(spawn,Blocks.AIR.defaultBlockState(),3);
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,p.getInventory().removeItem(0,4));
            v.interact(p);check(h,v.bossId()!=null,"Holding dust activates a legacy completed sanctuary through the actual core interaction");var b=(FracturedCustodian)h.getLevel().getEntity(v.bossId());b.setNoAi(true);
            check(h,!v.summonBoss(p)&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==2,"Multiplayer interaction spawns one boss and spends exactly two dust");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());b.saveWithoutId(out);
            var reloaded=(FracturedCustodian)EntityType.create(VestigeEntities.CUSTODIAN.get(),TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()),h.getLevel(),EntitySpawnReason.LOAD).orElseThrow();
            check(h,reloaded.bound()&&v.acceptsBoss(reloaded.getUUID(),reloaded.trialToken()),"A native entity reload retains boss UUID and sanctuary binding");
            b.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),10000);check(h,b.isDeadOrDying()&&v.bossCleared(),"Only the actual registered boss death clears the encounter");
            v.interact(p);check(h,p.getInventory().countItem(ModContent.INERTIAL_MATRIX.get())==1&&p.getInventory().countItem(ModContent.TEMPERED_ALLOY.get())==6,"The actual core interaction claims exactly one matrix and six alloys even while holding invocation dust");
            check(h,!v.claimBoss(p)&&!v.claimBoss(other)&&!v.summonBoss(p),"Neither a second player nor a repeated click can duplicate a completed boss reward");
            var restored=(VaultCoreEntity)BlockEntity.loadStatic(at,v.getBlockState(),v.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());restored.setLevel(h.getLevel());
            check(h,restored.bossCleared()&&!restored.claimBoss(p),"Claimed boss treasure survives native serialization");
        }finally{v.abort();GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();
    }
    static void bossRecovery(GameTestHelper h){
        var v=vault(h);var p=GameplayTests.player(h);var at=v.getBlockPos();p.setPos(at.getX()+.5,at.getY(),at.getZ()-1);p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        check(h,v.summonBoss(p),"Optional boss begins");var id=v.bossId();var b=(FracturedCustodian)h.getLevel().getEntity(id);b.setNoAi(true);
        var saved=v.saveWithFullMetadata(h.getLevel().registryAccess());var restored=(VaultCoreEntity)BlockEntity.loadStatic(at,v.getBlockState(),saved,h.getLevel().registryAccess());v.setRemoved();restored.setLevel(h.getLevel());h.getLevel().setBlockEntity(restored);
        check(h,id.equals(restored.bossId())&&restored.acceptsBoss(id,b.trialToken()),"Controller reload keeps exactly the existing boss UUID");b.discard();
        h.runAfterDelay(220,()->{try{check(h,restored.bossId()==null&&!restored.bossCleared()&&!restored.claimBoss(p),"A missing entity resets the attempt without victory or treasure");check(h,restored.completed()==3&&restored.summonBoss(p),"Completed seals survive failure and a paid retry works");var retry=restored.bossId();h.getLevel().destroyBlock(at,false);check(h,h.getLevel().getEntity(retry)==null||h.getLevel().getEntity(retry).isRemoved(),"Removing the core cleans up the bound boss");}finally{restored.abort();GameplayTests.cleanup(p);}h.succeed();});
    }
    private static ResonanceForgeEntity forge(GameTestHelper h,BlockPos relative){h.setBlock(relative,MachineRegistry.FORGE.get());return (ResonanceForgeEntity)h.getLevel().getBlockEntity(h.absolutePos(relative));}
    static void forgeProcessing(GameTestHelper h){
        var f=forge(h,new BlockPos(2,1,2));var p=GameplayTests.player(h);var profile=p.getData(PlayerProgression.PROFILE);
        f.setItem(0,new ItemStack(Items.RAW_IRON,3));f.setItem(1,new ItemStack(ModContent.ECHO_DUST.get(),2));
        h.runAfterDelay(ResonanceForgeEntity.PERIOD+25,()->{try{
            check(h,f.getItem(2).is(Items.IRON_INGOT)&&f.getItem(2).getCount()==2&&f.getItem(0).getCount()==2&&f.getItem(1).getCount()==1,"A loaded real forge doubles one raw iron and spends exactly one echo dust");
            check(h,p.getData(PlayerProgression.PROFILE).equals(profile),"Automated processing grants no mastery XP");
            var menu=new ResonanceForgeMenu(3,p.getInventory(),f,f.data);p.setPos(f.getBlockPos().getX()+.5,f.getBlockPos().getY(),f.getBlockPos().getZ()+1);
            check(h,menu.quickMoveStack(p,2).getCount()==2&&p.getInventory().countItem(Items.IRON_INGOT)==2,"Shift-click delivers real output to the player inventory");
            check(h,!menu.getSlot(2).mayPlace(new ItemStack(Items.DIRT)),"Players cannot insert into the result slot");
        }finally{GameplayTests.cleanup(p);}h.succeed();});
    }
    static void forgeRecovery(GameTestHelper h){
        var f=forge(h,new BlockPos(2,1,2));var w=h.getLevel();var at=f.getBlockPos();var state=f.getBlockState();
        f.setItem(0,new ItemStack(ModContent.ANCIENT_ALLOY.get().asItem(),5));f.setItem(1,new ItemStack(ModContent.ECHO_DUST.get(),3));
        var tag=f.saveWithFullMetadata(w.registryAccess());tag.putLong("progress",399);
        // Stop native ticking while its physical inventory remains in the serialized block entity.
        w.removeBlockEntity(at);
        h.runAfterDelay(ResonanceForgeEntity.PERIOD*3+20,()->{
            var restored=(ResonanceForgeEntity)BlockEntity.loadStatic(at,state,tag,w.registryAccess());restored.setLevel(w);w.setBlockEntity(restored);restored.settle();
            check(h,restored.getItem(2).getCount()==3&&restored.getItem(0).getCount()==2&&restored.getItem(1).isEmpty(),"Native reload catch-up is limited by finite dust and keeps exact physical inventory");
            restored.setItem(1,new ItemStack(ModContent.ECHO_DUST.get()));check(h,restored.getItem(2).getCount()==3,"New fuel cannot retroactively use blocked time");
            restored.setItem(2,new ItemStack(Items.GOLD_INGOT));restored.settle();check(h,restored.status()==2,"Incompatible output pauses without replacing any item");
            restored.setItem(2,ItemStack.EMPTY);w.setBlock(at.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);restored.settle();check(h,restored.status()==3&&restored.getItem(0).getCount()==2,"Redstone pauses real processing without consuming input");
            var pausedTag=restored.saveWithFullMetadata(w.registryAccess());pausedTag.putLong("lastTick",0);w.setBlock(at.east(),Blocks.AIR.defaultBlockState(),3);
            var unpaused=(ResonanceForgeEntity)BlockEntity.loadStatic(at,state,pausedTag,w.registryAccess());restored.setRemoved();unpaused.setLevel(w);w.setBlockEntity(unpaused);unpaused.settle();
            check(h,unpaused.getItem(2).isEmpty()&&unpaused.getItem(1).getCount()==1,"A persisted redstone pause never produces a backlog when power disappears before reload");
            check(h,ResonanceForgeEntity.result(new ItemStack(Items.RAW_COPPER)).getCount()==2&&ResonanceForgeEntity.result(new ItemStack(Items.RAW_GOLD)).is(Items.GOLD_INGOT),"Copper and gold use the same explicit doubling chain");
            check(h,!ResonanceForgeEntity.validInput(new ItemStack(ModContent.TEMPERED_ALLOY.get()))&&!ResonanceForgeEntity.validFuel(new ItemStack(Items.COAL)),"Output cannot be fed back as input or invalid fuel");
            w.destroyBlock(at,true);check(h,w.getBlockState(at).isAir(),"Breaking an active forge never reinstalls its lit block state");
            var drops=w.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(2));
            check(h,drops.stream().filter(e->e.getItem().is(ModContent.ANCIENT_ALLOY.get().asItem())).mapToInt(e->e.getItem().getCount()).sum()==2
                    &&drops.stream().filter(e->e.getItem().is(ModContent.ECHO_DUST.get())).mapToInt(e->e.getItem().getCount()).sum()==1,"Breaking drops the exact remaining input and fuel once");
            h.succeed();
        });
    }
    static void forgeHoppers(GameTestHelper h){
        var rel=new BlockPos(3,2,3);var f=forge(h,rel);var at=f.getBlockPos();var w=h.getLevel();
        w.setBlock(at.above(),Blocks.HOPPER.defaultBlockState(),3);w.setBlock(at.west(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.EAST),3);w.setBlock(at.below(),Blocks.HOPPER.defaultBlockState(),3);
        var input=(HopperBlockEntity)w.getBlockEntity(at.above());var fuel=(HopperBlockEntity)w.getBlockEntity(at.west());var output=(HopperBlockEntity)w.getBlockEntity(at.below());
        input.setItem(0,new ItemStack(Items.RAW_GOLD));fuel.setItem(0,new ItemStack(ModContent.ECHO_DUST.get()));
        h.runAfterDelay(500,()->{
            check(h,input.isEmpty()&&fuel.isEmpty()&&output.countItem(Items.GOLD_INGOT)==2,"Actual vanilla hoppers insert above/from the side and extract only the processed output below");
            check(h,output.countItem(ModContent.ECHO_DUST.get())==0&&output.countItem(Items.RAW_GOLD)==0,"Bottom hoppers never steal fuel or unprocessed inputs");h.succeed();
        });
    }
    private ExpeditionTests(){}
}
