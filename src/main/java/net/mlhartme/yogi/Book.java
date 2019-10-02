package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Book implements Comparable<Book> {
    public static final String EXT = ".yogi";

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

    public Statistics statistics(FileNode userProtocols) throws IOException {
        return Statistics.collect(userProtocols, this);
    }

    public Map<String, IntSet> sections() throws IOException {
        LinkedHashMap<String, IntSet> result;

        result = new LinkedHashMap<>(sections.size() + 1);
        result.putAll(sections);
        return result;
    }

    public Map<String, String> selection(IntSet selection) {
        LinkedHashMap<String, String> result;

        result = new LinkedHashMap<>(sections.size());
        for (Integer integer : selection) {
            result.put(lefts.get(integer), rights.get(integer));
        }
        return result;
    }

    public IntSet enabled(FileNode userProtocols) throws IOException {
        FileNode file;
        IntSet result;
        Set<String> asked;
        String question;
        List<String> lst;
        int idx;

        file = userProtocols.join(name, ".enabled");
        result = new IntSet();
        if (file.exists()) {
            lst = file.readLines();
            for (String active : lst) {
                idx = lefts.indexOf(active);
                if (idx < 0) {
                    throw new IllegalStateException("unknown question: " + active);
                }
                result.add(idx);
            }
        } else {
            // TODO: wait until all users/books have an active file; then dump the asked() code
            lst = new ArrayList<>();
            asked = Protocol.asked(userProtocols, name);
            for (int i = 0; i < lefts.size(); i++) {
                question = lefts.get(i);
                if (asked.contains(question)) {
                    result.add(i);
                    lst.add(question);
                }
            }
            file.writeLines(lst);
        }
        return result;
    }

    public IntSet newWords(FileNode userProtocols) throws IOException {
        IntSet result;
        IntSet enabled;

        enabled = enabled(userProtocols);
        result = new IntSet();
        for (int i = 0; i < lefts.size(); i++) {
            if (!enabled.contains(i)) {
                result.add(i);
            }
        }
        return result;
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

    public String left(int idx) {
        return lefts.get(idx);
    }

    public String right(int idx) {
        return rights.get(idx);
    }

    public int lookupLeft(String left) {
        return lefts.indexOf(left);
    }

    @Override
    public int compareTo(Book right) {
        return name.compareTo(right.name);
    }
}
