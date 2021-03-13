package xyz.hstudio.horizon.command;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.configuration.Config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InGameCmd {

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);
    private static final MethodAccess ACCESS = MethodAccess.get(InGameCmd.class);
    private static final Map<Cmd, Integer> COMMAND_MAP = new HashMap<>();

    public InGameCmd() {
        for (Method method : InGameCmd.class.getMethods()) {
            Cmd annotation = method.getAnnotation(Cmd.class);
            if (annotation == null) {
                continue;
            }

            COMMAND_MAP.put(annotation, ACCESS.getIndex(method.getName()));
        }
    }

    public void onCommand(HPlayer p, String all) {
        String[] origin = all.split(" ");
        if (origin.length == 0) {
            return;
        }
        if (!origin[0].equalsIgnoreCase("/horizon") &&
                Config.COMMAND_ALIAS.stream().noneMatch(s -> origin[0].equalsIgnoreCase(s))) {
            return;
        }

        String[] args = ArrayUtils.remove(origin, 0);

        String matcher = args.length == 0 ? "" : args[0];

        for (Map.Entry<Cmd, Integer> entry : COMMAND_MAP.entrySet()) {
            Cmd cmd = entry.getKey();
            if (!cmd.name().equalsIgnoreCase(matcher)) {
                continue;
            }

            if (!p.nms.getBukkitEntity().hasPermission(cmd.permission())) {
                // TODO: Send message
                return;
            }

            ACCESS.invoke(this, entry.getValue(), p, args);
            break;
        }
    }

    @Cmd(
            name = "bungee",
            permission = "horizon.command.bungee"
    )
    public void bungee(HPlayer p, String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        inst.executeBungeeCommand(p, builder.toString());
        // TODO: Send message
    }
}