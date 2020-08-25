package gold24park.railkorea;


import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import gold24park.railkorea.module.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class RailKorea extends JavaPlugin implements Listener {
    
    public static FileConfiguration config = null;
    private ProtocolManager protocolManager;

    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = getConfig();
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        VillagerModule.getInstance().initSellingItems();
        VillagerModule.getInstance().initBuyingItems();
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("래하래하 플러그인 활성화 완료! :)");

        LocationModule.getInstance().run(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("래바래바 플러그인을 종료합니다~");
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("admin")) {
            if (commandLabel.equalsIgnoreCase("create_merchant")) {
                VillagerModule.getInstance().createMerchant(player, args);
            }
            if (commandLabel.equalsIgnoreCase("money")) {
                MoneyModule.getInstance().money(player, args);
            }
            if (commandLabel.equalsIgnoreCase("create_buyer")) {
                VillagerModule.getInstance().createBuyer(player, args);
            }
            if (commandLabel.equalsIgnoreCase("register")) {
                ProfessionModule.getInstance().register(player, args);
                saveConfig();
            }
            if (commandLabel.equalsIgnoreCase("edit_merchant")) {
                VillagerModule.getInstance().openMerchantInventorySettings(this, player);
            }
            if (commandLabel.equalsIgnoreCase("edit_buyer")) {
                VillagerModule.getInstance().openBuyerInventorySettings(this, player);
            }
        }
        if (commandLabel.equalsIgnoreCase("home")) {
            Location spawnLocation = player.getBedSpawnLocation();
            if (spawnLocation != null) {
                player.sendMessage(ChatColor.AQUA + "[!] 나의 스폰지점 좌표: " +
                        Util.getLocationText(
                                spawnLocation.getBlockX(),
                                spawnLocation.getBlockY(),
                                spawnLocation.getBlockZ()));
            }
        }
        if (commandLabel.equalsIgnoreCase("destination")) {
            setDestination(player, args[0], args[1]);
        }
        if (commandLabel.equalsIgnoreCase("hud")) {
            if (args != null && args.length > 0) {
                if (args[0].equalsIgnoreCase("on")) {
                    config.set("hide_hud." + player.getName(), 0);
                    player.sendMessage(ChatColor.DARK_GRAY + "[!] HUD를 띄웁니다.");
                }
                else if (args[0].equalsIgnoreCase("off")) {
                    config.set("hide_hud." + player.getName(), 1);
                    player.sendMessage(ChatColor.DARK_GRAY + "[!] HUD가 숨겨졌습니다.");
                }
                else {
                    player.sendMessage(ChatColor.RED + "[!] /hud on 또는 off를 입력 해 주시오.");
                }
                saveConfig();
            }
        }
        if (commandLabel.equalsIgnoreCase("whereis")) {
            if (args != null && args.length > 0) {
                Player targetPlayer = null;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().equalsIgnoreCase(args[0])) {
                        targetPlayer = p;
                        break;
                    }
                }
                if (targetPlayer != null) {
                    player.sendMessage(ChatColor.GREEN + "[!] 해당 플레이어의 좌표가 목적지로 등록되었습니다.");
                    Location location = targetPlayer.getLocation();
                    setDestination(player, location.getBlockX(), location.getBlockZ());
                } else {
                    player.sendMessage(ChatColor.RED + "[!] 플레이어를 찾을 수 없어요.");
                }
            }
        }
        if (commandLabel.equalsIgnoreCase("map")) {
            LocationModule.getInstance().openMapGUI(player);
        }
        return false;
    }

    private void setDestination(Player player, String dx, String dz) {
        int x = 0;
        int z = 0;
        try {
            x = Util.parseInt(dx);
            z = Util.parseInt(dz);
        } catch (Exception e) {

        }
        setDestination(player, x, z);
    }
    private void setDestination(Player player, int x, int z) {
        config.set("destination." + player.getName() + ".x", x);
        config.set("destination." + player.getName() + ".z", z);
        config.set("hide_hud." + player.getName(), 0);
        saveConfig();
    }

    private void openMerchant(Player player, boolean isMerchant) {
        String storeName = config.getString(isMerchant ?
                "store_name.merchant" : "store_name.buyer");
        Merchant merchant = Bukkit.createMerchant(storeName);
        merchant.setRecipes(
                isMerchant ?
                        VillagerModule.getInstance().getMerchantRecipes(this, player) :
                        VillagerModule.getInstance().getBuyerRecipes(this, player));
        player.openMerchant(merchant, true);
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        VillagerModule.getInstance().onInventoryClosed(this, event);
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String villagerName = villager.getCustomName();

            if (VillagerModule.getInstance().isMerchant(villagerName)) {
                event.setCancelled(true);
                openMerchant(player, true);
            } else if (VillagerModule.getInstance().isBuyer(villagerName)) {
                event.setCancelled(true);
                openMerchant(player, false);
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
        DeathMessageModule.getInstance().broadcast(event);
        Location location = player.getLocation();
        player.sendMessage(ChatColor.RED + "[!] 마지막으로 당신이 죽은 곳: " +
                Util.getLocationText(location));
        saveConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ProfessionModule.getInstance().chat(player, event.getMessage());
        event.setCancelled(true);
    }

    // Player가 책 편집을 했을때
    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        LocationModule.getInstance().saveMap(event);
        saveConfig();
    }

    @EventHandler
    public void onPlayerEditBook(PlayerTakeLecternBookEvent event) {
        LocationModule.getInstance().saveMap(event);
        saveConfig();
    }

    // Player가 움직일때 시간 업데이트
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        TabListModule.getInstance().update(protocolManager, player, false);
        LocationModule.getInstance().checkBiome(this, player);

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
        TabListModule.getInstance().update(protocolManager, e.getPlayer(), true);
    }
}
