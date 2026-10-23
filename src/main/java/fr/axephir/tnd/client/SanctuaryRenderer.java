package fr.axephir.tnd.client;

import fr.axephir.tnd.sanctuary.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Original articulated stone-metal silhouettes, matched to physical entity dimensions. */
public final class SanctuaryRenderer extends MobRenderer<SanctuaryConstruct,SanctuaryRenderer.State,SanctuaryRenderer.Model> {
    private final SanctuaryConstruct.Kind kind;
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath("tnd",s);}
    private static ModelLayerLocation layer(SanctuaryConstruct.Kind k){return new ModelLayerLocation(id(k.id),"main");}
    public SanctuaryRenderer(EntityRendererProvider.Context c,SanctuaryConstruct.Kind k){super(c,new Model(c.bakeLayer(layer(k)),k),k.width*.45f);kind=k;addLayer(new net.minecraft.client.renderer.entity.layers.EyesLayer<State,Model>(this){
        public net.minecraft.client.renderer.rendertype.RenderType renderType(){return net.minecraft.client.renderer.rendertype.RenderTypes.eyes(id("textures/entity/"+k.id+"_glow.png"));}
    });}
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){for(var k:SanctuaryConstruct.Kind.values())e.registerLayerDefinition(layer(k),()->Model.layer(k));}
    public static void renderers(EntityRenderersEvent.RegisterRenderers e){SanctuaryEntities.TYPES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new SanctuaryRenderer(c,k)));}
    public static final class State extends LivingEntityRenderState {int charge,exposed,phase,pattern,awakening;boolean ritual;}
    public State createRenderState(){return new State();}
    public void extractRenderState(SanctuaryConstruct e,State s,float partial){super.extractRenderState(e,s,partial);s.charge=e.charge();s.exposed=e.exposed();s.phase=e.phase();s.pattern=e.pattern();s.awakening=e.awakening();s.ritual=e.ritual();}
    public Identifier getTextureLocation(State s){return id("textures/entity/"+kind.id+(s.exposed>0?"_open":"")+".png");}
    protected int getBlockLightLevel(SanctuaryConstruct e,BlockPos p){return Math.max(e.exposed()>0?15:e.kind().boss()?13:11,super.getBlockLightLevel(e,p));}
    public static final class Model extends EntityModel<State> {
        private final SanctuaryConstruct.Kind kind;private final ModelPart root,torso,core,crown;private final ModelPart[] arms=new ModelPart[4],legs=new ModelPart[2];
        Model(ModelPart root,SanctuaryConstruct.Kind kind){super(root);this.root=root;this.kind=kind;torso=root.getChild("torso");core=torso.getChild("core");crown=torso.getChild("crown");for(int i=0;i<4;i++)arms[i]=torso.getChild("arm"+i);for(int i=0;i<2;i++)legs[i]=root.getChild("leg"+i);}
        private static CubeListBuilder box(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
        static LayerDefinition layer(SanctuaryConstruct.Kind kind){var mesh=new MeshDefinition();var r=mesh.getRoot();
            // The shared skeleton is normalised to 24 model units, then scaled to each registered hitbox.
            r.addOrReplaceChild("torso",box(0,0,-4,-6,-2.5f,8,10,5),PartPose.offset(0,8,0));var t=r.getChild("torso");
            t.addOrReplaceChild("core",box(48,0,-1.5f,-1.5f,-1.5f,3,3,3),PartPose.offsetAndRotation(0,-2,-3,0,0,.785f));
            var head=t.addOrReplaceChild("head",box(0,16,-2.5f,-3,-2,5,5,4),PartPose.offset(0,-8,0));
            head.addOrReplaceChild("visor",box(48,0,-2.8f,-.8f,-2.5f,5.6f,1,1),PartPose.ZERO);
            var crown=t.addOrReplaceChild("crown",CubeListBuilder.create(),PartPose.offset(0,-12,0));
            int teeth=kind==SanctuaryConstruct.Kind.ARCHON?12:kind.ranged()?8:6;
            for(int n=0;n<teeth;n++){double a=n*Math.PI*2/teeth;float radius=kind.ranged()?6:4.5f;
                crown.addOrReplaceChild("tooth"+n,box(n%2==0?48:32,0,-.4f,-2,-.5f,.8f,4,1),PartPose.offsetAndRotation((float)Math.cos(a)*radius,0,(float)Math.sin(a)*radius,.2f,(float)-a,.1f));}
            t.addOrReplaceChild("breastplate",box(0,32,-4.8f,-5.5f,-3,9.6f,6,1),PartPose.ZERO);
            t.addOrReplaceChild("waist",box(0,32,-3.5f,3,-2.8f,7,2,5.6f),PartPose.ZERO);
            t.addOrReplaceChild("belt",box(0,32,-4,2.7f,-3.2f,8,1,6.4f),PartPose.ZERO);
            for(int side:new int[]{-1,1}){
                t.addOrReplaceChild("collar"+side,box(0,32,-2,-.5f,-.5f,4,1,1),PartPose.offsetAndRotation(side*2.7f,-6,-3.2f,0,0,side*.18f));
                for(int n=0;n<3;n++){
                    t.addOrReplaceChild("rib"+side+"_"+n,box(0,32,-1.6f,-.35f,-.4f,3.2f,.7f,.8f),PartPose.offsetAndRotation(side*3.1f,-3+n*1.8f,-3.3f,0,0,side*.22f));
                    t.addOrReplaceChild("inlay"+side+"_"+n,box(48,0,-.7f,-.12f,-.12f,1.4f,.24f,.24f),PartPose.offsetAndRotation(side*3.4f,-3+n*1.8f,-3.8f,0,0,side*.22f));
                }
                t.addOrReplaceChild("core_frame"+side,box(0,32,-.3f,-2.2f,-.3f,.6f,4.4f,.6f),PartPose.offsetAndRotation(side*2,-2,-4,0,0,side*.4f));
            }
            for(int i=0;i<4;i++){int side=i%2==0?-1:1;boolean lower=i>=2;var arm=t.addOrReplaceChild("arm"+i,box(32,0,-1.2f,-1,-1.5f,2.4f,6,3),PartPose.offset(side*(lower?4.5f:6),lower?1:-5,lower?2:0));
                var fore=arm.addOrReplaceChild("fore",box(32,0,-1.5f,0,-1.7f,3,6,3.4f),PartPose.offsetAndRotation(0,4,0,-.12f,0,side*.1f));
                fore.addOrReplaceChild("fingers",box(0,32,-1.5f,4,-2,3,2,3),PartPose.ZERO);
                if(kind==SanctuaryConstruct.Kind.COLOSSUS||kind==SanctuaryConstruct.Kind.SMELTER||kind==SanctuaryConstruct.Kind.FOUNDER||kind==SanctuaryConstruct.Kind.FOUNDRY_WARDEN)fore.addOrReplaceChild("hammer",box(0,0,-2,-1,-3,4,5,6),PartPose.offset(0,5,0));
                if(kind.ranged()||kind==SanctuaryConstruct.Kind.ARCHON)fore.addOrReplaceChild("blade",box(48,0,-.3f,0,-.6f,.6f,10,1.2f),PartPose.offsetAndRotation(side*2,1,0,-.3f,0,0));
                arm.addOrReplaceChild("pauldron",box(0,32,-2.4f,-1.8f,-2.2f,4.8f,2,4.4f),PartPose.ZERO);
                arm.addOrReplaceChild("shoulder_lip",box(0,32,-2.7f,-1.9f,-2.5f,5.4f,.6f,5),PartPose.ZERO);
                fore.addOrReplaceChild("bracer",box(0,32,-1.8f,1,-2,3.6f,1,4),PartPose.ZERO);
                fore.addOrReplaceChild("bracer_inlay",box(48,0,-1,1.2f,-2.2f,2,.25f,.25f),PartPose.ZERO);
                if(kind==SanctuaryConstruct.Kind.ARCHON&&i<2)for(int n=0;n<3;n++)arm.addOrReplaceChild("crest"+n,box(0,32,-.5f,-4.5f,-.6f,1,5,1.2f),PartPose.offsetAndRotation(side*(n*.9f),-1,n-1,0,0,side*(.25f+n*.22f)));
            }
            for(int i=0;i<2;i++){int side=i==0?-1:1;var leg=r.addOrReplaceChild("leg"+i,box(32,0,-1.6f,0,-1.6f,3.2f,7,3.2f),PartPose.offset(side*2.5f,11,0));var shin=leg.addOrReplaceChild("shin",box(32,0,-1.3f,0,-1.4f,2.6f,6,2.8f),PartPose.offset(0,6,0));shin.addOrReplaceChild("foot",box(0,32,-1.8f,4,-2.6f,3.6f,3,4.5f),PartPose.ZERO);}
            if(kind==SanctuaryConstruct.Kind.HORTULAN)for(int side:new int[]{-1,1})for(int n=0;n<4;n++)t.addOrReplaceChild("branch"+side+"_"+n,box(32,32,-.5f,-3,-.5f,1,6,1),PartPose.offsetAndRotation(side*(3+n),-8-n,2,0,0,side*(.3f+n*.1f)));
            if(kind==SanctuaryConstruct.Kind.SMELTER)for(int n=0;n<3;n++)t.addOrReplaceChild("chimney"+n,box(0,0,-.7f,-3,-1,1.4f,6,2),PartPose.offset((n-1)*2,-5,4));
            if(kind==SanctuaryConstruct.Kind.LENSKEEPER||kind==SanctuaryConstruct.Kind.ANTIPHON)for(int side:new int[]{-1,1})t.addOrReplaceChild("lens"+side,box(48,0,-.8f,-.8f,-.8f,1.6f,1.6f,1.6f),PartPose.offsetAndRotation(side*7,-9,0,0,0,.785f));
            if(kind==SanctuaryConstruct.Kind.ARCHON||kind==SanctuaryConstruct.Kind.LENSKEEPER){
                var halo=t.addOrReplaceChild("halo",CubeListBuilder.create(),PartPose.offset(0,-8,3));
                for(int n=0;n<12;n++){double angle=n*Math.PI/6;halo.addOrReplaceChild("segment"+n,box(0,32,-1.7f,-.4f,-.5f,3.4f,.8f,1),PartPose.offsetAndRotation((float)Math.cos(angle)*8,(float)Math.sin(angle)*8,0,0,0,(float)(angle+Math.PI/2)));}
            }
            if(kind==SanctuaryConstruct.Kind.FOUNDER){
                for(int side:new int[]{-1,1}){
                    t.addOrReplaceChild("furnace_shoulder"+side,box(0,0,-2.5f,-3,-2.5f,5,6,5),PartPose.offset(side*6,-5,1));
                    for(int n=0;n<4;n++)t.addOrReplaceChild("radiator"+side+"_"+n,box(48,0,-1.8f,-.25f,-.4f,3.6f,.5f,.8f),PartPose.offset(side*4.5f,-4+n*1.4f,3.6f));
                }
                for(int n=0;n<3;n++)t.addOrReplaceChild("exhaust"+n,box(0,32,-.7f,-5,-.9f,1.4f,8,1.8f),PartPose.offset((n-1)*2.4f,-6,3));
                armsHammer(t);
            }
            if(kind==SanctuaryConstruct.Kind.REGULATOR||kind==SanctuaryConstruct.Kind.FOUNDRY_ARBALIST){
                var cage=t.addOrReplaceChild("regulator_cage",CubeListBuilder.create(),PartPose.offset(0,-3,0));
                for(int n=0;n<12;n++){double a=n*Math.PI/6;cage.addOrReplaceChild("ring"+n,box(n%3==0?48:0,n%3==0?0:32,-1.6f,-.35f,-.6f,3.2f,.7f,1.2f),PartPose.offsetAndRotation((float)Math.cos(a)*8,(float)Math.sin(a)*8,1,0,0,(float)(a+Math.PI/2)));}
            }
            if(kind==SanctuaryConstruct.Kind.FOUNDRY_MENDER){
                for(int n=0;n<3;n++)t.addOrReplaceChild("repair_vessel"+n,box(48,0,-.6f,-2,-.6f,1.2f,4,1.2f),PartPose.offset((n-1)*2,-1,4));
            }
            return LayerDefinition.create(mesh,64,64);
        }
        private static void armsHammer(PartDefinition t){
            var fore=t.getChild("arm0").getChild("fore");
            fore.addOrReplaceChild("founder_handle",box(32,0,-.5f,-4,-.5f,1,12,1),PartPose.offset(0,4,-2));
            fore.addOrReplaceChild("founder_head",box(0,0,-4,-2,-2.5f,8,4,5),PartPose.offset(0,0,-2));
            fore.addOrReplaceChild("founder_edge",box(48,0,-3.75f,-.4f,-2.7f,7.5f,.8f,.3f),PartPose.offset(0,0,-2));
        }
        public void setupAnim(State s){super.setupAnim(s);float age=AtmosphereOptions.ANIMATIONS.get()?s.ageInTicks:0;float scale=kind.height/1.875f;
            root.xScale=scale;root.yScale=scale;root.zScale=scale;root.y=24-24*scale;if(AtmosphereOptions.ANIMATIONS.get()&&s.awakening>0)root.y+=Math.min(1,s.awakening/(s.ritual?160f:80f))*kind.height*13;
            float wind=s.charge>0?1-Math.min(1,s.charge/60f):0,release=s.exposed>35?(s.exposed-35)/45f:0;
            float waking=AtmosphereOptions.ANIMATIONS.get()&&s.ritual?Math.min(1,s.awakening/160f):0;
            torso.xRot=wind*.12f-release*.15f+waking*.55f;torso.y+=Mth.sin(age*.045f)*.12f;
            crown.yRot=age*(s.charge>0?.06f:.01f)*(kind==SanctuaryConstruct.Kind.ANTIPHON?-1:1);crown.y-=wind*1.8f+s.phase*.4f;
            core.yRot=age*(s.exposed>0?.07f:.02f);core.xScale=core.yScale=core.zScale=1+Mth.sin(age*.08f)*.06f+(s.exposed>0?.25f:0);
            for(int i=0;i<4;i++){arms[i].xRot=-wind*(i<2?1.7f:1.1f)+release*.8f+(i>=2?.3f:0)+Mth.cos(s.walkAnimationPos*.6f+i*Mth.PI)*s.walkAnimationSpeed*.3f;arms[i].xRot+=waking*.65f;arms[i].zRot=(i%2==0?-1:1)*(wind*.35f+(i>=2?.5f:0));if(i>=2)arms[i].visible=kind==SanctuaryConstruct.Kind.ARCHON||kind==SanctuaryConstruct.Kind.HORTULAN;}
            if(s.charge>0&&s.pattern==2){arms[0].xRot=-1.5f;arms[1].xRot=-wind*.8f;}
            if(s.charge>0&&s.pattern==3){arms[0].zRot=-wind*1.3f;arms[1].zRot=wind*1.3f;}
            if(kind==SanctuaryConstruct.Kind.REGULATOR||kind==SanctuaryConstruct.Kind.FOUNDRY_ARBALIST)torso.getChild("regulator_cage").zRot=age*(s.charge>0?.035f:.008f);
            if(s.charge>0&&s.pattern==5){arms[0].xRot=-wind*2.5f;torso.xRot=wind*.25f;}
            if(s.charge>0&&s.pattern==6){arms[0].xRot=-1.5f;arms[1].xRot=-1.5f;}
            for(int i=0;i<2;i++)legs[i].xRot=Mth.cos(s.walkAnimationPos*.65f+i*Mth.PI)*Math.min(1,s.walkAnimationSpeed)*.5f;
        }
    }
}
