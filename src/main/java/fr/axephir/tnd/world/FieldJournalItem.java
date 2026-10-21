package fr.axephir.tnd.world;

import fr.axephir.tnd.progression.MasteryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class FieldJournalItem extends Item {
    public FieldJournalItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.openMenu(new SimpleMenuProvider(MasteryMenu::new, Component.translatable("gui.tnd.masteries")));
        return InteractionResult.SUCCESS;
    }
}
