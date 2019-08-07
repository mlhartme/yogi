package net.mlhartme.yogi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** TODO: not a set yet */
public class IntSet implements Iterable<Integer> {
    public static IntSet parse(List<String> strings) {
        IntSet result;

        result = new IntSet();
        for (String str : strings) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

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


    @Override
    public Iterator<Integer> iterator() {
        return data.iterator();
    }
}
