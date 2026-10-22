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

/** Precise rounded light aperture: quiet glass, pearl rim, a soft halo and two flowing energy traces. */
public final class GateRenderer implements BlockEntityRenderer<PortalControllerEntity,GateRenderer.State> {
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("tnd","textures/block/gate_light.png");
    private static final int SEGMENTS=128;
    public static final class State extends BlockEntityRenderState {float time,open;boolean online,charging;net.minecraft.core.Direction facing;}
    public State createRenderState(){return new State();}
    @Override public void extractRenderState(PortalControllerEntity e,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderer.super.extractRenderState(e,s,partial,camera,overlay);
        long now=e.getLevel()==null?0:e.getLevel().getGameTime();s.time=now%24000+partial;s.online=e.getBlockState().getValue(PortalControllerBlock.ONLINE);s.charging=e.getBlockState().getValue(PortalControllerBlock.CHARGING);s.facing=e.getBlockState().getValue(PortalControllerBlock.FACING);s.open=s.online?1:s.charging?Math.clamp((now-e.chargeStart()+partial)/120f,.02f,1):0;if(!AtmosphereOptions.ANIMATIONS.get())s.time=0;
    }
    private static float contour(double a,boolean x){double v=x?Math.cos(a):Math.sin(a);return (float)(Math.copySign(Math.pow(Math.abs(v),.62),v)*(x?1.43:2.06));}
    @Override public void submit(State s,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
        if(s.open<=0)return;poses.pushPose();poses.translate(.5,0,.5);poses.mulPose(Axis.YP.rotationDegrees(180-s.facing.toYRot()));poses.translate(0,2.9,.58);
        float progress=s.open*s.open*(3-2*s.open);poses.scale(.9f+.1f*progress,progress,1);
        final float time=s.time;collector.submitCustomGeometry(poses,RenderTypes.entityTranslucentEmissive(TEXTURE),(pose,v)->{
            for(int i=0;i<SEGMENTS;i++){
                double a=i*2*Math.PI/SEGMENTS,b=(i+1)*2*Math.PI/SEGMENTS;float ax=contour(a,true),ay=contour(a,false),bx=contour(b,true),by=contour(b,false);
                // Uniform white texture, smooth vertex colour: no pixel noise, faceted UV fan or orbiting glyphs.
                vertex(pose,v,0,0,0,.5f,.5f,0x183a6578);vertex(pose,v,ax,ay,0,.5f,.5f,0x5399d2dd);vertex(pose,v,bx,by,0,.5f,.5f,0x5399d2dd);vertex(pose,v,0,0,0,.5f,.5f,0x183a6578);
                vertex(pose,v,0,0,.005f,.5f,.5f,0x183a6578);vertex(pose,v,bx,by,.005f,.5f,.5f,0x5399d2dd);vertex(pose,v,ax,ay,.005f,.5f,.5f,0x5399d2dd);vertex(pose,v,0,0,.005f,.5f,.5f,0x183a6578);
                for(int halo=3;halo>=0;halo--){float scale=1+halo*.018f;line(pose,v,ax*scale,ay*scale,bx*scale,by*scale,halo==0?.018f:.025f,halo==0?0xe6d8f8f8:((22-halo*5)<<24)|0xa7e4ee);}
                line(pose,v,ax*.965f,ay*.965f,bx*.965f,by*.965f,.006f,0x9b8dc9d8);
                double trace=Math.floorMod(i-(int)(time*.30),64)/64.;if(trace<.15)line(pose,v,ax,ay,bx,by,.029f,((int)(235*(1-trace/.15))<<24)|0xf4ffff);
            }
            // Sparse moving pin-lights stay on the light rail instead of obscuring the entrance.
            for(int i=0;i<4;i++){double a=time*.012+i*Math.PI/2;float x=contour(a,true),y=contour(a,false);line(pose,v,x-.018f,y-.025f,x+.018f,y+.025f,.018f,0xeaffffff);}
        });poses.popPose();
    }
    private static void line(PoseStack.Pose p,VertexConsumer v,float ax,float ay,float bx,float by,float width,int color){float dx=bx-ax,dy=by-ay,len=Math.max(.001f,(float)Math.sqrt(dx*dx+dy*dy)),nx=-dy/len*width,ny=dx/len*width;
        vertex(p,v,ax+nx,ay+ny,-.01f,0,0,color);vertex(p,v,ax-nx,ay-ny,-.01f,0,1,color);vertex(p,v,bx-nx,by-ny,-.01f,1,1,color);vertex(p,v,bx+nx,by+ny,-.01f,1,0,color);
        vertex(p,v,bx+nx,by+ny,.01f,1,0,color);vertex(p,v,bx-nx,by-ny,.01f,1,1,color);vertex(p,v,ax-nx,ay-ny,.01f,0,1,color);vertex(p,v,ax+nx,ay+ny,.01f,0,0,color);
    }
    private static void vertex(PoseStack.Pose p,VertexConsumer v,float x,float y,float z,float u,float t,int color){v.addVertex(p,x,y,z).setColor(color).setUv(u,t).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(p,0,0,-1);}
    @Override public AABB getRenderBoundingBox(PortalControllerEntity e){return new AABB(e.getBlockPos()).inflate(3,5,3);}
    @Override public boolean shouldRenderOffScreen(){return true;}
}
