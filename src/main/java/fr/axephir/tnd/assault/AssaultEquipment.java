package fr.axephir.tnd.assault;

import fr.axephir.tnd.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class AssaultEquipment {
    @SubscribeEvent public void tick(PlayerTickEvent.Post event){
        var p=event.getEntity();if(p.level().isClientSide()||p.tickCount%20!=0||p.isSpectator())return;
        for(var set:AssaultContent.Set.values())if(AssaultContent.ARMOR.get(set).entrySet().stream().allMatch(e->p.getItemBySlot(e.getKey()).is(e.getValue().get()))){
            for(var effect:effects(set)){var current=p.getEffect(effect);if(current==null||current.getDuration()<40)p.addEffect(new MobEffectInstance(effect,120,0,true,false,true));}
        }
    }
    public static java.util.List<net.minecraft.core.Holder<MobEffect>> effects(AssaultContent.Set set){return switch(set){case WARD->java.util.List.of(MobEffects.RESISTANCE,MobEffects.FIRE_RESISTANCE);case SHADE->java.util.List.of(MobEffects.SPEED,MobEffects.JUMP_BOOST);case CHOIR->java.util.List.of(MobEffects.REGENERATION,MobEffects.HASTE);};}
    public static int repair(ItemStack crystal){return crystal.is(ModContent.COMPOSITE_CRYSTAL.get())?250:crystal.is(AwakeningContent.NOCTILITE_CRYSTAL.get())||crystal.is(ModContent.RESONANT_SHARD.get())?150:crystal.is(ModContent.BLUE_SHARD.get())||crystal.is(ModContent.RED_SHARD.get())||crystal.is(ModContent.GREEN_SHARD.get())?100:0;}
    @SubscribeEvent public void tooltip(ItemTooltipEvent e){var set=AssaultContent.set(e.getItemStack());if(set==null){for(var memory:AssaultContent.MEMORIES.entrySet())if(e.getItemStack().is(memory.getValue().get()))for(var key:java.util.List.of("assault.tnd.memory."+memory.getKey().id,"assault.tnd.memory_use","assault.tnd.memory_salvage"))e.getToolTip().add(Component.translatable(key).withStyle(net.minecraft.ChatFormatting.AQUA));return;}e.getToolTip().add(Component.translatable("assault.tnd.set."+set.id).withStyle(net.minecraft.ChatFormatting.AQUA));e.getToolTip().add(Component.translatable("assault.tnd.repair").withStyle(net.minecraft.ChatFormatting.GRAY));}
}
