package fr.axephir.tnd.client;

import fr.axephir.tnd.familiar.*;
import fr.axephir.tnd.entities.Shardling;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact, fully paginated dossier. Real server state, live articulated portrait and explicit locked skills. */
public final class FamiliarScreen extends AbstractContainerScreen<FamiliarMenu> {
    static Screen pendingParent;private final Screen parent;private int page;private EditBox name;private Button rename,order,pack,heal,recover;
    private final PanelMotion motion=new PanelMotion();
    public FamiliarScreen(FamiliarMenu menu,Inventory inv,Component title){super(menu,inv,title,330,230);parent=pendingParent;pendingParent=null;}
    @Override protected void init(){super.init();
        for(int i=0;i<3;i++){final int p=i;addRenderableWidget(Button.builder(Component.translatable("familiar.tnd.tab."+i),b->page=p).bounds(leftPos+8+i*106,topPos+8,102,20).build());}
        name=new EditBox(font,leftPos+12,topPos+38,209,18,Component.translatable("familiar.tnd.rename"));name.setMaxLength(24);name.setValue(title.getString());addRenderableWidget(name);
        rename=Button.builder(Component.translatable("familiar.tnd.rename"),b->saveName()).bounds(leftPos+226,topPos+37,92,20).build();addRenderableWidget(rename);
        order=Button.builder(Component.empty(),b->action(0)).bounds(leftPos+10,topPos+194,99,20).build();pack=Button.builder(Component.translatable("familiar.tnd.pack"),b->action(1)).bounds(leftPos+115,topPos+194,99,20).build();heal=Button.builder(Component.translatable("familiar.tnd.heal"),b->action(3)).bounds(leftPos+220,topPos+194,99,20).build();recover=Button.builder(Component.translatable("familiar.tnd.recover_capsule"),b->action(2)).bounds(leftPos+115,topPos+194,204,20).build();
        addRenderableWidget(order);addRenderableWidget(pack);addRenderableWidget(heal);addRenderableWidget(recover);
    }
    private void saveName(){minecraft.player.connection.send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new FamiliarNetworking.Rename(menu.containerId,name.getValue())));name.setFocused(false);}
    private void action(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void containerTick(){super.containerTick();boolean bonded=menu.value(0)>0,stored=menu.value(5)==4,near=menu.value(17)>0;name.visible=rename.visible=bonded&&page==0;rename.active=!name.getValue().isBlank();order.visible=pack.visible=heal.visible=bonded&&!stored;recover.visible=bonded&&stored;
        order.active=menu.value(5)==1||menu.value(5)==2;order.setMessage(Component.translatable(menu.value(5)==2?"familiar.tnd.follow":"familiar.tnd.wait"));pack.active=near&&menu.value(5)!=5;heal.active=near&&menu.value(16)>0&&menu.value(3)<menu.value(4);
        pack.setTooltip(Tooltip.create(Component.translatable("familiar.tnd.pack_tip")));heal.setTooltip(Tooltip.create(Component.translatable("familiar.tnd.heal_tip",menu.value(16))));
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
    private void text(GuiGraphicsExtractor g,Component text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
    private void lines(GuiGraphicsExtractor g,String key,int x,int y,int width){var lines=font.split(Component.translatable(key),width);for(int i=0;i<lines.size();i++)g.text(font,lines.get(i),leftPos+x,topPos+y+i*11,0xffbfcdd4,false);}
    private void bar(GuiGraphicsExtractor g,int x,int y,int width,int value,int color){g.fill(leftPos+x,topPos+y,leftPos+x+width,topPos+y+5,0xff253842);g.fill(leftPos+x,topPos+y,leftPos+x+width*Math.clamp(value,0,1000)/1000,topPos+y+5,color);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        int x=leftPos,y=topPos;g.fill(x-1,y-1,x+331,y+231,0xff537a83);g.fill(x,y,x+330,y+230,0xff101d27);g.fill(x+7,y+31,x+323,y+32,0xff36535f);
        if(menu.value(0)==0){text(g,Component.translatable("familiar.tnd.no_bond"),12,43,0xffa8e3e5);lines(g,"familiar.tnd.guide.0",12,63,302);var hint=font.split(Component.translatable("familiar.tnd.city_hint",menu.x(),menu.z()),302);for(int i=0;i<hint.size();i++)g.text(font,hint.get(i),x+12,y+175+i*11,0xffd4bc8b,false);return;}
        int level=menu.value(1),stage=menu.value(6);
        if(page==0){
            g.fill(x+10,y+65,x+103,y+164,0xff1c303c);g.fill(x+11,y+66,x+102,y+163,0xff14242f);
            if(minecraft.level!=null&&minecraft.level.getEntity(menu.entityId()) instanceof Shardling pet)InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+12,y+67,x+101,y+160,46,.10f,mx,my,pet);
            else g.item(FamiliarContent.CAPSULE.get().getDefaultInstance(),x+49,y+106);
            text(g,Component.translatable("familiar.tnd.stage."+stage),13,170,0xff94cdd0);
            text(g,Component.translatable("familiar.tnd.level",level),115,66,0xffe6edee);text(g,Component.translatable("familiar.tnd.state."+menu.value(5)),115,80,menu.value(5)==3?0xffffb38f:0xff9bc5c8);
            text(g,Component.translatable("familiar.tnd.health",menu.value(3)/10f,menu.value(4)/10f),115,99,0xffd3e5dc);bar(g,115,111,201,motion.value(0,menu.value(3)*1000/Math.max(1,menu.value(4))),0xff76cfab);
            int req=Shardling.requiredXp(level);text(g,level>=30?Component.translatable("familiar.tnd.max_level"):Component.translatable("familiar.tnd.xp",menu.value(2),req),115,124,0xffd6dcf0);bar(g,115,136,201,motion.value(1,level>=30?1000:menu.value(2)*1000/Math.max(1,req)),0xff8eabef);
            text(g,Component.translatable("familiar.tnd.damage",menu.value(7)/10f,menu.value(8)),115,150,0xffc7d0d7);var position=font.split(Component.translatable("familiar.tnd.position",menu.x(),menu.z(),Component.translatable("familiar.tnd.dimension."+menu.value(15))),201);for(int i=0;i<Math.min(2,position.size());i++)g.text(font,position.get(i),x+115,y+168+i*10,0xff9aacb8,false);
        }else if(page==1){
            text(g,Component.translatable("familiar.tnd.evolution_header",level),12,40,0xffd9ecee);text(g,Component.translatable("familiar.tnd.evolution_path"),12,55,0xffb2c3ce);
            int[] unlocks={3,8,12,20};for(int i=0;i<4;i++){int yy=76+i*27,lv=unlocks[i];boolean active=level>=lv;g.fill(x+10,y+yy-2,x+320,y+yy+23,active?0xff203d40:0xff1a2b35);text(g,Component.translatable("familiar.tnd.skill."+lv),16,yy,active?0xffa8ebcd:0xffacb5bf);text(g,Component.translatable("familiar.tnd.skill_desc."+lv),16,yy+12,0xffb8c6cf);}
        }else{ text(g,Component.translatable("familiar.tnd.care"),12,41,0xffa8e3e5);lines(g,"familiar.tnd.guide.1",12,61,302);g.item(FamiliarContent.NUTRIENT.get().getDefaultInstance(),x+13,y+159);lines(g,"familiar.tnd.food_recipe",37,159,278);}
        text(g,Component.translatable("familiar.tnd.footer"),12,219,0xff8ca5b2);
    }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event){
        if(event.isEscape()){onClose();return true;}
        if(name!=null&&name.visible&&name.canConsumeInput()){
            if(event.key()==org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER||event.key()==org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER){if(rename.active)saveName();return true;}
            name.keyPressed(event);return true;
        }
        return super.keyPressed(event);
    }
    @Override public void onClose(){super.onClose();if(parent!=null)minecraft.gui.setScreen(parent);}
}
