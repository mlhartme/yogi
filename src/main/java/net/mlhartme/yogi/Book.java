package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.LinkedHashMap;

public class Book implements Comparable<Book> {
    public static final String EXT = ".yogi";

    public static Book load(Node<?> file) throws IOException {
        Book book;
        Vocabulary current;

        book = new Book(Strings.removeRight(file.getName(), EXT));
        current = null;
        for (String line : file.readLines()) {
            if (line.startsWith("# ")) {
                current = new Vocabulary();
                book.sections.put(line.substring(2), current);
            } else {
                if (current == null) {
                    throw new IOException("missing section header");
                }
                current.addInvLine(line);
            }
        }
        return book;
    }

    public final String name;
    public final LinkedHashMap<String, Vocabulary> sections;

    public Book(String name) {
        this.name = name;
        this.sections = new LinkedHashMap<>();
    }

    @Override
    public int compareTo(Book right) {
        return name.compareTo(right.name);
    }
}
