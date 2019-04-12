package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.util.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exercise {
    public static Exercise forParam(Node<?> base, String param) throws IOException {
        int idx;
        String unit;
        String ok;
        String wrong;

        idx = param.indexOf(':');
        if (idx == -1) {
            unit = param;
            ok = null;
            wrong = null;
        } else {
            unit = param.substring(0, idx);
            param = param.substring(idx + 1);
            idx = param.indexOf(':');
            if (idx == -1) {
                ok = param;
                wrong = null;
            } else {
                ok = param.substring(0, idx);
                wrong = param.substring(idx + 1);
            }
        }
        return create(base, unit, ok, wrong);
    }

    public static Exercise create(Node<?> base, String unit) throws IOException {
        return create(base, unit, null, null);
    }

    public static Exercise create(Node<?> base, String unit, String okParam, String wrongParam) throws IOException {
        List<Integer> ok;
        List<Integer> wrong;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        ok = toInt(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = toInt(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(unit, vocabulary, ok, wrong);
    }

    public final String unit;
    public final Vocabulary vocabulary;
    public final List<Integer> ok;
    public final List<Integer> wrong;

    public Exercise(String unit, Vocabulary vocabulary, List<Integer> ok, List<Integer> wrong) {
        this.unit = unit;
        this.vocabulary = vocabulary;
        this.ok = ok;
        this.wrong = wrong;
    }

    public String question() {
        return vocabulary.left(vocabulary.next(ok));
    }

    /** null if anwser is correct; otherwise the correct answer */
    public String answer(String question, String answer) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        if (idx == -1) {
            throw new IllegalArgumentException(question);
        }
        if (answer.equals(vocabulary.right(idx))) {
            ok.add(idx);
            return null;
        } else {
            return vocabulary.right(idx);
        }
    }

    public String toParam() {
        return unit + ":" + toString(ok) + ":" + toString(wrong);
    }

    public boolean allDone() {
        return ok.size() == vocabulary.size();
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
