package fr.axephir.tnd.client;
import fr.axephir.tnd.entities.*;
import fr.axephir.tnd.world.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.*;

/** Reads already tracked entities and the current loaded biome. Never requests chunks or network data. */
public final class VestigeHud {
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static String biome="";
    private static int reveal;
    private static final PanelMotion bar=new PanelMotion();
    public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(Identifier.fromNamespaceAndPath("tnd","vestige_details"),VestigeHud::render);}
    public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level!=world){world=mc.level;biome="";reveal=0;}
        if(mc.level==null||mc.player==null||mc.isPaused())return;if(reveal>0)reveal--;
        if(!mc.level.dimension().equals(DevelopmentCommands.DIMENSION))return;
        if(mc.player.tickCount%20==0){String current=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(k->k.identifier().getPath()).orElse("");
            if(!current.equals(biome)){biome=current;reveal=100;}}
    }
    private static void render(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker dt){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden()||mc.gui.screen()!=null||!AtmosphereOptions.HUD.get())return;
        if(reveal>0&&mc.level.dimension().equals(DevelopmentCommands.DIMENSION)){
            int alpha=Math.min(220,reveal*22);int col=(alpha<<24)|0xe4dfbf;
            g.fill(9,39,159,67,(alpha/2<<24)|0x182b28);g.fill(9,39,11,67,(alpha<<24)|0xb39b66);
            g.text(mc.font,Component.translatable("biome.tnd."+biome),16,44,col,false);
            g.text(mc.font,Component.translatable(VestigeTime.isNight(mc.level)?"hud.tnd.night":"hud.tnd.day"),16,56,(alpha<<24)|0xa7beb2,false);
        }
        LivingEntity target=null;double nearest=16*16;
        for(var candidate:mc.level.getEntitiesOfClass(LivingEntity.class,mc.player.getBoundingBox().inflate(16),e->e.isAlive()&&(e instanceof Shardling||e instanceof fr.axephir.tnd.city.CityMob||e instanceof AwakenedMob||e instanceof RiftSentinel||e instanceof FracturedCustodian))){
            var direction=candidate.getEyePosition().subtract(mc.player.getEyePosition());double distance=direction.lengthSqr();
            if(distance<nearest&&direction.normalize().dot(mc.player.getLookAngle())>.97&&mc.player.hasLineOfSight(candidate)){target=candidate;nearest=distance;}
        }
        if(target!=null){
            int x=g.guiWidth()/2,y=g.guiHeight()/2+23;var name=target.getDisplayName();g.text(mc.font,name,x-mc.font.width(name)/2,y,0xffeee4bf,true);
            int value=bar.value(0,(int)((target instanceof Shardling pet&&pet.downed()?0:target.getHealth())*1000/Math.max(1,target.getMaxHealth())));
            g.fill(x-55,y+12,x+55,y+17,0xbb172525);g.fill(x-54,y+13,x-54+108*value/1000,y+16,0xffaa8178);
            if(target instanceof Shardling pet&&pet.downed()){var hint=Component.translatable("familiar.tnd.state.3");g.text(mc.font,hint,x-mc.font.width(hint)/2,y+21,0xffffc39a,true);}
            boolean charging=target instanceof fr.axephir.tnd.city.CityMob cm&&cm.charge()>0||target instanceof AwakenedMob m&&m.charge()>0||target instanceof RiftSentinel s&&s.windup()>0||target instanceof FracturedCustodian c&&c.charge()>0;
            boolean opening=target instanceof fr.axephir.tnd.city.CityMob cm&&cm.recovery()>0&&cm.recovery()<(cm.kind()==fr.axephir.tnd.city.CityMob.Kind.DRONE?62:cm.kind()==fr.axephir.tnd.city.CityMob.Kind.ENFORCER?47:45)&&cm.charge()==0||target instanceof AwakenedMob m&&m.recovery()>0&&m.recovery()<(m.heavy()?60:44)||target instanceof FracturedCustodian c&&c.exposed()>0;
            if(charging||opening){var warning=Component.translatable(charging?"hud.tnd.attack":"hud.tnd.opening");g.text(mc.font,warning,x-mc.font.width(warning)/2,y+21,charging?0xffffcf88:0xffb7ddb1,true);}
        }
    }
    private VestigeHud(){}
}
