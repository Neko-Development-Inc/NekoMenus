package n.e.k.o.menus.utils;

public class Utils {

    public static int[] slotToGrid(int slot) {
        return new int[] { slot % 9, slot / 9 };
    }

    public static int gridToSlot(int x, int y) {
        return y * 9 + x;
    }

}
