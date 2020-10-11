package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;

@RequiredArgsConstructor
@Getter
public class MoveEvent extends InEvent {

    private final Location to;
    private final boolean onGround;
    private final boolean hasLook;
    private final boolean hasPos;

    private boolean onGroundReally;
    private Vector3D velocity;

    @Override
    public boolean pre(HPlayer p) {
        this.onGroundReally = to.isOnGround(p, false, 0.001);
        this.velocity = new Vector3D(to.getX() - p.physics().position.getX(), to.getY() - p.physics().position.getY(), to.getZ() - p.physics().position.getZ());

        return super.pre(p);
    }

    @Override
    public void post(HPlayer p) {
        HPlayer.Physics physics = p.physics();

        physics.position = to;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.velocity = velocity;
        super.post(p);
    }
}