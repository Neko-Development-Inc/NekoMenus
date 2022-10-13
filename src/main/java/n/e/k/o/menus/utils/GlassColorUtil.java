package n.e.k.o.menus.utils;

public enum GlassColorUtil {

    White(0),
    Orange(1),
    Magenta(2),
    LightBlue(3),
    Yellow(4),
    Lime(5),
    Pink(6),
    Gray(7),
    LightGray(8),
    Cyan(9),
    Purple(10),
    Blue(11),
    Brown(12),
    Green(13),
    Red(14),
    Black(15);

    public final int color;

    GlassColorUtil(int color) {
        this.color = color;
    }

    public static GlassColorUtil get(String name) {
        for (GlassColorUtil glassColorUtil : values())
            if (glassColorUtil.name().equalsIgnoreCase(name) || ("" + glassColorUtil.color).equalsIgnoreCase(name))
                return glassColorUtil;
        return White;
    }

}
