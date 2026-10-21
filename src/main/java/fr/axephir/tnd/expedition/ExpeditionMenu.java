package fr.axephir.tnd.expedition;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.Expedition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class ExpeditionMenu extends AbstractContainerMenu {
    public ExpeditionMenu(int id,Inventory inv){super(ModContent.EXPEDITION_MENU.get(),id);}
    public ExpeditionMenu(int id,Inventory inv,Player player){this(id,inv);}
    @Override public boolean stillValid(Player p){return p.isAlive();}
    @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int button){return p instanceof ServerPlayer server&&stillValid(p)&&button>=0&&button<Expedition.values().length&&ExpeditionService.claim(server,Expedition.values()[button]);}
}
