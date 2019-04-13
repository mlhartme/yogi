package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.util.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exercise {
    private static final Separator COLON = Separator.on(':');

    public static Exercise forParam(Node<?> base, String param) throws IOException {
        List<String> args;

        args = COLON.split(param);

        String unit;
        int round;
        String ok;
        String wrong;

        unit = eat(args, "");
        round = Integer.parseInt(eat(args, "1"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(base, unit, round, ok, wrong);
    }

    private static String eat(List<String> lst, String dflt) {
        if (lst.isEmpty()) {
            return dflt;
        } else {
            return lst.remove(0);
        }
    }

    public static Exercise create(Node<?> base, String unit) throws IOException {
        return create(base, unit, 1, null, null);
    }

    public static Exercise create(Node<?> base, String unit, int round, String okParam, String wrongParam) throws IOException {
        List<Integer> ok;
        List<Integer> wrong;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        ok = toInt(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = toInt(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(unit, vocabulary, round, ok, wrong);
    }

    public final String unit;
    public final Vocabulary vocabulary;
    public int round;
    public final List<Integer> ok;
    public final List<Integer> wrong;

    public Exercise(String unit, Vocabulary vocabulary, int round, List<Integer> ok, List<Integer> wrong) {
        if (vocabulary.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.unit = unit;
        this.vocabulary = vocabulary;
        this.round = round;
        this.ok = ok;
        this.wrong = wrong;
    }

    public int number(String question) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        return ok.size() + wrong.size() + (wrong.contains(idx) ? 0 : 1);
    }

    public String question() {
        if (ok.size() + wrong.size() == vocabulary.size()) {
            round++;
            if (wrong.isEmpty()) {
                ok.clear();
                // start from beginning
            } else {
                wrong.clear();
            }
        }
        return vocabulary.left(vocabulary.next(union(ok, wrong)));
    }

    /** null if anwser is correct; otherwise the correct answer */
    public String answer(String question, String answer) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        if (idx == -1) {
            throw new IllegalArgumentException(question);
        }
        if (answer.equals(vocabulary.right(idx))) {
            if (!wrong.contains(idx)) {
                ok.add(idx);
            }
            return null;
        } else {
            if (!wrong.contains(idx)) {
                wrong.add(idx);
            }
            return vocabulary.right(idx);
        }
    }

    public String toParam() {
        return unit + ":" + round + ":" + toString(ok) + ":" + toString(wrong);
    }

    public boolean allDone() {
        return ok.size() == vocabulary.size();
    }

    //--

    private static List<Integer> union(List<Integer> left, List<Integer> right) {
        List<Integer> result;

        result = new ArrayList<>(left.size() + right.size());
        result.addAll(left);
        result.addAll(right);
        return result;
    }

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
