package n.e.k.o.menus;


import n.e.k.o.menus.utils.ClickAction;
import n.e.k.o.menus.utils.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MenuItem {

    private final Logger logger;
    public String item = null;
    public int variant;
    public String name = "";
    public int amount = 1;
    public int slot;
    public boolean clickable;
    public List<String> lore;
    public Map<ClickAction, String> actions;

    public static MenuItem builder() {
        return new MenuItem(null);
    }

    public MenuItem() {
        this(null);
    }

    public MenuItem(Logger logger) {
        this.logger = logger;
        this.lore = new ArrayList<>();
        this.actions = new HashMap<>();
    }

    public MenuItem setItem(String item) {
        this.item = item;
        return this;
    }

    public MenuItem setVariant(int variant) {
        this.variant = variant;
        return this;
    }

    public MenuItem setName(String name) {
        this.name = name;
        return this;
    }

    public MenuItem setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public MenuItem setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public MenuItem setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public MenuItem addLore(String lore) {
        this.lore.add(lore);
        return this;
    }

    public MenuItem setLore(String lore) {
        this.lore = new ArrayList<>(Collections.singletonList(lore));
        return this;
    }

    public MenuItem addLore(List<String> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public MenuItem setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
        return this;
    }

    public MenuItem setAction(ClickAction key, String action) {
        this.actions.put(key, action);
        return this;
    }

    private static final Map<String, ClickAction> mapActions = new HashMap<>() {{
        put(ClickType.PICKUP.name() + ".0", ClickAction.LEFT);
        put(ClickType.PICKUP.name() + ".1", ClickAction.RIGHT);
        put(ClickType.CLONE.name()  + ".2", ClickAction.MIDDLE);

        put(ClickType.QUICK_MOVE.name() + ".0", ClickAction.SHIFT_LEFT);
        put(ClickType.QUICK_MOVE.name() + ".1", ClickAction.SHIFT_RIGHT);
    }};

    final void slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
        String key = clickType.name();
        ClickAction clickAction = ClickAction.get(key);
        clickAction = mapActions.getOrDefault(key + "." + dragType, clickAction);
        if (logger != null)
            logger.info("slotClick in menu '" + menu.id + "' (" + slotId + ", " + dragType + ", " + clickType.name() + ", '" + player.getDisplayName().getString() + "')");
        if (!actions.containsKey(clickAction)) {
            if (logger != null)
                logger.info("key '" + clickAction.name() + "' not in action");
            return;
        }
        String action = actions.get(clickAction);
        this.slotClickOverridable(action, slotId, dragType, clickType, player, menu);
    }

    public void slotClickOverridable(String action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
        if (logger != null)
            logger.info("Action: " + action + ", Player: " + player.getDisplayName().getString());
    }

    public String print() {
        int[] xy = Utils.slotToGrid(slot);
        return "MenuItem { Item = '" + item + "', Name = '" + name + "', Slot = " + slot + " (" + xy[0] + ", " + xy[1] + "), Amount = " + amount + " }";
    }

    @Override
    public String toString() {
        return print();
    }

}
