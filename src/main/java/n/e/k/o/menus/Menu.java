package n.e.k.o.menus;

import io.netty.buffer.Unpooled;
import n.e.k.o.menus.utils.StringColorUtils;
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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class Menu {

    public final String id;
    public String title = "";
    public int height = 3;

    public Map<Integer, MenuItem> items;
    public List<MenuItem> emptyItems;
    public Map<String, MenuItem> referencedItems;
    List<TaskTimer> tasks;
    private CustomChestContainer chestContainer;

    public static Menu builder() {
        return new Menu();
    }

    public Menu() {
        this("");
    }

    public Menu(String id) {
        this.id = id;
        this.items = new HashMap<>();
        this.emptyItems = new ArrayList<>();
        this.referencedItems = new HashMap<>();
        this.tasks = new ArrayList<>();
    }

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

    public INamedContainerProvider build() {
        testItems(); // Test all item ids etc
        Inventory inventory = new Inventory(9 * height);
        boolean hasEmptyItems = !emptyItems.isEmpty();
        for (int slot = 0, emptySlot = 0; slot < 9 * height; slot++) {
            MenuItem guiItem = items.getOrDefault(slot, !hasEmptyItems ? null : emptyItems.get((emptySlot++) % emptyItems.size()));
            if (guiItem == null || guiItem.item == null)
                continue;
            guiItem.setMenu(this); // Update reference to owner
            guiItem.setReferencedItems(this); // Update missing references
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(guiItem.item));
            ItemStack stack = new ItemStack(item, guiItem.amount);
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
                chestContainer.onContainerOpen();
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

    private Constructor<?> ctr = null;
    private Field playChannelField = null;

    public boolean setTitle(ServerPlayerEntity player, String title) {
        try {
            Container c = player.openContainer;
            if (c == null) return true;
            ContainerType<?> type = c.getType();
            int id = Registry.MENU.getId(type);

            PacketBuffer extraData = new PacketBuffer(Unpooled.buffer());
            Consumer<PacketBuffer> extraDataWriter = buff -> {};
            extraDataWriter.accept(extraData);
            extraData.readerIndex(0);

            PacketBuffer output = new PacketBuffer(Unpooled.buffer());
            output.writeVarInt(extraData.readableBytes());
            output.writeBytes(extraData);

            if (ctr == null) {
                ctr = FMLPlayMessages.OpenContainer.class.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, ITextComponent.class, PacketBuffer.class);
                ctr.setAccessible(true);
            }
            FMLPlayMessages.OpenContainer msg = (FMLPlayMessages.OpenContainer) ctr.newInstance(id, player.currentWindowId, StringColorUtils.getColoredString(title), output);

            if (playChannelField == null) {
                playChannelField = FMLNetworkConstants.class.getDeclaredField("playChannel");
                playChannelField.setAccessible(true);
            }
            SimpleChannel playChannel = (SimpleChannel) playChannelField.get(null);

            NetworkManager networkManager = player.connection.getNetworkManager();
            NetworkDirection direction = NetworkDirection.PLAY_TO_CLIENT;

            playChannel.sendTo(msg, networkManager, direction);

            setTitle(title);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public void testItems() {
        Map<String, Item> itemCache = new HashMap<>();
        Consumer<MenuItem> c = item -> {
            if (item.item == null)
                return;
            String itemId = item.item.toLowerCase();
            Item _item;
            if (itemCache.containsKey(itemId))
                _item = itemCache.get(itemId);
            else {
                _item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                itemCache.put(itemId, _item);
            }
            if (_item == null)
                System.err.println("Didn't find item by name: '" + item.item + "' ('" + itemId + "') in menu (id = '" + id + "') at slot '" + item.slot + "'. The menu may display the wrong item.");
        };
        items.values().forEach(c);
        emptyItems.forEach(c);
        itemCache.clear();
    }

}
