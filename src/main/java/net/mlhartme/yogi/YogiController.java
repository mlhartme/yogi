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
        List<String> files;

        files = new ArrayList<>();
        for (Node<?> node : base.find("**/*.txt")) {
            files.add(Strings.removeRight(node.getRelative(base), ".txt"));
        }
        Collections.sort(files);
        model.addAttribute("base", base);
        model.addAttribute("files", files);
        return "index";
    }

    @RequestMapping("/question.html")
    public String question(Model model, @RequestParam(value = "e") String e,
                           @RequestParam(value = "question", required = false) String question) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(base, e);
        if (question == null) {
            question = exercise.question();
        }
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        return "question";
    }

    @RequestMapping("/answer.html")
    public String answer(Model model, @RequestParam("e") String e, @RequestParam("question") String question, @RequestParam("answer") String answer) throws IOException {
        Exercise exercise;
        String correction;

        exercise = Exercise.forParam(base, e);
        correction = exercise.answer(question, answer);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("correction", correction);
        return "answer";
    }
}