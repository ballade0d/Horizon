package xyz.hstudio.horizon.module.check;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.EnumCheckType;
import xyz.hstudio.horizon.event.InEvent;

public class VerticalMovement extends CheckBase {

    public VerticalMovement(HPlayer p, int decayAmount, int decayDelay, int decayInterval) {
        super(p, decayAmount, decayDelay, decayInterval);
    }

    @Override
    public EnumCheckType getType() {
        return EnumCheckType.VERTICAL_MOVEMENT;
    }

    @Override
    public void received(InEvent event) {
    }
}