package gold24park.railkorea.model;

import org.bukkit.Material;

public class StoreItem {
    public Material material;
    public int materialAmount;
    public int price;

    public StoreItem(Material material, int materialAmount, int price) {
        this.material = material;
        this.materialAmount = materialAmount;
        this.price = price;
    }
}
