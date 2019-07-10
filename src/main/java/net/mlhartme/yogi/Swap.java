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
        int idx;

        world = World.create();
        for (FileNode file : world.getWorking().join("src/main/resources/data/english").find("*.txt")) {
            lines = file.readLines();
            for (int i = 0; i < lines.size(); i++) {
                line = lines.get(i);
                idx = line.indexOf('=');
                if (idx != -1) {
                    lines.set(i, line.substring(idx + 1).trim() + " = " + line.substring(0, idx).trim());
                }
            }
            file.writeLines(lines);
        }
    }
}
