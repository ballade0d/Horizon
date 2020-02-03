package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Location;

import java.util.ArrayList;
import java.util.List;

public class HitBoxData extends Data {

    // Hit
    public List<Pair<Location, Long>> history = new ArrayList<>();
}