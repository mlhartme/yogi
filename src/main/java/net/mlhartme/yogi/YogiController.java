package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Controller
public class YogiController {
    private Node<?> base;
    private Random random = new Random();

    public YogiController(World world) throws IOException {
        this.base = world.resource("data/english");
    }

    @RequestMapping("/")
    public String index(Model model) throws IOException {
        List<String> files;

        files = new ArrayList<>();
        for (Node<?> node : base.find("**/*.txt")) {
            files.add(Strings.removeRight(node.getRelative(base), ".txt"));
        }
        Collections.sort(files);
        model.addAttribute("files", files);
        return "index";
    }
    @RequestMapping("/{file}")
    public String question(Model model, @PathVariable("file") String file) throws IOException {
        Vocabulary vocabulary;
        int idx;
        String word;

        file = file.replace("=", "/") + ".txt";
        vocabulary = Vocabulary.load(base.join(file));

        idx = random.nextInt(vocabulary.size());
        word = vocabulary.left(idx);
        model.addAttribute(word);
        model.addAttribute("idx", idx);
        model.addAttribute("word", word);
        model.addAttribute("file", file);
        return "question";
    }

    @RequestMapping("/{file}/answer.html")
    public String answer(Model model, @PathVariable("file") String file, @RequestParam("answer") String answer,
                         @RequestParam("idx") int idx) throws IOException {
        Vocabulary vocabulary;

        file = file.replace("=", "/") + ".txt";
        vocabulary = Vocabulary.load(base.join(file));

        model.addAttribute("answer", answer);
        model.addAttribute("correct", vocabulary.right(idx));
        return "answer";
    }
}