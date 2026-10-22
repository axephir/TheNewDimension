package fr.axephir.tnd.client;

import fr.axephir.tnd.core.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;

final class InventoryMasteries {
    private static final java.util.Map<net.minecraft.client.gui.screens.Screen,java.util.List<java.lang.ref.WeakReference<Button>>> TABS=new java.util.WeakHashMap<>();
    static void position(ScreenEvent.Render.Pre event){
        var tabs=TABS.get(event.getScreen());if(tabs==null)return;
        var screen=(AbstractContainerScreen<?>)event.getScreen();var player=Minecraft.getInstance().player;
        boolean book=screen instanceof InventoryScreen&&player!=null&&player.getRecipeBook().isOpen(net.minecraft.world.inventory.RecipeBookType.CRAFTING);
        boolean overlay=book&&screen.width<379;
        int x=Math.max(2,screen.getLeftPos()-(book?204:24));
        for(int i=0;i<tabs.size();i++){var tab=tabs.get(i).get();if(tab!=null){tab.setPosition(x,screen.getTopPos()+6+i*25);tab.visible=!overlay;}}
    }
    static void init(ScreenEvent.Init.Post event) {
        if(event.getScreen() instanceof net.minecraft.client.gui.screens.options.OptionsScreen settings){
            var familiar=Button.builder(Component.translatable("familiar.tnd.title"),b->{var mc=Minecraft.getInstance();if(mc.player!=null){FamiliarScreen.pendingParent=settings;mc.gui.setScreen(null);mc.player.connection.sendCommand("tnd familiar");}}).bounds(settings.width-108,5,102,20).build();
            familiar.active=Minecraft.getInstance().player!=null;event.addListener(familiar);return;
        }
        if (!(event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen)) return;
        var screen = (AbstractContainerScreen<?>) event.getScreen();
        // Potion effects own the right edge. Keep our tabs attached to the left.
        int x = screen.getLeftPos()-24, y = screen.getTopPos()+6;
        Button button = new Button(x,y,22,22,Component.translatable("gui.tnd.masteries"), b -> {
            var player = Minecraft.getInstance().player;
            if (player != null) player.connection.sendCommand("tnd masteries");
        }, narration -> narration.get()) {
            @Override protected void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
                StonePanel.frame(g,getX(),getY(),getWidth(),getHeight());
                if(isHoveredOrFocused()) g.fill(getX()+3,getY()+3,getX()+19,getY()+19,0xffd8d3a3);
                g.item(ModContent.LOCATOR_CORE.get().getDefaultInstance(),getX()+3,getY()+3);
            }
        };
        button.setTooltip(Tooltip.create(Component.translatable("gui.tnd.inventory_masteries")));
        event.addListener(button);
        Button expeditions=new Button(x,y+25,22,22,Component.translatable("gui.tnd.expeditions"),b->{var p=Minecraft.getInstance().player;if(p!=null)p.connection.sendCommand("tnd expeditions");},n->n.get()){
            @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){StonePanel.frame(g,getX(),getY(),22,22);if(isHoveredOrFocused())g.fill(getX()+3,getY()+3,getX()+19,getY()+19,0xffd8d3a3);g.item(ModContent.STAR_CHART.get().asItem().getDefaultInstance(),getX()+3,getY()+3);}
        };expeditions.setTooltip(Tooltip.create(Component.translatable("gui.tnd.inventory_expeditions")));event.addListener(expeditions);
        var guide=Button.builder(Component.literal("?"),b->Minecraft.getInstance().gui.setScreen(new FieldGuideScreen(event.getScreen()))).bounds(x,y+50,22,22).tooltip(Tooltip.create(Component.translatable("gui.tnd.inventory_guide"))).build();event.addListener(guide);
        var archives=Button.builder(Component.literal("≡"),b->{var p=Minecraft.getInstance().player;if(p!=null)p.connection.sendCommand("tnd archives");}).bounds(x,y+75,22,22).tooltip(Tooltip.create(Component.translatable("gui.tnd.archives"))).build();event.addListener(archives);
        var familiar=Button.builder(Component.literal("◇"),b->{var p=Minecraft.getInstance().player;if(p!=null)p.connection.sendCommand("tnd familiar");}).bounds(x,y+100,22,22).tooltip(Tooltip.create(Component.translatable("familiar.tnd.title"))).build();event.addListener(familiar);
        // Click callbacks retain their parent screen; weak values avoid keeping old
        // inventories and worlds alive through the otherwise weak screen keys.
        TABS.put(event.getScreen(),java.util.List.of(new java.lang.ref.WeakReference<>(button),new java.lang.ref.WeakReference<>(expeditions),new java.lang.ref.WeakReference<>(guide),new java.lang.ref.WeakReference<>(archives),new java.lang.ref.WeakReference<>(familiar)));
    }
    private InventoryMasteries() {}
}
