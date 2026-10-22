package fr.axephir.tnd.gametest;

import fr.axephir.tnd.technology.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Production, conservation, actual plants/animals, real hoppers and native saved machine inventories. */
final class IndustryTests {
    private static void check(GameTestHelper h,boolean b,String m){h.assertTrue(b,Component.literal(m));}
    private static IndustrialEntity machine(GameTestHelper h,int x,int y,int z,Industry.Kind k){var p=h.absolutePos(new BlockPos(x,y,z));h.getLevel().setBlock(p,Industry.BLOCKS.get(k).get().defaultBlockState(),3);return (IndustrialEntity)h.getLevel().getBlockEntity(p);}
    static void foundation(GameTestHelper h,IndustrialEntity e){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)h.getLevel().setBlock(e.getBlockPos().offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),3);}
    static void derrick(GameTestHelper h,IndustrialEntity e){var w=h.getLevel();var p=e.getBlockPos();w.setBlock(p.above(),Industry.FRAME.get().defaultBlockState(),3);for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(Math.abs(x)==3||Math.abs(z)==3)w.setBlock(p.offset(x,-1,z),Industry.FRAME.get().defaultBlockState(),3);for(int x:new int[]{-3,3})for(int z:new int[]{-3,3})for(int y=0;y<3;y++)w.setBlock(p.offset(x,y,z),Industry.FRAME.get().defaultBlockState(),3);}
    private static void arm(GameTestHelper h,IndustrialEntity e){var p=GameplayTests.player(h);try{p.setPos(Vec3.atCenterOf(e.getBlockPos().above()));check(h,e.toggleQuarry(p),"Quarry must be deliberately armed by a nearby player");}finally{GameplayTests.cleanup(p);}}
    static void energy(GameTestHelper h){
        var battery=machine(h,8,2,8,Industry.Kind.BATTERY);var crusher=machine(h,12,2,8,Industry.Kind.CRUSHER);battery.energy.set(20000);
        var port=h.getLevel().getCapability(Capabilities.Energy.BLOCK,crusher.getBlockPos(),Direction.WEST);check(h,port!=null,"Native FE capability is registered");
        try(var tx=Transaction.openRoot()){check(h,port.insert(5000,tx)==5000,"Transactional insertion accepts FE");}
        check(h,crusher.energy.getAmountAsInt()==0,"Aborted transaction rolls back all FE");
        for(int x=9;x<12;x++)h.getLevel().setBlock(h.absolutePos(new BlockPos(x,2,8)),Industry.CABLE.get().defaultBlockState(),3);
        h.runAfterDelay(35,()->{check(h,crusher.energy.getAmountAsInt()>0&&battery.energy.getAmountAsInt()+crusher.energy.getAmountAsInt()==20000,"Real cable ticks conserve exact total FE without consumer work");
            int before=crusher.energy.getAmountAsInt();h.getLevel().setBlock(h.absolutePos(new BlockPos(10,2,8)),Blocks.AIR.defaultBlockState(),3);
            h.runAfterDelay(25,()->{check(h,crusher.energy.getAmountAsInt()==before,"Breaking a cable immediately disconnects the network");h.succeed();});});
    }
    static void generation(GameTestHelper h){
        var e=machine(h,8,2,8,Industry.Kind.DYNAMO);e.setItem(0,new ItemStack(Items.COAL));
        h.runAfterDelay(275,()->{check(h,e.energy.getAmountAsInt()==8000&&e.getItem(0).isEmpty(),"One real coal produces exactly 8000 FE across native ticks");e.setItem(0,new ItemStack(Items.CHARCOAL));h.getLevel().setBlock(e.getBlockPos().east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
            h.runAfterDelay(35,()->{check(h,e.energy.getAmountAsInt()==8000&&e.getItem(0).getCount()==1&&e.status()==3,"Redstone prevents both burning and fuel consumption");h.succeed();});});
    }
    static void processing(GameTestHelper h){
        var c=machine(h,8,2,8,Industry.Kind.CRUSHER);c.energy.set(10000);c.setItem(0,new ItemStack(Items.RAW_IRON,2));
        h.runAfterDelay(100,()->{check(h,c.countItem(Industry.IRON_DUST.get())==0&&c.getItem(0).getCount()==2&&c.status()==6,"Processing requires its actual frame foundation");foundation(h,c);
            h.runAfterDelay(175,()->{check(h,c.countItem(Industry.IRON_DUST.get())==4&&c.getItem(0).isEmpty()&&c.energy.getAmountAsInt()==5200,"Crusher consumes exactly two raws and 4800 FE for four grit");
                check(h,IndustrialEntity.crushed(new ItemStack(Industry.IRON_DUST.get())).isEmpty()&&IndustrialEntity.crushed(new ItemStack(Items.IRON_INGOT)).isEmpty(),"Processed metal cannot loop through doubling again");
                var kiln=machine(h,15,2,8,Industry.Kind.FURNACE);foundation(h,kiln);kiln.energy.set(2400);kiln.setItem(0,c.removeItem(2,2));
                h.runAfterDelay(140,()->{check(h,kiln.countItem(Items.IRON_INGOT)==2&&kiln.energy.getAmountAsInt()==0,"Native electric kiln turns real crusher outputs into exactly two ingots");h.succeed();});});});
    }
    static void hoppers(GameTestHelper h){
        var e=machine(h,8,3,8,Industry.Kind.FURNACE);foundation(h,e);e.energy.set(2400);var w=h.getLevel();w.setBlock(e.getBlockPos().above(),Blocks.HOPPER.defaultBlockState(),3);w.setBlock(e.getBlockPos().below(),Blocks.HOPPER.defaultBlockState(),3);
        var top=(HopperBlockEntity)w.getBlockEntity(e.getBlockPos().above());var bottom=(HopperBlockEntity)w.getBlockEntity(e.getBlockPos().below());top.setItem(0,new ItemStack(Industry.GOLD_DUST.get(),2));
        h.runAfterDelay(170,()->{check(h,top.isEmpty()&&e.isEmpty()&&bottom.countItem(Items.GOLD_INGOT)==2,"Real hoppers insert valid feedstock above and extract only products below");h.succeed();});
    }
    static void quarry(GameTestHelper h){
        var e=machine(h,10,5,10,Industry.Kind.QUARRY);derrick(h,e);e.energy.set(10000);e.setItem(0,new ItemStack(Industry.DRILL.get()));var target=e.getBlockPos().offset(-3,-2,-3);h.getLevel().setBlock(target,Blocks.DIAMOND_ORE.defaultBlockState(),3);
        h.runAfterDelay(35,()->{check(h,h.getLevel().getBlockState(target).is(Blocks.DIAMOND_ORE)&&e.status()==10,"A newly placed quarry never starts excavating by itself");arm(h,e);
            h.runAfterDelay(30,()->{check(h,h.getLevel().isEmptyBlock(target)&&e.countItem(Items.DIAMOND)==1,"Armed quarry removes actual native ore and stores its real pickaxe loot");check(h,e.energy.getAmountAsInt()==5200&&e.drillRemaining()==1023,"Exactly 4800 FE and one stored drill use are charged");h.succeed();});});
    }
    static void quarryHopper(GameTestHelper h){
        var e=machine(h,10,5,10,Industry.Kind.QUARRY);derrick(h,e);e.energy.set(10000);var w=h.getLevel();var at=e.getBlockPos();
        w.setBlock(at.below(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.EAST),3);w.setBlock(at.below().east(),Blocks.CHEST.defaultBlockState(),3);
        w.setBlock(at.east(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.WEST),3);var feed=(HopperBlockEntity)w.getBlockEntity(at.east());feed.setItem(0,new ItemStack(Industry.DRILL.get()));
        w.setBlock(at.offset(-3,-2,-3),Blocks.DIAMOND_ORE.defaultBlockState(),3);arm(h,e);
        h.runAfterDelay(65,()->{check(h,e.derrickBuilt(),"The complete derrick leaves a physical hopper outlet below its controller");check(h,feed.isEmpty()&&((ChestBlockEntity)w.getBlockEntity(at.below().east())).countItem(Items.DIAMOND)==1,"Real side hopper supplies a drill and bottom hopper exports mined diamond into a real chest");check(h,e.drillRemaining()==1023,"Automated insertion charges a drill exactly once");h.succeed();});
    }
    static void quarryGuards(GameTestHelper h){
        var e=machine(h,10,5,10,Industry.Kind.QUARRY);derrick(h,e);arm(h,e);e.energy.set(10000);e.setItem(0,new ItemStack(Industry.DRILL.get()));var at=e.getBlockPos().offset(-3,-2,-3);var w=h.getLevel();w.setBlock(at,Blocks.CHEST.defaultBlockState(),3);((ChestBlockEntity)w.getBlockEntity(at)).setItem(0,new ItemStack(Items.DIAMOND,7));w.setBlock(at.east(),Blocks.BEDROCK.defaultBlockState(),3);w.setBlock(at.east(2),Blocks.IRON_ORE.defaultBlockState(),3);
        for(int i=2;i<11;i++)e.setItem(i,new ItemStack(Items.COBBLESTONE,64));
        h.runAfterDelay(40,()->{check(h,((ChestBlockEntity)w.getBlockEntity(at)).countItem(Items.DIAMOND)==7&&w.getBlockState(at.east()).is(Blocks.BEDROCK),"Quarry preserves block entities and unbreakable terrain");check(h,w.getBlockState(at.east(2)).is(Blocks.IRON_ORE)&&e.energy.getAmountAsInt()==10000&&e.getItem(0).getCount()==1,"Full output preserves ore, fuel and drill without loss");
            w.setBlock(e.getBlockPos().offset(-3,2,-3),Blocks.AIR.defaultBlockState(),3);e.clearContent();e.setItem(0,new ItemStack(Industry.DRILL.get()));h.runAfterDelay(35,()->{check(h,e.status()==6&&w.getBlockState(at.east(2)).is(Blocks.IRON_ORE),"Removing a required derrick pillar disables extraction");h.succeed();});});
    }
    static void farming(GameTestHelper h){
        var e=machine(h,10,2,10,Industry.Kind.FARM);e.energy.set(8000);var p=e.getBlockPos().east(2);var w=h.getLevel();w.setBlock(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7),3);w.setBlock(p,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),3);w.setBlock(p.above(),Blocks.GLOWSTONE.defaultBlockState(),3);
        h.runAfterDelay(55,()->{check(h,e.countItem(Items.WHEAT)==1&&w.getBlockState(p).is(Blocks.WHEAT)&&w.getBlockState(p).getValue(CropBlock.AGE)<7,"Cultivator stores a real harvest and replants the existing crop");check(h,e.energy.getAmountAsInt()==7200,"One harvest uses exactly 800 FE");h.succeed();});
    }
    private static void penFloor(GameTestHelper h,IndustrialEntity e){for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)h.getLevel().setBlock(e.getBlockPos().offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);}
    static void ranching(GameTestHelper h){
        var e=machine(h,10,2,10,Industry.Kind.RANCH);e.energy.set(8000);e.setItem(0,new ItemStack(Items.WHEAT,4));penFloor(h,e);var w=h.getLevel();var at=e.getBlockPos();
        // A real enclosed pen keeps native navigation inside the machine's working area.
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)if(Math.abs(x)==5||Math.abs(z)==5)w.setBlock(at.offset(x,0,z),Blocks.OAK_FENCE.defaultBlockState(),3);
        var chunks=new java.util.ArrayList<net.minecraft.world.level.ChunkPos>();
        for(int x=(at.getX()>>4)-1;x<=(at.getX()>>4)+1;x++)for(int z=(at.getZ()>>4)-1;z<=(at.getZ()>>4)+1;z++){w.setChunkForced(x,z,true);chunks.add(new net.minecraft.world.level.ChunkPos(x,z));}
        var cows=new java.util.ArrayList<Animal>();
        for(int i=0;i<2;i++){var cow=EntityTypes.COW.create(w,EntitySpawnReason.EVENT);cow.setNoAi(true);cow.setPos(Vec3.atBottomCenterOf(at.east(2+i)));w.addFreshEntity(cow);cows.add(cow);}
        boolean[] fed={false};
        h.onEachTick(()->{
            if(fed[0]||!cows.stream().allMatch(Animal::isInLove))return;
            check(h,e.getItem(0).getCount()==2&&e.energy.getAmountAsInt()==6400,"Exactly two food items and 1600 FE feed the native adult pair");
            // Block ticking and entity ticking become ready asynchronously on a cold server.
            if(!cows.stream().allMatch(c->w.isPositionEntityTicking(c.blockPosition())))return;
            fed[0]=true;for(var cow:cows)cow.setNoAi(false);
        });
        h.succeedWhen(()->{
            check(h,fed[0],"The ranch feeds both adults in entity-ticking chunks");
            var herd=w.getEntitiesOfClass(Animal.class,new AABB(at).inflate(6));
            check(h,herd.stream().filter(Animal::isBaby).count()==1&&cows.stream().allMatch(c->c.isAlive()&&c.getAge()>0),
                    "Native AI must produce one baby and apply both parent cooldowns; "+cows.stream().map(c->"ticks="+c.tickCount+", age="+c.getAge()+", love="+c.isInLove()+", pos="+c.blockPosition()).toList());
            check(h,e.getItem(0).getCount()==2&&e.energy.getAmountAsInt()==6400,"Cooldown parents cannot consume more feed or energy");
            for(var animal:herd)animal.discard();for(var chunk:chunks)w.setChunkForced(chunk.x(),chunk.z(),false);
        });
    }
    static void pasture(GameTestHelper h){
        var e=machine(h,10,2,10,Industry.Kind.PASTURE);e.energy.set(8000);e.setItem(0,new ItemStack(Items.SHEARS));penFloor(h,e);var sheep=EntityTypes.SHEEP.create(h.getLevel(),EntitySpawnReason.EVENT);sheep.setNoAi(true);sheep.setPos(Vec3.atBottomCenterOf(e.getBlockPos().east(2)));h.getLevel().addFreshEntity(sheep);
        h.runAfterDelay(160,()->{check(h,sheep.isSheared()&&e.getItem(0).getDamageValue()==1,"Native shearing sets the actual sheep state and wears the real tool once");int wool=0;for(int i=2;i<11;i++)if(e.getItem(i).is(net.minecraft.tags.ItemTags.WOOL))wool+=e.getItem(i).getCount();check(h,wool>=1&&wool<=3,"Collector stores only native shearing loot");sheep.discard();for(int i=2;i<11;i++)e.setItem(i,new ItemStack(Items.COBBLESTONE,64));var egg=new ItemEntity(h.getLevel(),e.getBlockPos().getX()+2.5,e.getBlockPos().getY()+.1,e.getBlockPos().getZ()+.5,new ItemStack(Items.EGG));h.getLevel().addFreshEntity(egg);int stored=e.energy.getAmountAsInt();
            h.runAfterDelay(45,()->{check(h,egg.isAlive()&&egg.getItem().getCount()==1&&e.energy.getAmountAsInt()==stored&&e.status()==2,"Full pastoral output leaves the actual dropped egg and all FE intact and reports storage saturation");egg.discard();h.succeed();});});
    }
    static void recovery(GameTestHelper h){
        var e=machine(h,10,5,10,Industry.Kind.QUARRY);derrick(h,e);arm(h,e);e.energy.set(75000);e.setItem(0,new ItemStack(Industry.DRILL.get(),2));e.setItem(2,new ItemStack(Items.DIAMOND,3));var saved=e.saveWithFullMetadata(h.getLevel().registryAccess());saved.putInt("drill",930);saved.putInt("cursor",249);saved.putInt("progress",10);
        var restored=(IndustrialEntity)BlockEntity.loadStatic(e.getBlockPos(),e.getBlockState(),saved,h.getLevel().registryAccess());restored.setLevel(h.getLevel());var again=restored.saveWithFullMetadata(h.getLevel().registryAccess());
        check(h,restored.energy.getAmountAsInt()==75000&&restored.drillRemaining()==930&&restored.cursor()==249&&restored.armed()&&again.getIntOr("progress",0)==10,"Native serialization retains energy, remaining bit wear, scan position, work and explicit arming");
        check(h,restored.getItem(0).getCount()==2&&restored.countItem(Items.DIAMOND)==3,"Physical inventories reload exactly without any offline output");
        var player=GameplayTests.player(h);try{var menu=new IndustrialMenu(1,player.getInventory(),restored,restored.data);check(h,menu.energy()==75000,"Container data faithfully transmits energy above signed-short range");}finally{GameplayTests.cleanup(player);}h.succeed();
    }
    static void energyGuards(GameTestHelper h){
        var a=machine(h,8,2,8,Industry.Kind.BATTERY);var b=machine(h,10,2,8,Industry.Kind.BATTERY);a.energy.set(10000);h.getLevel().setBlock(a.getBlockPos().east(),Industry.CABLE.get().defaultBlockState(),3);
        var consumer=machine(h,14,2,8,Industry.Kind.FARM);consumer.energy.set(1000);
        check(h,EnergyHandlerUtil.move(consumer.port,a.port,500,null)==0,"Consumers never export their working buffer");
        var generator=machine(h,20,2,8,Industry.Kind.DYNAMO);generator.energy.set(64000);generator.setItem(0,new ItemStack(Items.COAL));
        h.runAfterDelay(50,()->{check(h,a.energy.getAmountAsInt()==10000&&b.energy.getAmountAsInt()==0,"Accumulators cannot feed one another around a cable loop");check(h,generator.energy.getAmountAsInt()==64000&&generator.getItem(0).getCount()==1,"Full dynamo retains unopened fuel");h.succeed();});
    }
    static void farmGuards(GameTestHelper h){
        var e=machine(h,10,2,10,Industry.Kind.FARM);penFloor(h,e);e.energy.set(8000);for(int i=2;i<11;i++)e.setItem(i,new ItemStack(Items.COBBLESTONE,64));var p=e.getBlockPos().east(2);var w=h.getLevel();w.setBlock(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7),3);w.setBlock(p,Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE,7),3);w.setBlock(p.above(),Blocks.GLOWSTONE.defaultBlockState(),3);
        h.runAfterDelay(65,()->{check(h,w.getBlockState(p).getValue(CropBlock.AGE)==7&&e.energy.getAmountAsInt()==8000&&e.status()==2,"Full buffers leave mature crops, planting material and FE untouched");e.clearContent();w.setBlock(e.getBlockPos().west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);h.runAfterDelay(50,()->{check(h,w.getBlockState(p).getValue(CropBlock.AGE)==7&&e.energy.getAmountAsInt()==8000&&e.status()==3,"Redstone pauses real world harvesting without plant loss");h.succeed();});});
    }
    static void ranchCap(GameTestHelper h){
        var e=machine(h,10,2,10,Industry.Kind.RANCH);penFloor(h,e);e.energy.set(8000);e.setItem(0,new ItemStack(Items.WHEAT,32));
        for(int i=0;i<16;i++){var cow=EntityTypes.COW.create(h.getLevel(),EntitySpawnReason.EVENT);cow.setNoAi(true);cow.setPos(Vec3.atBottomCenterOf(e.getBlockPos().offset(i%4-2,0,i/4-2)));h.getLevel().addFreshEntity(cow);}
        h.runAfterDelay(130,()->{var animals=h.getLevel().getEntitiesOfClass(Animal.class,new AABB(e.getBlockPos()).inflate(5));check(h,animals.size()==16&&animals.stream().noneMatch(Animal::isInLove)&&e.getItem(0).getCount()==32&&e.energy.getAmountAsInt()==8000,"Local herd cap prevents unlimited automatic crowding and consumes no feed");for(var a:animals)a.discard();h.succeed();});
    }
    static void milking(GameTestHelper h){
        var a=machine(h,10,2,10,Industry.Kind.PASTURE);penFloor(h,a);var b=machine(h,10,2,12,Industry.Kind.PASTURE);for(var e:java.util.List.of(a,b)){e.energy.set(4000);e.setItem(1,new ItemStack(Items.BUCKET,2));}
        var cow=EntityTypes.COW.create(h.getLevel(),EntitySpawnReason.EVENT);cow.setNoAi(true);cow.setPos(Vec3.atBottomCenterOf(a.getBlockPos().east(2)));h.getLevel().addFreshEntity(cow);
        h.runAfterDelay(130,()->{check(h,a.countItem(Items.MILK_BUCKET)+b.countItem(Items.MILK_BUCKET)==1,"Two stations share one persistent cow cooldown and cannot duplicate its milk");check(h,a.getItem(1).getCount()+b.getItem(1).getCount()==3&&a.energy.getAmountAsInt()+b.energy.getAmountAsInt()==7600,"Exactly one bucket and 400 FE pay for one milk bucket");check(h,cow.getPersistentData().contains("tnd_last_milk"),"Milk timestamp is attached to the actual saved animal");cow.discard();h.succeed();});
    }
    static void solar(GameTestHelper h){
        var e=machine(h,10,27,10,Industry.Kind.SOLAR);var w=h.getLevel();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)w.setBlock(e.getBlockPos().offset(x,0,z),Industry.PANEL.get().defaultBlockState(),3);
        check(h,e.solarBuilt(),"A full physical 3 by 3 array above the test enclosure has clear sky");
        var clock=w.dimensionType().defaultClock().orElseThrow();long old=w.getServer().clockManager().getTotalTicks(clock);
        h.onEachTick(()->w.getServer().clockManager().setTotalTicks(clock,6000));
        h.runAfterDelay(35,()->{check(h,e.energy.getAmountAsInt()>0,"A complete outdoor solar array produces real FE in native daylight ticks");
            w.setBlock(e.getBlockPos().above(4),Blocks.STONE.defaultBlockState(),3);check(h,!e.solarBuilt(),"A roof above even the controller immediately invalidates solar access");int stored=e.energy.getAmountAsInt();
            h.runAfterDelay(25,()->{try{check(h,e.energy.getAmountAsInt()==stored&&e.status()==6,"A covered array stops producing electricity");w.setBlock(e.getBlockPos().above(4),Blocks.AIR.defaultBlockState(),3);w.setBlock(e.getBlockPos().east(),Blocks.AIR.defaultBlockState(),3);check(h,!e.solarBuilt(),"Missing panel prevents power from an incomplete array");}finally{w.getServer().clockManager().setTotalTicks(clock,old);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)w.setBlock(e.getBlockPos().offset(x,0,z),Blocks.AIR.defaultBlockState(),3);}h.succeed();});});
    }
}
