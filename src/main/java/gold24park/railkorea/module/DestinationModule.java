package gold24park.railkorea.module;

import gold24park.railkorea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DestinationModule {

    private static DestinationModule instance;
    private Plugin main;

    public static DestinationModule getInstance(Plugin main) {
        if (instance == null)
            instance = new DestinationModule(main);
        return instance;
    }

    private DestinationModule(Plugin main) {
        this.main = main;
    }

    public void setDestination(Player player, String[] args) {
        int x = 0;
        int z = 0;
        try {
            x = Util.parseInt(args[0]);
            z = Util.parseInt(args[1]);
        } catch (Exception e) {

        }
        registerDestination(player, x, z);
    }

    public void whereIs(Player player, String[] args) {
        if (args != null && args.length > 0) {
            Player targetPlayer = Util.findPlayer(args[0]);
            if (targetPlayer != null) {
                player.sendMessage(ChatColor.GREEN + "[!] 해당 플레이어의 좌표가 목적지로 등록되었습니다.");
                Location location = targetPlayer.getLocation();
                registerDestination(player, location.getBlockX(), location.getBlockZ());
            } else {
                player.sendMessage(ChatColor.RED + "[!] 플레이어를 찾을 수 없어요.");
            }
        }
    }

    private void registerDestination(Player player, int x, int z) {
        main.getConfig().set("destination." + player.getName() + ".x", x);
        main.getConfig().set("destination." + player.getName() + ".z", z);
        main.getConfig().set("hide_hud." + player.getName(), 0);
        main.saveConfig();
    }

}