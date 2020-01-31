package xyz.hstudio.horizon.bukkit.learning.core;

public class KnnValueSort {

    public final String typeId;
    public final double distance;

    public KnnValueSort(final String typeId, final double distance) {
        this.typeId = typeId;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return typeId + ", Distance:" + distance;
    }
}