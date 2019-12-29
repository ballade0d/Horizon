package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.AxisAlignedBB;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.util.Set;

public class MoveEvent extends Event {

    public final Location from;
    public final Location to;
    public final Vector velocity;
    public final AxisAlignedBB cube;
    public final boolean onGround;
    public final boolean updatePos;
    public final boolean updateRot;
    public final MoveType moveType;
    public final boolean hitSlowdown;
    public final boolean onGroundReally;
    public final boolean isUnderBlock;
    public final float oldFriction;
    public final float newFriction;
    public Set<Material> collidingBlocks;

    public MoveEvent(final HoriPlayer player, final Location to, final boolean onGround, final boolean updatePos, final boolean updateRot, final MoveType moveType, final WrappedPacket packet) {
        super(player, packet);
        this.from = player.position;
        this.to = to;
        this.velocity = new Vector(to.x - from.x, to.y - from.y, to.z - from.z);
        // Get player's bounding box and move it to the update position.
        this.cube = McAccess.getInst().getCube(player.player).add(this.velocity);
        this.onGround = onGround;
        this.updatePos = updatePos;
        this.updateRot = updateRot;
        this.moveType = moveType;

        this.hitSlowdown = player.currentTick == player.hitSlowdownTick;
        // TODO: A real on ground check.
        this.onGroundReally = onGround;
        this.isUnderBlock = !this.cube.add(0, 1.5, 0, 0, 0.5, 0).isEmpty(to.world);

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        // TODO: Probably optimize it? It takes 0.02ms to run.
        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (this.player.isOnGround) {
            Block b = this.player.position.add(0, -1, 0).getBlock();
            if (b != null) {
                friction *= McAccess.getInst().getFriction(b);
            }
        }
        return friction;
    }

    @Override
    public boolean pre() {
        this.player.currentTick++;
        return true;
    }

    @Override
    public void post() {
        this.player.position = this.to;
        this.player.isOnGround = this.onGround;
        this.player.friction = this.newFriction;
        this.player.velocity = this.velocity;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}