package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.List;

public class Swap {
    public static void main(String[] args) throws IOException {
        World world;
        List<String> lines;
        String line;
        int first;
        int last;

        world = World.create();
        for (FileNode file : world.getWorking().join("prod-logs").find("*.log")) {
            lines = file.readLines();
            for (int i = 0; i < lines.size(); i++) {
                line = lines.get(i);
                if (line.endsWith("null")) {
                    first = line.indexOf(" -> ");
                    if (first == -1) {
                        throw new IOException("invalid line: " + line);
                    }
                    last = line.lastIndexOf(" -> ");
                    if (last == -1) {
                        throw new IOException("invalid line: " + line);
                    }
                    if (first == last) {
                        throw new IOException("invalid line: " + line);
                    }
                    try {
                        line = line.substring(0, last + 4) + line.substring(first + 4, last);
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(e.getMessage());
                    }
                    lines.set(i, line);
                }
            }
            //file.getParent().join(file.getName() + ".null").
            file.writeLines(lines);
        }
    }
}
