package n.e.k.o.menus.utils;

public enum ClickAction {

    LEFT,
    RIGHT,
    MIDDLE,
    SHIFT_LEFT,
    SHIFT_RIGHT;

    public static ClickAction get(String key) {
        for (ClickAction action : values())
            if (action.name().equalsIgnoreCase(key))
                return action;
        return LEFT;
    }

}