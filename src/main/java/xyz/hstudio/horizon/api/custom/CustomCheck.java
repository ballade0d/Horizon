package xyz.hstudio.horizon.api.custom;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

import java.util.HashMap;
import java.util.Map;

public abstract class CustomCheck<T extends CustomConfig> {

    public final T config;
    private final float rewardMultiplier;
    private final Map<HoriPlayer, Float> vlMap;

    public CustomCheck(final T config, final float rewardMultiplier) {
        this.config = config;
        this.rewardMultiplier = rewardMultiplier;
        this.vlMap = new HashMap<>();
    }

    public void reward(final HoriPlayer player) {
        this.vlMap.computeIfPresent(player, (k, v) -> v * this.rewardMultiplier);
    }

    public abstract void doCheck(final Event event, final HoriPlayer player, final T config);
}