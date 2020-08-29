package gold24park.railkorea.command;

import gold24park.railkorea.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Home {
    public static void run(Player player) {
        Location spawnLocation = player.getBedSpawnLocation();
        if (spawnLocation != null) {
            player.sendMessage(ChatColor.AQUA + "[!] 나의 스폰지점 좌표: " +
                    Util.getLocationText(spawnLocation));
        }
    }
}
