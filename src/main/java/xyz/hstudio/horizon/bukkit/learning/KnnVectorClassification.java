package xyz.hstudio.horizon.bukkit.learning;

import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.learning.core.KnnClassification;

public class KnnVectorClassification extends KnnClassification<Vector> {

    public KnnVectorClassification() {
        super(2);
    }

    @Override
    public double computeSimilarity(final Vector o1, final Vector o2) {
        // return o1.dot(o2) / (o1.length() * o2.length());
        return o1.distance(o2);
    }
}