package net.mlhartme.yogi;

import net.oneandone.sushi.fs.MkdirException;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class YogiController {
    private final FileNode protocolRoot;
    private final Library library;

    public YogiController(World world) throws IOException {
        this.protocolRoot = world.getWorking().join("protocols");
        this.library = Library.load(world.resource("books"));
    }


    private FileNode userProtocols() throws MkdirException {
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
        model.addAttribute("userProtocols", userProtocols());
        return "book";
    }

    @RequestMapping("/books/{book}/selection")
    public String selection(Model model, @PathVariable(value = "book") String bookName, @RequestParam("title") String title,
                            @RequestParam("selection") String selectionStr) throws IOException {
        IntSet selection;

        selection = IntSet.parse(Separator.COMMA.split(selectionStr));
        model.addAttribute("userProtocols", userProtocols());
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(bookName));
        model.addAttribute("title", title);
        model.addAttribute("selection", selection);
        return "selection";
    }

    @RequestMapping("/books/{book}/select-and-start")
    public String selectAndStart(
            @PathVariable(value = "book") String book,
            @RequestParam("title") String title, HttpServletRequest request /* for selection */) throws IOException {
        IntSet selection;

        selection = getChecked(request, "select_");
        return doStart(library.get(book), title, selection);
    }

    @RequestMapping("/books/{book}/enabled")
    public String enabled(Model model, @PathVariable(value = "book") String bookName, @RequestParam("title") String title,
                            @RequestParam("selection") String selectionStr) throws IOException {
        IntSet selection;

        selection = IntSet.parse(Separator.COMMA.split(selectionStr));
        model.addAttribute("userProtocols", userProtocols());
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(bookName));
        model.addAttribute("title", title);
        model.addAttribute("selection", selection);
        return "enabled";
    }

    @RequestMapping("/books/{book}/set-enabled")
    public String setEnableD(@PathVariable(value = "book") String book, @RequestParam(value = "selection") String selection,
                             HttpServletRequest request /* for selection */)
            throws IOException {
        IntSet enable;

        enable = getChecked(request, "enable_");
        library.get(book).enable(userProtocols(), IntSet.parseArg(selection), enable);
        return "redirect:/books/" + book + "/";
    }

    private static IntSet getChecked(HttpServletRequest request, String prefix) {
        Enumeration<String> names;
        String name;
        IntSet result;

        result = new IntSet();
        names = request.getParameterNames();
        while (names.hasMoreElements()) {
            name = names.nextElement();
            if (name.startsWith(prefix)) {
                result.add(Integer.parseInt(request.getParameter(name)));
            }
        }
        return result;
    }

    @RequestMapping("/books/{book}/start")
    public String start(@PathVariable(value = "book") String bookName,
                        @RequestParam("title") String title, @RequestParam("selection") String selectionStr,
                        @RequestParam("count") String countOrAll) throws IOException {
        Book book;
        IntSet disabled;
        IntSet selection;
        List<Integer> sorted;
        int count;

        book = library.get(bookName);
        disabled = book.disabled(userProtocols());
        selection = IntSet.parse(Separator.COMMA.split(selectionStr));
        selection.removeAll(disabled);
        if (!countOrAll.equals("all")) {
            sorted = book.statistics(userProtocols()).sort(selection, book); // TODO: expensive
            count = Integer.parseInt(countOrAll);
            while (sorted.size() > count) {
                sorted.remove(sorted.size() - 1);
            }
            selection = new IntSet(sorted);;
        }
        return doStart(book, title, selection);
    }

    private String doStart(Book book, String title, IntSet selection) throws IOException {
        Exercise exercise;

        exercise = Exercise.create(book, userProtocols(), title, selection);
        exercise.logTitle(userProtocols(), title);
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
        List<FileNode> protocols;
        LinkedHashMap<String, String> map;
        String basename;

        protocols = Protocol.list(userProtocols(), book);
        Collections.reverse(protocols);
        map = new LinkedHashMap<>();
        for (FileNode log : protocols) {
            basename = log.getBasename();
            map.put(basename, basename + ": " + Protocol.load(log).title());
        }
        model.addAttribute("map", map);
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocols";
    }

    @RequestMapping("/books/{book}/protocols/{id}")
    public String protocol(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "id") long id) throws IOException {
        model.addAttribute("protocol", Protocol.load(userProtocols().join(book, id + ".log")));
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocol";
    }

    //-- exercise

    @PostMapping("/books/{book}/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@PathVariable(value = "book") String book, @RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), body.get("e"));
        exercise.logComment(userProtocols(), body.get("comment"));
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
        exercise.logAnswer(userProtocols(), question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}