package n.e.k.o.menus;

import n.e.k.o.menus.menus.Menu;
import n.e.k.o.menus.menus.MenuItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Map;

public class CustomChestContainer extends ChestContainer {

    private final int rows;
    private final Map<Integer, MenuItem> items;
    private final Menu menu;
    private final ItemStack deny = new ItemStack(Items.BARRIER);

    public CustomChestContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInventory, IInventory simple, int rows, Map<Integer, MenuItem> items, Menu menu) {
        super(containerType, windowId, playerInventory, simple, rows);
        this.rows = rows;
        this.items = items;
        this.menu = menu;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        if (slotId < 0 || slotId >= 9 * rows) {
            // Allow modification in player's inventory
            return super.slotClick(slotId, dragType, clickType, player);
        }

        // Execute click event, if it exists for this slot
        if (items.containsKey(slotId)) {
            MenuItem guiItem = items.get(slotId);
            if (guiItem != null && guiItem.clickable) {
                guiItem.slotClick(slotId, dragType, clickType, player, menu);
                if (guiItem.nbt != null)
                    guiItem.setUnique();
            }
        }

        // Check if we can modify item in slot
        if (menu.allowsSlotModification.contains(slotId)) {
            // Allow modification in chest menu
            return super.slotClick(slotId, dragType, clickType, player);
        }

        return deny;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var _menu = menu; // For performance
        if (_menu == null) return;
        if (!_menu.tasks.isEmpty())
            _menu.tasks.forEach(task -> {
                task.tick();
                if (task.isFinished)
                    _menu.tasks.remove(task);
            });
        if (!_menu.runLaters.isEmpty())
            _menu.runLaters.forEach(task -> {
                task.tick();
                if (task.isFinished)
                    _menu.runLaters.remove(task);
            });
    }

    public void onContainerOpenPost(PlayerEntity player) {
        if (menu != null && !menu.onOpenPostEvent.isEmpty()) {
            menu.onOpenPostEvent.forEach(event -> event.accept(menu, player));
        }
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity player) {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.onContainerClosed(player);
        if (menu != null && !menu.onCloseEvent.isEmpty()) {
            menu.onCloseEvent.forEach(event -> event.accept(menu, player));
        }
    }

    public int getRows() {
        return rows;
    }

    public Map<Integer, MenuItem> getItems() {
        return items;
    }

    public Menu getMenu() {
        return menu;
    }

    public void removeItem(int slot) {
        items.remove(slot);
        putStackInSlot(slot, new ItemStack(Items.AIR, 0));
    }

    public void setTitle(String title) {
        this.menu.setTitle(title);
    }

}
