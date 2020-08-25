package gold24park.railkorea.module;

import gold24park.railkorea.RailKorea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageModule {
    private static DeathMessageModule instance;

    public static DeathMessageModule getInstance() {
        if (instance == null)
            instance = new DeathMessageModule();
        return instance;
    }

    public void broadcast(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage("");
        String key = "count_death." + player.getName();
        int countDeath = 0;
        try {
            countDeath = RailKorea.config.getInt(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RailKorea.config.set(key, ++countDeath);

        Bukkit.broadcastMessage(deathMessage +
                ChatColor.ITALIC + " " + ChatColor.GOLD + "(" + countDeath + "번째 죽음을 맞이함)");
    }
}
