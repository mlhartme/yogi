package net.mlhartme.yogi;

import java.util.ArrayList;
import java.util.List;

public class IntSet {
    private final List<Integer> data;

    public IntSet() {
        this.data = new ArrayList<>();
    }

    public void add(int i) {
        data.add(i);
    }

    public void clear() {
        data.clear();
    }

    public boolean contains(int i) {
        return data.contains(i);
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public String toString() {
        StringBuilder result;

        result = new StringBuilder();
        for (Integer i : data) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(i);
        }
        return result.toString();
    }

    public static IntSet union(IntSet left, IntSet right) {
        IntSet result;

        result = new IntSet();
        result.data.addAll(left.data);
        result.data.addAll(right.data);
        return result;
    }


}
