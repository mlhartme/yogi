package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProtocolTest {
    @Test
    public void histogram() throws IOException {
        World w;
        FileNode file;
        Protocol p;
        Map<String, Integer> tries;
        Map<Integer, List<String>> hist;

        w = World.create();
        file = w.guessProjectHome(getClass()).join("src/test/1.log");
        p = Protocol.load(file);

        tries = new HashMap<>();
        tries.put("1", 3);
        tries.put("2", 1);
        tries.put("3", 2);
        assertEquals(tries, p.questionCount());

        hist = new HashMap<>();
        hist.put(1, Strings.toList("2"));
        hist.put(2, Strings.toList("3"));
        hist.put(3, Strings.toList("1"));
        assertEquals(hist, p.histogramRaw());
    }

    @Test
    public void histogramWithMissing() throws IOException {
        World w;
        FileNode file;
        Protocol p;
        Map<String, Integer> tries;
        Map<Integer, List<String>> hist;

        w = World.create();
        file = w.guessProjectHome(getClass()).join("src/test/2.log");
        p = Protocol.load(file);

        tries = new HashMap<>();
        tries.put("2", 1);
        tries.put("3", -1);
        assertEquals(tries, p.questionCount());

        hist = new HashMap<>();
        hist.put(1, Strings.toList("2"));
        hist.put(Protocol.NOT_ANSWERED, Strings.toList("3"));
        assertEquals(hist, p.histogramRaw());
    }
}
