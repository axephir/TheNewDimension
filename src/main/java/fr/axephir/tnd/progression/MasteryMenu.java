package fr.axephir.tnd.progression;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Vanilla menu button packets carry only an index; the server owns points and prerequisites. */
public final class MasteryMenu extends AbstractContainerMenu {
    public MasteryMenu(int containerId, Inventory inventory) { super(ModContent.MASTERY_MENU.get(), containerId); }
    public MasteryMenu(int containerId, Inventory inventory, Player player) { this(containerId, inventory); }
    @Override public boolean stillValid(Player player) { return player.isAlive(); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer server)||!stillValid(player))return false;
        if(button>=1000&&button<1000+Mastery.values().length) {
            OreSense.scan(server,Mastery.values()[button-1000]);return true;
        }
        if(button<0||button>=Mastery.values().length*Talent.values().length)return false;
        Mastery mastery=Mastery.values()[button/Talent.values().length];
        Talent talent = Talent.values()[button % Talent.values().length];
        Profile profile = player.getData(PlayerProgression.PROFILE);
        try {
            player.setData(PlayerProgression.PROFILE, profile.with(mastery, profile.get(mastery).learn(talent)));
            return true;
        } catch (IllegalStateException unavailable) { return false; }
    }
}
