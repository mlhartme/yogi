package net.mlhartme.yogi;

import net.oneandone.sushi.fs.file.FileNode;
import org.springframework.objenesis.SpringObjenesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** represents the log file for one book */
public class Statistics {
    /** @return question mapped to number of tries list */
    private final Map<String, List<Integer>> map;

    public static Statistics collect(FileNode protocolBase, String book) throws IOException {
        List<FileNode> logs;
        Protocol protocol;
        Statistics result;
        int count;

        result = new Statistics();
        logs = Protocol.list(protocolBase, book);
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

    public Statistics() {
        this.map = new HashMap<>();
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

        if (selection.isEmpty()) {
            return 0;
        }
        sum = 0;
        for (Integer question : selection) {
            sum += quality(book.left(question));
        }
        return sum / selection.size();
    }

    public int quality(String question) {
        List<Integer> tries;
        int count;

        tries = map.get(question);
        if (tries == null) {
            return 0;
        } else {
            count = tries.get(tries.size() - 1);
            if (count == Protocol.NOT_ANSWERED) {
                return 0;
            } else {
                return Math.max(0, 100 - (count - 1) * 20);
            }
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

    public static String style(int quality) {
        if (quality < 5) {
            return "color: red;";
        } else if (quality < 20) {
            return "color: lightred;";
        } else if (quality < 40) {
            return "color: orange;";
        } else if (quality < 60) {
            return "color: yellow;";
        } else if (quality < 80) {
            return "color: yellowgreen;";
        } else if (quality < 95) {
            return "color: lightgreen;";
        } else {
            return "color: green;";
        }
    }

}
