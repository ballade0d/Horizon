package xyz.hstudio.horizon.command;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.command.annotation.Cmd;
import xyz.hstudio.horizon.lang.Lang;
import xyz.hstudio.horizon.util.collect.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    // CommandSender, String, String[], Language
    private final List<Pair<Cmd, Integer>> commands = new ArrayList<>();

    public Commands() {
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
            sender.sendMessage(prefix + "Horizon by MrCraftGoo");
            return true;
        }
        Lang lang = Lang.getLang(Horizon.getInst().config.personalized_themes_default_lang);
        String subName = args[0];
        String[] subArgs = (String[]) ArrayUtils.remove(args, 0);
        for (Pair<Cmd, Integer> pair : this.commands) {
            Cmd cmd = pair.key;
            if (!cmd.name().equalsIgnoreCase(subName)) {
                continue;
            }
            if (cmd.onlyPlayer() && !(sender instanceof Player)) {
                // Send info
                return true;
            }
            if (!sender.hasPermission(cmd.perm())) {
                // Send info
                return true;
            }
            try {
                this.getClass().getMethods()[pair.value].invoke(this, sender, subArgs, lang, prefix);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}