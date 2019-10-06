package net.mlhartme.yogi;

import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** represents the log file for one book */
public class Statistics {
    public static Statistics collect(FileNode userProtocols, Book book) throws IOException {
        List<FileNode> logs;
        Protocol protocol;
        Statistics result;
        int count;

        result = new Statistics(book.enabled(userProtocols), book.disabled(userProtocols));
        logs = Protocol.list(userProtocols, book.name);
        for (FileNode node : logs) {
            protocol = Protocol.load(node);
            for (Map.Entry<Integer, List<String>> entry : protocol.histogramRaw().entrySet()) {
                count = entry.getKey();
                for (String question : entry.getValue()) {
                    result.add(question, count);
                }
            }
        }
        return result;
    }

    //--

    /** @return question mapped to number of tries list */
    private final Map<String, List<Integer>> map;

    public final IntSet enabled;
    public final IntSet disabled;

    public Statistics(IntSet enabled, IntSet disabled) {
        this.map = new HashMap<>();
        this.enabled = enabled;
        this.disabled = disabled;
    }

    public boolean hasEnabled(IntSet selection) {
        for (Integer i : selection) {
            if (!disabled.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDisabled(IntSet selection) {
        for (Integer i : selection) {
            if (disabled.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public void add(String question, int count) {
        List<Integer> tries;

        tries = map.get(question);
        if (tries == null) {
            tries = new ArrayList<>();
            map.put(question, tries);
        }
        tries.add(count);
    }

    public int quality(Book book, IntSet selection) {
        int sum;
        int count;

        sum = 0;
        count = 0;
        for (Integer question : selection) {
            if (enabled.contains(question)) {
                sum += quality(book.left(question));
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    public int quality(String question) {
        List<Integer> tries;

        tries = map.get(question);
        if (tries == null) {
            return 0;
        } else {
            return previousCountQuality(tries, 1) * 100 / 200     // 50%
                    + previousCountQuality(tries, 2) * 100 / 400  // 25%
                    + previousCountQuality(tries, 3) * 100 / 400; // 25%
        }
    }

    private int previousCountQuality(List<Integer> tries, int back) {
        int idx;

        idx = tries.size() - back;
        return countQuality(tries.get(idx < 0 ? 0 : idx));
    }

    private int countQuality(int count) {
        if (count == Protocol.NOT_ANSWERED) {
            return 0;
        } else {
            return Math.max(1, 100 - (count - 1) * 20);
        }
    }

    public String tries(String question) {
        List<Integer> tries;
        StringBuilder builder;

        tries = map.get(question);
        if (tries == null) {
            return "(new)";
        } else {
            builder = new StringBuilder();
            for (int count : tries) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                if (count == Protocol.NOT_ANSWERED) {
                    builder.append('-');
                } else {
                    builder.append(count);
                }
            }
            return builder.toString();
        }
    }

    /** sort by quality, lower quality first */
    public List<Integer> sort(IntSet selection, Book book) {
        List<Integer> result;

        result = new ArrayList<>(selection.size());
        for (Integer i : selection) {
            result.add(i);
        }
        Collections.sort(result, new Comparator<Integer>() {
            @Override
            public int compare(Integer left, Integer right) {
                return Integer.compare(quality(book.left(left)), quality(book.left(right)));
            }
        });
        return result;
    }

    public static String style(int quality) {
        if (quality == 0) {
            return "color: white;";
        } else if (quality < 30) {
            return "color: red;";
        } else if (quality < 60) {
            return "color: orange;";
        } else if (quality < 80) {
            return "color: yellow;";
        } else if (quality < 95) {
            return "color: yellowgreen;";
        } else {
            return "color: green;";
        }
    }


    public String enableMarker(IntSet selection) {
        int e;
        int all;

        e = 0;
        for (int i : selection) {
            if (enabled.contains(i)) {
                e++;
            }
        }
        all = selection.size();
        if (e == all) {
            return Integer.toString(all);
        } else {
            return Integer.toString(e) + "/" + selection.size();
        }
    }
}
