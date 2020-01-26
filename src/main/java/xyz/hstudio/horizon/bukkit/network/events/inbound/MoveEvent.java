package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.IMcAccessor;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.AABB;
import xyz.hstudio.horizon.bukkit.util.Location;
import xyz.hstudio.horizon.bukkit.util.MatUtils;
import xyz.hstudio.horizon.bukkit.util.MathUtils;

import java.util.Set;

public class MoveEvent extends Event {

    public final Location from;
    public final Location to;
    public final Vector velocity;
    public final AABB cube;
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
    public final Set<Material> collidingBlocks;
    public final boolean strafeNormally;
    public boolean onGround;
    public boolean isTeleport;

    public MoveEvent(final HoriPlayer player, final Location to, final boolean onGround, final boolean updatePos, final boolean updateRot, final MoveType moveType, final WrappedPacket packet) {
        super(player, packet);
        this.from = player.position;
        this.to = to;
        this.velocity = new Vector(to.x - from.x, to.y - from.y, to.z - from.z);
        // Get player's bounding box and move it to the update position.
        this.cube = McAccessor.INSTANCE.getCube(player.player).add(this.velocity);
        this.onGround = onGround;
        this.updatePos = updatePos;
        this.updateRot = updateRot;
        this.moveType = moveType;

        this.hitSlowdown = player.currentTick == player.hitSlowdownTick;

        this.onGroundReally = this.to.isOnGround(false, 0.001);
        this.isUnderBlock = !this.cube.add(0, 1.5, 0, 0, 0.5, 0).isEmpty(to.world);

        this.isOnSlime = this.checkSlime();
        this.isOnBed = this.checkBed();

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);

        this.strafeNormally = this.checkStrafe();
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
                player.prevDeltaY <= 0 && this.velocity.getY() > 0 && this.velocity.getY() <= slimeExpect;
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
                player.prevDeltaY <= 0 && this.velocity.getY() > 0 && this.velocity.getY() <= bedExpect;
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (player.isOnGround) {
            Block b = player.position.add(0, -1, 0).getBlock();
            if (b != null) {
                friction *= McAccessor.INSTANCE.getFriction(b);
            }
        }
        return friction;
    }

    /**
     * Check if player's strafing is normal.
     * I learnt this from Islandscout, but much lighter than his.
     * <p>
     * TODO: Ignore when colliding entities
     * TODO: Ignore the first tick player jump (Unimportant)
     * TODO: Ignore when getting knock back
     *
     * @author Islandscout, MrCraftGoo
     */
    private boolean checkStrafe() {
        if (!this.updateRot || player.getVehicle() != null || this.isUnderBlock || !this.collidingBlocks.isEmpty()) {
            return true;
        }
        Block footBlock = player.position.add(0, -1, 0).getBlock();
        if (footBlock == null) {
            return true;
        }
        Vector velocity = this.velocity.clone().setY(0);
        Vector prevVelocity = player.velocity.clone();
        if (this.hitSlowdown) {
            prevVelocity.multiply(0.6);
        }
        if (MathUtils.abs(prevVelocity.getX() * this.oldFriction) < 0.005) {
            prevVelocity.setX(0);
        }
        if (MathUtils.abs(prevVelocity.getZ() * this.oldFriction) < 0.005) {
            prevVelocity.setZ(0);
        }
        double dX = velocity.getX();
        double dZ = velocity.getZ();
        dX /= this.oldFriction;
        dZ /= this.oldFriction;
        dX -= prevVelocity.getX();
        dZ -= prevVelocity.getZ();
        Vector accelDir = new Vector(dX, 0, dZ);
        Vector yaw = MathUtils.getDirection(this.to.yaw, 0);

        if (velocity.length() < 0.15 || accelDir.lengthSquared() < 0.000001) {
            return true;
        }

        boolean vectorDir = accelDir.clone().crossProduct(yaw).dot(new Vector(0, 1, 0)) >= 0;
        double angle = (vectorDir ? 1 : -1) * MathUtils.angle(accelDir, yaw);

        double multiple = angle / (Math.PI / 4);
        double threshold = Math.toRadians(0.6);

        return MathUtils.abs(multiple - Math.round(multiple)) <= threshold;
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