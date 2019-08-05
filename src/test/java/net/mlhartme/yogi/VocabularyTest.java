package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VocabularyTest {
    @Test
    public void load() throws IOException {
        World world;

        world = World.create();
        for (Node node : world.resource("books").find("**/*.txt")) {
            Vocabulary.loadInv(node);
        }
    }

    @Test
    public void normal() {
        Vocabulary v;
        IntSet done;
        int idx;

        v = Vocabulary.create("1", "one", "2", "two");
        done = new IntSet();
        idx = v.next(done);
        done.add(idx);
        idx = v.next(done);
        done.add(idx);
        assertEquals(done.size(), v.size());

        try {
            v.next(done);
            fail();
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicate() {
        Vocabulary v;

        v = new Vocabulary();
        v.add("A", "a");
        v.add("A", "B");
        fail();
    }
}
