package net.mlhartme.yogi;

import net.oneandone.sushi.fs.file.FileNode;

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

    public int fastHits(Book book, IntSet selection) {
        List<Integer> tries;
        int count;

        count = 0;
        for (Integer question : selection) {
            tries = map.get(book.left(question));
            if (tries != null && tries.get(tries.size() - 1) < 3) {
                count++;
            }
        }
        return count * 100 / selection.size();
    }
}
