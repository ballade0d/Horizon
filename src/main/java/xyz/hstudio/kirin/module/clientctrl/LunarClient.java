package xyz.hstudio.kirin.module.clientctrl;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.kirin.module.ClientCtrl;

import java.util.List;

@LoadFrom("kirin/clientctrl/lunarclient.yml")
public class LunarClient extends ClientCtrl {

    @LoadInfo("execution")
    private static List<String> EXECUTION;

    public LunarClient(HPlayer p) {
        super(p, "Lunar-Client", EXECUTION);
    }
}