package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.task.Sync;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.AccessorBase;

public class MoveEvent extends InEvent {

    public final Location to;
    public final boolean onGround;
    public final boolean hasLook;
    public final boolean hasPos;

    public boolean teleport;
    public boolean onGroundReally;
    public Vector3D velocity;

    public MoveEvent(HPlayer p, Location to, boolean onGround, boolean hasLook, boolean hasPos) {
        super(p);
        this.to = to;
        this.onGround = onGround;
        this.hasLook = hasLook;
        this.hasPos = hasPos;
    }

    @Override
    public boolean pre() {
        int ping = AccessorBase.getInst().getPing(p); // TODO: Make it calculated by the plugin

        if (p.status.isTeleporting) {
            Location tpLoc;
            int elapsedTicks;
            if (p.teleports.size() == 0) {
                tpLoc = null;
                elapsedTicks = 0;
            } else {
                Pair<Location, Integer> tpPair = p.teleports.get(0);
                tpLoc = tpPair.getKey();
                elapsedTicks = p.tick.get() - tpPair.getValue();
            }

            if (!onGround && hasPos && hasLook && to.equals(tpLoc)) {
                p.physics.position = tpLoc;
                p.physics.velocity = new Vector3D(0, 0, 0);

                p.teleports.remove(0);

                this.teleport = true;
                if (p.teleports.size() == 0) {
                    p.status.isTeleporting = false;
                } else {
                    return false;
                }
            } else if (!p.bukkit.isSleeping()) {
                if (elapsedTicks > (ping / 50) + 40) {
                    Location tp;
                    if (p.teleports.size() > 0) {
                        tp = p.teleports.get(p.teleports.size() - 1).getKey();
                        p.teleports.clear();
                    } else {
                        tp = p.physics.position;
                    }
                    Sync.teleport(p, tp);
                }
            }
        }
        this.onGroundReally = to.isOnGround(p, false, 0.001);
        this.velocity = new Vector3D(to.x - p.physics.position.x, to.y - p.physics.position.y, to.z - p.physics.position.z);

        return super.pre();
    }

    @Override
    public void post() {
        HPlayer.Physics physics = p.physics;

        physics.position = to;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.velocity = velocity;
        super.post();
    }
}