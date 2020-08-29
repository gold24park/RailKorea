package gold24park.railkorea;

import gold24park.railkorea.module.*;
import gold24park.railkorea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class EventListener implements Listener {
    private final Plugin main;

    public EventListener(Plugin main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        VillagerModule.getInstance(main).onInventoryClosed(event);
    }

    @EventHandler
    public void interact(PlayerInteractEvent  event) {
        LocationModule.getInstance(main).openMap(event);
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String villagerName = villager.getCustomName();

            if (VillagerModule.getInstance(main).isMerchant(villagerName)) {
                event.setCancelled(true);
                VillagerModule.getInstance(main).openTradingGUI(player, true);
            } else if (VillagerModule.getInstance(main).isBuyer(villagerName)) {
                event.setCancelled(true);
                VillagerModule.getInstance(main).openTradingGUI(player, false);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (MoneyModule.CURRENCY_NAME.equalsIgnoreCase(meta.getDisplayName())) {
                Location location = block.getLocation();
                location.getBlock().setType(Material.AIR);
                player.sendMessage("화폐는 바닥에 놓을 수 없습니다.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DeathMessageModule.getInstance(main).broadcast(event);
        Location location = player.getLocation();
        player.sendMessage(ChatColor.RED + "[!] 마지막으로 당신이 죽은 곳: " +
                Util.getLocationText(location));
        main.saveConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ProfessionModule.getInstance(main).chat(player, event.getMessage());
        event.setCancelled(true);
    }

    // Player가 책 편집을 했을때
    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        LocationModule.getInstance(main).saveMap(event);
        main.saveConfig();
    }

    @EventHandler
    public void onPlayerEditBook(PlayerTakeLecternBookEvent event) {
        LocationModule.getInstance(main).saveMap(event);
        main.saveConfig();
    }

    // Player가 움직일때 시간 업데이트
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        TabListModule.getInstance(main).update(player, false);
        LocationModule.getInstance(main).checkBiome(player);

        // 움직일때 체력 보이기 - 혼자서 테스트 못함
        try {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();

            Objective objective = scoreboard.registerNewObjective("showhealth", "health");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName("/ 20 HP");

            player.setScoreboard(scoreboard);
            player.setHealth(player.getHealth()); // Update Health
        } catch (Exception exception) {

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        TabListModule.getInstance(main).update(e.getPlayer(), true);
    }
}
