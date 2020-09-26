package xyz.hstudio.horizon.wrapper.v1_12;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.wrapper.PackerBase;

public class Packer_v1_12 extends PackerBase {

    @Override
    public InEvent received(HPlayer p, Object packet) {
        return null;
    }

    @Override
    public OutEvent sent(HPlayer p, Object packet) {
        return null;
    }
}