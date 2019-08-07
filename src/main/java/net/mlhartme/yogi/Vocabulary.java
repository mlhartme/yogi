package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class Vocabulary {
    private static final Random random = new Random();

    public static Vocabulary create(String ... leftRights) {
        Vocabulary result;

        result = new Vocabulary();
        for (int i = 0; i < leftRights.length; i += 2) {
            result.add(leftRights[i], leftRights[i + 1]);
        }
        return result;
    }

    private final List<String> lefts;
    private final List<String> rights;

    public Vocabulary() {
        this.lefts = new ArrayList<>();
        this.rights = new ArrayList<>();
    }

    public void add(String left, String right) {
        if (lookupLeft(left) >= 0) {
            throw new IllegalArgumentException("duplicate word: " + left);
        }
        this.lefts.add(left);
        this.rights.add(right);
    }

    public int next(IntSet done) {
        int vocabularySize;
        int doneSize;
        int rnd;

        vocabularySize = size();
        doneSize = done.size();
        if (doneSize >= vocabularySize) {
            throw new IllegalStateException("all done");
        }
        rnd = random.nextInt(vocabularySize - doneSize);
        for (int idx = 0; idx < vocabularySize; idx++) {
            if (!done.contains(idx)) {
                if (rnd == 0) {
                    return idx;
                }
                rnd--;
            }
        }
        throw new IllegalStateException();
    }

    public int size() {
        return lefts.size();
    }

    public String left(int idx) {
        return lefts.get(idx);
    }

    public int lookupLeft(String left) {
        return lefts.indexOf(left);
    }


    public String right(int idx) {
        return rights.get(idx);
    }

    public String right(String left) {
        int idx;

        idx = lefts.indexOf(left);
        if (idx == -1) {
            throw new IllegalArgumentException(left);
        }
        return rights.get(idx);
    }
}