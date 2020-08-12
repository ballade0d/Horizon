package xyz.hstudio.horizon.module.checks;

import org.bukkit.GameMode;
import org.bukkit.Material;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InteractData;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.BlockBreakEvent;
import xyz.hstudio.horizon.event.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.file.node.InteractNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.enums.BlockFace;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.stream.DoubleStream;

public class Interact extends Module<InteractData, InteractNode> {

    public Interact() {
        super(ModuleType.Interact, new InteractNode(), "Packet", "Angle");
    }

    @Override
    public InteractData getData(final HoriPlayer player) {
        return player.interactData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            e.setCancelled(true);
            McAccessor.INSTANCE.updateBlock(player, e.block.getPos());
        } else if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            e.setCancelled(true);
            McAccessor.INSTANCE.updateBlock(player, e.placed);
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (config.packet_enabled) {
            typeA(event, player, data, config);
        }
        if (config.angle_enabled) {
            typeB(event, player, data, config);
        }
    }

    /**
     * Order check. This will detect some poorly made Scaffold/Tower.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 7/10 - Detects some Block hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            if (player.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }

            Vector3D interaction = e.interaction;
            if (e.face == BlockFace.INVALID) {
                // Punish
                this.punish(event, player, data, 0, 5);
            } else if (DoubleStream.of(interaction.x, interaction.y, interaction.z)
                    .anyMatch(d -> d > 1 || d < 0)) {
                // Punish
                this.punish(event, player, data, 0, 5, "1:" + interaction);
            } else {
                reward(0, data, 0.999);
            }
        }
    }

    /**
     * Angle check.
     * <p>
     * Accuracy: 10/10 - Haven't found any false positives.
     * Efficiency: 6/10 - Detects some poorly made Scaffold/Tower.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            IWrappedBlock block = e.placed.getBlock();
            if (block == null || block.getType() != Material.AIR) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getPlaceBlockFace());
            if (angle > Math.toRadians(config.angle_max_angle)) {
                // Punish
                this.punish(event, player, data, 1, 4, "t:place");
            } else {
                reward(1, data, 0.999);
            }
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            if (e.digType != BlockBreakEvent.DigType.COMPLETE) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getBreakBlockFace());
            if (angle > Math.toRadians(config.angle_max_angle)) {
                // Punish
                this.punish(event, player, data, 1, 4, "t:break");
            } else {
                reward(1, data, 0.999);
            }
        }
    }
}