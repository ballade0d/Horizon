package xyz.hstudio.kirin.module.clientctrl;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.kirin.module.ClientCtrl;

import java.util.List;

@LoadFrom("kirin/clientctrl/lunarclient.yml")
public class LunarClient extends ClientCtrl {

    @LoadPath("execution")
    private static List<String> EXECUTION;

    public LunarClient(HPlayer p) {
        super(p, "Lunar-Client", EXECUTION);
    }
}