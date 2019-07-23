package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class YogiController {
    private Node<?> base;
    private FileNode logBase;

    public YogiController(World world) throws IOException {
        this.base = world.resource("data");
        this.logBase = world.getWorking().join("logs").mkdirOpt();
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

    @PostMapping("/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(base, body.get("e"));
        exercise.logComment(logBase, body.get("comment"));
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
        exercise.logAnswer(logBase, question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}