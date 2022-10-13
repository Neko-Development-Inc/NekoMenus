package n.e.k.o.menus.utils;

public enum ClickAction {

    LEFT,
    RIGHT,
    MIDDLE,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    DOUBLE_LEFT,
    SLOT_1,
    SLOT_2,
    SLOT_3,
    SLOT_4,
    SLOT_5,
    SLOT_6,
    SLOT_7,
    SLOT_8,
    SLOT_9;

    public static ClickAction get(String key) {
        for (ClickAction action : values())
            if (action.name().equalsIgnoreCase(key))
                return action;
        return LEFT;
    }

}