package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.util.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exercise {
    public static Exercise forRequest(Node<?> base, String unit, String doneParam) throws IOException {
        List<Integer> done;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        done = toInt(doneParam == null ? new ArrayList<>() : Separator.COMMA.split(doneParam));
        return new Exercise(vocabulary, done);
    }

    public final Vocabulary vocabulary;
    public final List<Integer> done;

    public Exercise(Vocabulary vocabulary, List<Integer> done) {
        this.vocabulary = vocabulary;
        this.done = done;
    }

    public String doneParam() {
        return toString(done);
    }

    public boolean allDone() {
        return done.size() == vocabulary.size();
    }

    //--


    private static String toString(List<Integer> done) {
        StringBuilder result;

        result = new StringBuilder();
        for (Integer i : done) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(i);
        }
        return result.toString();
    }

    private static List<Integer> toInt(List<String> strings) {
        List<Integer> result;

        result = new ArrayList<>(strings.size());
        for (String str : strings) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

}
