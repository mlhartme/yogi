package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exercise {
    private static final Separator COLON = Separator.on(':');

    public static Exercise forParam(Node<?> base, String param) throws IOException {
        List<String> args;

        args = COLON.split(param);

        Long id;
        String file;
        int round;
        int ofs;
        String ok;
        String wrong;

        id = Long.parseLong(eat(args, Long.toHexString(System.currentTimeMillis())), 16);
        file = eat(args, "");
        round = Integer.parseInt(eat(args, "1"));
        ofs = Integer.parseInt(eat(args, "0"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(id, base, file, round, ofs, ok, wrong);
    }

    private static String eat(List<String> lst, String dflt) {
        if (lst.isEmpty()) {
            return dflt;
        } else {
            return lst.remove(0);
        }
    }

    public static Exercise create(Node<?> base, String file) throws IOException {
        return create(System.currentTimeMillis(), base, file, 1, 0, null, null);
    }
    public static Exercise create(Long id, Node<?> base, String file, int round, int ofs, String okParam, String wrongParam) throws IOException {
        List<Integer> ok;
        List<Integer> wrong;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.loadInv(base.join(file + ".txt"));
        ok = toInt(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = toInt(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(id, file, vocabulary, round, ofs, ok, wrong);
    }

    public final Long id;
    public final String file;
    public final Vocabulary vocabulary;
    public int round;
    public int ofs;  // number of oks when this round started
    public final List<Integer> ok;  // oks in this and previous rounds
    public final List<Integer> wrong; // wrong answers in this round

    public Exercise(Long id, String file, Vocabulary vocabulary, int round, int ofs, List<Integer> ok, List<Integer> wrong) {
        if (vocabulary.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.file = file;
        this.vocabulary = vocabulary;
        this.round = round;
        this.ofs = ofs;
        this.ok = ok;
        this.wrong = wrong;
    }

    public void logComment(FileNode base, String comment) throws IOException {
        doLog(base, "# " + comment);
    }

    public void logAnswer(FileNode base, String question, String answer, String correct) throws IOException {
        doLog(base, question + " -> " + answer + " -> " + correct);
    }

    private void doLog(FileNode base, String line) throws IOException {
        try (Writer writer = base.join(Long.toHexString(id) + ".log").newAppender()) {
            writer.append(Protocol.FMT.format(new Date()));
            writer.append(' ');
            writer.append(line.replace("\n", " // "));
            writer.append('\n');
        }
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
        int next;

        if (ok.size() + wrong.size() == vocabulary.size()) {
            round++;
            if (wrong.isEmpty()) {
                // all correct - start from beginning
                ofs = 0;
                ok.clear();
            } else {
                ofs = ok.size();
                wrong.clear();
            }
        }
        next = vocabulary.next(union(ok, wrong));
        if (ok.contains(next)) {
            throw new IllegalStateException(ok.toString() + " vs " + next);
        }
        if (wrong.contains(next)) {
            throw new IllegalStateException(wrong.toString() + " vs " + next);
        }
        return vocabulary.left(next);
    }

    /** null if answer is correct; otherwise the correct answer */
    public String answer(String question, String answer) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        if (idx == -1) {
            throw new IllegalArgumentException(question);
        }
        if (answer.equals(vocabulary.right(idx))) {
            if (wrong.contains(idx)) {
                // question was re-asked
            } else {
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

    public String lookup(String question) {
        int idx;

        idx = vocabulary.lookupLeft(question);
        if (idx == -1) {
            return null;
        } else {
            return vocabulary.right(idx);
        }
    }

    public String toParam() {
        return Long.toHexString(id) + ":" + file + ":" + round + ":" + ofs + ":" + toString(ok) + ":" + toString(wrong);
    }

    public boolean allDone() {
        return ok.size() == vocabulary.size();
    }

    //--

    public static List<Integer> union(List<Integer> left, List<Integer> right) {
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
