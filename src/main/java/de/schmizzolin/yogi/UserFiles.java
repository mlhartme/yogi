/*
 * Copyright Michael Hartmeier, https://github.com/mlhartme/
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
package de.schmizzolin.yogi;

import net.oneandone.sushi.fs.MkdirException;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistent session state per user */
public class UserFiles {
    /* <yogi.lib>/<username> */
    private final FileNode root;

    public UserFiles(FileNode lib) throws IOException {
        this.root = lib.join(YogiSecurity.username()).mkdirsOpt();
        migrationBefore14();
    }

    // TODO: dump when 1.4 rollout is done
    private void migrationBefore14() throws IOException {
        for (FileNode book : root.list()) {
            if (book.isDirectory()) {
                migrateBefore14selection(book);
                migrateBefore14protocols(book);
            }
        }
    }

    // TODO: dump when 1.4 rollout is done
    private void migrateBefore14protocols(FileNode book) throws IOException {
        var oldSelection = book.join(".enabled");
        if (oldSelection.exists()) {
            var newSelection = book.join("freigeschaltet.selection");
            System.out.println("selection migration " + oldSelection + " -> " + newSelection);
            oldSelection.move(newSelection);
        }
    }

    // TODO: dump when 1.4 rollout is done
    private void migrateBefore14selection(FileNode book) throws IOException {
        for (FileNode oldProtocol : book.find("*.log")) {
            FileNode newProtocol = book.join(oldProtocol.getBasename() + ".protocol");
            System.out.println("protocol migration " + oldProtocol + " -> " + newProtocol);
            oldProtocol.move(newProtocol);
        }
    }

    //-- protocols

    private static final String PROTOCOL_EXT = ".protocol";

    public FileNode protocolFile(String book, long id) {
        return root.join(book, id + PROTOCOL_EXT);
    }

    public int nextProtocol(String book) throws IOException {
        Node<?> dir;
        int id;
        int max;

        dir = root.join(book);
        max = 0;
        if (dir.exists()) {
            for (Node<?> file : dir.find("*" + PROTOCOL_EXT)) {
                try {
                    id = Integer.parseInt(file.getBasename());
                } catch (NumberFormatException e) {
                    throw new IOException("unexpected name: " + file.getName());
                }
                max = Math.max(id, max);
            }
        }
        return max + 1;
    }

    public List<FileNode> listProtocols(String book) throws IOException {
        List<FileNode> lst;
        FileNode dir;

        dir = root.join(book);
        if (!dir.exists()) {
            return new ArrayList<>();
        }
        lst = dir.find("*" + PROTOCOL_EXT);
        Collections.sort(lst, (o1, o2) -> {
            Integer left;
            Integer right;

            left = numberOpt(o1);
            right = numberOpt(o2);
            if (left != null && right != null) {
                return left.compareTo(right);
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return lst;
    }


    private static Integer numberOpt(FileNode log) {
        try {
            return Integer.parseInt(log.getBasename());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //-- book image


    //-- selections

    public List<String> loadSelectionOpt(String book, String name) throws IOException {
        FileNode file;

        file = selectionFile(book, name);
        return file.exists() ? file.readLines() : null;
    }

    public void saveSelection(String book, String name, List<String> lst) throws IOException {
        selectionFile(book, name).writeLines(lst);
    }

    private FileNode selectionFile(String book, String name) throws MkdirException {
        FileNode dir;

        dir = root.join(book).mkdirOpt();
        return dir.join(name + ".selection");
    }

    public List<String> listSelections(String book) throws IOException {
        FileNode dir;
        List<String> result;

        result = new ArrayList<>();
        dir = root.join(book);
        if (dir.isDirectory()) {
            for (var file : dir.find("*.selection")) {
                result.add(file.getBasename());
            }
        }
        return result;
    }

    public String newSelection(String book, String orig) throws IOException {
        FileNode file;
        String name;

        for (int i = 1; true; i++) {
            name = "auswahl-" + i;
            file = selectionFile(book, name);
            if (!file.exists()) {
                selectionFile(book, orig).copyFile(file);
                return name;
            }
        }
    }

    public void deleteSelection(String book, String name) throws IOException {
        selectionFile(book, name).deleteFile();
    }
}
