package n.e.k.o.menus.menus;

import n.e.k.o.menus.CustomChestContainer;
import n.e.k.o.menus.NekoMenus;
import n.e.k.o.menus.utils.StringColorUtils;
import n.e.k.o.menus.utils.TaskRunLater;
import n.e.k.o.menus.utils.TaskTimer;
import n.e.k.o.menus.utils.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Menu {

    public final String id;
    public String title = "";
    public int height = 3;

    public Map<Integer, MenuItem> items;
    public List<MenuItem> emptyItems;
    public Map<String, MenuItem> referencedItems;
    public List<TaskTimer> tasks;
    public List<TaskRunLater> runLaters;
    public List<BiConsumer<Menu, PlayerEntity>> onOpenEvent, onCloseEvent;

    private CustomChestContainer chestContainer;

    public static Menu builder() {
        return new Menu();
    }

    public static Menu builder(String title, int height) {
        return new Menu("", title, height);
    }

    public static Menu builder(String id, String title, int height) {
        return new Menu(id, title, height);
    }

    public Menu() {
        this("");
    }

    public Menu(String id) {
        this.id = id;
        this.items = new HashMap<>();
        this.emptyItems = new ArrayList<>();
        this.referencedItems = new HashMap<>();
        this.tasks = new CopyOnWriteArrayList<>();
        this.runLaters = new CopyOnWriteArrayList<>();
        this.onOpenEvent = new ArrayList<>();
        this.onCloseEvent = new ArrayList<>();
    }

    public Menu(String id, String title, int height) {
        this.id = id;
        this.title = title;
        this.height = height;
        this.items = new HashMap<>();
        this.emptyItems = new ArrayList<>();
        this.referencedItems = new HashMap<>();
        this.tasks = new CopyOnWriteArrayList<>();
        this.runLaters = new CopyOnWriteArrayList<>();
        this.onOpenEvent = new ArrayList<>();
        this.onCloseEvent = new ArrayList<>();
    }

    /**
     * Create a clone of this menu
     * @return cloned menu
     */
    public Menu clone() {
        Menu clone = new Menu(id, title, height);

        // Clone items
        items.forEach((slot, item) -> clone.items.put(slot, item.clone(clone)));

        // Clone empty items
        emptyItems.forEach(item -> clone.emptyItems.add(item.clone(clone)));

        // Clone references
        referencedItems.forEach((key, item) -> clone.referencedItems.put(key, item.clone(clone)));

        // Clone tasks
        tasks.forEach(task -> clone.tasks.add(task.clone()));

        // Clone runLaters
        runLaters.forEach(task -> clone.runLaters.add(task.clone()));

        // Clone open/close event
        clone.onOpenEvent = onOpenEvent;
        clone.onCloseEvent = onCloseEvent;

        return clone;
    }

    /**
     * This doesn't change the title in the window AFTER the menu has been opened.
     * Use `setTitle(player, title)` for that.
     */
    public Menu setTitle(String title) {
        this.title = title;
        return this;
    }

    public Menu setHeight(int height) {
        if (height < 1) height = 1; else
        if (height > 6) height = 6;
        this.height = height;
        return this;
    }

    public Menu setDefaultHeight() {
        this.height = 3;
        return this;
    }

    public Menu addItem(MenuItem item) {
        this.items.put(item.slot, item);
        ItemStack stack = item.getItemStack();
        if (this.chestContainer != null && stack != null) {
            this.chestContainer.putStackInSlot(item.slot, stack);
        }
        return this;
    }

    public MenuItem getItem(int x, int y) {
        return getItem(Utils.gridToSlot(x, y));
    }

    public MenuItem getItem(int slot) {
        return this.items.get(slot);
    }

    public Menu removeItem(MenuItem item) {
        this.items.remove(item.slot);
        if (this.chestContainer != null) {
            this.chestContainer.removeItem(item.slot);
        }
        return this;
    }

    public Menu moveItem(MenuItem item, int newX, int newY) {
        return moveItem(item, Utils.gridToSlot(newX, newY));
    }

    public Menu moveItem(MenuItem item, int newSlot) {
        removeItem(item);
        item.setSlot(newSlot);
        addItem(item);
        return this;
    }

    public Menu swapItems(MenuItem item1, MenuItem item2) {
        removeItem(item1);
        removeItem(item2);
        int tmp = item1.slot;
        item1.setSlot(item2.slot);
        item2.setSlot(tmp);
        addItem(item1);
        addItem(item2);
        return this;
    }

    public Menu addEmptyItem(MenuItem item) {
        this.emptyItems.add(item);
        return this;
    }

    public Menu removeEmptyItem(MenuItem item) {
        this.emptyItems.remove(item.slot);
        return this;
    }

    public Menu setReferencedItem(String key, MenuItem item) {
        this.referencedItems.put(key, item);
        return this;
    }

    public boolean hasReferencedItem(String key) {
        return this.referencedItems.containsKey(key);
    }

    public MenuItem getReferencedItem(String key) {
        return this.referencedItems.get(key);
    }

    public Menu removeReferencedItem(String key) {
        this.referencedItems.remove(key);
        return this;
    }

    public Menu removeReferencedItemByValue(MenuItem item) {
        for (String key : new ArrayList<>(this.referencedItems.keySet())) {
            MenuItem refItem = this.referencedItems.get(key);
            if (refItem == item) {
                return removeReferencedItem(key);
            }
        }
        return this;
    }

    public Menu runTaskTimer(Runnable task, int delay, int ticks) {
        this.tasks.add(new TaskTimer(task, delay, ticks));
        return this;
    }

    public Menu runTaskTimer(Runnable task, int delay, int ticks, int finishAfterRounds) {
        this.tasks.add(new TaskTimer(task, delay, ticks, finishAfterRounds));
        return this;
    }

    public Menu runTaskLater(Runnable task, int ticks) {
        this.runLaters.add(new TaskRunLater(task, ticks));
        return this;
    }

    public Menu addMenuOpenEvent(BiConsumer<Menu, PlayerEntity> event) {
        this.onOpenEvent.add(event);
        return this;
    }

    public Menu addMenuCloseEvent(BiConsumer<Menu, PlayerEntity> event) {
        this.onCloseEvent.add(event);
        return this;
    }

    public Menu removeMenuOpenEvent(BiConsumer<Menu, PlayerEntity> event) {
        this.onOpenEvent.remove(event);
        return this;
    }

    public Menu removeMenuCloseEvent(BiConsumer<Menu, PlayerEntity> event) {
        this.onCloseEvent.remove(event);
        return this;
    }

    public INamedContainerProvider build() {
        testItems(); // Test all item ids etc
        Inventory inventory = new Inventory(9 * height);
        boolean hasEmptyItems = !emptyItems.isEmpty();
        for (int slot = 0, emptySlot = 0; slot < 9 * height; slot++) {
            MenuItem guiItem = items.getOrDefault(slot, !hasEmptyItems ? null : emptyItems.get((emptySlot++) % emptyItems.size()));
            if (guiItem == null || (guiItem.itemStr == null && guiItem.item == null))
                continue;
            guiItem.setMenu(this); // Update reference to owner
            guiItem.setReferencedItems(this); // Update missing references
            Item item;
            if (guiItem.item != null)
                item = guiItem.item;
            else
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(guiItem.itemStr));
            ItemStack stack = new ItemStack(item, guiItem.amount);
            if (guiItem.name != null && !guiItem.name.isEmpty())
                stack.setDisplayName(StringColorUtils.getColoredString(guiItem.name));
            if (!guiItem.lore.isEmpty()) {
                CompoundNBT display = stack.getOrCreateChildTag("display");
                ListNBT lore = new ListNBT();
                for (String str : guiItem.lore) {
                    IFormattableTextComponent colored = StringColorUtils.getColoredString(str);
                    String json = ITextComponent.Serializer.toJson(colored);
                    lore.add(StringNBT.valueOf(json));
                }
                display.put("Lore", lore);
            }
            guiItem.setItemStack(stack);
            inventory.setInventorySlotContents(slot, stack);
        }
        final Menu thisMenu = this;
        return new INamedContainerProvider() {
            @Override
            public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
                ContainerType<?> containerType;
                switch (height) {
                    case 1:
                        containerType = ContainerType.GENERIC_9X1;
                        break;
                    case 2:
                        containerType = ContainerType.GENERIC_9X2;
                        break;
                    default:
                        height = 3;
                    case 3:
                        containerType = ContainerType.GENERIC_9X3;
                        break;
                    case 4:
                        containerType = ContainerType.GENERIC_9X4;
                        break;
                    case 5:
                        containerType = ContainerType.GENERIC_9X5;
                        break;
                    case 6:
                        containerType = ContainerType.GENERIC_9X6;
                }
                CustomChestContainer chestContainer = new CustomChestContainer(containerType, windowId, playerInventory, inventory, height, items, thisMenu);
                thisMenu.setCustomChestContainer(chestContainer);
                chestContainer.onContainerOpen(player);
                return chestContainer;
            }
            @Nonnull
            @Override
            public ITextComponent getDisplayName() {
                return StringColorUtils.getColoredString(title);
            }
        };
    }

    private void setCustomChestContainer(CustomChestContainer chestContainer) {
        this.chestContainer = chestContainer;
    }

    public void open(ServerPlayerEntity player) {
        NekoMenus.runLater(() -> NetworkHooks.openGui(player, build()));
    }

    public static void open(Menu menu, ServerPlayerEntity player) {
        NekoMenus.runLater(() -> NetworkHooks.openGui(player, menu.build()));
    }

    public static void open(INamedContainerProvider build, ServerPlayerEntity player) {
        NekoMenus.runLater(() -> NetworkHooks.openGui(player, build));
    }

    public String print() {
        return "Menu '" + id + "' { Title = '" + title + "', Height = " + height + ", Items = " + items.size() + " }";
    }

    @Override
    public String toString() {
        return print();
    }

    public void updateTitle(ServerPlayerEntity player, String title) {
        updateTitle(player, title, false);
    }
    public void updateTitle(ServerPlayerEntity player, String title, boolean insideTask) {
        setTitle(title);
        player.connection.sendPacket(new SOpenWindowPacket(chestContainer.windowId, chestContainer.getType(), StringColorUtils.getColoredString(title)));
        if (insideTask)
            chestContainer.inventorySlots.forEach(slot -> player.connection.sendPacket(new SSetSlotPacket(chestContainer.windowId, slot.slotNumber, slot.getStack())));
    }

    public void testItems() {
        Map<String, Item> itemCache = new HashMap<>();
        Consumer<MenuItem> c = item -> {
            if (item.itemStr == null || item.item != null)
                return;
            String itemId = item.itemStr.toLowerCase();
            Item _item;
            if (itemCache.containsKey(itemId))
                _item = itemCache.get(itemId);
            else {
                _item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                itemCache.put(itemId, _item);
            }
            if (_item == null)
                System.err.println("Didn't find item by name: '" + item.itemStr + "' ('" + itemId + "') in menu (id = '" + id + "') at slot '" + item.slot + "'. The menu may display the wrong item.");
        };
        items.values().forEach(c);
        emptyItems.forEach(c);
        itemCache.clear();
    }

}
