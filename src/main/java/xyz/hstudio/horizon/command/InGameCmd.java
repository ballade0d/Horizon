package xyz.hstudio.horizon.command;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.lang3.ArrayUtils;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.language.Language;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InGameCmd {

    private static final MethodAccess ACCESS = MethodAccess.get(InGameCmd.class);
    private static final Map<Cmd, Integer> COMMAND_MAP = new HashMap<>();
    private final Horizon inst;

    public InGameCmd(Horizon inst) {
        for (Method method : InGameCmd.class.getMethods()) {
            Cmd annotation = method.getAnnotation(Cmd.class);
            if (annotation == null) {
                continue;
            }

            COMMAND_MAP.put(annotation, ACCESS.getIndex(method.getName()));
        }
        this.inst = inst;
    }

    public boolean onCommand(HPlayer p, String all) {
        String[] origin = all.split(" ");
        if (origin.length == 0) {
            return false;
        }
        if (!origin[0].equalsIgnoreCase("/horizon") &&
                Config.COMMAND_ALIAS.stream().noneMatch(s -> origin[0].equalsIgnoreCase(s))) {
            return false;
        }

        String[] args = ArrayUtils.remove(origin, 0);
        Language lang = Language.getLang(p);

        String matcher = args.length == 0 ? "" : args[0];

        for (Map.Entry<Cmd, Integer> entry : COMMAND_MAP.entrySet()) {
            Cmd cmd = entry.getKey();
            if (!cmd.name().equalsIgnoreCase(matcher)) {
                continue;
            }

            if (!p.nms.getBukkitEntity().hasPermission(cmd.permission())) {
                p.sendMessage(Config.PREFIX + lang.NO_PERMISSION);
                break;
            }

            ACCESS.invoke(this, entry.getValue(), p, args, lang);
            break;
        }
        return true;
    }

    @Cmd(
            name = "bungee",
            permission = "horizon.command.bungee"
    )
    public void bungee(HPlayer p, String[] args, Language lang) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }

        inst.executeBungeeCommand(p, builder.toString());

        p.sendMessage(Config.PREFIX + lang.CMD_BUNGEE_EXECUTED);
    }
}