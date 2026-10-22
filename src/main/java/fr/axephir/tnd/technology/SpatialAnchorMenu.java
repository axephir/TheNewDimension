package fr.axephir.tnd.technology;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class SpatialAnchorMenu extends AbstractContainerMenu {
    private final SpatialAnchorEntity anchor;private final ContainerData data;
    public SpatialAnchorMenu(int id,Inventory inv){this(id,inv,null,new SimpleContainerData(5));}
    public SpatialAnchorMenu(int id,Inventory inv,SpatialAnchorEntity be,ContainerData data){super(SpatialAnchors.MENU.get(),id);anchor=be;this.data=data;addDataSlots(data);}
    public int energy(){return data.get(0)&65535|data.get(1)<<16;}public int status(){return data.get(2);}public boolean enabled(){return data.get(3)>0;}public int chunks(){return data.get(4);}
    @Override public boolean stillValid(Player p){return anchor==null||anchor.valid(p);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int id){return id==0&&p instanceof ServerPlayer sp&&anchor!=null&&stillValid(p)&&anchor.toggle(sp);}
}
