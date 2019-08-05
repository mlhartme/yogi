package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;

import java.io.IOException;
import java.util.LinkedHashMap;

public class Book {
    public static Book load(Node<?> file) throws IOException {
        Book book;
        Vocabulary current;

        book = new Book();
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

    public final LinkedHashMap<String, Vocabulary> sections;

    public Book() {
        this.sections = new LinkedHashMap<>();
    }
}
