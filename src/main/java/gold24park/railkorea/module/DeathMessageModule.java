package gold24park.railkorea.module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class DeathMessageModule {

    private static DeathMessageModule instance;
    private final Plugin main;

    public static DeathMessageModule getInstance(Plugin main) {
        if (instance == null)
            instance = new DeathMessageModule(main);
        return instance;
    }

    private DeathMessageModule(Plugin main) {
        this.main = main;
    }

    public void broadcast(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage("");
        String key = "count_death." + player.getName();
        int countDeath = 0;
        try {
            countDeath = main.getConfig().getInt(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        main.getConfig().set(key, ++countDeath);

        Bukkit.broadcastMessage(deathMessage +
                ChatColor.ITALIC + " " + ChatColor.GOLD + "(" + countDeath + "번째 죽음을 맞이함)");
    }
}
