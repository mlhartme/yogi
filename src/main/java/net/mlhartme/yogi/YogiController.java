package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;
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

    public YogiController(World world) throws IOException {
        this.base = world.resource("data/english");
    }

    @RequestMapping("/")
    public String index(Model model) throws IOException {
        List<String> units;

        units = new ArrayList<>();
        for (Node<?> node : base.find("*.txt")) {
            units.add(Strings.removeRight(node.getRelative(base), ".txt"));
        }
        Collections.sort(units);
        model.addAttribute("units", units);
        return "index";
    }
    @RequestMapping("/{unit}")
    public String question(Model model, @PathVariable("unit") String unit, @RequestParam(value = "done", required = false) String doneParam) throws IOException {
        Vocabulary vocabulary;
        int idx;
        String word;
        List<Integer> done;

        done = toInt(doneParam == null ? new ArrayList<>() : Separator.COMMA.split(doneParam));
        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        idx = vocabulary.next(done);
        word = vocabulary.left(idx);
        model.addAttribute(word);
        model.addAttribute("idx", idx);
        model.addAttribute("word", word);
        model.addAttribute("unit", unit);
        model.addAttribute("done", done);
        return "question";
    }

    private static List<Integer> toInt(List<String> strings) {
        List<Integer> result;

        result = new ArrayList<>(strings.size());
        for (String str : strings) {
            result.add(Integer.parseInt(str));
        }
        return result;
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