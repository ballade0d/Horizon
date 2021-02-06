package xyz.hstudio.horizon.kirin;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;

import java.util.List;

@RequiredArgsConstructor
public abstract class ClientCtrl {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;
    public final String channel;
    private final List<String> execution;

    public void send() {
    }
}