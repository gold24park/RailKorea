package gold24park.railkorea.module;

import gold24park.railkorea.model.Coin;
import gold24park.railkorea.model.StoreItem;
import gold24park.railkorea.util.Util;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VillagerModule {

    // 판매 아이템, 가격
    public final List<StoreItem> SELLING_ITEM_LIST = new ArrayList<>();
    public final List<StoreItem> BUYING_ITEM_LIST = new ArrayList<>();

    public final String MERCHANT_SETTING_INVENTORY_TITLE = "상인설정: [좌(아이템)]↔[우(가격)]";
    public final String BUYER_SETTING_INVENTORY_TITLE = "매입인설정: [좌(가격)]↔[우(아이템)]";

    public final String MERCHANT_INVENTORY_CONFIG_FILENAME = "merchant_inventory.yml";
    public final String BUYER_INVENTORY_CONFIG_FILENAME = "buyer_inventory.yml";

    public final String MERCHANT_NAME = "상점 주인";
    public final String BUYER_NAME = "전당포 주인";

    private static VillagerModule instance;
    private final Plugin main;

    private final ArrayList<String> dailyForbiddenCoinNames = new ArrayList<>();


    public static VillagerModule getInstance(Plugin main) {
        if (instance == null)
            instance = new VillagerModule(main);
        return instance;
    }

    private VillagerModule(Plugin main) {
        this.main = main;
        initSellingItems();
        initBuyingItems();
    }

    public void createMerchant(Player player, String[] args) {
        Player targetPlayer = player;
        if (args.length > 0 && player.hasPermission("admin")) {
            targetPlayer = Util.findPlayer(args[0], player);
        }
        Villager villager = (Villager) player.getWorld().spawnEntity(
                Util.getLocationInFrontOfPlayer(targetPlayer),
                EntityType.VILLAGER
        );
        villager.setVillagerType(Villager.Type.SWAMP);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(MERCHANT_NAME);
        player.sendMessage(ChatColor.GREEN + "[!] 새로운 상인이 생성되었습니다.");

        initializeMerchantInventorySettings(player);
    }

    public void createBuyer(Player player, String[] args) {
        Player targetPlayer = player;
        if (args.length > 0 && player.hasPermission("admin")) {
            targetPlayer = Util.findPlayer(args[0], player);
        }
        Villager villager = (Villager) player.getWorld().spawnEntity(
                Util.getLocationInFrontOfPlayer(targetPlayer),
                EntityType.VILLAGER
        );
        villager.setVillagerType(Villager.Type.SAVANNA);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(BUYER_NAME);
        player.sendMessage(ChatColor.GREEN + "[!] 새로운 매입인이 생성되었습니다.");

        initializeBuyerInventorySettings(player);
    }

    // 판매 아이템 기본값
    private void initSellingItems() {
        SELLING_ITEM_LIST.clear();
        SELLING_ITEM_LIST.add(new StoreItem(Material.DIAMOND, 1, 10));
        SELLING_ITEM_LIST.add(new StoreItem(Material.EMERALD, 1, 4));
    }

    private void initBuyingItems() {
        BUYING_ITEM_LIST.clear();
        BUYING_ITEM_LIST.add(new StoreItem(Material.DIAMOND, 1, 5));
        BUYING_ITEM_LIST.add(new StoreItem(Material.EMERALD, 1, 2));
    }

    public void banCoin(String[] args) {
        if (args.length == 0) {
            dailyForbiddenCoinNames.clear();
            Bukkit.broadcastMessage(ChatColor.AQUA + "[NEWS] 오늘의 거래금지 코인은 없습니다");
        } else {
            for (String arg : args) {
                Coin targetCoin = Coin.getCoin(arg);
                if (targetCoin != null) {
                    dailyForbiddenCoinNames.add(targetCoin.displayName);
                }
            }
            Bukkit.broadcastMessage(ChatColor.AQUA + "[NEWS] 오늘의 거래금지 코인: " + String.join(", ", dailyForbiddenCoinNames));
        }
    }

    private List<MerchantRecipe> getRecipes(Player player, boolean isMerchant) {
        String title = isMerchant ? MERCHANT_SETTING_INVENTORY_TITLE : BUYER_SETTING_INVENTORY_TITLE;
        String fileName = isMerchant ? MERCHANT_INVENTORY_CONFIG_FILENAME : BUYER_INVENTORY_CONFIG_FILENAME;

        List<MerchantRecipe> recipeList = new ArrayList<>();
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, title);
        restoreInventory(main, inventory, fileName);
        MerchantRecipe recipe = null;


        int totalPriceWeight = 0;

        for (int i = 0; i < inventory.getSize(); i += 1) {
            ItemStack item = inventory.getItem(i);
            boolean isMiddleSpacing = (i - 4) % 9 == 0;
            if (item != null && !isMiddleSpacing) {
                // 시세별 가격설정
                if (item.getType() == Material.BARRIER) {
                    totalPriceWeight += item.getAmount();
                }
            }
        }

        for (Coin coin : Coin.coins.values()) {
            if (dailyForbiddenCoinNames.contains(coin.displayName)) {
                continue;
            }
            for (int i = 0; i < inventory.getSize(); i += 1) {
                ItemStack item = inventory.getItem(i);
                boolean isMiddleSpacing = (i - 4) % 9 == 0;
                if (item != null && !isMiddleSpacing && item.getType() != Material.AIR) {
                    // 시세별 가격설정
                    if (item.getType() == Material.BARRIER) {
                        float priceWeight = (float) item.getAmount() / totalPriceWeight;
                        item = CoinModule.getInstance(main).getCoinStack(
                            coin, Math.max((int) (priceWeight * Coin.coins.get(coin.displayName).amount), 1)
                        );
                    }
                    // 가운데 줄 이후로는 바뀜
                    if (recipe == null) {
                        recipe = new MerchantRecipe(item, 99999);
                    } else {
                        recipe.addIngredient(item);
                        recipeList.add(recipe);
                        recipe = null;
                    }
                }
            }
        }
        return recipeList;
    }

    public boolean isMerchant(String villagerName) {
        return villagerName != null && villagerName.equalsIgnoreCase(MERCHANT_NAME);
    }

    public boolean isBuyer(String villagerName) {
        return villagerName != null && villagerName.equalsIgnoreCase(BUYER_NAME);
    }

    public void openMerchantInventorySettings(Player player) {
        initializeBuyerInventorySettings(player);

        Inventory inventory = Bukkit.getServer().createInventory(player, 54, MERCHANT_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, MERCHANT_INVENTORY_CONFIG_FILENAME);
        player.openInventory(inventory);
    }

    private void initializeMerchantInventorySettings(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, MERCHANT_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, MERCHANT_INVENTORY_CONFIG_FILENAME);
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) {
                count++;
            }
        }
        if (count == 0) {
            player.sendMessage(ChatColor.GRAY + "[!] 판매항목이 초기화 되었습니다.");
            // 초기화 시켜주기
            int cursor = 0;
            for (StoreItem storeItem : SELLING_ITEM_LIST) {
                // 판매 아이템 설정
                ItemStack itemStack = new ItemStack(storeItem.material, storeItem.materialAmount);
                inventory.setItem(cursor, itemStack);
                cursor++;
                // 판매 가격 설정
                ItemStack moneyStack = MoneyModule.getInstance().getMoneyItemStack(storeItem.price);
                inventory.setItem(cursor, moneyStack);
                cursor++;
                if ((cursor - 4) % 9 == 0) {
                    // 막기 블록 설정
                    inventory.setItem(cursor, getSpacingItemStack(Material.RED_STAINED_GLASS_PANE));
                    cursor++;
                }
            }
            // 나머지 공간도 막기 블록 채워주기
            while (cursor < inventory.getSize()) {
                if ((cursor - 4) % 9 == 0) {
                    inventory.setItem(cursor, getSpacingItemStack(Material.RED_STAINED_GLASS_PANE));
                }
                cursor++;
            }
            saveInventory(main, inventory, MERCHANT_INVENTORY_CONFIG_FILENAME);
        }
    }

    public void openBuyerInventorySettings(Player player) {
        initializeBuyerInventorySettings(player);

        Inventory inventory = Bukkit.getServer().createInventory(player, 54, BUYER_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, BUYER_INVENTORY_CONFIG_FILENAME);
        player.openInventory(inventory);
    }

    private void initializeBuyerInventorySettings(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, BUYER_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, BUYER_INVENTORY_CONFIG_FILENAME);
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) {
                count++;
            }
        }
        if (count == 0) {
            player.sendMessage(ChatColor.GRAY + "[!] 매입항목이 초기화 되었습니다.");
            // 초기화 시켜주기
            int cursor = 0;
            for (StoreItem storeItem : SELLING_ITEM_LIST) {
                // 판매 가격 설정
                ItemStack moneyStack = MoneyModule.getInstance().getMoneyItemStack(storeItem.price);
                inventory.setItem(cursor, moneyStack);
                cursor++;
                // 판매 아이템 설정
                ItemStack itemStack = new ItemStack(storeItem.material, storeItem.materialAmount);
                inventory.setItem(cursor, itemStack);
                cursor++;
                if ((cursor - 4) % 9 == 0) {
                    // 막기 블록 설정
                    inventory.setItem(cursor, getSpacingItemStack(Material.BLUE_STAINED_GLASS_PANE));
                    cursor++;
                }
            }
            // 나머지 공간도 막기 블록 채워주기
            while (cursor < inventory.getSize()) {
                if ((cursor - 4) % 9 == 0) {
                    inventory.setItem(cursor, getSpacingItemStack(Material.BLUE_STAINED_GLASS_PANE));
                }
                cursor++;
            }
            saveInventory(main, inventory, BUYER_INVENTORY_CONFIG_FILENAME);
        }
    }

    public void openTradingGUI(Player player, boolean isMerchant) {
        String storeName = main.getConfig().getString(isMerchant ?
                "store_name.merchant" : "store_name.buyer");
        Merchant merchant = Bukkit.createMerchant(storeName);
        merchant.setRecipes(getRecipes(player, isMerchant));
        player.openMerchant(merchant, true);
    }

    public void onInventoryClosed(InventoryCloseEvent event) {
        // 상인 및 매입인 세팅 인벤토리를 닫을때는 내용을 저장한다.
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.equalsIgnoreCase(MERCHANT_SETTING_INVENTORY_TITLE)) {
            saveInventory(main, event.getInventory(), MERCHANT_INVENTORY_CONFIG_FILENAME);
            event.getPlayer().sendMessage(ChatColor.GREEN + "[!] 상인 항목 변경사항이 저장되었습니다.");
        }
        else if (inventoryTitle.equalsIgnoreCase(BUYER_SETTING_INVENTORY_TITLE)) {
            saveInventory(main, event.getInventory(), BUYER_INVENTORY_CONFIG_FILENAME);
            event.getPlayer().sendMessage(ChatColor.GREEN + "[!] 매입인 항목 변경사항이 저장되었습니다.");
        }
    }

    private ItemStack getSpacingItemStack(Material material) {
        ItemStack spacingStack = new ItemStack(material, 1);
        ItemMeta spacingMeta = spacingStack.getItemMeta();
        spacingMeta.setDisplayName("이동금지");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("아이템 간격을 유지하기 위한 장치. 손 대지마세요.");
        lore.add("좌측상단부터 (x,y) 아이템과 (x+1,y) 아이템을 교환합니다.");
        spacingMeta.setLore(lore);
        spacingStack.setItemMeta(spacingMeta);
        return spacingStack;
    }

    // 인벤토리 yml 파일에 저장
    private void saveInventory(Plugin plugin, Inventory inventory, String fileName) {
        try {
            File file = new File(plugin.getDataFolder().getAbsolutePath(), fileName);
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            configuration.set("inventory.content", inventory.getContents());
            configuration.save(file);
        } catch (Exception e) {

        }
    }

    // yml 파일에서 인벤토리 복구
    private Inventory restoreInventory(Plugin plugin, Inventory inventory, String fileName) {
        try {
            File file = new File(plugin.getDataFolder().getAbsolutePath(), fileName);
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            ItemStack[] content = ((List<ItemStack>) configuration.get("inventory.content")).toArray(new ItemStack[0]);
            inventory.setContents(content);
        } catch (Exception e) {

        }
        return inventory;
    }
}
