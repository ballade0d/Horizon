package xyz.hstudio.horizon.bukkit.learning.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public abstract class KnnClassification<T> {

    private final List<KnnValueBean<T>> dataArray = new ArrayList<>();
    private final int K;

    public KnnClassification(final int K) {
        this.K = K;
    }

    public void addRecord(final T value, final String typeId) {
        dataArray.add(new KnnValueBean<>(value, typeId));
    }

    public KnnValueSort[] sort(final T[] values) {
        Multimap<String, Double> multimap = HashMultimap.create();
        for (final T value : values) {
            for (final KnnValueBean<T> bean : this.dataArray) {
                double score = this.computeSimilarity(bean.value, value);
                multimap.put(bean.typeId, score);
            }
        }

        List<KnnValueSort> sorts = new ArrayList<>();
        for (String typeId : multimap.keySet()) {
            sorts.add(new KnnValueSort(typeId, multimap.get(typeId).stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        }

        sorts.sort((o1, o2) -> Double.compare(o2.score, o1.score));
        return sorts.subList(0, K).toArray(new KnnValueSort[K]);
    }

    public abstract double computeSimilarity(final T o1, final T o2);
}