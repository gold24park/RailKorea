package gold24park.railkorea.module;

import gold24park.railkorea.model.Coin;
import gold24park.railkorea.model.ShopItem;
import gold24park.railkorea.model.ShopItemPrice;
import gold24park.railkorea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ShopModule {
    private final Plugin main;
    private static ShopModule instance;

    private final String SHOP_NAME = "No.1 코인상점 래일원";
    private final String TREASURE = "짱비싼보물";
    private final ArrayList<String> dailyForbiddenCoinNames = new ArrayList<>();

    public static ShopModule getInstance(Plugin main) {
        if (instance == null)
            instance = new ShopModule(main);
        return instance;
    }

    private ShopModule(Plugin main) {
        this.main = main;
    }

    private ItemStack buildShopItem(Material material, int amount, String coinDisplayName, int coinPrice, ChatColor color) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (material == Material.BARRIER) {
            coinPrice = Math.max(64, coinPrice);
            meta.setDisplayName(TREASURE);
        }
        ArrayList<String> lore = new ArrayList<>();
        lore.add("거래코인" + color + ": " + coinDisplayName);
        lore.add(ChatColor.WHITE + "구매 (좌클릭): " + coinPrice);
        if (material != Material.BARRIER) {
            lore.add(ChatColor.WHITE + "판매 (우클릭): " + coinPrice / 2);
        } else {
            lore.add(ChatColor.WHITE + "판매 불가");
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void banCoin(String[] args) {
        if (args.length == 0) {
            dailyForbiddenCoinNames.clear();
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[NEWS] 오늘의 거래금지 코인은 없습니다");
        } else {
            for (String arg : args) {
                Coin targetCoin = Coin.getCoin(arg);
                if (targetCoin != null) {
                    dailyForbiddenCoinNames.add(targetCoin.displayName);
                }
            }
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[NEWS] 오늘의 거래금지 코인: " + String.join(", ", dailyForbiddenCoinNames));
        }
    }

    public void interactEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String villagerName = villager.getCustomName();
            if (villagerName == null) {
                return;
            }

            for (Coin coin : Coin.coins.values()) {
                if (villagerName.startsWith(coin.displayName)) {
                    event.setCancelled(true);
                    openShop(player, coin);
                }
            }
        }
    }

    public void createCoinMerchant(Player player, String[] args) {
        Player targetPlayer = player;
        String coinDisplayName = args[0];

        if (args.length >= 2 && player.hasPermission("admin")) {
            targetPlayer = Util.findPlayer(args[0], player);
            coinDisplayName = args[1];
        }

        Coin coin = Coin.getCoin(coinDisplayName);
        if (coin == null) {
            player.sendMessage(ChatColor.YELLOW + "※ 찾을 수 없는 잡코인입니다.");
            return;
        }

        Villager villager = (Villager) player.getWorld().spawnEntity(
                Util.getLocationInFrontOfPlayer(targetPlayer),
                EntityType.VILLAGER
        );
        villager.setVillagerType(Villager.Type.SWAMP);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(coin.displayName + " 상인");

        player.sendMessage(ChatColor.GREEN + "[!] 새로운 상인이 생성되었습니다.");
    }
    public void openShop(Player player, Coin coin) {
        int numOfPlayer = Bukkit.getWhitelistedPlayers().size();
        Inventory shopInventory = Bukkit.createInventory(
            null, 54, SHOP_NAME
        );

        ShopItem[] shopItems = new ShopItem[] {
            new ShopItem(Material.BARRIER, 100, 1),
            new ShopItem(Material.SEA_LANTERN, 10, 10),
            new ShopItem(Material.QUARTZ_BLOCK, 10, 32),
            new ShopItem(Material.PURPUR_BLOCK, 10, 32),
            new ShopItem(Material.PRISMARINE_BRICKS, 10, 32),
            new ShopItem(Material.END_STONE_BRICKS, 10, 32),
            new ShopItem(Material.END_ROD, 10, 10),
            new ShopItem(Material.DIAMOND, 12, 2),
            new ShopItem(Material.MANGROVE_PROPAGULE, 4, 3),
            new ShopItem(Material.NETHERITE_BLOCK, 30, 1),
            new ShopItem(Material.SPONGE, 20, 2),
            new ShopItem(Material.SEA_PICKLE, 4, 4),
            new ShopItem(Material.GOLD_INGOT, 6, 16),
            new ShopItem(Material.IRON_INGOT, 3, 16),
            new ShopItem(Material.COPPER_INGOT, 2, 32),
        };

        int totalPriceWeight = Arrays.stream(shopItems).mapToInt(s -> s.price).sum();

        if (dailyForbiddenCoinNames.contains(coin.displayName)) {
            player.sendMessage(ChatColor.YELLOW + "※ 거래금지 코인이라 거래할 수 없습니다.");
            return;
        }
        try {
            for (ShopItem shopItem : shopItems) {
                float expect = (float) Coin.coins.get(coin.displayName).amount / numOfPlayer;
                float priceWeight = (float) shopItem.price / totalPriceWeight;
                int price = (int) (expect * priceWeight);
                int calculatedPrice = Math.max(price, 1);

                shopInventory.addItem(buildShopItem(shopItem.type, shopItem.amount, coin.displayName, calculatedPrice, coin.color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.openInventory(shopInventory);
    }

    private boolean isShopInventory(Player player, Inventory inventory) {
        return player.getOpenInventory().getTitle().equals(SHOP_NAME) && inventory != null &&
                inventory.getHolder() == null && inventory.getSize() == 54;
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (!player.getOpenInventory().getTitle().equals(SHOP_NAME)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();

        // 아이템을 못움직이게 한다.
        if (!isShopInventory(player, clickedInventory)) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
            return;
        }

        InventoryAction[] moveActions = {
                InventoryAction.MOVE_TO_OTHER_INVENTORY,
                InventoryAction.PICKUP_SOME,
                InventoryAction.PICKUP_ONE,
                InventoryAction.PLACE_ALL,
                InventoryAction.PLACE_ONE,
                InventoryAction.PLACE_SOME,
        };

        if (Arrays.asList(moveActions).contains(event.getAction())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isRightClick() && !event.isLeftClick()) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) {
            return;
        }

        // 여기서부터는 사고팔려는 마음이있음
        if (event.isRightClick()) {
            sellItem((Player) event.getWhoClicked(), itemStack);
        } else {
            buyItem((Player) event.getWhoClicked(), itemStack);
        }
        event.setCancelled(true);
    }

    private void sellItem(Player player, ItemStack itemStack) {
        if (itemStack.getType() == Material.BARRIER) {
            return;
        }
        ShopItemPrice shopItemPrice = getPrice(itemStack);
        Coin coin = Coin.getCoin(shopItemPrice.coinDisplayName);
        if (coin == null) {
            return;
        }

        int playerAmount = getPlayerItemAmount(player, itemStack.getType());
        if (playerAmount >= itemStack.getAmount()) {
            ItemStack sellItemStack = getNormalItemStack(itemStack);
            player.getInventory().removeItem(sellItemStack);

            // 코인 발행
            CoinModule.getInstance(main).issueCoin(coin, shopItemPrice.priceOnSell);

            ItemStack coinStack = CoinModule.getInstance(main).getCoinStack(coin, shopItemPrice.priceOnSell);
            HashMap<Integer, ItemStack> result2 = player.getInventory().addItem(coinStack);
            ItemStack addFailed = result2.getOrDefault(0, null);
            if (addFailed != null) {
                // 주위에 흩뿌린다.
                player.getWorld().dropItemNaturally(
                        Util.getLocationInFrontOfPlayer(player),
                        addFailed
                );
                player.sendMessage(ChatColor.YELLOW + "※ 인벤토리가 꽉 차 판매 코인 일부가 바닥에 떨어졌습니다.");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "※ 판매할 아이템을 충분히 가지고 있지 않습니다.");
        }
    }

    private void buyItem(Player player, ItemStack itemStack) {
        ShopItemPrice shopItemPrice = getPrice(itemStack);
        Coin coin = Coin.getCoin(shopItemPrice.coinDisplayName);
        if (coin == null) {
            return;
        }
        int balance = getPlayersCoinBalance(player, shopItemPrice.coinDisplayName);
        if (balance >= shopItemPrice.price) {
            // Start Transaction
            ItemStack coinStack = CoinModule.getInstance(main).getCoinStack(coin, shopItemPrice.price);
            HashMap<Integer, ItemStack> result = player.getInventory().removeItem(coinStack);

            ItemStack removeFailed = result.getOrDefault(0, null);
            if (removeFailed != null) {
                int rollbackAmount = shopItemPrice.price - removeFailed.getAmount();
                ItemStack rollbackCoinStack = CoinModule.getInstance(main).getCoinStack(coin, rollbackAmount);
                player.getInventory().addItem(rollbackCoinStack);
                return;
            }

            // 코인 소각
            CoinModule.getInstance(main).issueCoin(coin, -shopItemPrice.price / 2);

            HashMap<Integer, ItemStack> result2 = player.getInventory().addItem(getNormalItemStack(itemStack));
            ItemStack addFailed = result2.getOrDefault(0, null);
            if (addFailed != null) {
                // 주위에 흩뿌린다.
                player.getWorld().dropItemNaturally(
                        Util.getLocationInFrontOfPlayer(player),
                        addFailed
                );
                player.sendMessage(ChatColor.YELLOW + "※ 인벤토리가 꽉 차 구매 아이템 일부가 바닥에 떨어졌습니다.");
            }
        } else {
            // 잔액이 부족합니다.
            String message = "보유: " + balance + ", 필요: " + shopItemPrice.price;
            player.sendMessage(ChatColor.YELLOW + "※ 잔액이 부족합니다! " + message);
        }
    }

    private ItemStack getNormalItemStack(ItemStack itemStack) {
        if (itemStack.getType() != Material.BARRIER) {
            return new ItemStack(itemStack.getType(), itemStack.getAmount());
        } else {
            ItemStack itemStack1 = new ItemStack(itemStack.getType(), itemStack.getAmount());
            ItemMeta meta = itemStack1.getItemMeta();
            meta.setDisplayName(TREASURE);
            itemStack1.setItemMeta(meta);
            return itemStack1;
        }
    }


    private int getPlayerItemAmount(Player player, Material material) {
        int amount = 0;
        try {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack != null && stack.getType() == material) {
                    amount += stack.getAmount();
                }
            }
        } catch (Exception e) {

        }
        return amount;
    }

    private int getPlayersCoinBalance(Player player, String coinDisplayName) {
        int amount = 0;
        try {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                Coin coin = Coin.getCoin(stack);
                if (stack != null && coin != null && stack.getItemMeta().getDisplayName().equals(coinDisplayName)) {
                    amount += stack.getAmount();
                }
            }
        } catch (Exception e) {

        }
        return amount;
    }

    private ShopItemPrice getPrice(ItemStack itemStack) {
        ShopItemPrice shopItemPrice = new ShopItemPrice();
        try {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                return shopItemPrice;
            }

            List<String> lore = meta.getLore();
            if (lore == null || lore.size() < 3) {
                return shopItemPrice;
            }
            shopItemPrice.coinDisplayName = lore.get(0).split(": ")[1];
            shopItemPrice.price = Integer.parseInt(lore.get(1).split(": ")[1]);
            if (itemStack.getType() != Material.BARRIER) {
                shopItemPrice.priceOnSell = Integer.parseInt(lore.get(2).split(": ")[1]);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return shopItemPrice;
    }
}

