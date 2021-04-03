package xyz.hstudio.horizon.command;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.language.Language;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleCmd extends Command {

    private static final MethodAccess ACCESS = MethodAccess.get(ConsoleCmd.class);
    private static final Map<Cmd, Integer> COMMAND_MAP = new HashMap<>();
    private final Horizon inst;

    public ConsoleCmd(Horizon inst) {
        super("horizon");
        for (Method method : ConsoleCmd.class.getMethods()) {
            Cmd annotation = method.getAnnotation(Cmd.class);
            if (annotation == null) {
                continue;
            }

            COMMAND_MAP.put(annotation, ACCESS.getIndex(method.getName()));
        }

        this.setAliases(Config.COMMAND_ALIAS.stream()
                .map(s -> s.substring(1))
                .collect(Collectors.toList())
        );

        CommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();
        commandMap.register("horizon", this);

        this.inst = inst;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] origin) {
        if (sender instanceof Player) {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
            return true;
        }
        String[] args = ArrayUtils.remove(origin, 0);
        Language lang = Language.getLang(Config.CONSOLE_LANGUAGE);
        String matcher = args.length == 0 ? "" : args[0];

        for (Map.Entry<Cmd, Integer> entry : COMMAND_MAP.entrySet()) {
            Cmd cmd = entry.getKey();
            if (!cmd.name().equalsIgnoreCase(matcher)) {
                continue;
            }

            ACCESS.invoke(this, entry.getValue(), sender, args, lang);
            break;
        }
        return true;
    }

    @Cmd(
            name = "bungee",
            permission = "horizon.command.bungee"
    )
    public void bungee(CommandSender sender, String[] args, Language lang) {
        HPlayer p = inst.getPlayers().values().parallelStream().findAny().orElse(null);
        if (p == null) {
            sender.sendMessage(Config.PREFIX + lang.CMD_BUNGEE_NO_ONLINE);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }

        inst.executeBungeeCommand(p, builder.toString());

        sender.sendMessage(Config.PREFIX + lang.CMD_BUNGEE_EXECUTED);
    }
}