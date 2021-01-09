package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookTest {
    @Test
    public void load() throws IOException {
        World world;
        Book book;

        world = World.create();
        for (Node file : world.guessProjectHome(getClass()).join("src/test/etc/books").find("*.yogi")) {
            book = Book.load(file);
            assertTrue(book.size() > 0);
        }
    }
}
