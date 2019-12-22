package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.Location;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class MoveEvent extends Event {

    public final Location from;
    public final Location to;
    public final boolean onGround;
    public final boolean updatePos;
    public final boolean updateRot;
    public final MoveType moveType;

    public final boolean hitSlowdown;
    public final boolean onGroundReally;

    public MoveEvent(final HoriPlayer player, final Location to, final boolean onGround, final boolean updatePos, final boolean updateRot, final MoveType moveType, final WrappedPacket packet) {
        super(player, packet);
        this.from = player.position;
        this.to = to;
        this.onGround = onGround;
        this.updatePos = updatePos;
        this.updateRot = updateRot;
        this.moveType = moveType;

        this.hitSlowdown = player.currentTick == player.hitSlowdownTick;
        this.onGroundReally = onGround;
    }

    @Override
    public boolean pre() {
        this.player.currentTick++;
        return true;
    }

    @Override
    public void post() {
        this.player.position = this.to;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}