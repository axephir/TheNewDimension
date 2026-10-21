package fr.axephir.tnd.client;

import fr.axephir.tnd.entities.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** A suspended broken reliquary, articulated crown and six detached stone fingers. 64px UV islands. */
public final class CustodianRenderer extends MobRenderer<FracturedCustodian,CustodianRenderer.State,CustodianRenderer.Model> {
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    private static final ModelLayerLocation LAYER=new ModelLayerLocation(id("fractured_custodian"),"main");
    public CustodianRenderer(EntityRendererProvider.Context c){super(c,new Model(c.bakeLayer(LAYER)),.85f);}
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,Model::layer);}
    public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(VestigeEntities.CUSTODIAN.get(),CustodianRenderer::new);}
    public static final class State extends LivingEntityRenderState {public int charge,exposed;public boolean enraged;}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(FracturedCustodian entity,State state,float partial){super.extractRenderState(entity,state,partial);state.charge=entity.charge();state.exposed=entity.exposed();state.enraged=entity.enraged();}
    @Override public Identifier getTextureLocation(State s){return id("textures/entity/fractured_custodian"+(s.exposed>0?"_open":s.enraged?"_enraged":"")+".png");}
    @Override protected int getBlockLightLevel(FracturedCustodian e,BlockPos p){return Math.max(e.exposed()>0?12:6,super.getBlockLightLevel(e,p));}
    public static final class Model extends EntityModel<State> {
        private final ModelPart crown,core,body,left,right;
        Model(ModelPart root){super(root);crown=root.getChild("crown");core=root.getChild("core");body=root.getChild("body");left=root.getChild("left");right=root.getChild("right");}
        private static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
        static LayerDefinition layer(){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            var body=r.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),new PartPose(0,4,2,0,0,0,3,2,2));
            body.addOrReplaceChild("plinth",cube(32,0,-1,-1,-1,2,2,2),PartPose.offset(0,4,0).withScale(1.6f));
            for(int side:new int[]{-1,1})body.addOrReplaceChild("broken_rib"+side,cube(0,32,-.4f,-2.5f,-.5f,.8f,5,1),PartPose.offsetAndRotation(side*1.6f,-2,-1.5f,0,0,side*.18f));
            r.addOrReplaceChild("core",cube(48,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,-4,-1,0,0,.785f).withScale(3.4f));
            var crown=r.addOrReplaceChild("crown",CubeListBuilder.create(),PartPose.offset(0,-12,0));
            for(int i=0;i<6;i++){double a=i*Math.PI/3;float x=(float)Math.cos(a)*11,z=(float)Math.sin(a)*11;
                crown.addOrReplaceChild("tooth"+i,cube(32,0,-1,-2,-1,2,4,2),new PartPose(x,-3,z,.2f,(float)-a,.2f,1.5f,2,1.5f));}
            for(int i=0;i<8;i++){double a=i*Math.PI/4;float x=(float)Math.cos(a)*7,z=(float)Math.sin(a)*7;
                crown.addOrReplaceChild("diadem"+i,cube(0,32,-1.5f,-.5f,-1,3,1,2),PartPose.offsetAndRotation(x,1,z,0,(float)-a,0));
                r.addOrReplaceChild("cage"+i,cube(32,32,-.5f,-3,-.5f,1,6,1),PartPose.offsetAndRotation(x,-3,z,0,(float)-a,i%2==0?.2f:-.2f));}
            for(int side:new int[]{-1,1}){
                var arm=r.addOrReplaceChild(side<0?"left":"right",cube(0,0,-2,-2,-2,4,4,4),new PartPose(side*13,0,0,0,0,side*-.25f,1.5f,1.5f,1.5f));
                arm.addOrReplaceChild("cuff",cube(0,32,-2.4f,-1.8f,-2.4f,4.8f,1.1f,4.8f),PartPose.ZERO);
                for(int i=0;i<3;i++)arm.addOrReplaceChild("finger"+i,cube(32,0,-1,0,-1,2,4,2),new PartPose((i-1)*2.3f,3,-1,0,0,(i-1)*-.15f,.65f,2.4f,.65f));
            }
            r.addOrReplaceChild("keel",cube(32,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,19,0,0,0,.785f).withScale(2));
            return LayerDefinition.create(mesh,64,64);
        }
        @Override public void setupAnim(State s){super.setupAnim(s);float a=s.ageInTicks;
            crown.yRot=a*(s.charge>0?.085f:.018f);crown.y+=Mth.sin(a*.055f)*.7f;
            core.yRot=a*(s.exposed>0?.10f:.03f);core.zRot+=Mth.sin(a*.08f)*.15f;
            body.y+=Mth.sin(a*.07f)*.6f;body.zRot=s.charge>0?Mth.sin(a*1.4f)*.025f:0;
            float tension=s.charge>0?1-Math.min(1,s.charge/48f):0;
            left.xRot=-tension*1.25f;right.xRot=-tension*1.25f;crown.y+=tension*2;
            core.xScale*=1+(s.exposed>0?.08f:0)+Mth.sin(a*.12f)*.025f;
            core.yScale*=1+(s.exposed>0?.08f:0);core.zScale*=1+(s.exposed>0?.08f:0);
            float spread=s.exposed>0?3:s.charge>0?-1:0;
            left.x-=spread;right.x+=spread;left.zRot+=Mth.sin(a*.06f)*.08f;right.zRot-=Mth.sin(a*.06f)*.08f;
        }
    }
}
