package xyz.hstudio.horizon.command;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.command.annotation.Cmd;
import xyz.hstudio.horizon.command.annotation.Tab;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.menu.MenuHolder;
import xyz.hstudio.horizon.menu.menus.MainMenu;
import xyz.hstudio.horizon.util.collect.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Executors {

    private final List<Pair<Cmd, Integer>> cmds = new ArrayList<>();
    private final List<Pair<Tab, Integer>> tabs = new ArrayList<>();

    public Executors() {
        CmdExecutor cmdExecutor = new CmdExecutor();
        TabExecutor tabExecutor = new TabExecutor();
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance("horizon", Horizon.getInst());
            command.setAliases(Horizon.getInst().config.command_alias);
            constructor.setAccessible(accessible);

            Method method = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) method.invoke(Bukkit.getServer());
            commandMap.register("horizon", command);

            command = Bukkit.getPluginCommand("horizon");

            command.setExecutor(cmdExecutor);
            command.setTabCompleter(tabExecutor);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register commands!", e);
        }

        {
            Method[] methods = cmdExecutor.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Cmd annotation = method.getAnnotation(Cmd.class);
                if (annotation == null) {
                    continue;
                }
                cmds.add(new Pair<>(annotation, i));
            }
        }

        {
            Method[] methods = tabExecutor.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Tab annotation = method.getAnnotation(Tab.class);
                if (annotation == null) {
                    continue;
                }
                tabs.add(new Pair<>(annotation, i));
            }
        }
    }

    private class CmdExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            String prefix = Horizon.getInst().config.prefix;
            if (args.length == 0) {
                String version = Horizon.getInst().getDescription().getVersion();
                sender.sendMessage(prefix + "Horizon(" + version + ") by MrCraftGoo");
                return true;
            }
            LangFile lang = Horizon.getInst().getLang(sender instanceof Player ?
                    Horizon.PLAYERS.get(((Player) sender).getUniqueId()).lang :
                    Horizon.getInst().config.default_lang);
            String subName = args[0];
            String[] subArgs = ArrayUtils.remove(args, 0);
            for (Pair<Cmd, Integer> pair : cmds) {
                Cmd cmd = pair.key;
                if (!cmd.name().equalsIgnoreCase(subName)) {
                    continue;
                }
                if (cmd.onlyPlayer() && !(sender instanceof Player)) {
                    sender.sendMessage(prefix + lang.cmd_only_player);
                    return true;
                }
                if (!sender.hasPermission(cmd.perm())) {
                    sender.sendMessage(prefix + lang.cmd_no_permission);
                    return true;
                }
                try {
                    this.getClass()
                            .getMethods()[pair.value]
                            .invoke(this, sender, subArgs, prefix, lang);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sender.sendMessage(prefix + lang.cmd_unknown);
            return true;
        }

        @Cmd(name = "verbose", perm = "horizon.cmd.verbose", onlyPlayer = true)
        public void verbose(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            HoriPlayer player = Horizon.PLAYERS.get(((Player) sender).getUniqueId());
            boolean verbose = !player.verbose;
            sender.sendMessage(prefix + (verbose ? lang.cmd_verbose_enabled : lang.cmd_verbose_disabled));
            player.verbose = verbose;
        }

        @Cmd(name = "analysis", perm = "horizon.cmd.analysis", onlyPlayer = true)
        public void analysis(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            HoriPlayer player = Horizon.PLAYERS.get(((Player) sender).getUniqueId());
            boolean analysis = !player.analysis;
            sender.sendMessage(prefix + (analysis ? lang.cmd_analysis_enabled : lang.cmd_analysis_disabled));
            player.analysis = analysis;
        }

        @Cmd(name = "notify", perm = "horizon.cmd.notify")
        public void notify(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            StringBuilder builder = new StringBuilder();
            for (String s : args) {
                builder.append(s).append(" ");
            }
            String msg = builder.toString().trim();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("horizon.notify")) {
                    continue;
                }
                player.sendMessage(prefix + msg);
            }
            Logger.msg("Notify", msg);
            sender.sendMessage(prefix + lang.cmd_notify_sent);
        }

        @Cmd(name = "kick", perm = "horizon.cmd.kick")
        public void kick(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            if (args.length < 2) {
                sender.sendMessage(prefix + lang.cmd_kick_wrong_usage);
                return;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(prefix + lang.cmd_player_not_found);
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]).append(" ");
            }
            String reason = builder.toString().trim();
            player.kickPlayer(Horizon.getInst().applyPAPI(player, StringUtils.replace(reason, "%break%", "\n")));

            for (String broadcast : lang.kick_broadcast) {
                Bukkit.broadcastMessage(Horizon.getInst().applyPAPI(player,
                        StringUtils.replaceEach(broadcast,
                                new String[]{
                                        "%prefix%", "%player%", "%reason%"
                                },
                                new String[]{
                                        Horizon.getInst().config.prefix, player.getName(), reason
                                }
                        )));
            }
        }

        @Cmd(name = "bot", perm = "horizon.cmd.bot")
        public void bot(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            if (args.length < 2) {
                sender.sendMessage(prefix + lang.cmd_bot_wrong_usage);
                return;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(prefix + lang.cmd_player_not_found);
                return;
            }
            HoriPlayer hPlayer = Horizon.PLAYERS.get(player.getUniqueId());
            if (hPlayer == null) {
                sender.sendMessage(prefix + lang.cmd_player_not_found);
                return;
            }
            try {
                int time = Integer.parseInt(args[1]);
                hPlayer.killAuraBotData.checkEnd = System.currentTimeMillis() + time * 1000L;
                sender.sendMessage(prefix + lang.cmd_bot);
            } catch (Exception e) {
                sender.sendMessage(prefix + lang.cmd_bot_wrong_usage);
            }
        }

        @Cmd(name = "menu", perm = "horizon.cmd.menu", onlyPlayer = true)
        public void menu(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            HoriPlayer player = Horizon.PLAYERS.get(((Player) sender).getUniqueId());
            if (player.prevMenu == null) {
                new MainMenu(new MenuHolder((Player) sender, player)).open();
            } else {
                player.prevMenu.open();
            }
        }

        @Cmd(name = "reload", perm = "horizon.cmd.reload", onlyPlayer = false)
        public void reload(final CommandSender sender, final String[] args, final String prefix, final LangFile lang) {
            Horizon.getInst().reload();
            sender.sendMessage(prefix + lang.cmd_reload);
        }
    }

    private class TabExecutor implements TabCompleter {
        @Override
        public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
            if (args.length == 0) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                return cmds
                        .stream()
                        .map(pair -> pair.key.name())
                        .filter(name -> name.startsWith(args[0]))
                        .sorted()
                        .collect(Collectors.toList());
            } else {
                Pair<Tab, Integer> tab = tabs
                        .stream()
                        .filter(pair -> pair.key.name().equalsIgnoreCase(args[1]))
                        .findFirst()
                        .orElse(null);
                if (tab == null) {
                    return Collections.emptyList();
                } else {
                    try {
                        return (List<String>) this.getClass()
                                .getMethods()[tab.value]
                                .invoke(this, sender, ArrayUtils.removeAll(args, 0, 1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return Collections.emptyList();
        }

        @Tab(name = "kick")
        public List<String> kick(final CommandSender sender, final String[] args) {
            if (args.length == 0) {
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Tab(name = "bot")
        public List<String> bot(final CommandSender sender, final String[] args) {
            if (args.length == 0) {
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
    }
}