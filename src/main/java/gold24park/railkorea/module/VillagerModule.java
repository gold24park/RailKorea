package gold24park.railkorea.module;

import gold24park.railkorea.model.StoreItem;
import gold24park.railkorea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VillagerModule {

    // 판매 아이템, 가격
    public final List<StoreItem> SELLING_ITEM_LIST = new ArrayList<>();
    public final List<StoreItem> BUYING_ITEM_LIST = new ArrayList<>();

    public final String MERCHANT_SETTING_INVENTORY_TITLE = "상인 교환항목 설정: [좌]↔[우]";
    public final String BUYER_SETTING_INVENTORY_TITLE = "매입인 교환항목 설정: [좌]↔[우]";

    public final String MERCHANT_INVENTORY_CONFIG_FILENAME = "merchant_inventory.yml";
    public final String BUYER_INVENTORY_CONFIG_FILENAME = "buyer_inventory.yml";

    public final String MERCHANT_NAME = "상인";
    public final String BUYER_NAME = "매입인";

    private static VillagerModule instance;
    private final Plugin main;


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
        Player targetPlayer = null;
        if (args.length > 0) {
            targetPlayer = Util.findPlayer(args[0], player);
        }
        Villager villager = (Villager) player.getWorld().spawnEntity(
                getLocation(targetPlayer),
                EntityType.VILLAGER
        );
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(MERCHANT_NAME);
        player.sendMessage(ChatColor.GREEN + "[!] 새로운 상인이 생성되었습니다.");

        initializeMerchantInventorySettings(player);
    }

    public void createBuyer(Player player, String[] args) {
        Player targetPlayer = null;
        if (args.length > 0) {
            targetPlayer = Util.findPlayer(args[0], player);
        }
        Villager villager = (Villager) player.getWorld().spawnEntity(
                getLocation(targetPlayer),
                EntityType.VILLAGER
        );
        villager.setVillagerType(Villager.Type.SNOW);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(BUYER_NAME);
        player.sendMessage(ChatColor.GREEN + "[!] 새로운 매입인이 생성되었습니다.");

        initializeBuyerInventorySettings(player);
    }

    // 판매 아이템 기본값
    private void initSellingItems() {
        SELLING_ITEM_LIST.clear();
        SELLING_ITEM_LIST.add(new StoreItem(Material.DIAMOND, 1, 64));
        SELLING_ITEM_LIST.add(new StoreItem(Material.BLAZE_ROD, 1, 9));
        SELLING_ITEM_LIST.add(new StoreItem(Material.LAVA_BUCKET, 1, 5));
        SELLING_ITEM_LIST.add(new StoreItem(Material.SLIME_BALL, 1, 3));
        SELLING_ITEM_LIST.add(new StoreItem(Material.GOLD_INGOT, 1, 2));
        SELLING_ITEM_LIST.add(new StoreItem(Material.IRON_INGOT, 1, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.GLOWSTONE, 4, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.REDSTONE, 5, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.QUARTZ, 8, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.COAL, 9, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.SAND, 12, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.GRAVEL, 12, 1));
        SELLING_ITEM_LIST.add(new StoreItem(Material.CLAY_BALL, 16, 1));
    }

    private void initBuyingItems() {
        BUYING_ITEM_LIST.clear();
        BUYING_ITEM_LIST.add(new StoreItem(Material.DIAMOND, 1, 32));
        BUYING_ITEM_LIST.add(new StoreItem(Material.EMERALD, 1, 6));
        BUYING_ITEM_LIST.add(new StoreItem(Material.BUCKET, 1, 3));
        BUYING_ITEM_LIST.add(new StoreItem(Material.WHEAT, 10, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.CARROT, 6, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.POTATO, 6, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.POISONOUS_POTATO, 2, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.REDSTONE, 10, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.PUFFERFISH, 1, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.TROPICAL_FISH, 1, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.PHANTOM_MEMBRANE, 1, 4));
        BUYING_ITEM_LIST.add(new StoreItem(Material.ROTTEN_FLESH, 3, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.SPIDER_EYE, 2, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.GUNPOWDER, 1, 2));
        BUYING_ITEM_LIST.add(new StoreItem(Material.SLIME_BALL, 1, 3));
        BUYING_ITEM_LIST.add(new StoreItem(Material.FEATHER, 3, 1));
        BUYING_ITEM_LIST.add(new StoreItem(Material.NAUTILUS_SHELL, 1, 3));
        BUYING_ITEM_LIST.add(new StoreItem(Material.EGG, 3, 1));
    }

    public List<MerchantRecipe> getMerchantRecipes(Player player) {
        return getRecipes(player, true);
    }

    public List<MerchantRecipe> getBuyerRecipes(Player player) {
        return getRecipes(player, false);
    }

    private List<MerchantRecipe> getRecipes(Player player, boolean isMerchant) {
        String title = isMerchant ? MERCHANT_SETTING_INVENTORY_TITLE : BUYER_SETTING_INVENTORY_TITLE;
        String fileName = isMerchant ? MERCHANT_INVENTORY_CONFIG_FILENAME : BUYER_INVENTORY_CONFIG_FILENAME;

        List<MerchantRecipe> recipeList = new ArrayList<>();
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, title);
        restoreInventory(main, inventory, fileName);
        MerchantRecipe recipe = null;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            boolean isMiddleSpacing = (i - 4) % 9 == 0;
            if (item != null && !isMiddleSpacing) {
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
        return recipeList;
    }

    public boolean isMerchant(String villagerName) {
        return villagerName != null && villagerName.equalsIgnoreCase(MERCHANT_NAME);
    }

    public boolean isBuyer(String villagerName) {
        return villagerName != null && villagerName.equalsIgnoreCase(BUYER_NAME);
    }

    /**
     * 상인이 생성될 위치를 반환, 플레이어 바로 앞에 생성
     * @param player
     * @return location
     */
    private Location getLocation(Player player) {
        Location location = null;
        Location playerLocation = player.getLocation();
        Vector direction = player.getLocation().getDirection();
        playerLocation = player.getLocation().add(direction); // 플레이어 앞
        location = new Location(player.getWorld(),
                playerLocation.getX(),
                playerLocation.getY() + 2,
                playerLocation.getZ());
        return location;
    }

    public void openMerchantInventorySettings(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, MERCHANT_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, MERCHANT_INVENTORY_CONFIG_FILENAME);
        player.openInventory(inventory);
    }

    private void initializeMerchantInventorySettings(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, MERCHANT_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, MERCHANT_INVENTORY_CONFIG_FILENAME);
        if (Util.isEmpty(inventory)) {
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
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, BUYER_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, BUYER_INVENTORY_CONFIG_FILENAME);
        player.openInventory(inventory);
    }

    private void initializeBuyerInventorySettings(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(player, 54, BUYER_SETTING_INVENTORY_TITLE);
        inventory = restoreInventory(main, inventory, BUYER_INVENTORY_CONFIG_FILENAME);
        if (Util.isEmpty(inventory)) {
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
        merchant.setRecipes(
                isMerchant ?
                        getMerchantRecipes(player) :
                        getBuyerRecipes(player));
        player.openMerchant(merchant, true);
    }

    public void onInventoryClosed(InventoryCloseEvent event) {
        // 상인 및 매입인 세팅 인벤토리를 닫을때는 내용을 저장한다.
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.equalsIgnoreCase(MERCHANT_SETTING_INVENTORY_TITLE)) {
            saveInventory(main, event.getInventory(), MERCHANT_INVENTORY_CONFIG_FILENAME);
            event.getPlayer().sendMessage(ChatColor.GREEN + "[!] 판매 항목 변경사항이 저장되었습니다.");
        }
        else if (inventoryTitle.equalsIgnoreCase(BUYER_SETTING_INVENTORY_TITLE)) {
            saveInventory(main, event.getInventory(), BUYER_INVENTORY_CONFIG_FILENAME);
            event.getPlayer().sendMessage(ChatColor.GREEN + "[!] 매입 항목 변경사항이 저장되었습니다.");
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
