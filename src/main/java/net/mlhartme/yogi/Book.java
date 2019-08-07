package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Book implements Comparable<Book> {
    public static final String EXT = ".yogi";

    public static Book loadByName(Node<?> base, String name) throws IOException {
        return load(base.join(name + EXT));
    }

    public static Book load(Node<?> file) throws IOException {
        Book book;
        IntSet current;

        book = new Book(Strings.removeRight(file.getName(), EXT));
        current = null;
        for (String line : file.readLines()) {
            if (line.startsWith("# ")) {
                current = new IntSet();
                book.sections.put(line.substring(2), current);
            } else {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                } else {
                    if (current == null) {
                        throw new IOException("missing section header");
                    }
                    try {
                        current.add(book.addInvLine(line));
                    } catch (IllegalArgumentException e) {
                        System.out.println("TODO: " + e.getMessage());
                    }
                }
            }
        }
        return book;
    }

    public final String name;
    public final LinkedHashMap<String, IntSet> sections;
    private final List<String> lefts;
    private final List<String> rights;

    public Book(String name) {
        this.name = name;
        this.sections = new LinkedHashMap<>();
        this.lefts = new ArrayList<>();
        this.rights = new ArrayList<>();
    }

    public int size() {
        return lefts.size();
    }

    public int addInvLine(String line) throws IOException {
        int idx;

        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            throw new IllegalArgumentException(line);
        }
        idx = line.indexOf('=');
        if (idx == -1) {
            throw new IOException("syntax error: " + line);
        }
        return add(line.substring(idx + 1).trim(), line.substring(0, idx).trim());
    }

    public int add(String left, String right) {
        if (lookupLeft(left) >= 0) {
            throw new IllegalArgumentException("duplicate word: " + left);
        }
        this.lefts.add(left);
        this.rights.add(right);
        return lefts.size() - 1;
    }

    public int lookupLeft(String left) {
        return lefts.indexOf(left);
    }

    public Vocabulary select(String section) {
        Vocabulary result;

        result = new Vocabulary();
        for (Integer i : sections.get(section)) {
            result.add(lefts.get(i), rights.get(i));
        }
        return result;
    }

    @Override
    public int compareTo(Book right) {
        return name.compareTo(right.name);
    }
}
