package fr.axephir.tnd.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.narrative.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.*;

/** One previous GPU frame, softly blended. Never reads pixels back, changes world data or affects another player's perception. */
public final class ConvergenceEffects {
    private static final Identifier MEMORY=Identifier.fromNamespaceAndPath("tnd","frame_memory");
    private static FrameMemory frame;private static Object world;private static int sequence,rareTicks,phaseTicks,lastPhase=-1,worldTicks;private static boolean valid,failed,skipped,using;
    private static final RandomSource RANDOM=RandomSource.create();
    private static final class FrameMemory extends AbstractTexture {
        final int width,height;
        FrameMemory(GpuTexture source,int w,int h){width=w;height=h;texture=RenderSystem.getDevice().createTexture(()->"TND visual memory",7,source.getFormat(),w,h,1,1);textureView=RenderSystem.getDevice().createTextureView(texture);}
    }
    public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(Identifier.fromNamespaceAndPath("tnd","convergence"),ConvergenceEffects::hud);}
    public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.level!=world){world=mc.level;sequence=mc.player==null?0:mc.player.getData(RareAnomalies.SEQUENCE);rareTicks=0;worldTicks=0;valid=false;lastPhase=-1;skipped=false;}
        if(mc.player==null||mc.level==null||mc.isPaused())return;
        boolean use=mc.options.keyUse.isDown();
        if(use&&!using&&mc.gui.screen()==null&&Convergence.inEcho(mc.player))mc.player.connection.send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new ConvergenceNetworking.Choice(-1,5)));
        using=use;
        int phase=mc.player.getData(Convergence.PHASE);if(phase!=lastPhase){lastPhase=phase;phaseTicks=0;skipped=false;}else phaseTicks++;
        if(phase==2&&!AtmosphereOptions.ANIMATIONS.get()&&!skipped){skipped=true;mc.player.connection.send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new ConvergenceNetworking.Choice(-1,3)));}
        if(Convergence.inEcho(mc.player))mc.getMusicManager().stopPlaying();
        int next=mc.player.getData(RareAnomalies.SEQUENCE);if(worldTicks++<40)sequence=next;if(next!=sequence){sequence=next;if(AtmosphereOptions.RARE_EFFECTS.get()&&AtmosphereOptions.ANIMATIONS.get()){
            rareTicks=32;int variant=mc.player.getData(RareAnomalies.VARIANT);var at=net.minecraft.world.phys.Vec3.atCenterOf(mc.player.getData(RareAnomalies.ORIGIN));
            String sound=switch(variant){case 1->"stone_shift";case 2->"metal_echo";case 3->"distant_pulse";default->"reverse_breath";};
            if(AtmosphereOptions.ENABLED.get())mc.getSoundManager().play(new SimpleSoundInstance(ModContent.atmosphereSound(sound),SoundSource.AMBIENT,.28f*AtmosphereOptions.VOLUME.get().floatValue(),variant==2?.65f:.8f,RANDOM,at.x,at.y,at.z));
            if(variant==0)for(int n=0;n<18;n++){double y=(n/3)*.29;mc.level.addParticle(ParticleTypes.WHITE_ASH,at.x+(n%3-1)*.17,at.y+y,at.z,0,.006,0);}
            if(variant==1)for(int n=0;n<12;n++)mc.level.addParticle(ParticleTypes.ASH,at.x+(n-6)*.3,at.y-1.1,at.z,0,.015,0);
            if(variant>=2)for(int n=0;n<16;n++){double angle=n*Math.PI/8;mc.level.addParticle(ParticleTypes.END_ROD,at.x+Math.cos(angle)*.6,at.y+Math.sin(angle)*.6,at.z,0,0,0);}
        }}if(rareTicks>0)rareTicks--;
    }
    private static boolean active(Minecraft mc){return !failed&&mc.player!=null&&mc.level!=null&&mc.gui.screen()==null&&AtmosphereOptions.ANIMATIONS.get()&&AtmosphereOptions.AFTERIMAGES.get()&&(Convergence.inEcho(mc.player)||rareTicks>0);}
    public static void frame(RenderFrameEvent.Post e){var mc=Minecraft.getInstance();if(!active(mc)){valid=false;if(frame!=null){mc.getTextureManager().release(MEMORY);frame=null;}return;}
        try{var target=mc.gameRenderer.mainRenderTarget();var source=target.getColorTexture();if(source==null)return;
            if(frame==null||frame.width!=target.width||frame.height!=target.height){valid=false;frame=new FrameMemory(source,target.width,target.height);mc.getTextureManager().register(MEMORY,frame);}
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source,frame.getTexture(),0,0,0,0,0,target.width,target.height);valid=true;
        }catch(RuntimeException failure){failed=true;valid=false;org.slf4j.LoggerFactory.getLogger("tnd/visual-memory").warn("Visual memory disabled after GPU allocation/copy failure; gameplay is unaffected",failure);}
    }
    private static void hud(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker dt){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden()||mc.gui.screen()!=null)return;
        boolean echo=Convergence.inEcho(mc.player);int phase=mc.player.getData(Convergence.PHASE),w=g.guiWidth(),h=g.guiHeight();
        if(valid&&frame!=null&&active(mc)){double distance=echo?mc.player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(mc.player.getData(Convergence.SIGNAL))):48;int alpha=(int)(Math.clamp((distance-8)/40,0,1)*(rareTicks>0?27:20));
            if(alpha>0)g.blit(RenderPipelines.GUI_TEXTURED,MEMORY,0,0,0,frame.height,w,h,frame.width,-frame.height,frame.width,frame.height,(alpha<<24)|0xffffff);
        }
        if(!echo)return;
        if((phase==3||phase==4)&&mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK&&hit.getLocation().distanceToSqr(mc.player.getEyePosition())<=36){
            var hint=Component.translatable("echo.tnd.examine");var count=Component.translatable("echo.tnd.count",Integer.bitCount(mc.player.getData(Convergence.TRACES)));
            int panel=Math.max(mc.font.width(hint),mc.font.width(count))+16;g.fill(7,24,7+panel,53,0xc2101c22);g.fill(7,24,9,53,0xff779f9b);
            g.text(mc.font,count,14,29,0xff819f9e,false);g.text(mc.font,hint,14,41,0xffb2cec9,false);
        }
        if(phase==2){g.fill(0,0,w,17,0xc9000000);g.fill(0,h-39,w,h,0xc9000000);center(g,mc,Component.translatable("story.tnd.cinema"),h-30,0xffd0dddd);center(g,mc,Component.translatable("story.tnd.skip"),h-17,0xff8ea8b1);}
        if(phase==4){var at=mc.player.getData(Convergence.SIGNAL);int distance=(int)mc.player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(at));center(g,mc,Component.translatable("story.tnd.guidance",distance,at.getX(),at.getZ()),h-50,0xffb9e4df);}
        if(phase==5){int a=Math.min(240,phaseTicks*9);g.fill(0,0,w,h,(a<<24)|0x102733);center(g,mc,Component.translatable("story.tnd.restoring"),h/2,0xffd0eeee);}
    }
    private static void center(GuiGraphicsExtractor g,Minecraft mc,Component line,int y,int color){g.text(mc.font,line,(g.guiWidth()-mc.font.width(line))/2,y,color,true);}
    private ConvergenceEffects(){}
}
