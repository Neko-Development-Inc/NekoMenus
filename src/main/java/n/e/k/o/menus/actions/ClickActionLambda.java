package n.e.k.o.menus.actions;

import n.e.k.o.menus.menus.Menu;
import n.e.k.o.menus.menus.MenuItem;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface ClickActionLambda {
    void run(ClickAction action, int slotId, PlayerEntity player, Menu menu, MenuItem item);
}