package gold24park.railkorea.model;

import org.bukkit.Material;

public class ShopItem {
    public Material type;
    public int price;
    public int amount;

    public ShopItem(Material type, Integer price, Integer amount) {
        this.type = type;
        this.price = price;
        this.amount = amount;
    }
}
