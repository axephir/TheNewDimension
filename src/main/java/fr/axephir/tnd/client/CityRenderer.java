package fr.axephir.tnd.client;
import fr.axephir.tnd.city.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Three original articulated 64px models: armour plates, surgical rotors and a segmented cloak. */
public final class CityRenderer extends MobRenderer<CityMob,CityRenderer.State,CityRenderer.Model> {
    private final CityMob.Kind kind;
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    private static ModelLayerLocation layer(CityMob.Kind k){return new ModelLayerLocation(id(k.id),"main");}
    public CityRenderer(EntityRendererProvider.Context c,CityMob.Kind k){super(c,new Model(c.bakeLayer(layer(k)),k),k==CityMob.Kind.DRONE?.5f:.65f);kind=k;}
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){for(var k:CityMob.Kind.values())e.registerLayerDefinition(layer(k),()->Model.layer(k));}
    public static void renderers(EntityRenderersEvent.RegisterRenderers e){CityMobs.TYPES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new CityRenderer(c,k)));}
    public static final class State extends LivingEntityRenderState {int charge,recovery;}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(CityMob e,State s,float partial){super.extractRenderState(e,s,partial);s.charge=e.charge();s.recovery=e.recovery();}
    @Override public Identifier getTextureLocation(State s){return id("textures/entity/"+kind.id+(s.charge>0?"_charged":"")+".png");}
    @Override protected int getBlockLightLevel(CityMob e,BlockPos p){return Math.max(e.charge()>0?13:6,super.getBlockLightLevel(e,p));}
    public static final class Model extends EntityModel<State> {
        final CityMob.Kind kind;final ModelPart torso,head;final ModelPart[] arms=new ModelPart[2],legs=new ModelPart[2],rotors=new ModelPart[4];
        Model(ModelPart root,CityMob.Kind k){super(root);kind=k;torso=root.getChild("torso");head=torso.getChild("head");for(int i=0;i<2;i++){arms[i]=torso.getChild("arm"+i);legs[i]=root.getChild("leg"+i);}if(k==CityMob.Kind.DRONE)for(int i=0;i<4;i++)rotors[i]=torso.getChild("rotor"+i);}
        private static CubeListBuilder box(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
        static LayerDefinition layer(CityMob.Kind k){
            var mesh=new MeshDefinition();var root=mesh.getRoot();boolean heavy=(k==CityMob.Kind.ENFORCER||k==CityMob.Kind.MARSHAL),drone=k==CityMob.Kind.DRONE;
            var body=root.addOrReplaceChild("torso",box(0,0,-5,-7,-3,10,12,6),PartPose.offset(0,drone?13:heavy?-3:0,0));
            if(drone){
                body=root.addOrReplaceChild("torso",box(0,0,-5,-2,-5,10,4,10),PartPose.offset(0,14,0));
                body.addOrReplaceChild("head",box(0,16,-3,-1,-2,6,3,4),PartPose.offset(0,3,-3));
                body.getChild("head").addOrReplaceChild("lens",box(48,0,-1.5f,-1,-1,3,2,2),PartPose.offset(0,0,-2));
                for(int i=0;i<4;i++){
                    int sx=i%2==0?-1:1,sz=i<2?-1:1;
                    body.addOrReplaceChild("strut"+i,box(32,0,-1,-1,-1,2,2,8),PartPose.offsetAndRotation(sx*4,0,sz*2,0,sx*sz*.75f,0));
                    var rotor=body.addOrReplaceChild("rotor"+i,box(32,32,-4,-.5f,-4,8,1,8),PartPose.offset(sx*9,-1,sz*8));
                    rotor.addOrReplaceChild("hub",box(48,0,-1,-1,-1,2,2,2),PartPose.ZERO);
                    rotor.addOrReplaceChild("blade",box(0,32,-6,-.2f,-.6f,12,.4f,1.2f),PartPose.offset(0,-1,0));
                }
                for(int i=0;i<2;i++){
                    var arm=body.addOrReplaceChild("arm"+i,box(32,0,-.7f,0,-.7f,1.4f,5,1.4f),PartPose.offsetAndRotation(i==0?-4:4,1,0,0,0,i==0?.5f:-.5f));
                    arm.addOrReplaceChild("tool",box(48,16,-1,0,-1,2,4,2),PartPose.offsetAndRotation(0,4,0,-.6f,0,0));
                    root.addOrReplaceChild("leg"+i,box(32,0,-.5f,0,-.5f,1,5,1),PartPose.offsetAndRotation(i==0?-5:5,17,3,.35f,0,i==0?.25f:-.25f));
                }
                body.addOrReplaceChild("antenna",box(48,0,-.3f,-6,-.3f,.6f,6,.6f),PartPose.offset(0,-2,2));
            }else{
                var head=body.addOrReplaceChild("head",box(0,16,-3,-4,-3,6,7,6),PartPose.offset(0,-11,0));
                head.addOrReplaceChild("visor",box(48,0,-3.2f,-1.5f,-3.4f,6.4f,1.4f,.7f),PartPose.ZERO);
                head.addOrReplaceChild("mask",box(32,32,-2,0,-3.5f,4,2.5f,1),PartPose.ZERO);
                head.addOrReplaceChild("antenna",box(48,16,-.3f,-4,-.3f,.6f,5,.6f),PartPose.offset(heavy?3:-3,-2,1));
                body.addOrReplaceChild("breastplate",box(0,32,-4.5f,-6.5f,-3.7f,9,7,1),PartPose.ZERO);
                body.addOrReplaceChild("reactor",box(48,0,-1.5f,-1.5f,-1,3,3,2),PartPose.offsetAndRotation(0,-3,-3.5f,0,0,.785f));
                body.addOrReplaceChild("backpack",box(0,0,-3,-4,2,6,7,4),PartPose.ZERO);
                body.addOrReplaceChild("belt",box(32,32,-5.5f,3,-3.5f,11,2,7),PartPose.ZERO);
                for(int i=0;i<2;i++){
                    int side=i==0?-1:1;
                    var arm=body.addOrReplaceChild("arm"+i,box(32,0,-2,-1,-2,4,9,4),PartPose.offset(side*(heavy?8:7),-6,0));
                    arm.addOrReplaceChild("shoulder",box(0,32,-3,-2,-3,6,4,6),PartPose.ZERO);
                    var fore=arm.addOrReplaceChild("forearm",box(32,0,-2.1f,0,-2.1f,4.2f,7,4.2f),PartPose.offsetAndRotation(0,7,0,-.15f,0,0));
                    fore.addOrReplaceChild("knuckles",box(0,32,-2,5,-2.5f,4,3,4),PartPose.ZERO);
                    if(heavy&&i==0)fore.addOrReplaceChild("shield",box(0,0,-1,0,-5,2,12,9),PartPose.offset(-3,-1,1));
                    if(heavy&&i==1){var baton=fore.addOrReplaceChild("baton",box(32,0,-.7f,-2,-.7f,1.4f,17,1.4f),PartPose.offsetAndRotation(0,6,-3,1.1f,0,0));baton.addOrReplaceChild("coil",box(48,0,-1,-1,-1,2,9,2),PartPose.offset(0,4,0));}
                    if(!heavy)fore.addOrReplaceChild("blade",box(48,0,-.4f,-3,-1,.8f,17,2),PartPose.offsetAndRotation(side*2,1,-2,-.18f,0,side*.15f));
                    var leg=root.addOrReplaceChild("leg"+i,box(32,0,-2,0,-2,4,9,4),PartPose.offset(side*3,heavy?2:5,0));
                    var shin=leg.addOrReplaceChild("shin",box(32,0,-1.8f,0,-1.8f,3.6f,10,3.6f),PartPose.offset(0,8,0));
                    shin.addOrReplaceChild("knee",box(0,32,-2.2f,-1,-2.5f,4.4f,3,1),PartPose.ZERO);
                    shin.addOrReplaceChild("boot",box(0,32,-2.4f,8,-3.4f,4.8f,3,6),PartPose.ZERO);
                    if(!heavy){var cape=body.addOrReplaceChild("cape"+i,box(0,0,-3,0,0,6,10,1),PartPose.offsetAndRotation(side*3,-5,4,.12f,side*.1f,0));cape.addOrReplaceChild("tail",box(0,32,-3,0,0,6,11,1),PartPose.offsetAndRotation(0,9,0,.12f,0,side*.08f));}
                }
            }
            if(k==CityMob.Kind.MARSHAL)for(int i=-1;i<=1;i++)body.getChild("head").addOrReplaceChild("crown"+i,box(48,0,-.7f,-4,-.7f,1.4f,5,1.4f),PartPose.offset(i*2.6f,-4,0));
            if(k==CityMob.Kind.SURGEON){var h=body.getChild("head");h.addOrReplaceChild("halo_top",box(48,0,-6,-1,-1,12,1,1),PartPose.offset(0,-7,2));for(int side:new int[]{-1,1})h.addOrReplaceChild("halo"+side,box(48,0,-.5f,-3,-.5f,1,7,1),PartPose.offset(side*6,-3,2));}
            if(k==CityMob.Kind.PROWLER)for(int side:new int[]{-1,1})body.getChild("head").addOrReplaceChild("fin"+side,box(48,0,-.5f,-2,0,1,4,7),PartPose.offsetAndRotation(side*3,-1,2,.2f,side*.2f,0));
            return LayerDefinition.create(mesh,64,64);
        }
        @Override public void setupAnim(State s){super.setupAnim(s);float age=AtmosphereOptions.ANIMATIONS.get()?s.ageInTicks:0;float wind=s.charge>0?1-s.charge/(float)kind.windup:0;wind=wind*wind*(3-2*wind);float strikeTicks=kind==CityMob.Kind.DRONE||kind==CityMob.Kind.SURGEON?13f:10f;float strike=Math.clamp((s.recovery-(kind.recoveryTicks()-strikeTicks))/strikeTicks,0,1);
            head.yRot=s.yRot*Mth.DEG_TO_RAD*.65f;head.xRot=s.xRot*Mth.DEG_TO_RAD*.5f;
            if(kind==CityMob.Kind.DRONE){torso.y+=Mth.sin(age*.12f)*.6f;torso.zRot=Mth.sin(age*.08f)*.06f;for(int i=0;i<4;i++)rotors[i].yRot=age*(i%2==0?.9f:-.9f);for(int i=0;i<2;i++)arms[i].xRot=-wind*.85f+Mth.sin(age*.09f+i)*.1f;head.xRot-=wind*.3f;torso.xRot+=strike*.12f;}
            else {float gait=s.walkAnimationPos*((kind==CityMob.Kind.ENFORCER||kind==CityMob.Kind.MARSHAL)?.7f:1.2f),speed=Math.min(1,s.walkAnimationSpeed);
                for(int i=0;i<2;i++){legs[i].xRot=Mth.cos(gait+i*Mth.PI)*speed*.7f;arms[i].xRot=Mth.cos(gait+i*Mth.PI+Mth.PI)*speed*.45f-wind*((kind==CityMob.Kind.ENFORCER||kind==CityMob.Kind.MARSHAL)?1.7f:1.1f)+strike*.9f;arms[i].zRot=(i==0?-1:1)*wind*.3f;}
                torso.xRot=wind*.18f+strike*.28f;torso.y+=Mth.sin(age*.075f)*.16f;head.xRot+=wind*.15f-strike*.1f;
                if((kind==CityMob.Kind.REAVER||kind==CityMob.Kind.PROWLER))for(int i=0;i<2;i++){var cape=torso.getChild("cape"+i);cape.xRot+=Mth.sin(age*.09f+i)*.07f+speed*.35f+wind*.15f+strike*.35f;cape.getChild("tail").xRot+=Mth.sin(age*.13f+i)*.12f;}
            }
        }
    }
}
