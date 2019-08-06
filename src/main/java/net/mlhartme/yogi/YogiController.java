package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class YogiController {
    private Node<?> base;
    private FileNode protocolBase;

    public YogiController(World world) throws IOException {
        this.base = world.resource("books");
        this.protocolBase = world.getWorking().join("logs").mkdirOpt();
    }

    @RequestMapping("/")
    public String index(Model model) throws IOException {
        List<Book> books;

        books = new ArrayList<>();
        for (Node<?> book : base.find("*.yogi")) {
            books.add(Book.load(book));
        }
        Collections.sort(books);
        model.addAttribute("base", base);
        model.addAttribute("books", books);
        return "index";
    }

    @PostMapping("/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(base, body.get("e"));
        exercise.logComment(protocolBase, body.get("comment"));
    }

    @RequestMapping("/begin")
    public String begin(Model model, @RequestParam(value = "book") String bookName, String section) throws IOException {
        Exercise exercise;

        exercise = Exercise.create(base, protocolBase, bookName, section);
        exercise.logTitle(protocolBase, section);
        return "redirect:question.html?e=" + urlencode(exercise.toParam());
    }

    private static String urlencode(String str) {
        try {
            return URLEncoder.encode(str, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
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

    @RequestMapping("/protocols/{book}")
    public String protocols(Model model, @PathVariable(value = "book") String book) throws IOException {
        List<Integer> ids;
        LinkedHashMap<Integer, String> map;

        ids = new ArrayList<>();
        for (Node<?> node : protocolBase.find(book + "/*.log")) {
            ids.add(Integer.parseInt(Strings.removeRight(node.getName(), ".log")));
        }
        Collections.sort(ids);
        Collections.reverse(ids);
        map = new LinkedHashMap<>();
        for (int id : ids) {
            map.put(id, id + ": " + Protocol.load(protocolBase.join(book, id + ".log")).title());
        }
        model.addAttribute("map", map);
        return "protocols";
    }

    @RequestMapping("/protocols/{book}/{id}")
    public String protocol(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "id") long id) throws IOException {
        model.addAttribute("protocol", Protocol.load(protocolBase.join(book, id + ".log")));
        return "protocol";
    }

    @RequestMapping("/answer.html")
    public String answer(Model model, @RequestParam("e") String e, @RequestParam("question") String question, @RequestParam("answer") String answer) throws IOException {
        Exercise exercise;
        String correction;

        answer = answer.trim();
        exercise = Exercise.forParam(base, e);
        correction = exercise.answer(question, answer);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("correction", correction);
        exercise.logAnswer(protocolBase, question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}