/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Separator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final UserFiles context;
    private final Library library;
    private final String version;

    public YogiController(World world, UserFiles context) throws IOException {
        FileNode src;

        this.context = context;
        src = world.file("/usr/local/yogi/etc/books");
        this.library = src.exists() ? Library.load(src) : new Library(); // for SpringBoot test
        this.version = world.resource("yogi.version").readString().trim();
    }

    @ModelAttribute("yogiVersion")
    protected String getVersion() {
        return version;
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("login", true);
        return "index";
    }

    @RequestMapping("/books")
    public String books() {
        return "redirect:/books/" + library.iterator().next().name + "/"; // TODO: doesnt work for french books
    }

    @RequestMapping("/books/{book}/")
    public String book(Model model, @PathVariable(value = "book") String book) throws IOException {
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(book));
        return "book";
    }

    @RequestMapping("/books/{book}/selection")
    public String selection(Model model, @PathVariable(value = "book") String bookName, @RequestParam("title") String title,
                          @RequestParam(value = "selection", required = false) String selectionStr) throws IOException {
        Book book;
        IntSet selection;

        book = library.get(bookName);
        selection = selectionStr == null ? book.all() : IntSet.parse(Separator.COMMA.split(selectionStr));
        model.addAttribute("library", library);
        model.addAttribute("book", book);
        model.addAttribute("title", title);
        model.addAttribute("selection", selection);
        return "selection";
    }

    @RequestMapping("/books/{book}/set-selection")
    public String setSelection(@PathVariable(value = "book") String book, @RequestParam(value = "selection") String selection,
                               HttpServletRequest request /* for selection */)
            throws IOException {
        IntSet enable;

        enable = getChecked(request, "enable_");
        library.get(book).enable(context, IntSet.parseArg(selection), enable);
        return "redirect:";
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
                        @RequestParam("title") String title, @RequestParam("count") String countOrAll) throws IOException {
        Book book;
        IntSet selection;
        List<Integer> sorted;
        int count;

        book = library.get(bookName);
        selection = book.enabled(context);
        if (!countOrAll.equals("all")) {
            sorted = book.statistics(context).sort(selection, book); // TODO: expensive
            count = Integer.parseInt(countOrAll);
            while (sorted.size() > count) {
                sorted.remove(sorted.size() - 1);
            }
            selection = new IntSet(sorted);
        }
        return doStart(book, title, selection);
    }

    private String doStart(Book book, String title, IntSet selection) throws IOException {
        Exercise exercise;

        exercise = Exercise.create(context.nextProtocol(book.name), book, title, selection);
        exercise.logTitle(context.root(), title);
        return "redirect:question?e=" + urlencode(exercise.toParam());
    }

    public static String urlencode(String str) {
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
        Protocol p;
        Book b;
        Achievement a;
        String basename;

        protocols = context.listProtocols(book);
        Collections.reverse(protocols);
        map = new LinkedHashMap<>();
        b = library.get(book);
        for (FileNode log : protocols) {
            basename = log.getBasename();
            p = Protocol.load(log);
            a = p.achievement(context, b);
            map.put(basename, p.date() + " " + p.words() + " " + a.before + " -> " + a.after);
        }
        model.addAttribute("map", map);
        model.addAttribute("book", b);
        model.addAttribute("library", library);
        return "protocols";
    }

    @RequestMapping("/books/{book}/protocols/{id}")
    public String protocol(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "id") long id) throws IOException {
        Protocol protocol;

        protocol = Protocol.load(context.protocolFile(book, id));
        model.addAttribute("protocol", protocol);
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocol";
    }

    //-- exercise

    @PostMapping("/books/{book}/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@PathVariable(value = "book") String book, @RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), body.get("e"));
        exercise.logComment(context.root(), body.get("comment"));
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
    public String answer(Model model, @PathVariable(value = "book") String book, @RequestParam("e") String e,
                         @RequestParam("question") String question, @RequestParam("answer") String answer) throws IOException {
        Exercise exercise;
        String correction;

        answer = answer.trim();
        exercise = Exercise.forParam(library.get(book), e);
        correction = exercise.answer(question, answer);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("correction", correction);
        exercise.logAnswer(context.root(), question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}
