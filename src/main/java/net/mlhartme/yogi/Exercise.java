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

        String file;
        int round;
        int ofs;
        String ok;
        String wrong;

        file = eat(args, "");
        round = Integer.parseInt(eat(args, "1"));
        ofs = Integer.parseInt(eat(args, "0"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(base, file, round, ofs, ok, wrong);
    }

    private static String eat(List<String> lst, String dflt) {
        if (lst.isEmpty()) {
            return dflt;
        } else {
            return lst.remove(0);
        }
    }

    public static Exercise create(Node<?> base, String file) throws IOException {
        return create(base, file, 1, 0, null, null);
    }

    public static Exercise create(Node<?> base, String file, int round, int ofs, String okParam, String wrongParam) throws IOException {
        List<Integer> ok;
        List<Integer> wrong;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.load(base.join(file + ".txt"));
        ok = toInt(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = toInt(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(file, vocabulary, round, ofs, ok, wrong);
    }

    public final String file;
    public final Vocabulary vocabulary;
    public int round;
    public int ofs;
    public final List<Integer> ok;
    public final List<Integer> wrong;

    public Exercise(String file, Vocabulary vocabulary, int round, int ofs, List<Integer> ok, List<Integer> wrong) {
        if (vocabulary.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.file = file;
        this.vocabulary = vocabulary;
        this.round = round;
        this.ofs = ofs;
        this.ok = ok;
        this.wrong = wrong;
    }

    public int roundSize() {
        return vocabulary.size() - ofs;
    }

    public int number(String question) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        return ok.size() + wrong.size() + (wrong.contains(idx) ? 0 : 1) - ofs;
    }

    public String question() {
        if (ok.size() + wrong.size() == vocabulary.size()) {
            round++;
            if (wrong.isEmpty()) {
                ofs = 0;
                ok.clear();
                // start from beginning
            } else {
                ofs = ok.size();
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
        return file + ":" + round + ":" + ofs + ":" + toString(ok) + ":" + toString(wrong);
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
