package gold24park.railkorea.module;

import gold24park.railkorea.model.Coin;
import gold24park.railkorea.model.WorldTime;
import gold24park.railkorea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.text.NumberFormat;
import java.util.*;

public class CoinModule {

    public static final String CURRENCY_LORE = "여러분을 부자로 만들어줄 코인입니다!";
    public static final String CONFIG_PATH = "coins_issued.";
    public static final String CONFIG_COIN_ENABLED = "coin_enabled";

    private static CoinModule instance;

    private final Plugin main;
    private final Random random = new Random();

    private final HashMap<String, TradeAction> tradeListeners = new HashMap<String, TradeAction>();

    public boolean isEnabled = false;

    public static CoinModule getInstance(Plugin main) {
        if (instance == null) {
            instance = new CoinModule(main);
        }
        return instance;
    }

    private CoinModule(Plugin main) {
        this.main = main;

        Coin coin1 = new Coin("래일코인", Material.PHANTOM_SPAWN_EGG);
        Coin coin2 = new Coin( "트수코인", Material.GHAST_SPAWN_EGG);
        Coin coin3 = new Coin("존버코인", Material.VEX_SPAWN_EGG);
        Coin coin4 = new Coin( "붐은온다코인", Material.ZOGLIN_SPAWN_EGG);

        Coin.coins.put(coin1.displayName, coin1);
        Coin.coins.put(coin2.displayName, coin2);
        Coin.coins.put(coin3.displayName, coin3);
        Coin.coins.put(coin4.displayName, coin4);

        for (Coin coin : Coin.coins.values()) {
            coin.amount = main.getConfig().getInt(CONFIG_PATH + coin.displayName);
        }

        isEnabled = main.getConfig().getBoolean(CONFIG_COIN_ENABLED, false);
    }

    // TODO: 자산가치 출력
    public void printAssetValue() {

    }

    public void printCoinInfo(Player player) {
        player.sendMessage("== 코인정보 ==========================");
        player.sendMessage("* 총 발행: " + NumberFormat.getInstance().format(Coin.getTotalNumberOfIssuedCoins()));
        for (Coin coin : Coin.coins.values()) {
            player.sendMessage("* [" + coin.displayName + "] " + NumberFormat.getInstance().format(coin.amount));
        }
        player.sendMessage("=====================================");
    }

    interface TradeAction {
        void onTradeCompleted(HumanEntity player);
    }

    public void coinEvent(Player player, String[] args) {
        if (args != null && args.length > 0) {
            try {
                String command = args[0];
                if (command.equals("start")) {
                    player.sendMessage(ChatColor.YELLOW + "※ 코인관련 이벤트가 활성화 됩니다.");
                    isEnabled = true;
                    main.getConfig().set(CONFIG_COIN_ENABLED, isEnabled);
                    main.saveConfig();
                    return;
                }
                if (command.equals("stop")) {
                    player.sendMessage(ChatColor.YELLOW + "※ 코인관련 이벤트가 비활성화되었습니다.");
                    isEnabled = false;
                    main.getConfig().set(CONFIG_COIN_ENABLED, isEnabled);
                    main.saveConfig();
                    return;
                }
                player.sendMessage(ChatColor.YELLOW + "※ start 또는 stop 명령이 필요합니다.");
            } catch (Exception e) {

            }
        }
    }

    /**
     * 코인을 지급합니다.
     * @param player
     * @param args /coin [(string) PlayerName] [(string) CoinDisplayName] [(int) Amount]
     */
    public void coin(Player player, String[] args) {
        if (args != null && args.length > 0) {
            try {
                String playerName = args[0];
                Coin targetCoin = getCoin(args[1]);
                int amount = Integer.parseInt(args[2]);

                Player targetPlayer = Bukkit.getPlayer(playerName);
                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.YELLOW + "※ " + playerName + " 플레이어를 찾을 수 없습니다.");
                    return;
                }
                if (targetCoin == null) {
                    player.sendMessage(ChatColor.YELLOW + "※ 찾을 수 없는 잡코인입니다.");
                    return;
                }
                if (amount <= 0) {
                    player.sendMessage(ChatColor.YELLOW + "※ 코인 지급 수량은 0보다 커야합니다.");
                    return;
                }

                if (giveCoin(targetPlayer, targetCoin, amount)) {
                    // 발행처리
                    issueCoin(targetCoin, amount);
                }
            } catch (Exception e) {

            }
        }
    }

    public void issueCoin(Player player, String[] args) {
        if (args != null && args.length > 0) {
            try {
                Coin targetCoin = getCoin(args[0]);
                int amount = Integer.parseInt(args[1]);

                if (targetCoin == null) {
                    player.sendMessage(ChatColor.YELLOW + "※ 찾을 수 없는 잡코인입니다.");
                    return;
                }
                if (amount == 0) {
                    return;
                }

                issueCoin(targetCoin, amount);

                if (amount > 0) {
                    player.sendMessage(ChatColor.YELLOW + "※ [" + targetCoin + "] " + NumberFormat.getInstance().format(amount) + "개 발행");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "※ [" + targetCoin + "] " + NumberFormat.getInstance().format(amount) + "개 소각");
                }
            } catch (Exception e) {

            }
        }
    }

    // amount만큼 발행량을 늘리거나 줄입니다.
    public void issueCoin(Coin coin, int amount) {
        int currentAmount = main.getConfig().getInt(CONFIG_PATH + coin.displayName);
        int finalAmount = Math.max(currentAmount + amount, 0);
        main.getConfig().set(CONFIG_PATH + coin.displayName, finalAmount);
        main.saveConfig();

        Coin.coins.get(coin.displayName).amount = finalAmount;
    }

    private Coin getCoin(String coinDisplayName) {
        for (Coin coin : Coin.coins.values()) {
            if (coin.displayName.equals(coinDisplayName)) {
                return coin;
            }
        }
        return null;
    }

    private boolean giveCoin(Player player, Coin coin, int amount) {
        PlayerInventory inventory = player.getInventory();
        boolean hasAvailableSlot = hasAvailableSlot(inventory);
        if (hasAvailableSlot) {
            inventory.addItem(getCoinStack(coin, amount));
            Bukkit.broadcastMessage(ChatColor.AQUA + "[" + player.getName() + "]가 " + coin.displayName + " " + amount + "개를 얻었습니다.");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "[" + player.getName() +"]의 인벤토리가 충분하지 않아 코인을 받을 수 없어요.");
        }
        return hasAvailableSlot;
    }

    private boolean hasAvailableSlot(Inventory inventory){
        for (ItemStack item: inventory.getContents()) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }

    public void onInventoryOpen(InventoryOpenEvent event) {
        boolean isMerchant = event.getInventory().getType() == InventoryType.MERCHANT;
        if (!isMerchant) {
            return;
        }
        // inventory 오픈 당시 코인을 센다.
        HumanEntity targetPlayer = event.getPlayer();
        HashMap<String, Integer> coinWallet = countCoinsInInventory(targetPlayer.getInventory());
        TradeAction action = finalInventory -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Integer> finalCoinWallet = countCoinsInInventory(targetPlayer.getInventory());

                    for (Map.Entry<String, Integer> entry : coinWallet.entrySet()) {
                        String coinDisplayName = entry.getKey();
                        int amount = entry.getValue();
                        int finalAmount = 0;
                        if (finalCoinWallet.containsKey(coinDisplayName)) {
                            finalAmount = finalCoinWallet.get(coinDisplayName);
                        }
                        // 상점에서 구매했을때는 소각, 판매했을때는 발행된다.
                        Coin coin = getCoin(coinDisplayName);
                        if (finalAmount - amount != 0 && coin != null) {
                            issueCoin(coin, finalAmount - amount);
                        }
                    }
                    tradeListeners.put(targetPlayer.getName(), null);
                }
            }, 2L);
        };
        tradeListeners.put(targetPlayer.getName(), action);
    }

    private HashMap<String, Integer> countCoinsInInventory(Inventory inventory) {
        HashMap<String, Integer> coinWallet = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null) continue;
            Coin coin = Coin.getCoin(stack);
            if (coin != null) {
                int currentBalance = coinWallet.getOrDefault(coin.displayName, 0);
                coinWallet.put(coin.displayName, currentBalance + coin.amount);
            }
        }

        return coinWallet;
    }

    public void onTimeChanged(World world) {
        if (!isEnabled) return;

        Random rd = new Random();
        if (world.getTime() >= 3000 && world.getTime() < 4000) {
            String message = "";
            int randomValue = rd.nextInt(30);
            Coin randomCoin = (Coin) Coin.coins.values().toArray()[rd.nextInt(Coin.coins.values().size())];
            switch (randomValue) {
                case 0:
                    message = String.format("%s 코인이 10%% 상승합니다.\n이유는요? 없습니다!", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 0.9));
                    break;
                case 1:
                    message = String.format("%s 코인이 10%% 하락합니다.\n이유는요? 있겠습니까!", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 1.1));
                    break;
                case 2:
                    message = String.format("<%s : 여름의 전설> 영화가 곧 개봉합니다!\n%s 코인이 접속중인 모든 플레이어에게 10개씩 주어집니다!",
                            randomCoin.displayName,
                            randomCoin.displayName);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        giveCoin(player, randomCoin, 10);
                    }
                    break;
                case 3:
                    message = String.format("%s이 밈코인이 되고있습니다!\n가치가 15%% 상승합니다.", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 0.85));
                    break;
                case 4:
                    message = String.format("래일론 머스크가 '%s은(는) 최악이다'라고\n로켓단 카페에 포스팅했습니다! 가치가 15%% 하락합니다.", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 1.15));
                    break;
                case 5:
                    message = String.format("%s은(는) 인기가 식고있습니다!\n가치가 5%% 하락합니다.", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 0.95));
                    break;
                case 6:
                    message = String.format("%s 투자자들의 기도가 닿았습니다!\n가치가 5%% 상승합니다.", randomCoin.displayName);
                    issueCoin(randomCoin, (int) (Coin.coins.get(randomCoin.displayName).amount * 1.05));
                    break;
                case 7:
                    message = String.format("%s(이)가 알고리즘의 실수로 추가발행!\n%s 100개가 플레이어들의 주위에 흩뿌려졌습니다.",
                            randomCoin.displayName, randomCoin.displayName);
                    int droppedCoins = 0;
                    ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                    Collections.shuffle(players);
                    for (int i = 0; i < players.size(); i++) {
                        Player player = players.get(i);
                        int amount = 100 - droppedCoins;
                        if (i != players.size() - 1) {
                            amount = random.nextInt(100 - droppedCoins) + 1;
                            droppedCoins += amount;
                        }
                        int finalAmount = amount;
                        Bukkit.getScheduler().runTask(main, new Runnable() {
                            @Override
                            public void run() {
                                player.getWorld().dropItemNaturally(
                                        Util.getLocationInFrontOfPlayer(player),
                                        getCoinStack(randomCoin, finalAmount)
                                );
                            }
                        });
                    }
                    issueCoin(randomCoin, 100);
                    break;
                case 8:
                    int randomAmount = rd.nextInt(Coin.getTotalNumberOfIssuedCoins() / 4) + 1;
                    Player randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rd.nextInt(Bukkit.getOnlinePlayers().size())];
                    message = String.format(
                            "%s이(가) 꿈에 조상님을 만나 %s를 %d개 추가 매수합니다!\n인벤토리가 비어있길 바래요~",
                            randomPlayer.getName(),
                            randomCoin.displayName,
                            randomAmount
                    );
                    giveCoin(randomPlayer, randomCoin, randomAmount);
                    break;
            }

            if (message.length() > 0) {
                String finalMessage = message;
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendTitle("[NEWS]", finalMessage, 20, 70, 20);
                });
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[NEWS]" + message);
            }
        }
    }

    public void onInventoryClosed(InventoryCloseEvent event) {
        boolean isMerchant = event.getInventory().getType() == InventoryType.MERCHANT;
        if (!isMerchant) {
            return;
        }
        HumanEntity targetPlayer = event.getPlayer();
        if (tradeListeners.containsKey(targetPlayer.getName())) {
            TradeAction action = tradeListeners.get(targetPlayer.getName());
            if (action != null) {
                action.onTradeCompleted(targetPlayer);
            }
        }
    }

    // 코인이 소각되거나 암튼 사라짐
    public void onCoinDestroyed(EntityDamageEvent event) {
        if (!isEnabled) return;

        Entity entity = event.getEntity();
        if (event.getEntity().getType() == EntityType.DROPPED_ITEM && entity instanceof Item) {
            Coin coin = Coin.getCoin(((Item) entity).getItemStack());
            if (coin != null) {
                CoinModule.getInstance(main).issueCoin(coin, -((Item) entity).getItemStack().getAmount());
            }
        }
    }

    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!isEnabled) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            event.getItem() != null && Coin.isCoin(event.getItem())) {
            Coin coin = Coin.getCoin(event.getItem());
            if (coin != null) {
                CoinModule.getInstance(main).issueCoin(coin, -1);
            }
        }
    }

    // 코인 채굴에 성공
    public void onBlockDestroy(BlockBreakEvent event) {
        if (!isEnabled) return;

        Block block = event.getBlock();
        Player player = event.getPlayer();

        boolean isMiningBlock = block.getType() == Material.STONE ||
                block.getType() == Material.SAND ||
                block.getType() == Material.GRASS_BLOCK ||
                block.getType() == Material.GRAVEL;

        if (isMiningBlock && random.nextInt(512) == 0) {
            Coin randomCoin = (Coin) Coin.coins.values().toArray()[random.nextInt(Coin.coins.values().size())];
            int randomAmount = random.nextInt(3) + 1;
            player.getWorld().dropItemNaturally(block.getLocation(), getCoinStack(randomCoin, randomAmount));
            player.sendMessage(ChatColor.YELLOW + String.format("[!] %s 채굴에 성공했습니다.", randomCoin.displayName));
            issueCoin(randomCoin, randomAmount);
        }
    }

    public ItemStack getCoinStack(Coin coin, int amount) {
        ItemStack itemStack = new ItemStack(coin.material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(coin.displayName);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + CURRENCY_LORE);
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
