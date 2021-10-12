/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mlhartme.yogi;

import net.oneandone.sushi.fs.MkdirException;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exercise {
    private static final Separator COLON = Separator.on(':');

    public static Exercise forParam(Book book, String param) {
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

    private static String eat(List<String> lst, String dflt) {
        if (lst.isEmpty()) {
            return dflt;
        } else {
            return lst.remove(0);
        }
    }

    public static Exercise create(int id, Book book, String title, IntSet selection) {
        return new Exercise(id, book, title, selection, 1, 0, new IntSet(), new IntSet());
    }

    public static Exercise create(int id, Book book, String title, String selectionParam, int round, int ofs, String okParam, String wrongParam) {
        IntSet selection;
        IntSet ok;
        IntSet wrong;

        selection = IntSet.parseArg(selectionParam);
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

    public void logTitle(FileNode userProtocols, String withTitle) throws IOException {
        doLog(userProtocols, "! " + withTitle);
    }

    public void logComment(FileNode userProtocols, String comment) throws IOException {
        doLog(userProtocols, "# " + comment);
    }

    public void logAnswer(FileNode userProtocols, String question, String answer, String correct) throws IOException {
        doLog(userProtocols, question + " -> " + answer + " -> " + correct);
    }

    private void doLog(FileNode userProtocols, String line) throws IOException {
        try (Writer writer = protocolFile(userProtocols).newAppender()) {
            writer.append(Protocol.FMT.format(new Date()));
            writer.append(' ');
            writer.append(line.replace("\n", " // "));
            writer.append('\n');
        }
    }

    private FileNode protocolFile(FileNode userProtocols) throws MkdirException {  // TODO: move this code
        return userProtocols.join(book.name).mkdirOpt().join(id + UserFiles.PROTOCOL_EXT);
    }

    public Protocol protocol(FileNode userProtocols) throws IOException {
        return Protocol.load(protocolFile(userProtocols));
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
