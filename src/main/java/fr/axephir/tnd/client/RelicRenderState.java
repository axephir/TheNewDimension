package fr.axephir.tnd.client;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
public final class RelicRenderState extends LivingEntityRenderState {
    public boolean bonded,sitting,downed;
    public int stage,bondTicks,pulse;
    public int charge;
}
