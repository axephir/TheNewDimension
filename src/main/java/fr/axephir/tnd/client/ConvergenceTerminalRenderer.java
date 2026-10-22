package fr.axephir.tnd.client;

import com.mojang.blaze3d.vertex.*;
import fr.axephir.tnd.narrative.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.*;

/** Three rotating incomplete meridians, a stationary pearl core and a translating fourth-axis wire cube. */
public final class ConvergenceTerminalRenderer implements BlockEntityRenderer<ConvergenceTerminalEntity,ConvergenceTerminalRenderer.State> {
    private static final Identifier LIGHT=Identifier.fromNamespaceAndPath("tnd","textures/block/gate_light.png");
    public static final class State extends BlockEntityRenderState {float time;boolean reactor,active;}
    public State createRenderState(){return new State();}
    @Override public void extractRenderState(ConvergenceTerminalEntity e,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderer.super.extractRenderState(e,s,partial,camera,overlay);var p=Minecraft.getInstance().player;s.reactor=((ConvergenceTerminalBlock)e.getBlockState().getBlock()).reactor;s.active=e.getLevel()!=null&&e.getLevel().dimension().equals(Convergence.ECHO)||p!=null&&p.getData(Convergence.PHASE)==1&&p.distanceToSqr(Vec3.atCenterOf(e.getBlockPos()))<1600;s.time=AtmosphereOptions.ANIMATIONS.get()?(e.getLevel().getGameTime()%24000+partial)*(s.active?.025f:.006f):0;}
    @Override public void submit(State s,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){poses.pushPose();poses.translate(.5,1.35,.5);float t=s.time;boolean active=s.active;int color=s.reactor?0xc8e4c3a2:active?0xddc4f7ed:0x8394b6cd;
        collector.submitCustomGeometry(poses,RenderTypes.entityTranslucentEmissive(LIGHT),(pose,v)->{
            for(int ring=0;ring<3;ring++)for(int i=0;i<64;i++){if(i%16<3)continue;double a=i*Math.PI/32+t*(ring%2==0?1:-1),b=(i+1)*Math.PI/32+t*(ring%2==0?1:-1),r=.38+ring*.13;float[] p=point(a,r,ring,t),q=point(b,r,ring,t);line(pose,v,p,q,color,active?.011f:.008f);}
            float size=.105f;for(int axis=0;axis<3;axis++)for(int sign:new int[]{-1,1}){float[] a={0,0,0},b={0,0,0};b[axis]=size*sign;line(pose,v,a,b,0xffe7f8ee,.025f);}
        });poses.popPose();
    }
    private static float[] point(double a,double r,int axis,float t){float x=(float)(Math.cos(a)*r),y=(float)(Math.sin(a)*r);return switch(axis){case 0->new float[]{x,y*.8f,(float)(y*Math.sin(t*.7)*.6)};case 1->new float[]{y*.5f,x,y*.86f};default->new float[]{x,y*.4f,y*.91f};};}
    private static void line(PoseStack.Pose pose,VertexConsumer v,float[] a,float[] b,int color,float width){for(int axis=0;axis<3;axis++){float[] n={axis==0?width:0,axis==1?width:0,axis==2?width:0};for(int sign:new int[]{1,-1}){vertex(pose,v,a,n,sign,color);vertex(pose,v,a,n,-sign,color);vertex(pose,v,b,n,-sign,color);vertex(pose,v,b,n,sign,color);}}}
    private static void vertex(PoseStack.Pose p,VertexConsumer v,float[] a,float[] n,int sign,int c){v.addVertex(p,a[0]+n[0]*sign,a[1]+n[1]*sign,a[2]+n[2]*sign).setColor(c).setUv(.5f,.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(p,0,1,0);}
    @Override public AABB getRenderBoundingBox(ConvergenceTerminalEntity e){return new AABB(e.getBlockPos()).inflate(2);}
}
