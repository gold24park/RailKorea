package gold24park.railkorea;

import org.bukkit.Location;

public class Util {
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static String getLocationText(Location location) {
        return getLocationText(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static String getLocationText(int x, int y, int z) {
        return "<x: " + x +
                " y: " + y +
                " z: " + z + ">";
    }

    public static int parseInt(String text) {
        int number = 0;
        try {
            number = Integer.parseInt(text);
        } catch (Exception e) {

        }
        return number;
    }
}
