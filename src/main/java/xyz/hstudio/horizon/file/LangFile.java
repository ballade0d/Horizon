package xyz.hstudio.horizon.file;

import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class LangFile extends AbstractFile {

    @Load(path = "cmd_only_player")
    public String cmd_only_player = "This command can only be executed by a player.";
    @Load(path = "cmd_no_permission")
    public String cmd_no_permission = "You don't have the permission to execute this command.";

    @Load(path = "cmd_verbose_enabled")
    public String cmd_verbose_enabled = "Verbose is enabled now.";
    @Load(path = "cmd_verbose_disabled")
    public String cmd_verbose_disabled = "Verbose is disabled now.";
    @Load(path = "verbose")
    public String verbose = "Player %player% failed check %check% (%type%), VL: %vl_total% (+%vl_addition%), Ping: %ping%, %args%";

    @Load(path = "cmd_analysis_enabled")
    public String cmd_analysis_enabled = "Analysis is enabled now.";
    @Load(path = "cmd_analysis_disabled")
    public String cmd_analysis_disabled = "Analysis is disabled now.";
    @Load(path = "analysis")
    public String analysis = "Y Speed: %y_speed%, XZ Speed: %xz_speed%, tick: %tick%, cGround: %c_ground%, sGround: %s_ground%";

    @Override
    public Object getValue(final String path, final YamlLoader loader, final Class<?> type) {
        return loader.get(path);
    }
}