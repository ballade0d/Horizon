package xyz.hstudio.horizon.bukkit.learning.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class KnnClassification<T> {

    private final List<KnnValueBean<T>> dataArray = new ArrayList<>();

    public void addRecord(final T value, final String typeId) {
        dataArray.add(new KnnValueBean<>(value, typeId));
    }

    public KnnValueSort[] sort(final T[] values) {
        Multimap<String, Double> multimap = HashMultimap.create();
        for (final T value : values) {
            for (final KnnValueBean<T> bean : this.dataArray) {
                double score = this.computeDistance(bean.value, value);
                multimap.put(bean.typeId, score);
            }
        }

        List<KnnValueSort> sorts = new ArrayList<>();
        for (String typeId : multimap.keySet()) {
            sorts.add(new KnnValueSort(typeId, multimap.get(typeId).stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        }

        sorts.sort(Comparator.comparingDouble(o -> o.distance));
        return sorts.toArray(new KnnValueSort[0]);
    }

    public abstract double computeDistance(final T o1, final T o2);
}