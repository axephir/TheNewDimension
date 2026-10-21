package fr.axephir.tnd.client;
import net.minecraft.util.Util;
/** Frame-rate independent visual easing; never changes the server's displayed numerical state. */
final class PanelMotion {
    private final float[] values=new float[16];
    private final long[] times=new long[16];
    int value(int channel,int target){
        target=Math.clamp(target,0,1000);long now=Util.getMillis();long elapsed=times[channel]==0?16:Math.clamp(now-times[channel],0,100);times[channel]=now;
        if(!AtmosphereOptions.ANIMATIONS.get())return target;
        if(target<values[channel]-250)values[channel]=target;
        values[channel]+=(target-values[channel])*(1-(float)Math.exp(-elapsed/110.0));return Math.round(values[channel]);
    }
}
