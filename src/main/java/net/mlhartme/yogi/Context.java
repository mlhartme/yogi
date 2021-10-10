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
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistent session state */
public class Context {
    private final FileNode protocolRoot;

    public Context(World world) {
        this.protocolRoot = world.getWorking().join("protocols");
    }

    public FileNode userProtocols() throws MkdirException {
        return protocolRoot.join(YogiSecurity.username()).mkdirsOpt();
    }

    //-- protocols

    public FileNode protocolFile(String book, long id) throws MkdirException {
        return userProtocols().join(book, id + ".log");
    }

    public List<FileNode> listProtocols(String book) throws IOException {
        List<FileNode> lst;
        FileNode dir;

        dir = userProtocols().join(book);
        if (!dir.exists()) {
            return new ArrayList<>();
        }
        lst = dir.find("*.log");
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

    //-- selections

    public List<String> loadEnabledOpt(String book) throws IOException {
        FileNode file;

        file = enabledFile(book);
        if (!file.exists()) {
            return null;
        }
        return file.readLines();
    }

    public void saveEnabled(String book, List<String> lst) throws IOException {
        enabledFile(book).writeLines(lst);
    }

    private FileNode enabledFile(String book) throws MkdirException {
        FileNode dir;

        dir = userProtocols().join(book).mkdirOpt();
        return dir.join("freigeschaltet.selection");
    }
}
