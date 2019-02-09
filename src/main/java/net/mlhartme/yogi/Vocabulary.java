package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.springframework.stereotype.Controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class Vocabulary {
    public static Vocabulary load(Node<?>... files) throws IOException {
        List<String> lines;
        Vocabulary result;
        int idx;

        lines = new ArrayList<>();
        for (Node<?> file : files) {
            lines.addAll(file.readLines());
        }
        result = new Vocabulary();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            idx = line.indexOf('=');
            if (idx == -1) {
                throw new IOException("syntax error: " + line);
            }
            result.add(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }
        return result;
    }

    private final List<String> lefts;
    private final List<String> rights;

    public Vocabulary() {
        this.lefts = new ArrayList<>();
        this.rights = new ArrayList<>();
    }

    public void add(String left, String right) {
        this.lefts.add(left);
        this.rights.add(right);
    }

    public int size() {
        return lefts.size();
    }

    public String left(int idx) {
        return lefts.get(idx);
    }

    public String right(int idx) {
        return rights.get(idx);
    }
}