package xyz.hstudio.horizon.event;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class Event<T extends Packet<? extends PacketListener>> {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;
    private final Queue<Consumer<T>> consumers = new LinkedList<>();

    public void modify(Consumer<T> consumer) {
        consumers.add(consumer);
    }

    @SuppressWarnings("unchecked")
    public void apply(Object packet) {
        consumers.forEach(consumer -> consumer.accept((T) packet));
    }

    public boolean pre() {
        return true;
    }

    public void post() {
    }
}