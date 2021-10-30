/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schmizzolin.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ProtocolTest {
    @Test
    public void histogram() throws IOException {
        World w;
        FileNode file;
        Protocol p;
        Map<String, Integer> tries;
        Map<Integer, List<String>> hist;

        w = World.create();
        file = w.guessProjectHome(getClass()).join("src/test/1.protocol");
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
        file = w.guessProjectHome(getClass()).join("src/test/2.protocol");
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
