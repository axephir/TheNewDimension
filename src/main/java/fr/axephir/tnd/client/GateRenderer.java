package fr.axephir.tnd.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.axephir.tnd.world.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.*;

/** A real animated, full-bright aperture: a rippling membrane, counter-rotating rings and orbiting runes. */
public final class GateRenderer implements BlockEntityRenderer<PortalControllerEntity,GateRenderer.State> {
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("tnd","textures/block/gate_membrane.png");
    public static final class State extends BlockEntityRenderState {float time,open;boolean online,charging;net.minecraft.core.Direction facing;}
    public State createRenderState(){return new State();}
    @Override public void extractRenderState(PortalControllerEntity e,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderer.super.extractRenderState(e,s,partial,camera,overlay);
        long now=e.getLevel()==null?0:e.getLevel().getGameTime();s.time=now%24000+partial;s.online=e.getBlockState().getValue(PortalControllerBlock.ONLINE);s.charging=e.getBlockState().getValue(PortalControllerBlock.CHARGING);
        s.facing=e.getBlockState().getValue(PortalControllerBlock.FACING);s.open=s.online?1:s.charging?Math.clamp((now-e.chargeStart()+partial)/120f,.02f,1):0;
        if(!AtmosphereOptions.ANIMATIONS.get())s.time=0;
    }
    @Override public void submit(State s,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
        if(s.open<=0)return;poses.pushPose();poses.translate(.5,0,.5);poses.mulPose(Axis.YP.rotationDegrees(180-s.facing.toYRot()));poses.translate(0,2.9,.58);
        float scale=(float)Math.sin(s.open*Math.PI/2);poses.scale(scale,scale,scale);
        final float time=s.time,opening=s.open;
        collector.submitCustomGeometry(poses,RenderTypes.entityTranslucentEmissive(TEXTURE),(pose,v)->{
            // Double-sided translucent fan; UV rotation never leaves the texture's pixel-art domain.
            for(int i=0;i<48;i++){
                double a=i*Math.PI/24,b=(i+1)*Math.PI/24;
                float ax=(float)Math.cos(a)*1.38f,ay=(float)Math.sin(a)*1.96f,bx=(float)Math.cos(b)*1.38f,by=(float)Math.sin(b)*1.96f;
                float ripple=(float)Math.sin(a*5+time*.035)*.035f;
                int color=((int)(opening*155)<<24)|0x77d9e7;
                vertex(pose,v,0,0,.01f,.5f,.5f,color);vertex(pose,v,ax,ay,ripple,uv(a,time,true),uv(a,time,false),color);vertex(pose,v,bx,by,ripple,uv(b,time,true),uv(b,time,false),color);vertex(pose,v,0,0,.01f,.5f,.5f,color);
                vertex(pose,v,0,0,-.01f,.5f,.5f,color);vertex(pose,v,bx,by,-ripple,uv(b,-time,true),uv(b,-time,false),color);vertex(pose,v,ax,ay,-ripple,uv(a,-time,true),uv(a,-time,false),color);vertex(pose,v,0,0,-.01f,.5f,.5f,color);
            }
            for(int ring=0;ring<2;ring++)for(int i=0;i<48;i++){
                double a=i*Math.PI/24+time*(ring==0?.009:-.012),b=a+.105;
                if((i+ring)%6==0)continue;float r=ring==0?1.50f:1.66f;int color=ring==0?0xe855e5ee:0xdcb47bea;
                quad(pose,v,(float)Math.cos(a)*r,(float)Math.sin(a)*2.14f,(float)Math.cos(b)*r,(float)Math.sin(b)*2.14f,.055f,color);
            }
            for(int i=0;i<8;i++){double a=time*.012+i*Math.PI/4;float x=(float)Math.cos(a)*1.76f,y=(float)Math.sin(a)*2.25f;
                quad(pose,v,x-.055f,y-.08f,x+.055f,y+.08f,.03f,0xe8d8e9f2);
            }
        });poses.popPose();
    }
    private static float uv(double a,float time,boolean x){return .5f+.47f*(float)(x?Math.cos(a+time*.008):Math.sin(a+time*.008));}
    private static void quad(PoseStack.Pose p,VertexConsumer v,float ax,float ay,float bx,float by,float width,int color){
        float dx=bx-ax,dy=by-ay,len=(float)Math.sqrt(dx*dx+dy*dy);float nx=-dy/Math.max(.001f,len)*width,ny=dx/Math.max(.001f,len)*width;
        vertex(p,v,ax+nx,ay+ny,-.06f,0,0,color);vertex(p,v,ax-nx,ay-ny,-.06f,0,1,color);vertex(p,v,bx-nx,by-ny,-.06f,1,1,color);vertex(p,v,bx+nx,by+ny,-.06f,1,0,color);
        vertex(p,v,bx+nx,by+ny,.06f,1,0,color);vertex(p,v,bx-nx,by-ny,.06f,1,1,color);vertex(p,v,ax-nx,ay-ny,.06f,0,1,color);vertex(p,v,ax+nx,ay+ny,.06f,0,0,color);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer v,float x,float y,float z,float u,float t,int color){v.addVertex(p,x,y,z).setColor(color).setUv(u,t).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(p,0,0,-1);}
    @Override public AABB getRenderBoundingBox(PortalControllerEntity e){return new AABB(e.getBlockPos()).inflate(3,5,3);}
    @Override public boolean shouldRenderOffScreen(){return true;}
}
