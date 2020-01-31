package xyz.hstudio.horizon.bukkit.learning;

import xyz.hstudio.horizon.bukkit.learning.core.KnnClassification;
import xyz.hstudio.horizon.bukkit.util.Vec2D;

public class KnnVector extends KnnClassification<Vec2D> {

    @Override
    public double computeDistance(final Vec2D o1, final Vec2D o2) {
        return o1.distance(o2);
    }
}