package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Util {
    public static void main(String[] args) throws IOException {
        World world;
        String name;
        FileNode dest;
        List<FileNode> lst;

        world = World.create();
        name = "Découvertes 1";
        dest = world.getWorking().join("src/main/resources/books/" + name + ".yogi");
        try (Writer writer = dest.newWriter()) {
            lst = world.getWorking().join("src/main/resources/books", name).find("*.txt");
            Collections.sort(lst, new Comparator<FileNode>() {
                @Override
                public int compare(FileNode left, FileNode right) {
                    return left.getName().compareTo(right.getName());
                }
            });
            for (FileNode file : lst) {
                writer.write("# " + Strings.removeRight(file.getName(), ".txt") + "\n");
                for (String line : file.readLines()) {
                    writer.write(line + "\n");
                }
            }
        }
    }
}
