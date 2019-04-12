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
        int idx;
        String word;
        Exercise exercise;

        exercise = Exercise.forRequest(base, unit, doneParam);
        if (idxParam == null) {
            idx = exercise.vocabulary.next(exercise.done);
        } else {
            idx = idxParam;
        }
        word = exercise.vocabulary.left(idx);
        model.addAttribute(word);
        model.addAttribute("exercise", exercise);

        model.addAttribute("idx", idx);
        model.addAttribute("word", word);
        model.addAttribute("unit", unit);
        return "question";
    }

    @RequestMapping("/{unit}/answer.html")
    public String answer(Model model, @PathVariable("unit") String unit, @RequestParam("answer") String answer,
                         @RequestParam("idx") int idx, @RequestParam("done") String doneParam) throws IOException {
        Exercise exercise;
        String expected;
        boolean correct;

        exercise = Exercise.forRequest(base, unit, doneParam);
        expected = exercise.vocabulary.right(idx);
        correct = answer.equals(expected);
        if (correct) {
            exercise.done.add(idx);
        }
        model.addAttribute("exercise", exercise);

        model.addAttribute("answer", answer);
        model.addAttribute("expected", expected);
        model.addAttribute("correct", correct);
        model.addAttribute("idx", idx);
        return "answer";
    }
}