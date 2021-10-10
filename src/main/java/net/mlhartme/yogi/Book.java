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

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Book implements Comparable<Book> {
    public static final String EXT = ".yogi";

    public static Book load(Node<?> file) throws IOException {
        Book book;
        IntSet current;

        book = new Book(Strings.removeRight(file.getName(), EXT));
        current = null;
        for (String line : file.readLines()) {
            if (line.startsWith("# ")) {
                current = new IntSet();
                book.sections.put(line.substring(2), current);
            } else {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                } else {
                    if (current == null) {
                        throw new IOException("missing section header");
                    }
                    try {
                        current.add(book.addInvLine(line));
                    } catch (IllegalArgumentException e) {
                        System.out.println("TODO: " + e.getMessage());
                    }
                }
            }
        }
        return book;
    }

    public final String name;
    public final LinkedHashMap<String, IntSet> sections;
    private final List<String> lefts;
    private final List<String> rights;

    public Book(String name) {
        this.name = name;
        this.sections = new LinkedHashMap<>();
        this.lefts = new ArrayList<>();
        this.rights = new ArrayList<>();
    }

    public Statistics statistics(Context context) throws IOException {
        return Statistics.collect(context, context.userProtocols(), this);
    }

    public Map<String, IntSet> sections() throws IOException {
        LinkedHashMap<String, IntSet> result;

        result = new LinkedHashMap<>(sections.size() + 1);
        result.putAll(sections);
        return result;
    }

    public String sectionStart(int idx) {
        for (var entry : sections.entrySet()) {
            if (entry.getValue().iterator().next() == idx) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Map<String, String> selection(IntSet selection) {
        LinkedHashMap<String, String> result;

        result = new LinkedHashMap<>(sections.size());
        for (Integer integer : selection) {
            result.put(lefts.get(integer), rights.get(integer));
        }
        return result;
    }

    public IntSet selection(Protocol protocol) {
        IntSet result;
        int idx;

        result = new IntSet();
        for (String question : protocol.questionCount().keySet()) {
            idx = lefts.indexOf(question);
            if (idx < 0) {
                System.out.println("not found: " + question); // TODO - was corrected
            } else {
                result.add(idx);
            }
        }
        return result;
    }

    public void enable(Context context, FileNode userProtocols, IntSet selection, IntSet enable) throws IOException {
        IntSet result;
        List<String> lst;

        result = enabled(context, userProtocols);
        for (int idx : selection) {
            if (enable.contains(idx)) {
                if (!result.contains(idx)) {
                    result.add(idx);
                }
            } else {
                result.remove(idx);
            }
        }
        lst = new ArrayList<>();
        for (int idx : result) {
            lst.add(lefts.get(idx));
        }
        context.saveEnabled(name, lst);
    }

    public IntSet enabled(Context context, FileNode userProtocols) throws IOException {
        List<String> enabled;
        IntSet result;
        Set<String> asked;
        String question;
        int idx;

        enabled = context.loadEnabledOpt(name);
        result = new IntSet();
        if (enabled != null) {
            for (String active : enabled) {
                idx = lefts.indexOf(active);
                if (idx < 0) {
                    // happens if I fix a typo
                    System.out.println("unknown question - ignored: " + active);
                } else {
                    result.add(idx);
                }
            }
        } else {
            throw new IOException("selection not found");  // TODO: create empty?
        }
        return result;
    }

    public IntSet disabled(Context context, FileNode userProtocols) throws IOException {
        IntSet result;
        IntSet enabled;

        enabled = enabled(context, userProtocols);
        result = new IntSet();
        for (int i = 0; i < lefts.size(); i++) {
            if (!enabled.contains(i)) {
                result.add(i);
            }
        }
        return result;
    }

    public int size() {
        return lefts.size();
    }

    public int addInvLine(String line) throws IOException {
        int idx;

        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            throw new IllegalArgumentException(line);
        }
        idx = line.indexOf('=');
        if (idx == -1) {
            throw new IOException("syntax error: " + line);
        }
        return add(line.substring(idx + 1).trim(), line.substring(0, idx).trim());
    }

    public int add(String left, String right) {
        if (lookupLeft(left) >= 0) {
            throw new IllegalArgumentException("duplicate word: " + left);
        }
        this.lefts.add(left);
        this.rights.add(right);
        return lefts.size() - 1;
    }

    public String left(int idx) {
        return lefts.get(idx);
    }

    public String right(int idx) {
        return rights.get(idx);
    }

    public int lookupLeft(String left) {
        return lefts.indexOf(left);
    }

    public IntSet all() {
        IntSet result;

        result = new IntSet();
        for (int i = 0; i < lefts.size(); i++) {
            result.add(i);
        }
        return result;
    }

    @Override
    public int compareTo(Book right) {
        return name.compareTo(right.name);
    }
}
