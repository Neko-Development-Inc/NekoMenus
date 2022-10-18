package n.e.k.o.menus;

import n.e.k.o.menus.menus.Menu;
import n.e.k.o.menus.menus.MenuItem;
import n.e.k.o.menus.utils.TaskTimer;
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
            if (guiItem != null && guiItem.clickable)
                guiItem.slotClick(slotId, dragType, clickType, player, menu);
        }

        return deny;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (menu != null && !menu.tasks.isEmpty()) {
            menu.tasks.forEach(TaskTimer::tick);
        }
    }

    public void onContainerOpen() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity player) {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.onContainerClosed(player);
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
