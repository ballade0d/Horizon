package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.Bot;
import xyz.hstudio.horizon.compat.IBot;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.KillAuraBotData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.InteractCSEntityEvent;
import xyz.hstudio.horizon.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.KillAuraBotNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.concurrent.ThreadLocalRandom;

public class KillAuraBot extends Module<KillAuraBotData, KillAuraBotNode> {

    public KillAuraBot() {
        super(ModuleType.KillAuraBot, new KillAuraBotNode(), "Bot");
    }

    @Override
    public KillAuraBotData getData(final HoriPlayer player) {
        return player.killAuraBotData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final KillAuraBotData data, final KillAuraBotNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final KillAuraBotData data, final KillAuraBotNode config) {
        if (event instanceof InteractEntityEvent) {
            if (config.command_only && System.currentTimeMillis() > data.checkEnd) {
                return;
            }
            if (data.bot != null) {
                return;
            }
            IBot bot = Bot.createBot(player.getPlayer(), config.realistic_name);
            if (bot == null) {
                return;
            }
            Location to = getBotLocation(player.position, config.xz_distance, config.y_distance);
            this.run(() -> {
                bot.move(to.x, to.y, to.z, player.position.yaw, player.position.pitch, player);
                bot.spawn(player);
                if (config.show_armor) {
                    bot.setArmor(player);
                }
                if (!config.show_on_tab && !bot.isRealName()) {
                    bot.removeFromTabList(player);
                }
            }, config.async_packet);
            data.bot = bot;
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.isTeleport) {
                return;
            }
            if (data.bot == null) {
                return;
            }
            this.run(() -> {
                data.bot.despawn(player);
                data.bot = null;
            }, config.async_packet);
        } else if (event instanceof InteractCSEntityEvent) {
            if (data.bot == null) {
                return;
            }
            InteractCSEntityEvent e = (InteractCSEntityEvent) event;
            if (e.id == data.bot.getId()) {
                // Punish
                this.punish(event, player, data, 0, 1);
            }
        }
    }

    @Override
    public void tickAsync(final long currentTick, final KillAuraBotNode config) {
        if (currentTick % config.update_interval != 0) {
            return;
        }
        for (HoriPlayer player : Horizon.PLAYERS.values()) {
            IBot bot;
            if ((bot = getData(player).bot) == null) {
                continue;
            }
            if (this.canBypass(player) ||
                    System.currentTimeMillis() - bot.getSpawnTime() > config.respawn_interval * 1000L ||
                    (config.command_only && System.currentTimeMillis() > this.getData(player).checkEnd)) {
                this.run(() -> bot.despawn(player), config.async_packet);
                continue;
            }
            Location to = getBotLocation(player.position, config.xz_distance, config.y_distance);

            to.add(new Vector3D(config.offset_x * ThreadLocalRandom.current().nextDouble(), config.offset_y * ThreadLocalRandom.current().nextDouble(), config.offset_z * ThreadLocalRandom.current().nextDouble()));

            this.run(() -> {
                bot.move(to.x, to.y, to.z, player.position.yaw, player.position.pitch, player);

                boolean sprinting = ThreadLocalRandom.current().nextBoolean();
                if (sprinting) {
                    bot.setSneaking(false);
                    bot.setSprinting(true);
                } else {
                    bot.setSprinting(false);
                    bot.setSneaking(true);
                }
                bot.updateStatus(player);

                if (config.show_swing) {
                    bot.swingArm(player);
                }

                if (config.show_damage && currentTick % 30 == 0) {
                    bot.damage(player);
                }

                if (config.realistic_ping && currentTick % 40 == 0) {
                    bot.updatePing(player);
                }
            }, config.async_packet);
        }
    }

    private Location getBotLocation(final Location position, final double xz_distance, final double y_distance) {
        double yaw = Math.toRadians(position.yaw);
        return position.clone()
                .add(Math.sin(yaw) * xz_distance, y_distance, Math.cos(yaw) * -xz_distance);
    }

    private void run(final Runnable runnable, final boolean async) {
        if (async) {
            runnable.run();
        } else {
            McAccessor.INSTANCE.ensureMainThread(runnable);
        }
    }
}