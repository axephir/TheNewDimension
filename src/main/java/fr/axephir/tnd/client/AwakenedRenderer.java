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

/** Original jointed models. Wind-up, strike recoil, breathing, head tracking and gait use native render state. */
public final class AwakenedRenderer extends MobRenderer<AwakenedMob,AwakenedRenderer.State,AwakenedRenderer.Model> {
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    private static ModelLayerLocation layer(boolean heavy){return new ModelLayerLocation(id(heavy?"lithoguard":"glass_stalker"),"main");}
    private final boolean heavy;
    public AwakenedRenderer(EntityRendererProvider.Context c,boolean heavy){super(c,new Model(c.bakeLayer(layer(heavy)),heavy),heavy?.95f:.6f);this.heavy=heavy;}
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(layer(true),()->Model.layer(true));e.registerLayerDefinition(layer(false),()->Model.layer(false));}
    public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(VestigeEntities.LITHOGUARD.get(),c->new AwakenedRenderer(c,true));e.registerEntityRenderer(VestigeEntities.STALKER.get(),c->new AwakenedRenderer(c,false));}
    public static final class State extends LivingEntityRenderState {public int charge,recovery;public boolean awake;}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(AwakenedMob e,State s,float partial){super.extractRenderState(e,s,partial);s.charge=e.charge();s.recovery=e.recovery();s.awake=e.awake();}
    @Override public Identifier getTextureLocation(State s){return id("textures/entity/"+(heavy?"lithoguard":"glass_stalker")+(s.charge>0?"_charged":"")+".png");}
    @Override protected int getBlockLightLevel(AwakenedMob e,BlockPos p){return Math.max(e.charge()>0?12:5,super.getBlockLightLevel(e,p));}
    public static final class Model extends EntityModel<State> {
        private final ModelPart body,head,core,left,right;
        private final ModelPart[] legs=new ModelPart[4];
        private final boolean heavy;
        Model(ModelPart root,boolean heavy){super(root);this.heavy=heavy;body=root.getChild("body");head=root.getChild("head");core=root.getChild("core");left=root.getChild("left");right=root.getChild("right");for(int i=0;i<4;i++)legs[i]=root.getChild("leg"+i);}
        private static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
        static LayerDefinition layer(boolean heavy){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            if(heavy){
                var body=r.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),new PartPose(0,-2,1,.1f,0,0,3.3f,3,2.5f));
                for(int side:new int[]{-1,1})body.addOrReplaceChild("pauldron"+side,cube(0,8,-1,-1,-1,2,2,2),new PartPose(side*2.2f,-1.6f,-.1f,0,0,side*.2f,1.5f,1.5f,1.5f));
                var head=r.addOrReplaceChild("head",cube(0,0,-2,-2,-2,4,4,4),PartPose.offset(0,-17,-2).withScale(2));
                for(int side:new int[]{-1,1})head.addOrReplaceChild("horn"+side,cube(0,8,-.5f,-3,-.5f,1,3,1),PartPose.offsetAndRotation(side*1.4f,-1,0,0,0,side*.3f));
                r.addOrReplaceChild("core",cube(8,8,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,-3,-5,0,0,.785f).withScale(2.1f));
                for(int side:new int[]{-1,1}){
                    var arm=r.addOrReplaceChild(side<0?"left":"right",cube(0,0,-1,-1,-1,2,4,2),new PartPose(side*12,-5,0,-.1f,0,side*-.12f,2,2,2));
                    arm.addOrReplaceChild("forearm",cube(0,8,-1,0,-1,2,4,2),PartPose.offset(0,2.8f,-.1f).withScale(1.3f));
                    arm.addOrReplaceChild("knuckle",cube(8,8,-1,-1,-1,2,2,2),PartPose.offset(0,8,-.2f).withScale(1.8f));
                }
                for(int i=0;i<4;i++)r.addOrReplaceChild("leg"+i,cube(0,8,-1,0,-1,2,4,2),new PartPose(i%2==0?-5:5,12,i<2?-2.5f:3.5f,0,0,0,1.6f,3,1.6f));
            }else{
                r.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),new PartPose(0,14,0,0,0,0,2.1f,1.1f,2.4f));
                var head=r.addOrReplaceChild("head",cube(0,8,-1,-1,-1,2,2,2),PartPose.offset(0,13,-7).withScale(2));
                for(int side:new int[]{-1,1})head.addOrReplaceChild("mandible"+side,cube(0,8,-.5f,0,-2,1,2,3),PartPose.offsetAndRotation(side*1.1f,.3f,-.3f,.25f,side*.2f,0));
                r.addOrReplaceChild("core",cube(8,8,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,10,-1,0,0,.785f).withScale(1.5f));
                for(int side:new int[]{-1,1}){
                    var fin=r.addOrReplaceChild(side<0?"left":"right",cube(0,8,0,-3,-1,1,6,2),new PartPose(side*5,12,1,.3f,side*.3f,side*.65f,1,1.7f,2));
                    for(int j=0;j<3;j++)fin.addOrReplaceChild("blade"+j,cube(8,8,0,-2,-.5f,1,4,1),PartPose.offsetAndRotation(side*(j+1),-j,j*.8f,0,0,side*.2f));
                }
                for(int i=0;i<4;i++){var leg=r.addOrReplaceChild("leg"+i,cube(0,8,-.5f,0,-.5f,1,4,1),new PartPose(i%2==0?-6:6,17,i<2?-4:5,0,0,i%2==0?.5f:-.5f,1.3f,1.8f,1.3f));leg.addOrReplaceChild("claw",cube(8,8,-.5f,0,-1,1,1,2),PartPose.offset(0,3,-.5f));}
            }
            return LayerDefinition.create(mesh,16,16);
        }
        @Override public void setupAnim(State s){
            super.setupAnim(s);float age=s.ageInTicks,gait=s.walkAnimationPos*(heavy?.9f:1.9f),speed=Math.min(1,s.walkAnimationSpeed);
            float wind=s.charge>0?1-s.charge/(heavy?40f:32f):0;wind=wind*wind*(3-2*wind);
            float recoil=Math.max(0,(s.recovery-(heavy?49:40))/(heavy?16f:15f));
            for(int i=0;i<4;i++)legs[i].xRot=Mth.cos(gait+(i==0||i==3?0:Mth.PI))*speed*(heavy?.35f:.75f);
            head.yRot=s.yRot*Mth.DEG_TO_RAD*.6f;head.xRot=s.xRot*Mth.DEG_TO_RAD*.4f;
            body.y+=Mth.sin(age*.07f)*(heavy?.15f:.3f)+wind*(heavy?1.8f:2)-recoil;
            core.yRot=age*(s.charge>0?.12f:.02f);core.zRot+=Mth.sin(age*.08f)*.08f;
            if(heavy){left.xRot=-wind*2.1f+recoil*.6f;right.xRot=-wind*2.1f+recoil*.6f;left.zRot-=wind*.25f;right.zRot+=wind*.25f;head.xRot+=wind*.25f;if(!s.awake){head.xRot=.4f;body.y+=2;core.y+=1;}}
            else{left.zRot-=wind*.8f+Mth.sin(age*.14f)*.09f;right.zRot+=wind*.8f+Mth.sin(age*.14f)*.09f;head.z-=wind*1.5f;head.xRot-=wind*.3f;body.xRot-=recoil*.15f;}
        }
    }
}
