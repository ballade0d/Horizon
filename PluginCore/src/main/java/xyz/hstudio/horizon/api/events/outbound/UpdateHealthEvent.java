package xyz.hstudio.horizon.api.events.outbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class UpdateHealthEvent extends Event {

    public final float health;
    public final int foodLevel;
    public final float saturation;

    public UpdateHealthEvent(final HoriPlayer player, final float health, final int foodLevel, final float saturation) {
        super(player);
        this.health = health;
        this.foodLevel = foodLevel;
        this.saturation = saturation;
    }

    @Override
    public void post() {
        if (this.foodLevel >= player.foodLevel) {
            player.foodLevel = this.foodLevel;
        } else {
            player.sendSimulatedAction(() -> player.foodLevel = this.foodLevel);
        }
    }
}