package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class YogiController {
    private World world;

    public YogiController(World world) {
        this.world = world;
    }

    @RequestMapping("/{file}")
    public String hello(Model model, @PathVariable("file") String file) throws IOException {
        Map<String, String> lines;
        Node<?> root;

        try {
            // started with spring-boot:run
            root = world.resource("data");
        } catch (FileNotFoundException e) {
            // started as executable jar
            // TODO: doesn't work yet ...
            root = world.resource("BOOT-INF/classes/data");
        }
        System.out.println("root: " + root);
        lines = load(root.join("english", file));
        model.addAttribute("lines", lines);
        return "yogi";
    }

    private static Map<String, String> load(Node<?>... files) throws IOException {
        List<String> lines;
        Map<String, String> map;
        int idx;

        lines = new ArrayList<>();
        for (Node<?> file : files) {
            lines.addAll(file.readLines());
        }
        map = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            idx = line.indexOf('=');
            if (idx == -1) {
                throw new IOException("syntax error: " + line);
            }
            map.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }
        return map;
    }

}