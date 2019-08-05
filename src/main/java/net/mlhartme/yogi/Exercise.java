package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exercise {
    private static final Separator COLON = Separator.on(':');

    public static Exercise forParam(Node<?> base, FileNode logbase, String param) throws IOException {
        List<String> args;

        args = COLON.split(param);

        int id;
        String file;
        int round;
        int ofs;
        String ok;
        String wrong;

        id = Integer.parseInt(eat(args, "-1"));
        if (id == -1) {
            id = next(logbase);
        }
        file = eat(args, "");
        round = Integer.parseInt(eat(args, "1"));
        ofs = Integer.parseInt(eat(args, "0"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(id, base, file, round, ofs, ok, wrong);
    }

    private static int next(Node<?> logbase) throws IOException {
        int id;
        int max;

        max = 0;
        for (Node<?> file : logbase.list()) {
            try {
                id = Integer.parseInt(Strings.removeRight(file.getName(), ".log"));
            } catch (NumberFormatException e) {
                throw new IOException("unexpected name: " + file.getName());
            }
            max = Math.max(id, max);
        }
        return max + 1;
    }

    private static String eat(List<String> lst, String dflt) {
        if (lst.isEmpty()) {
            return dflt;
        } else {
            return lst.remove(0);
        }
    }

    public static Exercise create(Node<?> base, Node<?> logbase, String file) throws IOException {
        Vocabulary vocabulary;

        vocabulary = Vocabulary.loadInv(base.join(file + ".txt"));
        return new Exercise(next(logbase), file, vocabulary, 1, 0, new IntSet(), new IntSet());
    }

    public static Exercise create(int id, Node<?> base, String file, int round, int ofs, String okParam, String wrongParam) throws IOException {
        IntSet ok;
        IntSet wrong;
        Vocabulary vocabulary;

        vocabulary = Vocabulary.loadInv(base.join(file + ".txt"));
        ok = IntSet.parse(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = IntSet.parse(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(id, file, vocabulary, round, ofs, ok, wrong);
    }

    public final int id;
    public final String file;
    public final Vocabulary vocabulary;
    public int round;
    public int ofs;  // number of oks when this round started
    public final IntSet ok;  // oks in this and previous rounds
    public final IntSet wrong; // wrong answers in this round

    public Exercise(int id, String file, Vocabulary vocabulary, int round, int ofs, IntSet ok, IntSet wrong) {
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

    public void logTitle(FileNode logbase, String title) throws IOException {
        doLog(logbase, "! " + title);
    }

    public void logComment(FileNode logbase, String comment) throws IOException {
        doLog(logbase, "# " + comment);
    }

    public void logAnswer(FileNode logbase, String question, String answer, String correct) throws IOException {
        doLog(logbase, question + " -> " + answer + " -> " + correct);
    }

    private void doLog(FileNode logbase, String line) throws IOException {
        try (Writer writer = logbase.join(id + ".log").newAppender()) {
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
        next = vocabulary.next(IntSet.union(ok, wrong));
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
        return id + ":" + file + ":" + round + ":" + ofs + ":" + ok.toString() + ":" + wrong.toString();
    }

    public boolean allDone() {
        return ok.size() == vocabulary.size();
    }
}
