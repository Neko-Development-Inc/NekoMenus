package n.e.k.o.menus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

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
            if (guiItem.clickable)
                guiItem.slotClick(slotId, dragType, clickType, player, menu);
        }

        return deny;
    }

}
