package fr.axephir.tnd.client;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.world.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Extra layers are local audio only: no entities, searches, chunk requests or network traffic. */
public final class AtmosphereDirector {
    private static ClientLevel previous;
    private static FadingBed night,interior;
    private static final List<SoundInstance> cues=new ArrayList<>();
    private static final RandomSource random=RandomSource.create();
    private static int cueDelay,deepDelay;
    public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(mc.level!=previous){clear(mc);previous=mc.level;cueDelay=120+random.nextInt(400);deepDelay=3600+random.nextInt(4800);}
        if(mc.level==null||mc.player==null||!mc.level.dimension().equals(DevelopmentCommands.DIMENSION)||!AtmosphereOptions.ENABLED.get()){
            clear(mc);return;
        }
        if(mc.isPaused())return;
        float gain=AtmosphereOptions.VOLUME.get().floatValue();
        night=bed(mc,night,"night_pressure",VestigeTime.isNight(mc.level)?.32f*gain:0);
        // The sky test reads the heightmap of the already simulated player chunk.
        interior=bed(mc,interior,"enclosed_hum",!mc.level.canSeeSky(mc.player.blockPosition())?.26f*gain:0);
        cues.removeIf(s->!mc.getSoundManager().isActive(s));
        if(--cueDelay<=0){
            cueDelay=20*(35+random.nextInt(66));
            String biome=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(k->k.identifier().getPath()).orElse("");
            String[] choices=!mc.level.canSeeSky(mc.player.blockPosition())?new String[]{"metal_echo","stone_shift","reverse_breath"}:
                switch(biome){
                    case "glass_woodland"->new String[]{"glass_tinkle","spatial_chime","distant_pulse"};
                    case "ashen_basin"->new String[]{"submerged_groan","reverse_breath","stone_shift"};
                    case "obsidian_dunes"->new String[]{"dust_scrape","distant_pulse","stone_shift"};
                    default->new String[]{"metal_echo","distant_pulse","stone_shift"};
                };
            cue(mc,choices[random.nextInt(choices.length)],.65f*gain);
        }
        if(--deepDelay<=0){deepDelay=20*(180+random.nextInt(241));cue(mc,"low_signal",.6f*gain);}
    }
    private static FadingBed bed(Minecraft mc,FadingBed current,String id,float target){
        if(current==null||current.isStopped()||current.age>40&&!mc.getSoundManager().isActive(current)){
            if(target==0)return null;
            current=new FadingBed(ModContent.atmosphereSound(id),mc.level);current.target=target;mc.getSoundManager().play(current);
        }
        current.target=target;return current;
    }
    private static void cue(Minecraft mc,String id,float volume){
        if(volume<=0||cues.size()>=2)return;
        double angle=random.nextDouble()*Math.PI*2,distance=8+random.nextDouble()*10;
        var p=mc.player;
        var sound=new SimpleSoundInstance(ModContent.atmosphereSound(id),SoundSource.AMBIENT,volume,.96f+random.nextFloat()*.08f,random,
                p.getX()+Math.cos(angle)*distance,p.getY()+random.nextInt(7)-3,p.getZ()+Math.sin(angle)*distance);
        cues.add(sound);mc.getSoundManager().play(sound);
    }
    private static void clear(Minecraft mc){
        if(night!=null)mc.getSoundManager().stop(night);if(interior!=null)mc.getSoundManager().stop(interior);
        cues.forEach(mc.getSoundManager()::stop);cues.clear();night=null;interior=null;
    }
    private static final class FadingBed extends AbstractTickableSoundInstance {
        private final ClientLevel world;
        private float target;
        private int age;
        FadingBed(SoundEvent event,ClientLevel world){
            super(event,SoundSource.AMBIENT,AtmosphereDirector.random);this.world=world;volume=.001f;looping=true;relative=true;attenuation=SoundInstance.Attenuation.NONE;
        }
        @Override public void tick(){
            var mc=Minecraft.getInstance();if(mc.level!=world){stop();return;}if(mc.isPaused())return;
            age++;volume+=Math.clamp(target-volume,-.0025f,.0025f);
            if(target==0&&volume<=.001f)stop();
        }
        @Override public boolean canStartSilent(){return true;}
    }
    private AtmosphereDirector(){}
}
