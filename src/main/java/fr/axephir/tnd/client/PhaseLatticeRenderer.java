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

/** An actual rotating 4D tesseract projected into 3D, visible only during earned spatial perception. */
public final class PhaseLatticeRenderer implements BlockEntityRenderer<PhaseLatticeEntity,PhaseLatticeRenderer.State> {
    private static final Identifier LIGHT=Identifier.fromNamespaceAndPath("tnd","textures/block/gate_light.png");
    public static final class State extends BlockEntityRenderState {boolean visible;float time;}
    public State createRenderState(){return new State();}
    @Override public void extractRenderState(PhaseLatticeEntity e,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderer.super.extractRenderState(e,s,partial,camera,overlay);var p=Minecraft.getInstance().player;s.visible=p!=null&&e.getLevel()!=null&&p.getData(Convergence.PERCEPTION)>e.getLevel().getGameTime();s.time=AtmosphereOptions.ANIMATIONS.get()?(e.getLevel().getGameTime()%24000+partial)*.012f:0;}
    @Override public void submit(State s,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){if(!s.visible)return;poses.pushPose();poses.translate(.5,2,.5);final float time=s.time;
        collector.submitCustomGeometry(poses,RenderTypes.entityTranslucentEmissive(LIGHT),(pose,v)->{
            float[][] p=new float[16][3];for(int i=0;i<16;i++){double x=(i&1)==0?-1:1,y=(i&2)==0?-1:1,z=(i&4)==0?-1:1,w=(i&8)==0?-1:1;
                double xx=x*Math.cos(time)-w*Math.sin(time),ww=x*Math.sin(time)+w*Math.cos(time),yy=y*Math.cos(time*.63)-z*Math.sin(time*.63),zz=y*Math.sin(time*.63)+z*Math.cos(time*.63),scale=1.55/(2.7-ww*.65);
                p[i][0]=(float)(xx*scale);p[i][1]=(float)(yy*scale);p[i][2]=(float)(zz*scale);}
            for(int i=0;i<16;i++)for(int bit=0;bit<4;bit++){int j=i^(1<<bit);if(j<=i)continue;line(pose,v,p[i],p[j],bit==3?0xbfa6bff7:0xdfb0efe9);}
        });poses.popPose();
    }
    private static void line(PoseStack.Pose pose,VertexConsumer v,float[] a,float[] b,int color){float w=.011f;for(int axis=0;axis<3;axis++){float[] n=new float[]{axis==0?w:0,axis==1?w:0,axis==2?w:0};vertex(pose,v,a,n,1,color);vertex(pose,v,a,n,-1,color);vertex(pose,v,b,n,-1,color);vertex(pose,v,b,n,1,color);vertex(pose,v,b,n,1,color);vertex(pose,v,b,n,-1,color);vertex(pose,v,a,n,-1,color);vertex(pose,v,a,n,1,color);}}
    private static void vertex(PoseStack.Pose p,VertexConsumer v,float[] a,float[] n,int sign,int color){v.addVertex(p,a[0]+n[0]*sign,a[1]+n[1]*sign,a[2]+n[2]*sign).setColor(color).setUv(.5f,.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(p,0,1,0);}
    @Override public AABB getRenderBoundingBox(PhaseLatticeEntity e){return new AABB(e.getBlockPos()).inflate(4);}
    @Override public boolean shouldRenderOffScreen(){return true;}
}
