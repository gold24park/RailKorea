package gold24park.railkorea;

import gold24park.railkorea.module.LocationModule;
import org.bukkit.plugin.Plugin;

public class Initializer {
    private final Plugin main;

    public Initializer(Plugin main) {
        this.main = main;
        run();
    }

    private void run() {
        LocationModule.getInstance(main).run();
    }
}
