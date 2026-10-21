package fr.axephir.tnd.client;

import fr.axephir.tnd.entities.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class RelicRenderer<T extends Mob> extends MobRenderer<T,RelicRenderState,RelicModel> {
    private static final ModelLayerLocation SCOUT_LAYER=new ModelLayerLocation(id("shardling"),"main");
    private static final ModelLayerLocation SENTINEL_LAYER=new ModelLayerLocation(id("rift_sentinel"),"main");
    private final boolean sentinel;
    public RelicRenderer(EntityRendererProvider.Context context,boolean sentinel) {
        super(context,new RelicModel(context.bakeLayer(sentinel?SENTINEL_LAYER:SCOUT_LAYER),sentinel),sentinel?.5f:.3f);this.sentinel=sentinel;
    }
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(SCOUT_LAYER,()->RelicModel.layer(false));e.registerLayerDefinition(SENTINEL_LAYER,()->RelicModel.layer(true));
    }
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(VestigeEntities.SHARDLING.get(),c->new RelicRenderer<Shardling>(c,false));
        e.registerEntityRenderer(VestigeEntities.SENTINEL.get(),c->new RelicRenderer<RiftSentinel>(c,true));
    }
    @Override public RelicRenderState createRenderState(){return new RelicRenderState();}
    @Override public void extractRenderState(T entity,RelicRenderState state,float partial) {
        super.extractRenderState(entity,state,partial);
        state.charge=entity instanceof RiftSentinel s?s.windup():0;
        state.bonded=entity instanceof Shardling s&&s.isTame();state.sitting=entity instanceof Shardling s&&s.isInSittingPose();
    }
    @Override public Identifier getTextureLocation(RelicRenderState state) {
        return id("textures/entity/"+(sentinel?"rift_sentinel"+(state.charge>0?"_charged":""):"shardling"+(state.bonded?"_bonded":""))+".png");
    }
    @Override protected int getBlockLightLevel(T entity,BlockPos pos){return Math.max(5,super.getBlockLightLevel(entity,pos));}
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("tnd",path);}
}
