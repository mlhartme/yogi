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
    @RequestMapping("/{unit}/question.html")
    public String question(Model model, @PathVariable("unit") String unit,
                           @RequestParam(value = "idx", required = false) Integer idxParam,
                           @RequestParam(value = "done", required = false) String doneParam) throws IOException {
        Vocabulary vocabulary;
        int idx;
        String word;
        List<Integer> done;

        done = doneParam(doneParam);
        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        if (idxParam == null) {
            idx = vocabulary.next(done);
        } else {
            idx = idxParam;
        }
        word = vocabulary.left(idx);
        model.addAttribute(word);
        model.addAttribute("idx", idx);
        model.addAttribute("word", word);
        model.addAttribute("unit", unit);
        model.addAttribute("done", toString(done));
        return "question";
    }

    private static List<Integer> doneParam(String doneParam) {
        return toInt(doneParam == null ? new ArrayList<>() : Separator.COMMA.split(doneParam));
    }

    private static String toString(List<Integer> done) {
        StringBuilder result;

        result = new StringBuilder();
        for (Integer i : done) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(i);
        }
        return result.toString();
    }

    private static List<Integer> toInt(List<String> strings) {
        List<Integer> result;

        result = new ArrayList<>(strings.size());
        for (String str : strings) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

    @RequestMapping("/{unit}/answer.html")
    public String answer(Model model, @PathVariable("unit") String unit, @RequestParam("answer") String answer,
                         @RequestParam("idx") int idx, @RequestParam("done") String doneParam) throws IOException {
        Vocabulary vocabulary;
        List<Integer> done;
        String expected;
        boolean correct;

        done = doneParam(doneParam);
        vocabulary = Vocabulary.load(base.join(unit + ".txt"));
        expected = vocabulary.right(idx);
        correct = answer.equals(expected);
        if (correct) {
            done.add(idx);
        }
        model.addAttribute("answer", answer);
        model.addAttribute("expected", expected);
        model.addAttribute("correct", correct);
        model.addAttribute("idx", idx);
        model.addAttribute("done", toString(done));
        return "answer";
    }
}