package net.mlhartme.yogi;

import net.oneandone.sushi.fs.MkdirException;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;
import net.oneandone.sushi.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
    private final FileNode protocolRoot;
    private final Library library;

    public YogiController(World world) throws IOException {
        this.protocolRoot = world.getWorking().join("logs");
        this.library = Library.load(world.resource("books"));
    }


    private FileNode protocolBase() throws MkdirException {
        return protocolRoot.join(YogiSecurity.username()).mkdirsOpt();
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("login", true);
        return "index";
    }

    @RequestMapping("/books")
    public String books() {
        return "redirect:/books/" + library.iterator().next().name + "/";
    }

    @RequestMapping("/books/{book}/")
    public String book(Model model, @PathVariable(value = "book") String book) throws IOException {
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(book));
        model.addAttribute("protocolBase", protocolBase());
        return "book";
    }

    @RequestMapping("/books/{book}/selection")
    public String selection(Model model, @PathVariable(value = "book") String bookName, @RequestParam("title") String title,
                            @RequestParam("selection") String selectionStr) throws IOException {
        IntSet selection;

        selection = IntSet.parse(Separator.COMMA.split(selectionStr));
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(bookName));
        model.addAttribute("title", title);
        model.addAttribute("selection", selection);
        return "selection";
    }

    @RequestMapping("/books/{book}/begin")
    public String begin(Model model, @PathVariable(value = "book") String bookName, @RequestParam("title") String title,
                        @RequestParam("selection") String selectionStr) throws IOException {
        Exercise exercise;

        exercise = Exercise.create(library.get(bookName), protocolBase(), title, IntSet.parse(Separator.COMMA.split(selectionStr)));
        exercise.logTitle(protocolBase(), title);
        return "redirect:question?e=" + urlencode(exercise.toParam());
    }

    private static String urlencode(String str) {
        try {
            return URLEncoder.encode(str, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequestMapping("/books/{book}/protocols/")
    public String protocols(Model model, @PathVariable(value = "book") String book) throws IOException {
        List<Integer> ids;
        LinkedHashMap<Integer, String> map;

        ids = new ArrayList<>();
        for (Node<?> node : protocolBase().find(book + "/*.log")) {
            ids.add(Integer.parseInt(Strings.removeRight(node.getName(), ".log")));
        }
        Collections.sort(ids);
        Collections.reverse(ids);
        map = new LinkedHashMap<>();
        for (int id : ids) {
            map.put(id, id + ": " + Protocol.load(protocolBase().join(book, id + ".log")).title());
        }
        model.addAttribute("map", map);
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocols";
    }

    @RequestMapping("/books/{book}/protocols/{id}")
    public String protocol(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "id") long id) throws IOException {
        model.addAttribute("protocol", Protocol.load(protocolBase().join(book, id + ".log")));
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocol";
    }

    //-- exercise

    @PostMapping("/books/{book}/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@PathVariable(value = "book") String book, @RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), body.get("e"));
        exercise.logComment(protocolBase(), body.get("comment"));
    }

    @RequestMapping("/books/{book}/question")
    public String question(Model model, @PathVariable(value = "book") String book, @RequestParam(value = "e") String e,
                           @RequestParam(value = "question", required = false) String question) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), e);
        if (question == null) {
            question = exercise.question();
        }
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        return "question";
    }

    @RequestMapping("/books/{book}/answer")
    public String answer(Model model, @PathVariable(value = "book") String book, @RequestParam("e") String e, @RequestParam("question") String question, @RequestParam("answer") String answer) throws IOException {
        Exercise exercise;
        String correction;

        answer = answer.trim();
        exercise = Exercise.forParam(library.get(book), e);
        correction = exercise.answer(question, answer);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("correction", correction);
        exercise.logAnswer(protocolBase(), question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}