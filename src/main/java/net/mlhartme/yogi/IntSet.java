package net.mlhartme.yogi;

import net.oneandone.sushi.util.Separator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** TODO: not a set yet */
public class IntSet implements Iterable<Integer> {
    public static IntSet parseArg(String arg) {
        return parse(Separator.COMMA.split(arg));
    }

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
    public IntSet(IntSet orig) {
        this.data = new ArrayList<>(orig.data);
    }
    public IntSet(Collection<Integer> collection) {
        this();
        addAll(collection);
    }

    public void addAll(Collection<Integer> collection) {
        for (Integer i : collection) {
            add(i);
        }
    }

    public void add(int i) {
        // TODO: only if not present
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

    private static final Random RANDOM = new Random();

    public int next(IntSet done) {
        int selectionSize;
        int doneSize;
        int rnd;

        selectionSize = size();
        doneSize = done.size();
        if (doneSize >= selectionSize) {
            throw new IllegalStateException("all done");
        }
        rnd = RANDOM.nextInt(selectionSize - doneSize);
        for (Integer i : this) {
            if (!done.contains(i)) {
                if (rnd == 0) {
                    return i;
                }
                rnd--;
            }
        }
        throw new IllegalStateException();
    }

    public void retain(int count) {
        while (data.size() > count) {
            data.remove(data.size() - 1);
        }
    }

    public void retain(IntSet set) {
        int value;

        for (int i = data.size() -1; i >= 0; i--) {
            value = data.get(i);
            if (!set.contains(value)) {
                data.remove(i);
            }
        }
    }

    public IntSet createRetained(IntSet set) {
        IntSet result;

        result = new IntSet(this);
        result.retain(set);
        return result;
    }

    public void removeAll(IntSet set) {
        int value;

        for (int i = data.size() -1; i >= 0; i--) {
            value = data.get(i);
            if (set.contains(value)) {
                data.remove(i);
            }
        }
    }

    public boolean remove(int remove) {
        for (int i = data.size() -1; i >= 0; i--) {
            if (data.get(i) == remove) {
                data.remove(i);
                return true;
            }
        }
        return false;
    }
}
