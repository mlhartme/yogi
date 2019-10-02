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

    public static Exercise forParam(Book book, String param) throws IOException {
        List<String> args;

        args = COLON.split(param);

        int id;
        String selection;
        String title;
        int round;
        int ofs;
        String ok;
        String wrong;

        id = Integer.parseInt(eat(args, "-1"));
        if (id == -1) {
            throw new IllegalStateException();
        }
        title = eat(args, "");
        selection = eat(args, null);
        if (selection == null) {
            throw new IllegalStateException();
        }
        round = Integer.parseInt(eat(args, "1"));
        ofs = Integer.parseInt(eat(args, "0"));
        ok = eat(args, null);
        wrong = eat(args, null);
        return create(id, book, title, selection, round, ofs, ok, wrong);
    }

    private static int next(Node<?> userProtocols, String book) throws IOException {
        Node<?> dir;
        int id;
        int max;

        dir = userProtocols.join(book);
        max = 0;
        if (dir.exists()) {
            for (Node<?> file : dir.find("*.log")) {
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

    public static Exercise create(Book book, Node<?> userProtocols, String title, IntSet selection) throws IOException {
        return new Exercise(next(userProtocols, book.name), book, title, selection, 1, 0, new IntSet(), new IntSet());
    }

    public static Exercise create(int id, Book book, String title, String selectionParam, int round, int ofs, String okParam, String wrongParam) {
        IntSet selection;
        IntSet ok;
        IntSet wrong;

        selection = IntSet.parse(Separator.COMMA.split(selectionParam));
        ok = IntSet.parse(okParam == null ? new ArrayList<>() : Separator.COMMA.split(okParam));
        wrong = IntSet.parse(wrongParam == null ? new ArrayList<>() : Separator.COMMA.split(wrongParam));
        return new Exercise(id, book, title, selection, round, ofs, ok, wrong);
    }

    public final int id;
    public final Book book;
    public final String title;
    private final IntSet selection;
    public int round;
    public int ofs;  // number of oks when this round started
    public final IntSet ok;  // oks in this and previous rounds
    public final IntSet wrong; // wrong answers in this round

    public Exercise(int id, Book book, String title, IntSet selection, int round, int ofs, IntSet ok, IntSet wrong) {
        this.id = id;
        this.book = book;
        this.title = title;
        this.selection = selection;
        this.round = round;
        this.ofs = ofs;
        this.ok = ok;
        this.wrong = wrong;
    }

    public void logTitle(FileNode userProtocols, String title) throws IOException {
        doLog(userProtocols, "! " + title);
    }

    public void logComment(FileNode userProtocols, String comment) throws IOException {
        doLog(userProtocols, "# " + comment);
    }

    public void logAnswer(FileNode userProtocols, String question, String answer, String correct) throws IOException {
        doLog(userProtocols, question + " -> " + answer + " -> " + correct);
    }

    private void doLog(FileNode userProtocols, String line) throws IOException {
        try (Writer writer = userProtocols.join(book.name).mkdirOpt().join(id + ".log").newAppender()) {
            writer.append(Protocol.FMT.format(new Date()));
            writer.append(' ');
            writer.append(line.replace("\n", " // "));
            writer.append('\n');
        }
    }
    public int roundSize() {
        return selection.size() - ofs;
    }

    public int number(String question) {
        int idx;

        idx = book.lookupLeft(question);
        return ok.size() + wrong.size() + (wrong.contains(idx) ? 0 : 1) - ofs;
    }

    public String question() {
        int next;

        if (ok.size() + wrong.size() == selection.size()) {
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
        next = selection.next(IntSet.union(ok, wrong));
        if (ok.contains(next)) {
            throw new IllegalStateException(ok.toString() + " vs " + next);
        }
        if (wrong.contains(next)) {
            throw new IllegalStateException(wrong.toString() + " vs " + next);
        }
        return book.left(next);
    }

    /** null if answer is correct; otherwise the correct answer */
    public String answer(String question, String answer) {
        int idx;

        idx = book.lookupLeft(question);
        if (idx == -1) {
            throw new IllegalArgumentException(question);
        }
        if (!selection.contains(idx)) {
            throw new IllegalArgumentException(selection + " " + idx);
        }
        if (answer.equals(book.right(idx))) {
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
            return book.right(idx);
        }
    }

    public String lookup(String question) {
        int idx;

        idx = book.lookupLeft(question);
        if (idx == -1) {
            return null;
        } else {
            return book.right(idx);
        }
    }

    public String toParam() {
        return id + ":" + title + ":" + selection.toString() + ":" + round + ":" + ofs + ":" + ok.toString() + ":" + wrong.toString();
    }

    public boolean allDone() {
        return ok.size() == selection.size();
    }
}
