package xyz.hstudio.horizon.command;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.command.annotation.Cmd;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.util.collect.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    // CommandSender, String[], String, Lang
    private final List<Pair<Cmd, Integer>> commands = new ArrayList<>();

    public Commands() {
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

            Bukkit.getPluginCommand("horizon").setExecutor(this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register commands!");
        }
        Method[] methods = this.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Cmd annotation = method.getAnnotation(Cmd.class);
            if (annotation == null) {
                continue;
            }
            commands.add(new Pair<>(annotation, i));
        }
    }

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
                Horizon.getInst().config.personalized_themes_default_lang);
        String subName = args[0];
        String[] subArgs = (String[]) ArrayUtils.remove(args, 0);
        for (Pair<Cmd, Integer> pair : this.commands) {
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
                this.getClass().getMethods()[pair.value].invoke(this, sender, subArgs, prefix, lang);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
}