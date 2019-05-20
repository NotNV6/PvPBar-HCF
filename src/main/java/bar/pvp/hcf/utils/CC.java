package bar.pvp.hcf.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public enum CC {
    BLACK('0', "black"),
    DARK_BLUE('1', "dark_blue"),
    DARK_GREEN('2', "dark_green"),
    DARK_AQUA('3', "dark_aqua"),
    DARK_RED('4', "dark_red"),
    PURPLE('4', "dark_purple"),
    DARK_PURPLE('5', "dark_purple"),
    GOLD('6', "gold"),
    ORANGE('6', "gold"),
    GRAY('7', "gray"),
    DARK_GRAY('8', "dark_gray"),
    BLUE('9', "blue"),
    GREEN('a', "green"),
    LIGHT_BLUE('b', "aqua"),
    AQUA('b', "aqua"),
    RED('c', "red"),
    LIGHT_PURPLE('d', "light_purple"),
    PINK('d', "light_purple"),
    YELLOW('e', "yellow"),
    WHITE('f', "white"),
    MAGIC('k', "obfuscated"),
    BOLD('l', "bold"),
    STRIKETHROUGH('m', "strikethrough"),
    UNDERLINE('n', "underline"),
    ITALIC('o', "italic"),
    RESET('r', "reset");

    public static Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");
    private static Map<Character, CC> BY_CHAR = new HashMap();
    private char code;
    private String toString;
    private String name;

    CC(char code, String name) {
        this.code = code;
        this.name = name;
        this.toString = new String(new char[]{'§', code});
    }

    public String toString() {
        return this.toString;
    }

    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static List<String> translate(List<String> stringList) {
        List<String> strings = new ArrayList<>();
        stringList.forEach(string -> strings.add(CC.translate(string)));

        return strings;
    }

    public static String translate(String textToTranslate) {
        if(textToTranslate == null) return "";
        return textToTranslate.contains("&") ? translateAlternateColorCodes('&', textToTranslate) : textToTranslate;
    }

    public static CC getByChar(char code) {
        return BY_CHAR.get(code);
    }

    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        for(int index = length - 1; index > -1; --index) {
            char section = input.charAt(index);
            if (section == 167 && index < length - 1) {
                char c = input.charAt(index + 1);
                CC color = getByChar(c);
                if (color != null) {
                    result = color.toString() + result;
                    if (color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    public String getName() {
        return this.name;
    }

    static {
        CC[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            CC colour = var0[var2];
            BY_CHAR.put(colour.code, colour);
        }

    }
}
