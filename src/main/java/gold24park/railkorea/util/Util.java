package gold24park.railkorea.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Util {
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static String getLocationText(Location location) {
        return getLocationText(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static String getLocationText(int x, int y, int z) {
        return "[XYZ: " + x +
                " / " + y +
                " / " + z + "]";
    }

    public static String getLocationText(int x, int z) {
        return "[XZ: " + x +
                " / " + z + "]";
    }

    public static int parseInt(String text) {
        int number = 0;
        try {
            number = Integer.parseInt(text);
        } catch (Exception e) {

        }
        return number;
    }

    public static boolean isEmpty(Inventory inventory) {
        boolean isEmpty = true;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public static Player findPlayer(String nickname) {
        Player targetPlayer = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(nickname)) {
                targetPlayer = p;
                break;
            }
        }
        return targetPlayer;
    }

    public static Player findPlayer(String nickname, Player defaultPlayer) {
        Player targetPlayer = defaultPlayer;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(nickname)) {
                targetPlayer = p;
                break;
            }
        }
        return targetPlayer;
    }

}
