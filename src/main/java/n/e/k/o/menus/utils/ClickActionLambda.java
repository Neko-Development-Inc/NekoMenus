package n.e.k.o.menus.utils;

import n.e.k.o.menus.Menu;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface ClickActionLambda {
    void run(ClickAction action, int slotId, PlayerEntity player, Menu menu);
}