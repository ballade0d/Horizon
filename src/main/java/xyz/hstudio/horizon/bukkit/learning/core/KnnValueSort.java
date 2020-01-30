package xyz.hstudio.horizon.bukkit.learning.core;

public class KnnValueSort {

    public final String typeId;
    public final double score;

    public KnnValueSort(final String typeId, final double score) {
        this.typeId = typeId;
        this.score = score;
    }

    @Override
    public String toString() {
        return typeId + ", Score:" + score;
    }
}