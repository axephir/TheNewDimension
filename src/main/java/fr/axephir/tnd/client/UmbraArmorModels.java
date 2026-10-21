package fr.axephir.tnd.client;

import fr.axephir.tnd.core.AwakeningContent;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.*;
import java.util.*;

/** Slot-specific relief geometry follows the native humanoid pose, including crouching and riding. */
final class UmbraArmorModels implements IClientItemExtensions {
    private final Map<EquipmentSlot,HumanoidModel<HumanoidRenderState>> models=new EnumMap<>(EquipmentSlot.class);
    static void register(RegisterClientExtensionsEvent e){e.registerItem(new UmbraArmorModels(),AwakeningContent.UMBRA_HELMET.get(),AwakeningContent.UMBRA_CHESTPLATE.get(),AwakeningContent.UMBRA_LEGGINGS.get(),AwakeningContent.UMBRA_BOOTS.get());}
    @Override public Model getHumanoidArmorModel(ItemStack stack,EquipmentClientInfo.LayerType type,Model original){
        // Preserve native scaling for equipping baby humanoids.
        if(type==EquipmentClientInfo.LayerType.HUMANOID_BABY)return original;
        var equip=stack.get(DataComponents.EQUIPPABLE);if(equip==null)return original;
        return models.computeIfAbsent(equip.slot(),slot->new HumanoidModel<>(layer(slot).bakeRoot()));
    }
    private static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
    private static LayerDefinition layer(EquipmentSlot slot){
        var mesh=HumanoidModel.createArmorMeshSet(new CubeDeformation(.55f),new CubeDeformation(1)).get(slot);
        var r=mesh.getRoot();
        if(slot==EquipmentSlot.HEAD){
            var head=r.getChild("head");
            head.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
            head.addOrReplaceChild("crest",cube(0,32,-1,-11,-3,2,4,7),PartPose.ZERO);
            for(int side:new int[]{-1,1}){
                head.addOrReplaceChild("temple"+side,cube(20,32,-1,-2,-3,2,5,6),PartPose.offsetAndRotation(side*4.5f,-7,0,0,0,side*-.16f));
                head.addOrReplaceChild("cheek"+side,cube(40,32,-1,0,-1,2,4,3),PartPose.offsetAndRotation(side*3.8f,-4,-4,0,0,side*.12f));
            }
        }else if(slot==EquipmentSlot.CHEST){
            var body=r.getChild("body");
            body.addOrReplaceChild("breastplate",cube(0,44,-4,1,-3.5f,8,7,2),PartPose.ZERO);
            body.addOrReplaceChild("seal",cube(48,48,-1.5f,-1.5f,-1,3,3,1),PartPose.offsetAndRotation(0,4,-4,0,0,.785f));
            body.addOrReplaceChild("belt",cube(22,48,-4.7f,9,-3,9.4f,2,6),PartPose.ZERO);
            for(String name:new String[]{"right_arm","left_arm"}){
                int side=name.equals("left_arm")?1:-1;var arm=r.getChild(name);
                arm.addOrReplaceChild("pauldron",cube(0,32,-3,-3,-3,6,4,6),PartPose.offsetAndRotation(side,0,0,0,0,side*-.12f));
                arm.addOrReplaceChild("shoulder_blade",cube(28,32,-.5f,-5,-2,1,6,4),PartPose.offsetAndRotation(side*3.8f,0,0,0,0,side*-.28f));
                arm.addOrReplaceChild("bracer",cube(42,38,-2.5f,5,-2.5f,5,4,5),PartPose.offset(side,0,0));
            }
        }else if(slot==EquipmentSlot.LEGS){
            for(String name:new String[]{"right_leg","left_leg"}){
                int side=name.equals("left_leg")?1:-1;var leg=r.getChild(name);
                leg.addOrReplaceChild("tasset",cube(0,44,-2.5f,0,-3,5,5,1),PartPose.offsetAndRotation(0,0,0,.08f,0,side*.07f));
                leg.addOrReplaceChild("knee",cube(40,32,-2.2f,5,-3,4.4f,3,2),PartPose.ZERO);
            }
        }else if(slot==EquipmentSlot.FEET){
            for(String name:new String[]{"right_leg","left_leg"}){
                var leg=r.getChild(name);
                leg.addOrReplaceChild("shin",cube(40,32,-2.5f,6,-3,5,5,1),PartPose.ZERO);
                leg.addOrReplaceChild("toe",cube(0,54,-2.6f,10,-4.5f,5.2f,3,7),PartPose.ZERO);
            }
        }
        return LayerDefinition.create(mesh,64,64);
    }
}
