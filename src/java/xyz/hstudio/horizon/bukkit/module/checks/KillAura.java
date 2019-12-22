package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractItemEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.TimeUtils;

public class KillAura extends Module<KillAuraData, KillAuraConfig> {

    public KillAura() {
        super(ModuleType.KillAura, new KillAuraConfig());
    }

    @Override
    public KillAuraData getData(final HoriPlayer player) {
        return null;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
    }

    /**
     * An easy packet order check.
     * This will detect post killaura
     * Including LB's ones. Yes, it's like 10 lines.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            long now = TimeUtils.now();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        } else if (event instanceof InteractEntityEvent) {
            // Ignoring laggy players so this check won't false af like matrix while lagging
            // Inspired by funkemonkey
            if (data.lagging || TimeUtils.now() - data.lastMove >= 20) {
                return;
            }
            if (++data.typeAFails > 5) {
                // Punish
            }
        }
    }

    /**
     * An autoblock check.
     * This will detect most autoblock, probably all.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof InteractItemEvent) {
            InteractItemEvent e = (InteractItemEvent) event;
            ItemStack item = e.itemStack;
            // Check if the item can be used to block
            if (item == null || EnchantmentTarget.WEAPON.includes(item)) {
                // TODO: Remember to skip 1.9+ because client tick disappeared
                return;
            }
            switch (e.interactType) {
                case START_USE_ITEM: {
                    data.startBlockTick = player.currentTick;
                }
                case RELEASE_USE_ITEM: {
                    data.failTick = data.startBlockTick;
                }
            }
        } else if (event instanceof InteractEntityEvent) {
            if (player.currentTick - data.failTick <= 1) {
                // Punish
            }
        }
    }
}