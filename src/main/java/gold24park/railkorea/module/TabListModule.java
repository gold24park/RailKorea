package gold24park.railkorea.module;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import gold24park.railkorea.model.WorldTime;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class TabListModule {

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

    public void onTimeChanged(World world) {
        final PacketContainer packetContainer = protocolManager
                .createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        WorldTime worldTime = new WorldTime(world.getTime());

        WrappedChatComponent headerChatComponent =
                WrappedChatComponent.fromText(ChatColor.AQUA + "[" + worldTime.getLabel() + "] " + ChatColor.WHITE + worldTime.getClockTime());
        WrappedChatComponent footerChatComponent =
                WrappedChatComponent.fromText(ProfessionModule.getInstance(main).getOfflinePlayerList());
        packetContainer.getChatComponents()
                .write(0, headerChatComponent)
                .write(1, footerChatComponent);

        try {
            protocolManager.broadcastServerPacket(packetContainer);
        } catch (Exception e) {

        }
    }
}