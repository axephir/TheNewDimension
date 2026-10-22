package fr.axephir.tnd.progression;

import com.mojang.serialization.Codec;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Advanced branches remain server-owned; actions never create resources or award experience. */
public final class MasteryAbilities {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> NEXT=PlayerProgression.TYPES.register("ability_next",()->AttachmentType.builder(()->0L).serialize(Codec.LONG.fieldOf("tick")).copyOnDeath().sync((h,p)->h==p,ByteBufCodecs.VAR_LONG).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> PIN=PlayerProgression.TYPES.register("pin_until",()->AttachmentType.builder(()->0L).build());
    private record Plant(ServerPlayer owner,net.minecraft.server.level.ServerLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState state,Item cost,long due){}
    private static final java.util.Map<net.minecraft.server.MinecraftServer,java.util.List<Plant>> PLANTS=new java.util.WeakHashMap<>();
    public static void bootstrap(){}
    @SubscribeEvent public void plantTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){var list=PLANTS.get(e.getServer());if(list==null)return;long now=e.getServer().overworld().getGameTime();var ready=new java.util.ArrayList<Plant>();list.removeIf(q->{if(q.due()>now)return false;ready.add(q);return true;});for(var q:ready){var p=q.owner();var w=q.level();var at=q.pos();if(!p.isAlive()||p.isSpectator()||p.level()!=w||!w.hasChunkAt(at)||!w.getBlockState(at).isAir()||!q.state().canSurvive(w,at)||fr.axephir.tnd.city.CityRegions.protectedAt(w,at))continue;if(consume(p,q.cost(),1))w.setBlock(at,q.state(),3);}}
    @SubscribeEvent public void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){PLANTS.remove(e.getServer());}
    public static long now(ServerPlayer p){return p.level().getServer().overworld().getGameTime();}
    public static boolean has(ServerPlayer p,Talent t){return p.getData(PlayerProgression.PROFILE).masteries().values().stream().anyMatch(m->m.rank(t)>0);}
    public static boolean activate(ServerPlayer p,Mastery m){
        if(!p.isAlive()||p.isCreative()||p.isSpectator()||!has(p,Talent.ability(m)))return false;
        long now=now(p);if(now<p.getData(NEXT)){p.sendOverlayMessage(Component.translatable("ability.tnd.cooldown",(p.getData(NEXT)-now+19)/20));return false;}
        boolean done=switch(m){
            case MINING->{if(!p.getMainHandItem().is(net.minecraft.tags.ItemTags.PICKAXES))yield false;effect(p,MobEffects.HASTE,300,1);yield true;}
            case LOGGING->{if(!(p.getMainHandItem().getItem() instanceof AxeItem))yield false;effect(p,MobEffects.HASTE,500,1);OreSense.scan(p,m);yield true;}
            case FARMING->{var crops=new java.util.ArrayList<BlockPos>();for(var at:BlockPos.betweenClosed(p.blockPosition().offset(-5,-2,-5),p.blockPosition().offset(5,2,5))){if(!p.level().hasChunk(at.getX()>>4,at.getZ()>>4)||fr.axephir.tnd.city.CityRegions.protectedAt(p.level(),at))continue;var s=p.level().getBlockState(at);if(s.getBlock() instanceof CropBlock c&&!c.isMaxAge(s)){crops.add(at.immutable());if(crops.size()==8)break;}}
                if(crops.isEmpty()||!consume(p,Items.BONE_MEAL,4))yield false;for(var at:crops){var s=p.level().getBlockState(at);var c=(CropBlock)s.getBlock();p.level().setBlock(at,c.getStateForAge(Math.min(c.getMaxAge(),c.getAge(s)+1)),3);}yield true;}
            case FISHING->{effect(p,MobEffects.LUCK,600,1);effect(p,MobEffects.WATER_BREATHING,600,0);yield true;}
            case HUNTING->{effect(p,MobEffects.INVISIBILITY,200,0);effect(p,MobEffects.SPEED,200,0);yield true;}
            case BREEDING->{var animals=p.level().getEntitiesOfClass(Animal.class,p.getBoundingBox().inflate(8),a->a.isAlive()&&a.getHealth()<a.getMaxHealth()&&!(a instanceof fr.axephir.tnd.entities.Shardling));if(animals.isEmpty()||!consume(p,Items.WHEAT,4))yield false;animals.stream().limit(12).forEach(a->a.heal(4));yield true;}
            case MELEE->{var mobs=p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(5),a->a instanceof Enemy&&a.isAlive()&&p.hasLineOfSight(a));if(mobs.isEmpty())yield false;for(var a:mobs){a.knockback(1.5,p.getX()-a.getX(),p.getZ()-a.getZ(),p.damageSources().playerAttack(p),0);effect(a,MobEffects.SLOWNESS,80,1);}yield true;}
            case RANGED->{p.setData(PIN,now+400);yield true;}
            case DEFENSE->{effect(p,MobEffects.ABSORPTION,200,1);yield true;}
            case CRAFTING->{var tool=p.getMainHandItem();if(!tool.isDamageableItem()||tool.getDamageValue()==0||!consume(p,ModContent.TEMPERED_ALLOY.get(),1))yield false;tool.setDamageValue(Math.max(0,tool.getDamageValue()-Math.max(1,tool.getMaxDamage()/4)));yield true;}
            case BUILDING->{effect(p,MobEffects.SLOW_FALLING,600,0);yield true;}
            case EXPLORATION->{effect(p,MobEffects.SPEED,400,1);effect(p,MobEffects.JUMP_BOOST,400,0);yield true;}
            case ALCHEMY->{if(!p.hasEffect(MobEffects.POISON)&&!p.hasEffect(MobEffects.WITHER)&&!p.hasEffect(MobEffects.WEAKNESS))yield false;if(!consume(p,Items.HONEY_BOTTLE,1))yield false;p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WITHER);p.removeEffect(MobEffects.WEAKNESS);var glass=new ItemStack(Items.GLASS_BOTTLE);if(!p.getInventory().add(glass))p.drop(glass,false);yield true;}
            case ENGINEERING->{var hit=p.pick(5,1,false);if(!(hit instanceof net.minecraft.world.phys.BlockHitResult b)||!(p.level().getBlockEntity(b.getBlockPos()) instanceof fr.axephir.tnd.technology.ExtractorBlockEntity extractor)||extractor.relicCalibration()>=25||!consume(p,fr.axephir.tnd.technology.Industry.CIRCUIT.get(),1))yield false;extractor.calibrateRelic(25);yield true;}
            case ANCIENT_TECH->{OreSense.scan(p,m);for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(8),a->a instanceof Enemy&&p.hasLineOfSight(a)))effect(mob,MobEffects.WEAKNESS,160,0);yield true;}
        };
        if(!done){p.sendOverlayMessage(Component.translatable("ability.tnd.context."+m.id()));return false;}
        p.setData(NEXT,now+1200);p.level().playSound(null,p.blockPosition(),ModContent.atmosphereSound("spatial_chime"),net.minecraft.sounds.SoundSource.PLAYERS,.45f,1.2f);
        p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),24,.6,.7,.6,.02);
        p.sendOverlayMessage(Component.translatable("ability.tnd.used",Component.translatable("talent.tnd."+Talent.ability(m).id())));return true;
    }
    public static boolean consume(ServerPlayer p,Item item,int count){int available=0;for(var s:p.getInventory().getNonEquipmentItems())if(s.is(item))available+=s.getCount();if(available<count)return false;int left=count;for(var s:p.getInventory().getNonEquipmentItems())if(s.is(item)){int take=Math.min(left,s.getCount());s.shrink(take);left-=take;if(left==0)break;}return true;}
    public static void effect(LivingEntity p,net.minecraft.core.Holder<MobEffect> effect,int ticks,int amplifier){p.addEffect(new MobEffectInstance(effect,ticks,amplifier,true,true,true));}
    @SubscribeEvent public void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;boolean active=p.isAlive()&&!p.isCreative()&&!p.isSpectator();
        modifier(p,Attributes.KNOCKBACK_RESISTANCE,"bulwark",active&&p.onGround()&&has(p,Talent.DEFENSE_BULWARK)?.25:0);
        modifier(p,Attributes.BLOCK_INTERACTION_RANGE,"architect",active&&p.getMainHandItem().getItem() instanceof BlockItem&&has(p,Talent.BUILDING_ARCHITECT)?.75:0);
        modifier(p,Attributes.MOVEMENT_SPEED,"wayfarer",active&&p.level().canSeeSky(p.blockPosition())&&has(p,Talent.EXPLORATION_WAYFARER)?.012:0);
        if(active&&p.isInWater()&&has(p,Talent.FISHING_DIVER))effect(p,MobEffects.WATER_BREATHING,60,0);
    }
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> type,String key,double value){var a=p.getAttribute(type);if(a==null)return;var id=net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd",key);var old=a.getModifier(id);if(value==0){if(old!=null)a.removeModifier(id);}else if(old==null||old.amount()!=value)a.addOrUpdateTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));}
    @SubscribeEvent public void damage(LivingIncomingDamageEvent e){if(!(e.getSource().getEntity() instanceof ServerPlayer p)||p.isCreative()||p.isSpectator()||e.getEntity() instanceof net.minecraft.world.entity.player.Player)return;
        boolean arrow=e.getSource().getDirectEntity() instanceof Projectile;
        if(arrow&&has(p,Talent.RANGED_MARKSMAN)&&p.distanceToSqr(e.getEntity())>=144)e.setAmount(e.getAmount()*1.15f);
        if(!arrow&&has(p,Talent.MELEE_DUELIST)&&p.level().getEntitiesOfClass(Mob.class,e.getEntity().getBoundingBox().inflate(3),a->a!=e.getEntity()&&a instanceof Enemy&&a.isAlive()).isEmpty())e.setAmount(e.getAmount()*1.12f);
        if(arrow&&p.getData(PIN)>now(p)){p.setData(PIN,0L);effect(e.getEntity(),MobEffects.SLOWNESS,120,2);}
    }
    @SubscribeEvent public void birth(BabyEntitySpawnEvent e){if(!e.isCanceled()&&e.getCausedByPlayer() instanceof ServerPlayer p&&!p.isCreative()&&e.getChild()!=null&&has(p,Talent.BREEDING_CARETAKER)){var a=e.getChild().getAttribute(Attributes.MAX_HEALTH);if(a!=null){a.addOrReplacePermanentModifier(new AttributeModifier(net.minecraft.resources.Identifier.fromNamespaceAndPath("tnd","caretaker"),.2,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));e.getChild().setHealth(e.getChild().getMaxHealth());}}}
    @SubscribeEvent public void craft(PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p&&!p.isCreative()&&has(p,Talent.CRAFTING_ARTISAN)){var s=e.getCrafting();if(s.isDamageableItem()&&s.getDamageValue()==0){int base=s.getItem().components().getOrDefault(DataComponents.MAX_DAMAGE,s.getMaxDamage());s.set(DataComponents.MAX_DAMAGE,Math.max(s.getMaxDamage(),base+base/10));}}}
    public static void replant(ServerPlayer p,BlockPos at,net.minecraft.world.level.block.state.BlockState harvested){if(!p.isShiftKeyDown()||fr.axephir.tnd.city.CityRegions.protectedAt(p.level(),at))return;
        Block block=harvested.getBlock();Item seed=null;net.minecraft.world.level.block.state.BlockState planted=null;
        if(has(p,Talent.FARMING_SEEDKEEPER)&&block instanceof CropBlock crop){seed=block==Blocks.WHEAT?Items.WHEAT_SEEDS:block==Blocks.CARROTS?Items.CARROT:block==Blocks.POTATOES?Items.POTATO:block==Blocks.BEETROOTS?Items.BEETROOT_SEEDS:null;planted=crop.getStateForAge(0);}
        if(has(p,Talent.LOGGING_FORESTER)&&harvested.is(net.minecraft.tags.BlockTags.LOGS)){for(var s:p.getInventory().getNonEquipmentItems())if(s.getItem() instanceof BlockItem b&&b.getBlock() instanceof SaplingBlock){seed=s.getItem();planted=b.getBlock().defaultBlockState();break;}}
        if(seed==null||planted==null)return;final Item cost=seed;final var state=planted;final var level=p.level();final var position=at.immutable();
        // NeoForge drops are prepared before vanilla removes the harvested block. Wait for the next server tick.
        var queue=PLANTS.computeIfAbsent(level.getServer(),server->new java.util.ArrayList<>());
        if(queue.size()<256&&queue.stream().noneMatch(q->q.level()==level&&q.pos().equals(position)))queue.add(new Plant(p,level,position,state,cost,now(p)+1));
    }
}
