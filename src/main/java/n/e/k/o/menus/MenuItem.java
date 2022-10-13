package n.e.k.o.menus;


import n.e.k.o.menus.utils.ClickAction;
import n.e.k.o.menus.utils.ClickActionLambda;
import n.e.k.o.menus.utils.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MenuItem {

    private Menu menu;
    private final Logger logger;
    public String item = null;
    public int variant;
    public String name = "";
    public int amount = 1;
    public int slot;
    public boolean clickable;
    public List<String> lore;
    public Map<ClickAction, String> actions;
    public List<ClickAction> clickActions;
    public Map<ClickAction, ClickActionLambda> actionLambdas;

    public static MenuItem builder() {
        return new MenuItem(null, null);
    }

    public static MenuItem builder(Menu menu) {
        return new MenuItem(menu, null);
    }

    public MenuItem() {
        this(null, null);
    }

    public MenuItem(Menu menu) {
        this(menu, null);
    }

    public MenuItem(Menu menu, Logger logger) {
        this.menu = menu;
        this.logger = logger;
        this.lore = new ArrayList<>();
        this.actions = new HashMap<>();
        this.clickActions = new ArrayList<>();
        this.actionLambdas = new HashMap<>();
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

    public MenuItem setSlot(int x, int y) {
        return setSlot(Utils.gridToSlot(x, y));
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

    public MenuItem removeAction(ClickAction key) {
        this.actions.remove(key);
        return this;
    }

    public MenuItem addClickAction(ClickAction key) {
        this.clickActions.add(key);
        return this;
    }

    public MenuItem removeClickAction(ClickAction key) {
        this.clickActions.remove(key);
        return this;
    }

    public MenuItem addClickActionLambda(ClickAction key, ClickActionLambda action) {
        this.actionLambdas.put(key, action);
        return this;
    }

    public MenuItem removeClickActionLambda(ClickAction key) {
        this.actionLambdas.remove(key);
        return this;
    }

    public void setMenu(Menu menu) {
        if (this.menu == null)
            this.menu = menu;
    }

    private static final Map<String, ClickAction> mapActions = new HashMap<>() {{
        put(ClickType.PICKUP.name() + ".0", ClickAction.LEFT);
        put(ClickType.PICKUP.name() + ".1", ClickAction.RIGHT);
        put(ClickType.CLONE.name()  + ".2", ClickAction.MIDDLE);

        put(ClickType.QUICK_MOVE.name() + ".0", ClickAction.SHIFT_LEFT);
        put(ClickType.QUICK_MOVE.name() + ".1", ClickAction.SHIFT_RIGHT);

        put(ClickType.PICKUP_ALL.name() + ".0", ClickAction.DOUBLE_LEFT);
        put(ClickType.THROW.name() + ".0", ClickAction.Q);

        put(ClickType.SWAP.name() + ".0", ClickAction.SLOT_1);
        put(ClickType.SWAP.name() + ".1", ClickAction.SLOT_2);
        put(ClickType.SWAP.name() + ".2", ClickAction.SLOT_3);
        put(ClickType.SWAP.name() + ".3", ClickAction.SLOT_4);
        put(ClickType.SWAP.name() + ".4", ClickAction.SLOT_5);
        put(ClickType.SWAP.name() + ".5", ClickAction.SLOT_6);
        put(ClickType.SWAP.name() + ".6", ClickAction.SLOT_7);
        put(ClickType.SWAP.name() + ".7", ClickAction.SLOT_8);
        put(ClickType.SWAP.name() + ".8", ClickAction.SLOT_9);
    }};

    final void slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
        final ClickAction clickAction = mapActions.get(clickType.name() + "." + dragType);
        if (logger != null)
            logger.info("slotClick in menu '" + menu.id + "' (" + slotId + ", " + dragType + ", " + (clickAction == null ? "<clickAction was null>" : clickAction.name()) + ", " + clickType.name() + ", '" + player.getDisplayName().getString() + "')");
        boolean clickActionLambdasHas = actionLambdas.containsKey(clickAction);
        boolean clickActionsHas = clickActions.contains(clickAction);
        boolean actionsHas = actions.containsKey(clickAction);
        if (!clickActionLambdasHas && !clickActionsHas && !actionsHas) {
            if (logger != null)
                logger.info("key '" + (clickAction == null ? "<clickAction was null>" : clickAction.name()) + "' not in action");
            return;
        }
        if (clickActionLambdasHas)
            this.actionLambdas.get(clickAction).run(clickAction, slotId, player, menu);
        if (clickActionsHas)
            this.onSlotClick(clickAction, slotId, dragType, clickType, player, menu);
        if (actionsHas)
            this.onSlotClick(actions.get(clickAction), slotId, dragType, clickType, player, menu);
    }

    public void onSlotClick(ClickAction action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
        if (logger != null)
            logger.info("Action: " + action.name() + ", Player: " + player.getDisplayName().getString());
    }

    public void onSlotClick(String action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
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
