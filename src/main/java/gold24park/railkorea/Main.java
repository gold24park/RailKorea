package gold24park.railkorea;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

    private Commands commands;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        new EventListener(this);
        new Initializer(this);
        commands = new Commands(this);

        getLogger().info("래하래하 플러그인 활성화 완료! :)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commands.onCommand(sender, label, args);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("래바래바 플러그인을 종료합니다~");
    }


}
