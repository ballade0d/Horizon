package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HitBoxData extends Data {

    // Hit
    public List<Map.Entry<Location, Long>> history = new ArrayList<>();
}