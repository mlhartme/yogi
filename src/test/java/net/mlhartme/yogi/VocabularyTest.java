package net.mlhartme.yogi;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VocabularyTest {
    @Test
    public void normal() {
        Vocabulary v;
        List<Integer> done;
        int idx;

        v = Vocabulary.create("1", "one", "2", "two");
        done = new ArrayList<>();
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
}
