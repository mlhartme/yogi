package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProtocolTest {
    @Test
    public void histogram() throws IOException {
        World w;
        FileNode file;
        Protocol p;
        Map<String, Integer> tries;
        Map<Integer, Integer> hist;

        w = World.create();
        file = w.guessProjectHome(getClass()).join("src/test/16c1fe80c7c.log");
        p = Protocol.load(file);

        tries = new HashMap<>();
        tries.put("1", 3);
        tries.put("2", 1);
        tries.put("3", 2);
        assertEquals(tries, p.questionCount());

        hist = new HashMap<>();
        hist.put(1, 1);
        hist.put(2, 1);
        hist.put(3, 1);
        assertEquals(hist, p.histogramRaw());
    }
}
