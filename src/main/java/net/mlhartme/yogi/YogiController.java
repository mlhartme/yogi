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
import java.util.Random;

@Controller
public class YogiController {
    private World world;
    private Random random = new Random();

    public YogiController(World world) {
        this.world = world;
    }

    @RequestMapping("/{file}")
    public String question(Model model, @PathVariable("file") String file) throws IOException {
        Vocabulary vocabulary;
        int idx;
        String word;

        vocabulary = Vocabulary.load(world, file);
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

        vocabulary = Vocabulary.load(world, file);
        model.addAttribute("answer", answer);
        model.addAttribute("correct", vocabulary.right(idx));
        return "answer";
    }
}