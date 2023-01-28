package n.e.k.o.menus.utils;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

public class StringColorUtils {

    // &([\da-fk-or])
    //            ^ r
    //         ^-^ from k to o
    //      ^-^ from a to f
    //    ^^ number 0-9
    //   ^ expect one char
    //  ^ first group
    // ^ &
    //
    // s&0s, &9, &a, &f, &k, &o, &r
    // s§0s, §9, §a, §f, §k, §o, §r

    public static IFormattableTextComponent getColoredString(String string) {
        return new StringTextComponent((string == null ? "" : string.replaceAll("&([\\da-fk-or])", "§$1")));
    }

    public static IFormattableTextComponent getColoredString(String string, char c) {
        return new StringTextComponent((string == null ? "" : string.replaceAll(c + "([\\da-fk-or])", "§$1")));
    }

}
