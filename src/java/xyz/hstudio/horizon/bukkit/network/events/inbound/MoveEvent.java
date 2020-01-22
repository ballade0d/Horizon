package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.AABB;
import xyz.hstudio.horizon.bukkit.util.Location;
import xyz.hstudio.horizon.bukkit.util.MatUtils;

import java.util.Set;

public class MoveEvent extends Event {

    public final Location from;
    public final Location to;
    public final Vector velocity;
    public final AABB cube;
    public final boolean onGround;
    public final boolean updatePos;
    public final boolean updateRot;
    public final MoveType moveType;
    public final boolean hitSlowdown;
    public final boolean onGroundReally;
    public final boolean isUnderBlock;
    public final boolean isOnSlime;
    public final boolean isOnBed;
    public final float oldFriction;
    public final float newFriction;
    public Set<Material> collidingBlocks;
    public boolean isTeleport;

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
        // TODO: A REAL on ground check.
        this.onGroundReally = this.to.isOnGround(this.cube, false);
        this.isUnderBlock = !this.cube.add(0, 1.5, 0, 0, 0.5, 0).isEmpty(to.world);

        this.isOnSlime = this.checkSlime();
        this.isOnBed = this.checkBed();

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);
    }

    /**
     * Check if player is bouncing on slime block
     *
     * @author Islandscout
     */
    private boolean checkSlime() {
        Block standing = this.from.add(0, -0.01, 0).getBlock();
        if (standing == null) {
            return false;
        }
        double slimeExpect = -0.96 * player.prevPrevDeltaY;
        return standing.getType() == MatUtils.SLIME_BLOCK.parse() && !player.isSneaking &&
                player.prevDeltaY < 0 && this.velocity.getY() > 0 && this.velocity.getY() <= slimeExpect;
    }

    /**
     * Check if player is bouncing on bed
     *
     * @author MrCraftGoo
     */
    private boolean checkBed() {
        Block standing = this.from.add(0, -0.01, 0).getBlock();
        if (standing == null) {
            return false;
        }
        double bedExpect = -0.62F * player.prevPrevDeltaY;
        return standing.getType().name().contains("BED") && !player.isSneaking &&
                player.prevDeltaY < 0 && this.velocity.getY() > 0 && this.velocity.getY() <= bedExpect;
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (player.isOnGround) {
            Block b = player.position.add(0, -1, 0).getBlock();
            if (b != null) {
                friction *= McAccess.getInst().getFriction(b);
            }
        }
        return friction;
    }

    @Override
    public boolean pre() {
        player.currentTick++;

        Location tpLoc = player.teleportPos;
        if (player.isTeleporting && tpLoc.world.equals(this.to.world) && to.distanceSquared(tpLoc) < 0.001) {
            player.isTeleporting = false;
            player.position = tpLoc;
            this.isTeleport = true;
        }
        return true;
    }

    @Override
    public void post() {
        player.position = this.to;
        player.isOnGround = this.onGround;
        player.friction = this.newFriction;
        player.prevPrevDeltaY = player.prevDeltaY;
        player.prevDeltaY = this.velocity.getY();
        player.velocity = this.velocity;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}