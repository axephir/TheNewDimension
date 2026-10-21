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
                for(int side:new int[]{-1,1})body.addOrReplaceChild("pauldron"+side,cube(32,0,-1,-1,-1,2,2,2),new PartPose(side*2.2f,-1.6f,-.1f,0,0,side*.2f,1.5f,1.5f,1.5f));
                body.addOrReplaceChild("backplate",cube(0,32,-1.7f,-1.5f,1.5f,3.4f,3,1),PartPose.ZERO);
                for(int i=0;i<3;i++)body.addOrReplaceChild("spine"+i,cube(32,32,-.35f,-1,-.35f,.7f,2,.7f),PartPose.offsetAndRotation(0,-1.7f+i*.9f,2.4f,.55f,0,0));
                r.addOrReplaceChild("hips",cube(0,32,-3,-2.5f,-2.5f,6,5,5),PartPose.offset(0,9,1).withScale(1.65f));
                r.addOrReplaceChild("neck",cube(32,0,-2,-2.5f,-2,4,5,4),PartPose.offset(0,-11,-1));
                var head=r.addOrReplaceChild("head",cube(0,16,-2,-2,-2,4,4,4),PartPose.offset(0,-17,-2).withScale(2));
                for(int side:new int[]{-1,1})head.addOrReplaceChild("horn"+side,cube(32,0,-.5f,-3,-.5f,1,3,1),PartPose.offsetAndRotation(side*1.4f,-1,0,0,0,side*.3f));
                head.addOrReplaceChild("brow",cube(0,32,-2.3f,-1.3f,-2.5f,4.6f,1,1),PartPose.ZERO);
                head.addOrReplaceChild("visor",cube(48,16,-1.7f,-.2f,-2.15f,3.4f,.4f,.4f),PartPose.ZERO);
                head.addOrReplaceChild("jaw",cube(32,32,-1.5f,1,-2.5f,3,1.4f,1.5f),PartPose.ZERO);
                r.addOrReplaceChild("core",cube(48,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,-3,-5,0,0,.785f).withScale(2.1f));
                for(int side:new int[]{-1,1}){
                    var arm=r.addOrReplaceChild(side<0?"left":"right",cube(0,0,-1,-1,-1,2,4,2),new PartPose(side*12,-5,0,-.1f,0,side*-.12f,2,2,2));
                    var forearm=arm.addOrReplaceChild("forearm",cube(32,0,-1,0,-1,2,4,2),PartPose.offset(0,2.8f,-.1f).withScale(1.3f));
                    var fist=forearm.addOrReplaceChild("knuckle",cube(0,32,-1,-1,-1,2,2,2),PartPose.offset(0,3.8f,-.2f).withScale(1.4f));
                    for(int j=0;j<3;j++)fist.addOrReplaceChild("finger"+j,cube(32,32,-.25f,0,-.4f,.5f,1.5f,.8f),PartPose.offset((j-1)*.7f,.7f,-.7f));
                    arm.addOrReplaceChild("elbow",cube(48,16,-1.2f,-.3f,-1.2f,2.4f,.6f,2.4f),PartPose.offset(0,3,0));
                }
                for(int i=0;i<4;i++){
                    var leg=r.addOrReplaceChild("leg"+i,cube(32,0,-1,0,-1,2,4,2),new PartPose(i%2==0?-5:5,12,i<2?-2.5f:3.5f,0,0,0,1.6f,3,1.6f));
                    leg.addOrReplaceChild("knee",cube(0,32,-1.3f,.8f,-1.4f,2.6f,1.5f,1),PartPose.ZERO);
                    leg.addOrReplaceChild("foot",cube(32,32,-1.3f,3.2f,-2,2.6f,.8f,3.5f),PartPose.ZERO);
                }
            }else{
                var thorax=r.addOrReplaceChild("body",cube(0,0,-2,-2,-2,4,4,4),new PartPose(0,14,0,0,0,0,2.1f,1.1f,2.4f));
                for(int i=0;i<3;i++)thorax.addOrReplaceChild("carapace"+i,cube(0,32,-2.3f,-.6f,-.6f,4.6f,1.2f,1.2f),PartPose.offsetAndRotation(0,-2.1f+i*.18f,-1.2f+i*1.2f,.1f,0,0));
                var head=r.addOrReplaceChild("head",cube(0,16,-1,-1,-1,2,2,2),PartPose.offset(0,13,-7).withScale(2));
                for(int side:new int[]{-1,1})head.addOrReplaceChild("mandible"+side,cube(32,0,-.5f,0,-2,1,2,3),PartPose.offsetAndRotation(side*1.1f,.3f,-.3f,.25f,side*.2f,0));
                for(int side:new int[]{-1,1}){
                    head.addOrReplaceChild("eye"+side,cube(48,16,-.25f,-.25f,-.2f,.5f,.5f,.4f),PartPose.offset(side*.65f,-.35f,-1.08f));
                    head.addOrReplaceChild("antenna"+side,cube(48,0,-.15f,-2.5f,-.15f,.3f,2.5f,.3f),PartPose.offsetAndRotation(side*.75f,-.8f,0,-.25f,0,side*.4f));
                }
                var tail=thorax.addOrReplaceChild("tail",cube(32,0,-.5f,0,0,1,1,3),PartPose.offsetAndRotation(0,0,1.7f,-.4f,0,0));
                tail.addOrReplaceChild("sting",cube(48,0,-.2f,-1.6f,0,.4f,2,1),PartPose.offset(0,0,2.6f));
                r.addOrReplaceChild("core",cube(48,0,-1,-1,-1,2,2,2),PartPose.offsetAndRotation(0,10,-1,0,0,.785f).withScale(1.5f));
                for(int side:new int[]{-1,1}){
                    var fin=r.addOrReplaceChild(side<0?"left":"right",cube(32,0,0,-3,-1,1,6,2),new PartPose(side*5,12,1,.3f,side*.3f,side*.65f,1,1.7f,2));
                    for(int j=0;j<3;j++)fin.addOrReplaceChild("blade"+j,cube(48,0,0,-2,-.5f,1,4,1),PartPose.offsetAndRotation(side*(j+1),-j,j*.8f,0,0,side*.2f));
                }
                for(int i=0;i<4;i++){var leg=r.addOrReplaceChild("leg"+i,cube(32,0,-.5f,0,-.5f,1,4,1),new PartPose(i%2==0?-6:6,17,i<2?-4:5,0,0,i%2==0?.5f:-.5f,1.3f,1.8f,1.3f));leg.addOrReplaceChild("claw",cube(48,0,-.5f,0,-1,1,1,2),PartPose.offset(0,3,-.5f));}
            }
            return LayerDefinition.create(mesh,64,64);
        }
        @Override public void setupAnim(State s){
            super.setupAnim(s);float age=s.ageInTicks,gait=s.walkAnimationPos*(heavy?.9f:1.9f),speed=Math.min(1,s.walkAnimationSpeed);
            float wind=s.charge>0?1-s.charge/(heavy?40f:32f):0;wind=wind*wind*(3-2*wind);
            float recoil=Math.max(0,(s.recovery-(heavy?49:40))/(heavy?16f:15f));
            for(int i=0;i<4;i++)legs[i].xRot=Mth.cos(gait+(i==0||i==3?0:Mth.PI))*speed*(heavy?.35f:.75f);
            head.yRot=s.yRot*Mth.DEG_TO_RAD*.6f;head.xRot=s.xRot*Mth.DEG_TO_RAD*.4f;
            body.y+=Mth.sin(age*.07f)*(heavy?.15f:.3f)+wind*(heavy?1.8f:2)-recoil;
            core.yRot=age*(s.charge>0?.12f:.02f);core.zRot+=Mth.sin(age*.08f)*.08f;
            if(heavy){left.xRot=-wind*2.1f+recoil*.6f;right.xRot=-wind*2.1f+recoil*.6f;left.zRot-=wind*.25f;right.zRot+=wind*.25f;head.xRot+=wind*.25f;
                left.getChild("forearm").xRot=-wind*.6f+recoil*.3f;right.getChild("forearm").xRot=-wind*.6f+recoil*.3f;
                head.getChild("jaw").y+=wind*.3f;if(!s.awake){head.xRot=.4f;body.y+=2;core.y+=1;}}
            else{left.zRot-=wind*.8f+Mth.sin(age*.14f)*.09f;right.zRot+=wind*.8f+Mth.sin(age*.14f)*.09f;head.z-=wind*1.5f;head.xRot-=wind*.3f;body.xRot-=recoil*.15f;
                for(int side:new int[]{-1,1})head.getChild("mandible"+side).yRot+=side*(wind*.45f+Mth.sin(age*.1f)*.04f);
                body.getChild("tail").yRot=Mth.sin(age*.09f)*.2f+wind*.3f;}
        }
    }
}
