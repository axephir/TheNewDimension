package fr.axephir.tnd.deepindustry;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
public final class DeepEquipment {
    public static boolean complete(net.minecraft.world.entity.player.Player p,DeepIndustry.Metal metal){var set=DeepIndustry.GEAR.get(metal);return p.getItemBySlot(EquipmentSlot.HEAD).is(set.get("helmet").get())&&p.getItemBySlot(EquipmentSlot.CHEST).is(set.get("chestplate").get())&&p.getItemBySlot(EquipmentSlot.LEGS).is(set.get("leggings").get())&&p.getItemBySlot(EquipmentSlot.FEET).is(set.get("boots").get());}
    @SubscribeEvent public void tick(PlayerTickEvent.Post e){var p=e.getEntity();if(p.level().isClientSide()||p.tickCount%20!=0||!p.isAlive()||p.isSpectator())return;
        if(complete(p,DeepIndustry.Metal.THALASSITE)&&p.isUnderWater()){p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,60,0,true,false));p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,60,0,true,false));}
        if(complete(p,DeepIndustry.Metal.ASTERIUM))p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,40,0,true,false));
        if(complete(p,DeepIndustry.Metal.FERRONYTE)&&p.isCrouching())p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,40,0,true,false));
    }
    @SubscribeEvent public void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){
        var s=e.getItemStack();for(var metal:DeepIndustry.Metal.values())for(var name:java.util.List.of("helmet","chestplate","leggings","boots"))if(s.is(DeepIndustry.GEAR.get(metal).get(name).get())){e.getToolTip().add(net.minecraft.network.chat.Component.translatable("deep.tnd.set."+metal.id).withStyle(net.minecraft.ChatFormatting.AQUA));return;}
        for(String n:java.util.List.of("raw_ferronyte","raw_thalassite","raw_asterium","maintenance_kit","hardened_drill_bit","sundered_drive"))if(s.is(DeepIndustry.item(n)))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("deep.tnd.item."+n).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
