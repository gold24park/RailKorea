package gold24park.railkorea.module;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import gold24park.railkorea.model.WorldTime;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Date;

public class TabListModule {

    private long lastUpdateTime = 0L; // 시간을 업데이트한 마지막 순간

    private static TabListModule instance;
    private final Plugin main;
    private final ProtocolManager protocolManager;

    public static TabListModule getInstance(Plugin main) {
        if (instance == null)
            instance = new TabListModule(main);
        return instance;
    }

    private TabListModule(Plugin main) {
        this.main = main;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void update(Player player, boolean isForceUpdate) {
        long threshold = 10; // 10초마다 시간 업데이트
        long currentTime = new Date().getTime() / 1000;
        if (currentTime - lastUpdateTime > threshold) {
            lastUpdateTime = currentTime;
            isForceUpdate = true;
        }

        if (isForceUpdate) {
            final PacketContainer packetContainer = protocolManager
                    .createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

            long time = player.getWorld().getTime();
            WorldTime worldTime = new WorldTime(time);

            WrappedChatComponent headerChatComponent =
                    WrappedChatComponent.fromText(ChatColor.AQUA + "[" + worldTime.getLabel() + "] " + ChatColor.WHITE + worldTime.getClockTime());
            WrappedChatComponent footerChatComponent =
                    WrappedChatComponent.fromText(ProfessionModule.getInstance(main).getOfflinePlayerList());
            packetContainer.getChatComponents()
                    .write(0, headerChatComponent)
                    .write(1, footerChatComponent);

            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (Exception e) {

            }
        }
    }
}