package xyz.hstudio.kirin.module.client;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.kirin.module.Client;

import java.util.List;

@LoadFrom("kirin/clientctrl/lunarclient.yml")
public class LunarClient extends Client {

    @LoadPath("execution")
    private static List<String> EXECUTION;

    public LunarClient(HPlayer p) {
        super(p, "Lunar-Client", EXECUTION);
    }
}