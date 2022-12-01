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
            case "create_merchant":
                ShopModule.getInstance(main).createCoinMerchant(sender, args);
                break;
            case "trigger_event":
                CoinModule.getInstance(main).triggerCoinEvent(args);
                break;
            case "_reset_coin":
                CoinModule.getInstance(main).resetCoin(sender);
                break;
            case "ban_coin":
                ShopModule.getInstance(main).banCoin(args);
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
            case "register":
                ProfessionModule.getInstance(main).register(sender, args);
                break;
        }
    }

    private void resolveNormalUserCommand(Player sender, String commandLabel, String[] args) {
        switch (commandLabel) {
            case "home":
                Home.run(sender);
                break;
        }
    }
}
