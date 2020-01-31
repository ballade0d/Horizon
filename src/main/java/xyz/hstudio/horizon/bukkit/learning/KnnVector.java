package xyz.hstudio.horizon.bukkit.learning;

import xyz.hstudio.horizon.bukkit.learning.core.KnnClassification;
import xyz.hstudio.horizon.bukkit.util.Vector2D;

public class KnnVector extends KnnClassification<Vector2D> {

    @Override
    public double computeDistance(final Vector2D o1, final Vector2D o2) {
        return o1.distance(o2);
    }
}