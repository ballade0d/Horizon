package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.List;
import java.util.Set;

public class MoveEvent extends Event {

    public final Location from;
    public final Location to;
    public final Vector3D velocity;
    public final AABB cube;
    public final boolean updatePos;
    public final boolean updateRot;
    public final MoveType moveType;
    public final boolean hitSlowdown;
    public final boolean onGroundReally;
    public final boolean isOnSlime;
    public final boolean isOnBed;
    public final Vector3D waterFlowForce;
    public final boolean isInLiquid1_8;
    public final boolean isInLiquid1_13;
    public final float oldFriction;
    public final float newFriction;
    public final Set<Material> collidingBlocks;
    public final ClientBlock clientBlock;
    public final Set<BlockFace> touchingFaces;
    public final boolean stepLegitly;
    public final boolean jumpLegitly;
    public final boolean strafeNormally;
    public final Vector3D knockBack;
    public boolean onGround;
    public boolean isTeleport;

    public MoveEvent(final HoriPlayer player, final Location to, final boolean onGround, final boolean updatePos, final boolean updateRot, final MoveType moveType) {
        super(player);
        this.from = player.position;
        this.to = to;
        this.onGround = onGround;
        this.velocity = new Vector3D(to.x - from.x, to.y - from.y, to.z - from.z);
        // Get player's bounding box and move it to the update position.
        this.cube = McAccessor.INSTANCE.getCube(player.player).add(this.velocity);
        this.updatePos = updatePos;
        this.updateRot = updateRot;
        this.moveType = moveType;

        this.hitSlowdown = player.currentTick == player.hitSlowdownTick;

        this.onGroundReally = this.to.isOnGround(false, 0.001);

        this.isOnSlime = this.checkSlime();
        this.isOnBed = this.checkBed();

        this.waterFlowForce = this.computeWaterFlowForce();

        this.isInLiquid1_8 = AABB.waterCollisionBox
                .add(this.to.toVector())
                .getMaterials(to.world)
                .stream()
                .anyMatch(MatUtils::isLiquid);
        this.isInLiquid1_13 = AABB.collisionBox
                .shrink(0.001, 0.001, 0.001)
                .add(this.from.toVector())
                .getMaterials(to.world)
                .stream()
                .anyMatch(MatUtils::isLiquid);

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);

        this.clientBlock = this.getClientBlock();
        this.touchingFaces = BlockUtils.checkTouchingBlock(new AABB(to.x - 0.299999, to.y + 0.000001, to.z - 0.299999, to.x + 0.299999, to.y + 1.799999, to.z + 0.299999), to.world, 0.0001);
        this.stepLegitly = this.checkStep();
        this.jumpLegitly = this.checkJump();
        this.strafeNormally = this.checkStrafe();
        this.knockBack = this.checkKnockBack();
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
        float deltaY = (float) this.velocity.y;
        float slimeExpect = (float) (-0.96F * player.prevPrevDeltaY);
        return standing.getType() == MatUtils.SLIME_BLOCK.parse() && !player.isSneaking &&
                player.velocity.y <= 0 && deltaY > 0 && deltaY <= slimeExpect;
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
        float deltaY = (float) this.velocity.y;
        float bedExpect = (float) (-0.62F * player.prevPrevDeltaY);
        return standing.getType().name().contains("BED") && !player.isSneaking &&
                player.velocity.y <= 0 && deltaY > 0 && deltaY <= bedExpect;
    }

    private Vector3D computeWaterFlowForce() {
        Vector3D finalForce = new Vector3D();
        for (Block block : AABB.waterCollisionBox.add(this.to.toVector()).getBlocks(to.world)) {
            if (!MatUtils.isLiquid(block.getType())) {
                continue;
            }
            finalForce.add(McAccessor.INSTANCE.getFlowDirection(block));
        }
        if (finalForce.lengthSquared() > 0) {
            finalForce.normalize();
            finalForce.multiply(0.014);
        }
        return finalForce;
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
     * Get player's client block.
     *
     * @author Islandscout
     */
    private ClientBlock getClientBlock() {
        AABB feet = new AABB(to.toVector().add(new Vector3D(-0.3, -0.02, -0.3)), to.toVector().add(new Vector3D(0.3, 0, 0.3)));
        AABB aboveFeet = feet.add(0, 0.20001, 0);
        AABB cube = new AABB(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
        for (Location loc : player.clientBlocks.keySet()) {
            if (!to.world.equals(loc.world)) {
                continue;
            }
            ClientBlock cBlock = player.clientBlocks.get(loc);
            AABB newAABB = cube.translateTo(loc.toVector());
            if (BlockUtils.isSolid(cBlock.material) && feet.isColliding(newAABB) && !aboveFeet.isColliding(newAABB)) {
                return cBlock;
            }
        }
        return null;
    }

    /**
     * Check if player is stepping up stair/block.
     *
     * @author Islandscout, MrCraftGoo
     */
    private boolean checkStep() {
        Vector3D extraVelocity = player.velocity.clone();
        if (player.onGroundReally) {
            extraVelocity.setY(-0.0784);
        } else {
            extraVelocity.setY((extraVelocity.y - 0.08) * 0.98);
        }
        Location extraPos = player.position.add(extraVelocity);
        float deltaY = (float) this.velocity.y;
        return extraPos.isOnGround(false, 0.001) && onGroundReally && deltaY > 0.002F && deltaY <= 0.6F;
    }

    /**
     * Check if player is jumping.
     *
     * @author Islandscout, MrCraftGoo
     */
    private boolean checkJump() {
        int jumpBoostLvl = player.getPotionEffectAmplifier("JUMP");
        float initJumpVelocity = 0.42F + jumpBoostLvl * 0.1F;
        float deltaY = (float) this.velocity.y;
        boolean hitCeiling = touchingFaces.contains(BlockFace.UP);
        return player.isOnGround && !onGround && (deltaY == initJumpVelocity || hitCeiling);
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
        if (!this.updateRot || player.getVehicle() != null || this.touchingFaces.contains(BlockFace.UP) || !this.collidingBlocks.isEmpty()) {
            return true;
        }
        Block footBlock = player.position.add(0, -1, 0).getBlock();
        if (footBlock == null) {
            return true;
        }
        Vector3D velocity = this.velocity.clone().setY(0);
        Vector3D prevVelocity = player.velocity.clone();
        if (this.hitSlowdown) {
            prevVelocity.multiply(0.6);
        }
        if (MathUtils.abs(prevVelocity.x * this.oldFriction) < 0.005) {
            prevVelocity.setX(0);
        }
        if (MathUtils.abs(prevVelocity.z * this.oldFriction) < 0.005) {
            prevVelocity.setZ(0);
        }
        double dX = velocity.x;
        double dZ = velocity.z;
        dX /= this.oldFriction;
        dZ /= this.oldFriction;
        dX -= prevVelocity.x;
        dZ -= prevVelocity.z;
        Vector3D accelDir = new Vector3D(dX, 0, dZ);
        Vector3D yaw = MathUtils.getDirection(this.to.yaw, 0);

        if (velocity.length() < 0.15 || accelDir.lengthSquared() < 0.000001 || this.jumpLegitly ||
                this.touchingFaces.contains(BlockFace.NORTH) || this.touchingFaces.contains(BlockFace.SOUTH) ||
                this.touchingFaces.contains(BlockFace.WEST) || this.touchingFaces.contains(BlockFace.EAST)) {
            return true;
        }

        boolean vectorDir = accelDir.clone().crossProduct(yaw).dot(new Vector3D(0, 1, 0)) >= 0;
        double angle = (vectorDir ? 1 : -1) * MathUtils.angle(accelDir, yaw);

        double multiple = angle / (Math.PI / 4);
        double threshold = Math.toRadians(0.6);

        return MathUtils.abs(multiple - Math.round(multiple)) <= threshold;
    }

    private Vector3D checkKnockBack() {
        List<Pair<Vector3D, Long>> velocities = player.velocities;
        if (velocities.size() <= 0) {
            return null;
        }
        long time = System.currentTimeMillis();

        int expiredKbs = 0;

        boolean jump = player.isOnGround && MathUtils.abs(0.42 - velocity.y) < 0.00001;
        boolean flying = player.isFlying();

        double sprintMultiplier = flying ? (player.isSprinting ? 2 : 1) : (player.isSprinting ? 1.3 : 1);
        // TODO: Check for liquid
        double weirdConstant = (jump && player.isSprinting ? 0.2518462 : 0.098);
        double baseMultiplier = flying ? (10 * player.player.getFlySpeed()) : (5 * player.player.getWalkSpeed() * (1 + player.getPotionEffectAmplifier("SPEED") * 0.2));
        double maxDiscrepancy = weirdConstant * baseMultiplier * sprintMultiplier + 0.003;

        Pair<Vector3D, Long> kb;
        for (int kbIndex = 0, size = velocities.size(); kbIndex < size; kbIndex++) {
            kb = velocities.get(kbIndex);
            if (time - kb.value > player.ping + 200) {
                expiredKbs++;
                continue;
            }
            Vector3D kbVelocity = kb.key;

            double y = kbVelocity.y;

            // TODO: Check for liquid
            if (!((touchingFaces.contains(BlockFace.UP) && y > 0) || (touchingFaces.contains(BlockFace.DOWN) && y < 0)) &&
                    MathUtils.abs(y - velocity.y) > 0.01 &&
                    !jump && !this.stepLegitly) {
                continue;
            }

            double x = hitSlowdown ? 0.6 * kbVelocity.x : kbVelocity.x;
            double minThresX = x - maxDiscrepancy;
            double maxThresX = x + maxDiscrepancy;
            double z = hitSlowdown ? 0.6 * kbVelocity.z : kbVelocity.z;
            double minThresZ = z - maxDiscrepancy;
            double maxThresZ = z + maxDiscrepancy;

            if (!((touchingFaces.contains(BlockFace.EAST) && x > 0) || (touchingFaces.contains(BlockFace.WEST) && x < 0)) &&
                    !(velocity.x <= maxThresX && velocity.x >= minThresX)) {
                continue;
            }
            if (!((touchingFaces.contains(BlockFace.SOUTH) && z > 0) || (touchingFaces.contains(BlockFace.NORTH) && z < 0)) &&
                    !(velocity.z <= maxThresZ && velocity.z >= minThresZ)) {
                continue;
            }
            velocities.subList(0, kbIndex + 1).clear();
            return kbVelocity;
        }
        velocities.subList(0, expiredKbs).clear();
        return null;
    }

    public boolean hasDeltaPos() {
        return from.x != to.x || from.y != to.y || from.z != to.z;
    }

    public boolean hasDeltaRot() {
        return from.yaw != to.yaw || from.pitch != to.pitch;
    }

    @Override
    public boolean pre() {
        player.currentTick++;
        for (Location loc : player.clientBlocks.keySet()) {
            ClientBlock clientBlock = player.clientBlocks.get(loc);
            if (player.currentTick - clientBlock.initTick > 3) {
                player.clientBlocks.remove(loc);
            }
        }

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
        player.onGroundReally = this.onGroundReally;
        player.friction = this.newFriction;
        player.prevPrevDeltaY = player.velocity.y;
        player.velocity = this.velocity;
        player.touchingFaces = this.touchingFaces;
        player.isInLiquid1_8 = this.isInLiquid1_8;
        player.isInLiquid1_13 = this.isInLiquid1_13;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}