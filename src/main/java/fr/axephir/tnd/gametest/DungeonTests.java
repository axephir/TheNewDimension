package fr.axephir.tnd.gametest;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.dungeon.*;
import fr.axephir.tnd.dungeon.VaultBlock;
import fr.axephir.tnd.entities.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;

final class DungeonTests {
    private static void check(GameTestHelper h,boolean b,String s){h.assertTrue(b,Component.literal(s));}
    private static VaultCoreEntity fixture(GameTestHelper h){
        var world=h.getLevel();var origin=h.absolutePos(new BlockPos(1,1,1));
        var template=world.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","resonance_vault")).orElseThrow();
        check(h,template.placeInWorld(world,origin,origin,new StructurePlaceSettings(),world.getRandom(),2),"The authored sanctuary places with native block entities");
        return (VaultCoreEntity)world.getBlockEntity(origin.offset(31,1,30));
    }
    private static void beside(ServerPlayer p,BlockPos at){p.setPos(at.getX()+.5,at.getY(),at.getZ()-1.5);}
    private static void defeat(GameTestHelper h,VaultCoreEntity core,ServerPlayer p){
        var ids=core.guardians();check(h,!ids.isEmpty(),"Trial spawns actual registered entities");
        for(var id:ids){
            var mob=(RiftSentinel)h.getLevel().getEntity(id);check(h,mob!=null,"Guardian is a real loaded sentinel");
            check(h,mob.awake()&&mob.getExperienceReward(h.getLevel(),p)==0,"Trial guardians work by day without farmable native XP");
            mob.setNoAi(true);mob.hurtServer(h.getLevel(),p.damageSources().playerAttack(p),10000);
            check(h,mob.isDeadOrDying(),"A real damaging attack kills the guardian");
        }
    }
    static void progress(GameTestHelper h){
        var p=GameplayTests.player(h);var other=GameplayTests.player(h);var vault=fixture(h);
        try{
            p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));beside(p,vault.sealPos(1));
            check(h,!vault.start(p,1)&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==4,"Out-of-order activation costs nothing");
            for(int i=0;i<3;i++){
                beside(p,vault.sealPos(i));check(h,vault.start(p,i),"Correct seal starts stage "+i);
                check(h,vault.guardians().size()==i+1,"Each stage has its specified finite guard count");
                check(h,!vault.start(p,i),"Repeated interaction cannot duplicate guardians");
                defeat(h,vault,p);check(h,vault.completed()==i+1&&vault.active()==-1,"Only actual deaths complete the stage");
                check(h,h.getLevel().getBlockState(vault.sealPos(i)).getValue(VaultBlock.LIT),"Completed seal has a persistent visible lit state");
            }
            check(h,p.getInventory().countItem(ModContent.ECHO_DUST.get())==1,"Three trials consume exactly three dust");
            beside(p,vault.getBlockPos());check(h,vault.claim(p),"Securing all wings unlocks the shared reward");
            check(h,p.getInventory().countItem(ModContent.RESONANCE_HEART.get())==1&&p.getInventory().countItem(ModContent.RELIC_FRAGMENT.get())==4,"Reward contains the heart and four fragments");
            check(h,!vault.claim(p),"Repeated clicks cannot duplicate the treasure");
            beside(other,vault.getBlockPos());check(h,!vault.claim(other)&&other.getInventory().countItem(ModContent.RESONANCE_HEART.get())==0,"A second player cannot claim the same shared treasure");
            var restored=(VaultCoreEntity)BlockEntity.loadStatic(vault.getBlockPos(),vault.getBlockState(),vault.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());restored.setLevel(h.getLevel());
            check(h,restored.completed()==3&&!restored.claim(p),"Reloading retains both progression and the claimed flag");
        }finally{vault.abort();GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();
    }
    static void recovery(GameTestHelper h){
        var p=GameplayTests.player(h);var vault=fixture(h);p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),4));
        beside(p,vault.sealPos(0));check(h,vault.start(p,0),"First trial starts");defeat(h,vault,p);
        beside(p,vault.sealPos(1));check(h,vault.start(p,1),"Second trial starts");
        var ids=vault.guardians();for(var id:ids)((RiftSentinel)h.getLevel().getEntity(id)).setNoAi(true);
        var saved=vault.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored=(VaultCoreEntity)BlockEntity.loadStatic(vault.getBlockPos(),vault.getBlockState(),saved,h.getLevel().registryAccess());
        vault.setRemoved();restored.setLevel(h.getLevel());h.getLevel().setBlockEntity(restored);
        check(h,restored.completed()==1&&restored.guardians().equals(ids),"A native reload retains completed wings and the existing live UUIDs");
        var first=(RiftSentinel)h.getLevel().getEntity(ids.iterator().next());
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());first.saveWithoutId(output);
        var reloaded=(RiftSentinel)EntityType.create(VestigeEntities.SENTINEL.get(),TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),output.buildResult()),h.getLevel(),EntitySpawnReason.LOAD).orElseThrow();
        check(h,reloaded.isTrialGuardian()&&reloaded.trialToken().equals(restored.token()),"Guardian binding survives entity serialization");
        first.discard(); // This is not a kill, as when an entity is removed by another mod.
        h.runAfterDelay(220,()->{
            try{
                check(h,restored.completed()==1&&restored.active()==-1,"Missing guardians reset only the unfinished trial, without granting victory");
                check(h,p.getInventory().countItem(ModContent.RESONANCE_HEART.get())==0,"An interrupted trial cannot create treasure");
                beside(p,restored.sealPos(1));check(h,restored.start(p,1),"An interrupted wing can be retried");
                check(h,restored.guardians().size()==2&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==1,"Retry creates exactly one fresh group and charges one dust");
            }finally{restored.abort();GameplayTests.cleanup(p);}h.succeed();
        });
    }
    static void safety(GameTestHelper h){
        var p=GameplayTests.player(h);var vault=fixture(h);
        try{
            beside(p,vault.sealPos(0));check(h,!vault.start(p,0),"A survival player needs dust");
            p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),2));
            var obstruction=vault.sealPos(0).offset(-3,0,4);h.getLevel().setBlock(obstruction,Blocks.GOLD_BLOCK.defaultBlockState(),3);
            check(h,!vault.start(p,0)&&vault.guardians().isEmpty(),"An obstructed spawn does not begin a partial trial");
            check(h,h.getLevel().getBlockState(obstruction).is(Blocks.GOLD_BLOCK)&&p.getInventory().countItem(ModContent.ECHO_DUST.get())==2,"Failure preserves blocks and fuel");
            h.getLevel().setBlock(obstruction,Blocks.AIR.defaultBlockState(),3);
            check(h,vault.start(p,0),"Clearing the existing obstruction allows activation");
            var guards=vault.guardians();h.getLevel().destroyBlock(vault.getBlockPos(),false);
            check(h,guards.stream().allMatch(id->h.getLevel().getEntity(id)==null||h.getLevel().getEntity(id).isRemoved()),"Breaking the controller cleans up its guardians");
            h.getLevel().setBlock(vault.getBlockPos(),ModContent.VAULT_CORE.get().defaultBlockState(),3);
            var placed=(VaultCoreEntity)h.getLevel().getBlockEntity(vault.getBlockPos());beside(p,placed.sealPos(0));
            check(h,!placed.start(p,0)&&placed.guardians().isEmpty(),"A player-placed core has no natural trial or reward authority");
        }finally{vault.abort();GameplayTests.cleanup(p);}h.succeed();
    }
    static void rotations(GameTestHelper h){
        var world=h.getLevel();var template=world.getStructureManager().get(Identifier.fromNamespaceAndPath("tnd","resonance_vault")).orElseThrow();
        for(var mirror:Mirror.values())for(var rotation:Rotation.values()){
            var settings=new StructurePlaceSettings().setRotation(rotation).setMirror(mirror);
            // Shift the real transformed bounding box into the loaded test fixture.
            var bounds=template.getBoundingBox(settings,BlockPos.ZERO);var origin=h.absolutePos(new BlockPos(1-bounds.minX(),1,1-bounds.minZ()));
            check(h,template.placeInWorld(world,origin,origin,settings,world.getRandom(),2),"Rotated sanctuary places");
            var corePos=origin.offset(StructureTemplate.transform(new BlockPos(31,1,30),mirror,rotation,BlockPos.ZERO));
            var core=(VaultCoreEntity)world.getBlockEntity(corePos);check(h,core!=null,"Rotated core retains its native entity");
            for(int i=0;i<3;i++){
                var state=world.getBlockState(core.sealPos(i));
                check(h,!state.hasBlockEntity(),"A seal is an ordinary block and must not request a missing block entity on chunk reload");
                check(h,state.is(ModContent.VAULT_SEAL.get())&&state.getValue(VaultBlock.SEAL)==i,"Core finds the correct seal after "+mirror+" / "+rotation);
                check(h,core.sealPos(i).subtract(VaultBlock.offset(state,i)).equals(corePos),"Seal resolves the same core after transformation");
            }
        }
        h.succeed();
    }
    static void aegis(GameTestHelper h){
        var p=GameplayTests.player(h);var other=GameplayTests.player(h);var item=new ItemStack(ModContent.RESONANT_AEGIS.get());
        try{
            check(h,!ResonantAegisItem.activate(p,item),"An empty aegis cannot activate");p.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get(),2));
            check(h,ResonantAegisItem.activate(p,item)&&p.getAbsorptionAmount()==4,"Activation creates two actual native absorption hearts");
            check(h,p.getInventory().countItem(ModContent.ECHO_DUST.get())==1&&!ResonantAegisItem.activate(p,item),"Fuel is consumed once and native cooldown prevents repeated activation");
            float health=p.getHealth();p.hurtServer(h.getLevel(),p.damageSources().generic(),2);
            check(h,p.getHealth()==health&&p.getAbsorptionAmount()==2,"The effect actually absorbs incoming damage");
            other.getInventory().add(new ItemStack(ModContent.ECHO_DUST.get()));other.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,1200,1));
            check(h,!ResonantAegisItem.activate(other,item)&&other.getAbsorptionAmount()==8&&other.getInventory().countItem(ModContent.ECHO_DUST.get())==1,"Existing stronger absorption is neither overwritten nor charged");
        }finally{GameplayTests.cleanup(p);GameplayTests.cleanup(other);}h.succeed();
    }
    private DungeonTests(){}
}
