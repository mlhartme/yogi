package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BookTest {
    @Test
    public void load() throws IOException {
        World world;
        Book book;

        world = World.create();
        for (Node file : world.resource("books").find("**/*.yogi")) {
            book = Book.load(file);
            assertTrue(book.size() > 0);
        }
    }
}
