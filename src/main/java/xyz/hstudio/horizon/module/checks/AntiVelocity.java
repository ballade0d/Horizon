package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;

@LoadFrom("checks/anti_velocity.yml")
public class AntiVelocity extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    public AntiVelocity(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.failedVelocity && !e.teleport && p.bukkit.getVehicle() == null) {
                punish(e, "AntiVelocity (uxzPm)", 1, Detection.ANTI_VELOCITY, null);
            }
        }
    }
}