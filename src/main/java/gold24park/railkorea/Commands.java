package gold24park.railkorea;

import gold24park.railkorea.command.Home;
import gold24park.railkorea.module.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Commands {
    private Plugin main;

    public Commands(Plugin main) {
        this.main = main;
    }
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("admin")) {
            resolveOpUserCommand(player, label, args);
        }
        resolveNormalUserCommand(player, label, args);
        return false;
    }

    private void resolveOpUserCommand(Player sender, String commandLabel, String[] args) {
        switch (commandLabel) {
            case "coin_event":
                CoinModule.getInstance(main).coinEvent(sender, args);
                break;
            case "coin_info":
                CoinModule.getInstance(main).printCoinInfo(sender);
                break;
            case "issue_coin":
                CoinModule.getInstance(main).issueCoin(sender, args);
                break;
            case "coin":
                CoinModule.getInstance(main).coin(sender, args);
                break;
            case "money":
                MoneyModule.getInstance().money(sender, args);
                break;
            case "register":
                ProfessionModule.getInstance(main).register(sender, args);
                break;
            case "edit_merchant":
                VillagerModule.getInstance(main).openMerchantInventorySettings(sender);
                break;
            case "edit_buyer":
                VillagerModule.getInstance(main).openBuyerInventorySettings(sender);
                break;
        }
    }

    private void resolveNormalUserCommand(Player sender, String commandLabel, String[] args) {
        switch (commandLabel) {
            case "create_buyer":
                VillagerModule.getInstance(main).createBuyer(sender, args);
                break;
            case "create_merchant":
                VillagerModule.getInstance(main).createMerchant(sender, args);
                break;
            case "home":
                Home.run(sender);
                break;
        }
    }
}
