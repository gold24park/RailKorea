package gold24park.railkorea;

import gold24park.railkorea.module.*;
import gold24park.railkorea.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class EventListener implements Listener {
    private final Plugin main;

    public EventListener(Plugin main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
        Bukkit.getScheduler().runTaskTimerAsynchronously(main, new Runnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld("world");
                if (world != null) {
                    TabListModule.getInstance(main).onTimeChanged(world);
                }
            }
        }, 0, 900L);
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        VillagerModule.getInstance(main).onInventoryClosed(event);
        CoinModule.getInstance(main).onInventoryClosed(event);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        CoinModule.getInstance(main).onInventoryOpen(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ShopModule.getInstance(main).onInventoryClick(event);
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        CoinModule.getInstance(main).onPlayerInteractEvent(event);
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent event) {
        ShopModule.getInstance(main).interactEntity(event);
    }

    @EventHandler
    public void onDestroyCoin(EntityDamageEvent event) {
        CoinModule.getInstance(main).onCoinDestroyed(event);
    }


    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        CoinModule.getInstance(main).onBlockDestroy(event);
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
}
