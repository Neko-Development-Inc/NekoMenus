package n.e.k.o.menus.menus;

import n.e.k.o.menus.NekoMenus;
import n.e.k.o.menus.actions.ClickAction;
import n.e.k.o.menus.actions.ClickActionLambda;
import n.e.k.o.menus.utils.StringColorUtils;
import n.e.k.o.menus.utils.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MenuItem {

    public Menu menu;
    private final Logger logger;
    public String itemStr = null;
    public Item item = null;
    public int variant;
    public String name = null;
    public int amount = 1;
    public int minAmount = 1;
    public int maxAmount = 64;
    public int slot;
    public boolean clickable;
    public boolean bypassMaxAmount;
    public boolean hiddenName;
    public boolean hiddenNameIfNull;
    public List<String> lore;
    public ItemStack itemStack = null;
    public CompoundNBT nbt = null;
    private final Map<ClickAction, String> actions;
    private final List<ClickAction> clickActions;
    private final Map<ClickAction, ClickActionLambda> actionLambdas;
    private final List<String> addToReferencedList;

    public static MenuItem builder() {
        return new MenuItem(null, null);
    }

    public static MenuItem hiddenName() {
        return new MenuItem(null, null).setHiddenName();
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
        this.addToReferencedList = new ArrayList<>();
    }

    public MenuItem setItem(Item item) {
        var regName = item.getRegistryName();
        this.itemStr = regName == null ? null : regName.toString();
        this.item = item;
        if (this.itemStack != null) {
            this.itemStack = new ItemStack(this.item, this.amount);
        }
        return this;
    }

    public MenuItem setItem(String itemStr) {
        this.itemStr = itemStr;
        if (this.item != null) {
            this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemStr));
            if (this.itemStack != null) {
                this.itemStack = new ItemStack(this.item, this.amount);
            }
        }
        return this;
    }

    public MenuItem setVariant(int variant) {
        this.variant = variant;
        return this;
    }

    public MenuItem setName(String name) {
        this.name = name;
        if (this.itemStack != null) {
            this.itemStack.setDisplayName(StringColorUtils.getColoredString(name));
        }
        return this;
    }

    public MenuItem setAmount(int amount) {
        int newAmount = amount;
        if (newAmount < minAmount)
            newAmount = minAmount;
        else if (newAmount > maxAmount)
            newAmount = maxAmount;
        if (itemStack != null) {
            if (!bypassMaxAmount)
                newAmount = Math.min(newAmount, Math.min(maxAmount, itemStack.getMaxStackSize()));
            itemStack.setCount(newAmount);
        }
        this.amount = newAmount;
        return this;
    }

    public MenuItem incrementAmount() {
        return incrementAmount(1);
    }

    public MenuItem incrementAmount(int num) {
        return setAmount(getAmount() + num);
    }

    public MenuItem decrementAmount() {
        return decrementAmount(1);
    }

    public MenuItem decrementAmount(int num) {
        return setAmount(getAmount() - num);
    }

    public int getAmount() {
        return itemStack == null ? this.amount : itemStack.getCount();
    }

    public MenuItem setMinAmount(int minAmount) {
        if (minAmount < 1)
            minAmount = 1;
        this.minAmount = minAmount;
        return this;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public MenuItem setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
        if (itemStack != null && maxAmount > itemStack.getMaxStackSize())
            bypassMaxAmount = true;
        return this;
    }

    public int getMaxAmount() {
        return maxAmount;
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

    public MenuItem enableClicking() {
        this.clickable = true;
        return this;
    }

    public MenuItem disableClicking() {
        this.clickable = false;
        return this;
    }

    public MenuItem setBypassMaxAmount(boolean bypassMaxAmount) {
        this.bypassMaxAmount = bypassMaxAmount;
        return this;
    }

    public MenuItem setHiddenName() {
        this.hiddenName = true;
        return this;
    }

    public MenuItem setHiddenName(boolean hiddenName) {
        this.hiddenName = hiddenName;
        return this;
    }

    public MenuItem setHiddenNameIfNull() {
        this.hiddenNameIfNull = true;
        return this;
    }

    public MenuItem setHiddenNameIfNull(boolean hiddenNameIfNull) {
        this.hiddenNameIfNull = hiddenNameIfNull;
        return this;
    }

    public MenuItem addLore(String lore) {
        this.lore.add(lore);
        updateLore();
        return this;
    }

    public MenuItem setLore(String lore) {
        this.lore = new ArrayList<>(Collections.singletonList(lore));
        updateLore();
        return this;
    }

    public MenuItem addLore(List<String> lore) {
        this.lore.addAll(lore);
        updateLore();
        return this;
    }

    public MenuItem setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
        updateLore();
        return this;
    }

    public void updateLore() {
        if (this.itemStack != null) {
            CompoundNBT display = this.itemStack.getOrCreateChildTag("display");
            ListNBT loreNbt = new ListNBT();
            for (String str : this.lore) {
                IFormattableTextComponent colored = StringColorUtils.getColoredString(str);
                String json = ITextComponent.Serializer.toJson(colored);
                loreNbt.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreNbt);
        }
    }

    public MenuItem setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getMaxStackSize() {
        if (this.itemStack != null) {
            return this.itemStack.getMaxStackSize();
        } else {
            return 64;
        }
    }

    public MenuItem setUnique() {
        var uuid = UUID.randomUUID();
        if (this.nbt == null) {
            this.nbt = new CompoundNBT();
        }
        this.nbt.putUniqueId("uuid", uuid);
        if (this.itemStack != null) {
            this.itemStack.setTagInfo("unique", this.nbt.get("uuid"));
        }
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

    public MenuItem setAsReferencedItem(String key) {
        if (this.menu == null)
            this.addToReferencedList.add(key);
        else
            this.menu.setReferencedItem(key, this);
        return this;
    }

    public boolean isReferencedItem(String key) {
        if (this.addToReferencedList.contains(key))
            return true;
        if (this.menu != null)
            return this.menu.hasReferencedItem(key) && this.menu.getReferencedItem(key) == this;
        return false;
    }

    public MenuItem removeAsReferencedItem() {
        this.menu.removeReferencedItemByValue(this);
        return this;
    }

    public void setMenu(Menu menu) {
        if (this.menu == null)
            this.menu = menu;
    }

    public void setReferencedItems(Menu menu) {
        if (!addToReferencedList.isEmpty()) {
            addToReferencedList.forEach(key -> {
                if (!menu.hasReferencedItem(key))
                    menu.setReferencedItem(key, this);
            });
        }
    }

    public void deleteItem() {
        deleteItem(false);
    }

    public void deleteItem(boolean fullyRemove) {
        if (menu != null) {
            if (itemStack != null) {
                int tmp = minAmount;
                minAmount = 0;
                setAmount(0);
                minAmount = tmp;
            }
            if (fullyRemove) {
                minAmount = 0;
                menu.removeItem(this);
                menu.removeReferencedItemByValue(this);
            }
        }
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

    private static final Map<ClickAction, String> reverseMapActions = new HashMap<>();
    static {
        mapActions.forEach((key, value) -> reverseMapActions.put(value, key));
    }

    public final void slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
        try {
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
                this.actionLambdas.get(clickAction).run(clickAction, slotId, player, menu, this);
            if (clickActionsHas)
                this.onSlotClick(clickAction, slotId, dragType, clickType, player, menu, this);
            if (actionsHas)
                this.onSlotClick(actions.get(clickAction), slotId, dragType, clickType, player, menu, this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onSlotClick(ClickAction action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu, MenuItem item) {
        if (logger != null)
            logger.info("Action: " + action.name() + ", Player: " + player.getDisplayName().getString());
    }

    public void onSlotClick(String action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu, MenuItem item) {
        if (logger != null)
            logger.info("Action: " + action + ", Player: " + player.getDisplayName().getString());
    }

    public boolean performClick(ClickAction action, PlayerEntity player) {
        try {
            String[] split = reverseMapActions.get(action).split("\\.");
            ClickType clickType = ClickType.valueOf(split[0]);
            int dragType = Integer.parseInt(split[1]);
            this.slotClick(slot, dragType, clickType, player, menu);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public String print() {
        int[] xy = Utils.slotToGrid(slot);
        return "MenuItem { Item = '" + itemStr + "', Name = '" + name + "', Slot = " + slot + " (" + xy[0] + ", " + xy[1] + "), Amount = " + amount + (itemStack == null ? "" : (", MaxAmount = " + itemStack.getMaxStackSize())) + " }";
    }

    @Override
    public String toString() {
        return print();
    }

    public MenuItem clone(Menu newOwner) {
        MenuItem clone = new MenuItem(newOwner, logger);

        clone.itemStr = itemStr;
        clone.item = item;
        clone.variant = variant;
        clone.name = name;
        clone.amount = amount;
        clone.minAmount = minAmount;
        clone.maxAmount = maxAmount;
        clone.slot = slot;
        clone.clickable = clickable;
        clone.bypassMaxAmount = bypassMaxAmount;
        clone.hiddenName = hiddenName;
        clone.hiddenNameIfNull = hiddenNameIfNull;
        clone.lore = lore;

        if (itemStack != null) {
            clone.itemStack = itemStack.copy();
        }
        else
            clone.itemStack = null;

        if (nbt != null)
            clone.setUnique();

        clone.actions.putAll(actions);
        clone.clickActions.addAll(clickActions);
        clone.actionLambdas.putAll(actionLambdas);
        clone.addToReferencedList.addAll(addToReferencedList);

        return clone;
    }

}
