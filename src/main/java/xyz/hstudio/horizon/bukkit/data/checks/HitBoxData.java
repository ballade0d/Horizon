package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;
import xyz.hstudio.horizon.bukkit.util.Location;
import xyz.hstudio.horizon.bukkit.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class HitBoxData extends Data {

    // Hit
    public List<Pair<Location, Long>> history = new ArrayList<>();
}