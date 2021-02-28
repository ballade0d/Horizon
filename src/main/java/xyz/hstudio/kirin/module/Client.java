package xyz.hstudio.kirin.module;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;

import java.util.List;

@RequiredArgsConstructor
public abstract class Client {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;
    protected final String channel;
    private final List<String> execution;

    public void send() {
    }
}