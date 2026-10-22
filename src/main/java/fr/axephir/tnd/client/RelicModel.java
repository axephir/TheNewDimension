package fr.axephir.tnd.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Original jointed silhouettes; 64px atlases separate shell, joints, face and luminous mineral islands. */
public final class RelicModel extends EntityModel<RelicRenderState> {
    private final ModelPart body,core,head;
    private final ModelPart[] legs=new ModelPart[4];
    private final boolean sentinel;
    public RelicModel(ModelPart root,boolean sentinel) {
        super(root);this.sentinel=sentinel;body=root.getChild("body");core=root.getChild("core");head=root.getChild("head");
        for(int i=0;i<4;i++)legs[i]=root.getChild("leg"+i);
    }
    private static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
    public static LayerDefinition layer(boolean sentinel) {
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        if(sentinel) {
            var shell=root.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),PartPose.offset(0,6,0).withScale(2));
            shell.addOrReplaceChild("carapace",cube(0,32,-2.4f,-2.5f,-2.4f,4.8f,1.2f,4.8f),PartPose.ZERO);
            for(int side:new int[]{-1,1})shell.addOrReplaceChild("crest"+side,cube(32,32,-.4f,-2,-1,.8f,3,2),PartPose.offsetAndRotation(side*1.7f,-2,0,0,0,side*.3f));
            root.addOrReplaceChild("core",cube(48,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,12,0,0,0,.785f).withScale(2));
            var face=root.addOrReplaceChild("head",cube(0,16,-1,-1,-1,2,2,2),PartPose.offset(0,5,-5).withScale(1.5f));
            face.addOrReplaceChild("optic",cube(48,16,-.8f,-.3f,-1.2f,1.6f,.6f,.4f),PartPose.ZERO);
            face.addOrReplaceChild("brow",cube(0,32,-1.2f,-1.1f,-1.4f,2.4f,.6f,1),PartPose.ZERO);
            for(int i=0;i<4;i++) {
                float x=(i%2==0?-1:1)*4,z=(i<2?-1:1)*4;
                var leg=root.addOrReplaceChild("leg"+i,cube(32,0,-1,0,-1,2,4,2),new PartPose(x,13,z,0,0,(i%2==0?.25f:-.25f),1,2.4f,1));
                leg.addOrReplaceChild("foot",cube(32,0,-1,0,-2,2,1,3),PartPose.offset(0,3.6f,-.5f));
                root.addOrReplaceChild("brace"+i,cube(32,0,-1,-2,-1,2,4,2),PartPose.offsetAndRotation(x*.8f,11,z*.8f,0,0,i%2==0?.7f:-.7f));
            }
        } else {
            var shell=root.addOrReplaceChild("body",cube(0,0,-2,0,-2,4,3,4),PartPose.offset(0,14.5f,0).withScale(1.65f));
            for(int i=0;i<3;i++)shell.addOrReplaceChild("segment"+i,cube(0,32,-2.1f,-.5f,-.5f,4.2f,1,1),PartPose.offset(0,0,-1.4f+i*1.4f));
            var tail=shell.addOrReplaceChild("tail",cube(32,32,-.5f,-.5f,0,1,1,2),PartPose.offsetAndRotation(0,1.5f,2,-.4f,0,0));
            tail.addOrReplaceChild("tip",cube(48,0,-.7f,-.7f,-.3f,1.4f,1.4f,1.4f),PartPose.offsetAndRotation(0,0,2.3f,0,0,.78f));
            for(int i=0;i<3;i++)shell.addOrReplaceChild("crest"+i,cube(48,0,-.45f,-2.3f,-.45f,.9f,2.5f,.9f),PartPose.offsetAndRotation(0,-.4f,-1.3f+i*1.3f,-.25f,0,0));
            for(int side:new int[]{-1,1})shell.addOrReplaceChild("mantle"+side,cube(0,32,-1.1f,-.3f,-1.9f,2.2f,.7f,3.8f),PartPose.offsetAndRotation(side*2.1f,.4f,0,0,0,side*.45f));
            root.addOrReplaceChild("core",cube(48,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,14,0,0,0,.785f).withScale(1.2f));
            var head=root.addOrReplaceChild("head",cube(48,0,-1,-1,-1,2,2,2),PartPose.offset(0,16,-4).withScale(1.5f));
            head.addOrReplaceChild("mask",cube(0,16,-1.3f,-1.1f,-1.3f,2.6f,1,1),PartPose.ZERO);
            for(int side:new int[]{-1,1})head.addOrReplaceChild("eye"+side,cube(48,16,-.2f,-.2f,-.2f,.4f,.4f,.4f),PartPose.offset(side*.65f,.15f,-1.1f));
            for(int i=0;i<2;i++)head.addOrReplaceChild("feelers"+i,cube(32,0,0,-3,0,1,3,1),PartPose.offsetAndRotation(i==0?-1.5f:.8f,-.5f,-.5f,0,0,i==0?-.3f:.3f));
            for(int i=0;i<4;i++)root.addOrReplaceChild("leg"+i,cube(32,0,-.5f,0,-.5f,1,4,1),PartPose.offsetAndRotation(i%2==0?-3.5f:3.5f,19,i<2?-2.5f:2.5f,0,0,i%2==0?.3f:-.3f));
        }
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(RelicRenderState state) {
        super.setupAnim(state);
        float gait=state.walkAnimationPos*1.6f,amount=Math.min(1,state.walkAnimationSpeed)*.9f;
        for(int i=0;i<4;i++) {
            legs[i].xRot=Mth.cos(gait+(i==0||i==3?0:Mth.PI))*amount;
            if(state.sitting){legs[i].xRot=1.2f;legs[i].y-=1;}
        }
        head.yRot=state.yRot*Mth.DEG_TO_RAD*.5f;head.xRot=state.xRot*Mth.DEG_TO_RAD*.4f;
        if(sentinel) {
            float wind=state.charge>0?1-state.charge/32f:0;wind=wind*wind*(3-2*wind);
            body.y+=wind*1.6f;head.xRot-=wind*.25f;head.z-=wind*.6f;
            for(int i=0;i<4;i++){legs[i].zRot+=(i%2==0?1:-1)*wind*.18f;legs[i].y-=wind*.4f;}
            core.xScale*=1+wind*.16f;core.yScale*=1+wind*.16f;core.zScale*=1+wind*.16f;
            core.yRot=state.ageInTicks*(state.charge>0?.18f:.025f);
            core.y+=Mth.sin(state.ageInTicks*.08f)*.4f;
            body.zRot=state.charge>0?Mth.sin(state.ageInTicks*1.8f)*.035f:0;
        } else {
            body.y+=state.sitting?3:Mth.sin(state.ageInTicks*.12f)*.18f;
            head.y+=state.sitting?3:0;core.y+=state.sitting?3:Mth.sin(state.ageInTicks*.12f)*.18f;
            core.yRot=state.ageInTicks*(state.bonded?.06f:.015f);
            for(int i=0;i<2;i++){var feeler=head.getChild("feelers"+i);feeler.zRot+=Mth.sin(state.ageInTicks*.09f+i)*.10f;}
            head.z+=Mth.sin(state.ageInTicks*.12f)*.15f;
            for(int i=0;i<3;i++){var crest=body.getChild("crest"+i);crest.visible=state.stage>=1;crest.xRot-=state.pulse>0?.3f:0;}
            for(int side:new int[]{-1,1}){var mantle=body.getChild("mantle"+side);mantle.visible=state.stage>=2;mantle.zRot+=Mth.sin(state.ageInTicks*.06f)*.06f*side;}
            var tail=body.getChild("tail");tail.yRot=Mth.sin(state.ageInTicks*.08f)*.22f;tail.zScale=1+state.stage*.35f;
            if(state.downed){body.y=21;body.zRot=.48f;head.y=22;head.zRot=.48f;core.y=21;for(var leg:legs){leg.y=22;leg.xRot=1.5f;}core.yRot=state.ageInTicks*.008f;}
            if(state.bondTicks>0){core.y-=1.4f;core.yRot=state.ageInTicks*.1f;head.xRot=-.2f;}
        }
    }
}
