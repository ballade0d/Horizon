package xyz.hstudio.horizon.bukkit.compat;

import xyz.hstudio.horizon.bukkit.util.Location;

public interface IBot {

    void spawn();

    void updatePing(final int ping);

    void updateHealth(final float health);

    void swing();

    void setSneaking(final boolean sneak);

    void setSprinting(final boolean sprint);

    void move(final Location to, final boolean onGround);
}