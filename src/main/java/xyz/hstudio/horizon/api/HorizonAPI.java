package xyz.hstudio.horizon.api;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.enums.Detection;

public enum HorizonAPI {

    INSTANCE;

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    public int getViolation(Player p, Detection detection) {
        Preconditions.checkArgument(p != null, "Player cannot be null.");
        Preconditions.checkArgument(detection != null, "Detection cannot be null.");
        HPlayer hp = inst.getPlayers().get(p.getUniqueId());
        if (hp == null) {
            return 0;
        }
        return hp.getCheckMap().get(detection).getViolation();
    }

    public boolean setViolation(Player p, Detection detection, int violation) {
        Preconditions.checkArgument(p != null, "Player cannot be null.");
        Preconditions.checkArgument(detection != null, "Detection cannot be null.");
        HPlayer hp = inst.getPlayers().get(p.getUniqueId());
        if (hp == null) {
            return false;
        }
        hp.getCheckMap().get(detection).setViolation(violation);
        return true;
    }
}