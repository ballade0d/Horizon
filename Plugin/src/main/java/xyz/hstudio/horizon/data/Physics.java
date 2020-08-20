package xyz.hstudio.horizon.data;

import lombok.Data;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.Location;

@Data
public class Physics {

    private Location pos;

    public Physics(HPlayer p) {
        this.pos = p.getBase().getPosition();
    }
}