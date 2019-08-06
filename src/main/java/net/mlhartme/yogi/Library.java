package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Library implements Iterable<Book> {
    public static Library load(Node<?> base) throws IOException {
        Library result;

        result = new Library();
        for (Node<?> book : base.find("*.yogi")) {
            result.books.add(Book.load(book));
        }
        Collections.sort(result.books);
        return result;
    }

    private final List<Book> books;

    public Library() {
        this.books = new ArrayList<>();
    }

    public Book get(String name) throws IOException {
        for (Book book : books) {
            if (book.name.equals(name)) {
                return book;
            }
        }
        throw new IOException("book not fount: " + name);
    }

    @Override
    public Iterator<Book> iterator() {
        return books.iterator();
    }
}
