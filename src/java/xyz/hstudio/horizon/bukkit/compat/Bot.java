package xyz.hstudio.horizon.bukkit.compat;

import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.Bot_v1_8_R3;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.util.Location;

public interface Bot {

    static Bot create(final HoriPlayer player, final String name, final Location location) {
        return Bot_v1_8_R3.create(player, name, location);
    }

    void spawn();

    void updatePing(final int ping);

    void updateHealth(final float health);

    void swing();

    void setSneak(final boolean sneak);

    void setSprint(final boolean sprint);

    void move(final Location to, final boolean onGround);
}