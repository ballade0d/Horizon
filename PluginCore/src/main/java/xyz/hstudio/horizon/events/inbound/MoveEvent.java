package xyz.hstudio.horizon.events.inbound;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.List;
import java.util.Set;

public class MoveEvent extends Event {

    private static final double STRAFE_THRESHOLD = Math.toRadians(0.5);
    private static final String[] ARGS = new String[]{
            "%y_speed%",
            "%xz_speed%",
            "%tick%",
            "%c_ground%",
            "%s_ground%"
    };

    public final Location from;
    public final Location to;
    public final Vector3D velocity;
    public final AABB cube;
    public final boolean updatePos;
    public final boolean updateRot;
    public final MoveType moveType;
    public final boolean hitSlowdown;
    public final boolean onGroundReally;
    public final boolean isCollidingEntities;
    public final boolean isOnSlime;
    public final boolean isOnSlimeNext;
    public final boolean isOnBed;
    public final Vector3D waterFlowForce;
    public final boolean isInWater;
    public final boolean isInLiquid;
    public final float oldFriction;
    public final float newFriction;
    public final boolean piston;
    public final Set<Material> touchedBlocks;
    public final Set<Material> collidingBlocks;
    public final Set<BlockFace> touchingFaces;
    public final boolean stepLegitly;
    public final Vector3D knockBack;
    public final boolean jumpLegitly;
    public final boolean strafeNormally;
    public final long clientBlock;
    public boolean failedKnockBack;
    public boolean onGround;
    public boolean isTeleport;

    public MoveEvent(final HoriPlayer player, final Location to, final boolean onGround, final boolean updatePos, final boolean updateRot, final MoveType moveType, final Object rawPacket) {
        super(player, rawPacket);
        this.from = player.position;
        this.to = to;
        this.onGround = onGround;
        this.velocity = new Vector3D(to.x - from.x, to.y - from.y, to.z - from.z);
        // Get player's bounding box and move it to the update position.
        AABB originCube = McAccessor.INSTANCE.getCube(player.getPlayer());
        this.cube = originCube.add(this.velocity);
        this.updatePos = updatePos;
        this.updateRot = updateRot;
        this.moveType = moveType;

        this.hitSlowdown = player.currentTick == player.hitSlowdownTick;

        this.onGroundReally = this.to.isOnGround(player, false, 0.001);

        this.isCollidingEntities = McAccessor.INSTANCE.isCollidingEntities(to.world, player.getPlayer(), cube);

        this.isOnSlime = this.checkSlime();
        this.isOnSlimeNext = this.checkSlimeNext();
        this.isOnBed = this.checkBed();

        Pair<Vector3D, Boolean> pair = this.computeWaterFlowForce();
        this.waterFlowForce = pair.key;
        this.isInWater = pair.value;

        this.isInLiquid = AABB.NORMAL_BOX
                .add(this.from.toVector())
                .getMaterials(to.world)
                .stream()
                .anyMatch(m -> MatUtils.WATER.contains(m) || MatUtils.LAVA.contains(m));

        this.oldFriction = player.friction;
        this.newFriction = this.computeFriction();

        this.piston = this.checkBlock();

        this.touchedBlocks = originCube.add(from.toVector().subtract(player.getPlayer().getLocation().toVector())).getMaterials(to.world);
        // This will only get the blocks that are colliding horizontally.
        this.collidingBlocks = this.cube.add(-0.0001, 0.0001, -0.0001, 0.0001, 0, 0.0001).getMaterials(to.world);

        this.touchingFaces = BlockUtils.checkTouchingBlock(player, new AABB(to.x - 0.299999, to.y + 0.000001, to.z - 0.299999, to.x + 0.299999, to.y + 1.799999, to.z + 0.299999), to.world, 0.0001);
        this.stepLegitly = this.checkStep();
        this.knockBack = this.checkKnockBack();
        this.jumpLegitly = this.checkJump();
        this.strafeNormally = this.checkStrafe();
        this.clientBlock = this.getClientBlock();
    }

    private boolean checkBlock() {
        Set<AABB> piston = player.piston;
        AABB cube = this.cube.add(-velocity.x, -velocity.y, -velocity.z).expand(0.1, 0.1, 0.1);
        piston.removeIf(aabb -> !cube.isColliding(aabb));
        return !piston.isEmpty();
    }

    /**
     * Check if player is bouncing on slime block
     *
     * @author Islandscout
     */
    private boolean checkSlime() {
        IWrappedBlock standingOn = this.from.add(0, -0.2, 0).getBlock();
        if (standingOn == null || standingOn.getType() != Material.SLIME_BLOCK) {
            return false;
        }
        float deltaY = (float) this.velocity.y;
        float slimeExpect = (float) ((-((player.prevPrevDeltaY - 0.08F) * 0.98F) - 0.08F) * 0.98F);
        return !player.isSneaking && player.onGround && !onGround && deltaY >= 0 && Math.abs(slimeExpect - deltaY) < 0.00001;
    }

    private boolean checkSlimeNext() {
        IWrappedBlock standingOn = this.to.add(0, -0.2, 0).getBlock();
        if (standingOn == null || standingOn.getType() != Material.SLIME_BLOCK) {
            return false;
        }
        float prevDeltaY = (float) player.velocity.y;
        return !player.isSneaking && onGround && !player.onGround && prevDeltaY < 0;
    }

    /**
     * Check if player is bouncing on bed
     *
     * @author MrCraftGoo
     */
    private boolean checkBed() {
        IWrappedBlock standing = this.from.add(0, -0.01, 0).getBlock();
        if (standing == null) {
            return false;
        }
        float deltaY = (float) this.velocity.y;
        float bedExpect = (float) (-0.66F * player.prevPrevDeltaY);
        return standing.getType().name().contains("BED") && !player.isSneaking &&
                player.velocity.y <= 0 && deltaY > 0 && deltaY <= bedExpect;
    }

    private Pair<Vector3D, Boolean> computeWaterFlowForce() {
        Vector3D finalForce = new Vector3D();
        boolean inLiquid = false;
        for (IWrappedBlock block : AABB.WATER_BOX.add(this.to.toVector()).getBlocks(to.world)) {
            if (!MatUtils.WATER.contains(block.getType())) {
                continue;
            }
            finalForce.add(block.getFlowDirection());
            inLiquid = true;
        }
        if (finalForce.lengthSquared() > 0) {
            finalForce.normalize();
            finalForce.multiply(0.014);
        }
        return new Pair<>(finalForce, inLiquid);
    }

    private float computeFriction() {
        boolean flying = player.isFlying();
        if (player.isInWater && flying) {
            float friction = 0.8F;
            float depthStrider = player.getEnchantmentEffectAmplifier("DEPTH_STRIDER");
            if (depthStrider > 3) {
                depthStrider = 3;
            }
            if (!onGround) {
                depthStrider *= 0.5F;
            }
            if (depthStrider > 0) {
                friction += (0.54600006F - friction) * depthStrider / 3F;
            }
            return friction;
        } else if (player.isInLava && !flying) {
            return 0.5F;
        } else {
            float friction = 0.91F;
            if (player.onGround) {
                IWrappedBlock b = player.position.add(0, -1, 0).getBlock();
                if (b != null) {
                    friction *= b.getFriction();
                }
            }
            return friction;
        }
    }

    private long getClientBlock() {
        AABB feet = new AABB(to.toVector().add(new Vector3D(-0.3, -0.02, -0.3)), to.toVector().add(new Vector3D(0.3, 0, 0.3)));
        AABB aboveFeet = feet.add(0, 0.20001, 0);
        AABB cube = new AABB(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
        for (Location loc : player.clientBlocks.keySet()) {
            if (!to.world.equals(loc.world)) {
                continue;
            }
            ClientBlock cBlock = player.clientBlocks.get(loc);
            AABB newAABB = cube.translateTo(loc.toVector());
            if (feet.isColliding(newAABB) && !aboveFeet.isColliding(newAABB) && BlockUtils.isSolid(cBlock.material)) {
                boolean tower = player.onGround && !onGround && player.velocity.y == 0 && (Math.abs(velocity.y - 0.4044449) < 0.001 || Math.abs(velocity.y - 0.3955759) < 0.001);
                if (!loc.equals(player.prevClientBlock) && (velocity.y == 0 || tower || jumpLegitly)) {
                    player.clientBlockCount++;
                }
                player.prevClientBlock = loc;
                return cBlock.initTick;
            }
        }
        return -1;
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
        return extraPos.isOnGround(player, false, 0.001) && onGroundReally && deltaY > 0.002F && deltaY <= 0.6F;
    }

    private Vector3D checkKnockBack() {
        final List<Pair<Vector3D, Long[]>> velocities = player.velocities;
        if (velocities.size() <= 0) {
            return null;
        }
        long time = System.currentTimeMillis();
        int ping = McAccessor.INSTANCE.getPing(player.getPlayer());

        int expiredKbs = 0;

        boolean jump = player.onGround && Math.abs(0.42 - velocity.y) < 0.00001;
        boolean flying = player.isFlying();

        double sprintMultiplier = flying ? (player.isSprinting ? 2 : 1) : (player.isSprinting ? 1.3 : 1);
        double weirdConstant = (jump && player.isSprinting ? 0.2518462 : (player.isInWater ? 0.0196 : 0.098));
        double baseMultiplier = flying ? (10 * player.getPlayer().getFlySpeed()) : (5 * player.getPlayer().getWalkSpeed() * (1 + player.getPotionEffectAmplifier("SPEED") * 0.2));
        double maxDiscrepancy = weirdConstant * baseMultiplier * sprintMultiplier + 0.003;

        Pair<Vector3D, Long[]> kb;
        for (int kbIndex = 0, size = velocities.size(); kbIndex < size; kbIndex++) {
            kb = velocities.get(kbIndex);

            long timeDiff = time - kb.value[0];
            long tickDiff = player.currentTick - kb.value[1];
            if (timeDiff > ping + 200 && tickDiff > (ping + 200) / 50) {
                failedKnockBack = true;
                expiredKbs++;
                continue;
            }
            Vector3D kbVelocity = kb.key;
            if (!collidingBlocks.contains(MatUtils.COBWEB.parse()) && !collidingBlocks.contains(Material.LADDER) && !collidingBlocks.contains(Material.VINE) && !piston) {
                double y = kbVelocity.y;

                if (!((touchingFaces.contains(BlockFace.UP) && y > 0) || (touchingFaces.contains(BlockFace.DOWN) && y < 0)) &&
                        Math.abs(y - velocity.y) > 0.01 &&
                        !jump && !player.isInWater && !this.stepLegitly) {
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
            } else {
                velocities.subList(0, kbIndex + 1).clear();
                return kbVelocity;
            }
        }
        velocities.subList(0, expiredKbs).clear();
        return null;
    }

    /**
     * Check if player is jumping.
     *
     * @author Islandscout, MrCraftGoo
     */
    private boolean checkJump() {
        float initJumpVelocity = 0.42F + player.getPotionEffectAmplifier("JUMP") * 0.1F;
        float deltaY = (float) this.velocity.y;

        AABB collisionBox = new AABB(from.x - 0.299999, to.y + 0.000001, from.z - 0.299999, from.x + 0.299999, to.y + 1.799999, from.z + 0.299999);
        boolean hitCeiling = BlockUtils.checkTouchingBlock(player, collisionBox, to.world, 0.0001).contains(BlockFace.UP);

        boolean kbSimilarToJump = this.knockBack != null && (Math.abs(knockBack.y - initJumpVelocity) < 0.001 || hitCeiling);
        boolean leftGround = player.onGround && !this.onGround;
        return !kbSimilarToJump && ((initJumpVelocity == 0 && player.onGround) || leftGround) && (deltaY == initJumpVelocity || hitCeiling);
    }

    /**
     * Check if player's strafing is normal.
     * I learnt this from Islandscout, but much lighter than his.
     *
     * @author Islandscout, MrCraftGoo
     */
    private boolean checkStrafe() {
        if (!this.updateRot || this.knockBack != null || this.jumpLegitly || this.isInLiquid || player.getVehicle() != null ||
                player.isFlying() || player.isSneaking || player.isEating || player.isPullingBow || player.isBlocking ||
                player.touchingFaces.contains(BlockFace.UP) || this.touchingFaces.contains(BlockFace.UP) ||
                this.touchingFaces.contains(BlockFace.NORTH) || this.touchingFaces.contains(BlockFace.SOUTH) ||
                this.touchingFaces.contains(BlockFace.WEST) || this.touchingFaces.contains(BlockFace.EAST) ||
                this.collidingBlocks.contains(Material.LADDER) || this.collidingBlocks.contains(Material.VINE) ||
                this.isCollidingEntities || player.isGliding || player.invalidMotionData.prevGliding ||
                player.currentTick - player.speedData.lastIdleTick <= 2) {
            return true;
        }
        IWrappedBlock footBlock = player.position.add(0, -1, 0).getBlock();
        if (footBlock == null) {
            return true;
        }
        Vector3D velocity = this.velocity.clone().setY(0);
        Vector3D prevVelocity = player.velocity.clone();
        if (this.hitSlowdown) {
            prevVelocity.multiply(0.6);
        }
        if (this.touchedBlocks.contains(Material.SOUL_SAND)) {
            prevVelocity.multiply(0.4);
        }
        if (Math.abs(prevVelocity.x * this.oldFriction) < 0.005) {
            prevVelocity.setX(0);
        }
        if (Math.abs(prevVelocity.z * this.oldFriction) < 0.005) {
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

        if (velocity.length() < 0.15 || accelDir.lengthSquared() < 0.000001) {
            return true;
        }

        boolean vectorDir = accelDir.clone().crossProduct(yaw).dot(new Vector3D(0, 1, 0)) >= 0;
        double angle = (vectorDir ? 1 : -1) * MathUtils.angle(accelDir, yaw);

        double modulo = (angle % (Math.PI / 4)) * (4 / Math.PI);
        double error = Math.abs(modulo - Math.round(modulo)) * (Math.PI / 4);

        return error <= STRAFE_THRESHOLD;
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

        if (player.analysis && this.updatePos) {
            LangFile lang = Horizon.getInst().getLang(player.lang);
            String analysis = StringUtils.replaceEach(
                    lang.analysis,
                    ARGS,
                    new String[]{
                            String.valueOf(this.velocity.y),
                            String.valueOf(MathUtils.distance2d(this.velocity.x, this.velocity.z)),
                            String.valueOf(player.currentTick),
                            String.valueOf(this.onGround),
                            String.valueOf(this.onGroundReally)
                    });
            player.sendMessage(Horizon.getInst().config.prefix + Horizon.getInst().applyPAPI(player.getPlayer(), analysis));
        }

        int ping = McAccessor.INSTANCE.getPing(player.getPlayer());

        player.tick(ping);

        if (player.isTeleporting && player.teleports.size() > 0) {
            Pair<Location, Long> tpPair = player.teleports.get(0);
            Location tpLoc = tpPair.key;

            int elapsedTicks = (int) (player.currentTick - tpPair.value);

            if (!onGround && updatePos && updateRot && tpLoc.equals(to)) {
                player.position = tpLoc;
                player.velocity = new Vector3D(0, 0, 0);

                player.teleports.remove(0);

                player.teleportAcceptTick = player.currentTick;

                this.isTeleport = true;

                if (player.teleports.size() == 0) {
                    player.isTeleporting = false;
                } else {
                    return false;
                }
            } else if (!player.getPlayer().isSleeping()) {
                if (elapsedTicks > (ping / 50) + 5) {
                    Location tp = player.teleports.get(player.teleports.size() - 1).key;
                    player.teleports.clear();
                    Sync.teleport(player, tp);
                }
            }
        }

        if (!isTeleport && Math.abs(velocity.y - -0.098) < 0.0000001 && (knockBack == null || Math.abs(knockBack.y - -0.098) > 0.0000001)) {
            player.position = to;
            return false;
        }
        return true;
    }

    @Override
    public void post() {
        player.position = this.to;
        player.onGround = this.onGround;
        player.onGroundReally = this.onGroundReally;
        player.friction = this.newFriction;
        player.prevPrevDeltaY = player.velocity.y;
        player.velocity = this.velocity;
        player.touchingFaces = this.touchingFaces;
        player.isInWater = this.isInWater;
        player.isInLava = this.checkLava();

        if (onGroundReally) {
            player.clientBlockCount = 0;
            player.prevClientBlock = null;
        }

        if (!updatePos) {
            player.speedData.lastIdleTick = player.currentTick;
        }
    }

    private boolean checkLava() {
        AABB lavaTest = AABB.LAVA_BOX;
        lavaTest.translate(to.toVector());
        List<IWrappedBlock> blocks = lavaTest.getBlocks(player.world);
        for (IWrappedBlock b : blocks) {
            if (MatUtils.LAVA.contains(b.getType())) {
                return true;
            }
        }
        return false;
    }

    public enum MoveType {
        POSITION, LOOK, POSITION_LOOK, FLYING
    }
}