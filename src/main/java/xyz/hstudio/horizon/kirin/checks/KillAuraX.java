package xyz.hstudio.horizon.kirin.checks;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.KillAuraData;
import xyz.hstudio.horizon.module.checks.KillAura;

public class KillAuraX extends KillAura {

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        super.doCheck(event, player, data, config);
    }
}