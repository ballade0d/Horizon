package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.ScaffoldConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.ScaffoldData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;

public class Scaffold extends Module<ScaffoldData, ScaffoldConfig> {

    public Scaffold() {
        super(ModuleType.Scaffold, new ScaffoldConfig());
    }

    @Override
    public ScaffoldData getData(final HoriPlayer player) {
        return player.scaffoldData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {

    }
}