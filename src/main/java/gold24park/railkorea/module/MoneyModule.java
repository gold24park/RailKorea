package gold24park.railkorea.module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class MoneyModule {

    public static final String CURRENCY_NAME = "가격";
    public static final String CURRENCY_LORE = "판매상품에 가격 가중치를 부여합니다.";
    private static MoneyModule instance;

    public static MoneyModule getInstance() {
        if (instance == null)
            instance = new MoneyModule();
        return instance;
    }

    public void money(Player player, String[] args) {
        int amount = 1;
        if (args != null && args.length > 0) {
            // 나에게 지급하는 것
            try {
                amount = Integer.parseInt(args[0]);
            } catch (Exception e) {
                amount = 0;
            }
        }

        if (amount > 0) {
            giveMoney(player, amount);
        }
    }

    private void giveMoney(Player player, int amount) {
        PlayerInventory inventory = player.getInventory();
        if (hasAvailableSlot(inventory)) {
            inventory.addItem(getMoneyItemStack(amount));
            Bukkit.broadcastMessage(ChatColor.AQUA + "[" + player.getName() + "]가 " + CURRENCY_NAME + " " + amount + "개를 얻었습니다.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "[!] 먼저 인벤토리를 충분히 확보 해 주세요.");
        }
    }

    private boolean hasAvailableSlot(Inventory inventory){
        for (ItemStack item: inventory.getContents()) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getMoneyItemStack(int amount) {
        ItemStack itemStack = new ItemStack(Material.BARRIER, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(CURRENCY_NAME);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + CURRENCY_LORE);
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
