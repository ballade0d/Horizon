package xyz.hstudio.horizon.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;

@RequiredArgsConstructor
public abstract class Event {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;

    public boolean pre() {
        return true;
    }

    public void post() {
    }
}
