package gold24park.railkorea.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class Coin {
    public String displayName;
    public Material material;
    public int amount; // 발행량

    public Coin(String displayName, Material material) {
        this.material = material;
        this.displayName = displayName;
    }

    public static HashMap<String, Coin> coins = new HashMap<String, Coin>();

    public static int getTotalNumberOfIssuedCoins() {
        return coins.values().stream().mapToInt(s -> s.amount).sum();
    }

    public static Coin getCoin(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        Optional<Coin> optionalCoin = coins.values().stream().filter(s -> s.material == itemStack.getType()).findAny();
        Coin coin = null;
        if (optionalCoin.isPresent()) {
            coin = new Coin(optionalCoin.get().displayName, optionalCoin.get().material);
            coin.amount = itemStack.getAmount();
        }
        return coin;
    }


    public static boolean isCoin(ItemStack itemStack) {
        return itemStack.getType() == Material.BLAZE_SPAWN_EGG ||
            itemStack.getType() == Material.GHAST_SPAWN_EGG ||
            itemStack.getType() == Material.VEX_SPAWN_EGG ||
            itemStack.getType() == Material.ZOGLIN_SPAWN_EGG;
    }
}
