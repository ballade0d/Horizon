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
import xyz.hstudio.horizon.bukkit.util.MaterialUtils;

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
    public final boolean isOnSlime;
    public final boolean isOnBed;
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
        // TODO: A REAL on ground check.
        this.onGroundReally = onGround;
        this.isUnderBlock = !this.cube.add(0, 1.5, 0, 0, 0.5, 0).isEmpty(to.world);

        this.isOnSlime = this.checkSlime();
        this.isOnBed = this.checkBed();

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);
    }

    /**
     * Check if player is bouncing on slime
     *
     * @author Islandscout
     */
    private boolean checkSlime() {
        Block standing = this.from.add(0, -0.01, 0).getBlock();
        if (standing == null) {
            return false;
        }
        double slimeExpect = -0.96 * this.player.prevPrevDeltaY;
        return standing.getType() == MaterialUtils.SLIME_BLOCK.parse() && !player.isSneaking &&
                player.prevDeltaY < 0 && this.velocity.getY() > 0 && this.velocity.getY() > (player.prevPrevDeltaY < -0.1F ? slimeExpect - 0.003 : 0) && this.velocity.getY() <= slimeExpect;
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
                player.prevDeltaY < 0 && this.velocity.getY() > 0 && this.velocity.getY() > (player.prevPrevDeltaY < -0.1F ? bedExpect - 0.003 : 0) && this.velocity.getY() <= bedExpect;
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

        this.player.world = this.to.world;

        if (this.player.isGliding && this.onGround) {
            this.player.isGliding = false;
        }
        return true;
    }

    @Override
    public void post() {
        this.player.position = this.to;
        this.player.isOnGround = this.onGround;
        this.player.friction = this.newFriction;
        this.player.prevPrevDeltaY = this.player.prevDeltaY;
        this.player.prevDeltaY = this.velocity.getY();
        this.player.velocity = this.velocity;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}