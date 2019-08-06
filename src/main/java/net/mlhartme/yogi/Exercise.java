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

    public static Exercise forParam(Library library, String param) throws IOException {
        List<String> args;

        args = COLON.split(param);

        int id;
        String book;
        String section;
        int round;
        int ofs;
        String ok;
        String wrong;

        id = Integer.parseInt(eat(args, "-1"));
        if (id == -1) {
            throw new IllegalStateException();
        }
        book = eat(args, "");
        section = eat(args, "");
        round = Integer.parseInt(eat(args, "1"));
        ofs = Integer.parseInt(eat(args, "0"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(id, library.get(book), section, round, ofs, ok, wrong);
    }

    private static int next(Node<?> protocolBase, String book) throws IOException {
        Node<?> dir;
        int id;
        int max;

        dir = protocolBase.join(book);
        max = 0;
        if (dir.exists()) {
            for (Node<?> file : dir.list()) {
                try {
                    id = Integer.parseInt(Strings.removeRight(file.getName(), ".log"));
                } catch (NumberFormatException e) {
                    throw new IOException("unexpected name: " + file.getName());
                }
                max = Math.max(id, max);
            }
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

    public static Exercise create(Book book, Node<?> protocolBase, String section) throws IOException {
        Vocabulary vocabulary;

        vocabulary = book.sections.get(section);
        return new Exercise(next(protocolBase, book.name), book.name, section, vocabulary, 1, 0, new IntSet(), new IntSet());
    }

    public static Exercise create(int id, Book book, String section, int round, int ofs, String okParam, String wrongParam) throws IOException {
        Vocabulary vocabulary;
        IntSet ok;
        IntSet wrong;

        vocabulary = book.sections.get(section);
        ok = IntSet.parse(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = IntSet.parse(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(id, book.name, section, vocabulary, round, ofs, ok, wrong);
    }

    public final int id;
    public final String book;
    public final String section;
    public final Vocabulary vocabulary;
    public int round;
    public int ofs;  // number of oks when this round started
    public final IntSet ok;  // oks in this and previous rounds
    public final IntSet wrong; // wrong answers in this round

    public Exercise(int id, String book, String section, Vocabulary vocabulary, int round, int ofs, IntSet ok, IntSet wrong) {
        if (vocabulary.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.book = book;
        this.section = section;
        this.vocabulary = vocabulary;
        this.round = round;
        this.ofs = ofs;
        this.ok = ok;
        this.wrong = wrong;
    }

    public void logTitle(FileNode protocolBase, String title) throws IOException {
        doLog(protocolBase, "! " + title);
    }

    public void logComment(FileNode protocolBase, String comment) throws IOException {
        doLog(protocolBase, "# " + comment);
    }

    public void logAnswer(FileNode protocolBase, String question, String answer, String correct) throws IOException {
        doLog(protocolBase, question + " -> " + answer + " -> " + correct);
    }

    private void doLog(FileNode protocolBase, String line) throws IOException {
        try (Writer writer = protocolBase.join(book).mkdirOpt().join(id + ".log").newAppender()) {
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
        return id + ":" + book + ":" + section + ":" + round + ":" + ofs + ":" + ok.toString() + ":" + wrong.toString();
    }

    public boolean allDone() {
        return ok.size() == vocabulary.size();
    }
}
