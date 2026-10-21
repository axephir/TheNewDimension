package fr.axephir.tnd.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Original jointed silhouettes; every UV island fits the same 16x16 atlas. */
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
            root.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),PartPose.offset(0,6,0).withScale(2));
            root.addOrReplaceChild("core",cube(8,8,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,12,0,0,0,.785f).withScale(2));
            root.addOrReplaceChild("head",cube(0,8,-1,-1,-1,2,2,2),PartPose.offset(0,5,-5).withScale(1.5f));
            for(int i=0;i<4;i++) {
                float x=(i%2==0?-1:1)*4,z=(i<2?-1:1)*4;
                var leg=root.addOrReplaceChild("leg"+i,cube(0,8,-1,0,-1,2,4,2),new PartPose(x,13,z,0,0,(i%2==0?.25f:-.25f),1,2.4f,1));
                leg.addOrReplaceChild("foot",cube(0,8,-1,0,-2,2,1,3),PartPose.offset(0,3.6f,-.5f));
                root.addOrReplaceChild("brace"+i,cube(0,8,-1,-2,-1,2,4,2),PartPose.offsetAndRotation(x*.8f,11,z*.8f,0,0,i%2==0?.7f:-.7f));
            }
        } else {
            root.addOrReplaceChild("body",cube(0,0,-2,0,-2,4,3,4),PartPose.offset(0,14.5f,0).withScale(1.65f));
            root.addOrReplaceChild("core",cube(8,8,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,14,0,0,0,.785f).withScale(1.2f));
            var head=root.addOrReplaceChild("head",cube(8,8,-1,-1,-1,2,2,2),PartPose.offset(0,16,-4).withScale(1.5f));
            for(int i=0;i<2;i++)head.addOrReplaceChild("feelers"+i,cube(0,8,0,-3,0,1,3,1),PartPose.offsetAndRotation(i==0?-1.5f:.8f,-.5f,-.5f,0,0,i==0?-.3f:.3f));
            for(int i=0;i<4;i++)root.addOrReplaceChild("leg"+i,cube(0,8,-.5f,0,-.5f,1,4,1),PartPose.offsetAndRotation(i%2==0?-3.5f:3.5f,19,i<2?-2.5f:2.5f,0,0,i%2==0?.3f:-.3f));
        }
        return LayerDefinition.create(mesh,16,16);
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
            core.yRot=state.ageInTicks*(state.charge>0?.18f:.025f);
            core.y+=Mth.sin(state.ageInTicks*.08f)*.4f;
            body.zRot=state.charge>0?Mth.sin(state.ageInTicks*1.8f)*.035f:0;
        } else {
            body.y+=state.sitting?3:Mth.sin(state.ageInTicks*.12f)*.18f;
            head.y+=state.sitting?3:0;core.y+=state.sitting?3:Mth.sin(state.ageInTicks*.12f)*.18f;
            core.yRot=state.ageInTicks*(state.bonded?.06f:.015f);
        }
    }
}
