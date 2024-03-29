/*
 * Copyright Michael Hartmeier, https://github.com/mlhartme/
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
package de.schmizzolin.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class YogiController {
    private final World world;
    private final UserFiles userFiles;
    private final String libraryStr;
    private final String libraryToken;
    private Library library;
    private final String version;

    public YogiController(World world, UserFiles userFiles,
                          @Value("${yogi.library}") String libraryStr, @Value("${yogi.libraryToken}") String libraryToken) throws IOException {
        this.world = world;
        this.userFiles = userFiles;
        this.libraryStr = libraryStr;
        this.libraryToken = libraryToken;
        this.library = Library.loadGithubRelease(world, libraryStr, libraryToken);
        this.version = world.resource("yogi.version").readString().trim();
        System.out.println("started YogiController " + version + ", loaded " + library.size() + " books:");
        for (Book b : library) {
            System.out.println("  " + b.name + " - " + b.size() + " words");
        }
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

    @RequestMapping("/reload")
    public String reload() throws IOException {
        System.out.println("reload ...");
        this.library = Library.loadGithubRelease(world, libraryStr, libraryToken);
        return "redirect:/logout";
    }

    @RequestMapping("/books")
    public String books() {
        return "redirect:/books/" + urlEncodeSegement(library.iterator().next().name);
    }

    @RequestMapping("/books/{book}")
    public String bookRaw(@PathVariable(value = "book") String book) throws IOException {
        List<String> lst;
        String selection;

        lst = userFiles.listSelections(book);
        if (lst.isEmpty()) {
            // TODO: user notice about selections
            // TODO: empty selection
            selection = "freigeschaltet";
            userFiles.saveSelection(book, selection, Collections.emptyList());
        } else {
            selection = lst.get(0);
        }
        return "redirect:/books/" + urlEncodeSegement(book) + "/" + urlEncodeSegement(selection);
    }

    // TODO: could collide wird selection
    @ResponseBody
    @GetMapping(value = "/books/{book}/jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] bookJpg(@PathVariable(value = "book") String book) throws IOException {
        return library.get(book).jpg();
    }

    @GetMapping("/books/{book}/{selection}")
    public String book(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection) throws IOException {
        model.addAttribute("library", library);
        model.addAttribute("book", library.get(book));
        model.addAttribute("selection", selection);
        return "book";
    }

    @GetMapping("/books/{book}/{selection}/selection")
    public String selection(Model model, @PathVariable(value = "book") String bookName,
                            @PathVariable(value = "selection") String selectionName) throws IOException {
        Book book;

        book = library.get(bookName);
        model.addAttribute("library", library);
        model.addAttribute("book", book);
        model.addAttribute("selectionName", selectionName);
        model.addAttribute("selection", book.loadSelection(userFiles, selectionName));
        return "selection";
    }

    @PostMapping("/books/{book}/{selection}/selection")
    public String updateSelection(@PathVariable(value = "book") String book,
                               @PathVariable(value = "selection") String oldName,
                               @RequestParam("newName") String newName,
                               HttpServletRequest request /* for selection */)
            throws IOException {
        IntSet enable;

        if (newName.isEmpty()) {
            throw new IllegalStateException();
        }
        enable = getChecked(request, "select_");
        userFiles.deleteSelection(book, oldName);
        library.get(book).saveSelection(userFiles, newName, enable);
        return "redirect:/books/" + urlEncodeSegement(book) + "/" + urlEncodeSegement(newName);
    }

    // browser cannot user method="delete" ...
    @GetMapping("/books/{book}/{selection}/new-selection")
    public String newSelection(@PathVariable(value = "book") String book,
                               @PathVariable(value = "selection") String selection) throws IOException {
        String newSelection;

        newSelection = userFiles.newSelection(book, selection);
        return "redirect:/books/" + urlEncodeSegement(book) + "/" + urlEncodeSegement(newSelection) + "/selection";
    }

    // browser cannot user method="delete" ...
    @PostMapping("/books/{book}/{selection}/delete-selection")
    public String deleteSelection(@PathVariable(value = "book") String book,
                                  @PathVariable(value = "selection") String selection) throws IOException {
        userFiles.deleteSelection(book, selection);
        return "redirect:/books/" + urlEncodeSegement(book);
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

    @RequestMapping("/books/{book}/{selection}/start")
    public String start(@PathVariable(value = "book") String bookName, @PathVariable("selection") String selectionName,
                        @RequestParam("count") String countOrAll) throws IOException {
        Book book;
        IntSet selection;
        List<Integer> sorted;
        int count;

        book = library.get(bookName);
        selection = book.loadSelection(userFiles, selectionName);
        if (!countOrAll.equals("all")) {
            sorted = book.statistics(userFiles).sort(selection, book); // TODO: expensive
            count = Integer.parseInt(countOrAll);
            while (sorted.size() > count) {
                sorted.remove(sorted.size() - 1);
            }
            selection = new IntSet(sorted);
        }
        return doStart(book, bookName + "-" + selectionName, selection);
    }

    private String doStart(Book book, String title, IntSet selection) throws IOException {
        Exercise exercise;

        exercise = Exercise.create(userFiles.nextProtocol(book.name), book, title, selection);
        exercise.logTitle(userFiles, title);
        return "redirect:question?e=" + urlEncodeQuery(exercise.toParam());
    }

    private static String urlEncodeSegement(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    public static String urlEncodeQuery(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    @RequestMapping("/books/{book}/{selection}/protocols/")
    public String protocols(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection)
            throws IOException {
        List<FileNode> protocols;
        LinkedHashMap<String, String> map;
        Protocol p;
        Book b;
        Achievement a;
        String basename;

        protocols = userFiles.listProtocols(book);
        Collections.reverse(protocols);
        map = new LinkedHashMap<>();
        b = library.get(book);
        for (FileNode log : protocols) {
            basename = log.getBasename();
            p = Protocol.load(log);
            a = p.achievement(userFiles, b);
            map.put(basename, p.date() + " " + p.words() + " " + a.before + " -> " + a.after);
        }
        model.addAttribute("map", map);
        model.addAttribute("book", b);
        model.addAttribute("selection", selection);
        model.addAttribute("library", library);
        return "protocols";
    }

    @RequestMapping("/books/{book}/{selection}/protocols/{id}")
    public String protocol(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection,
                           @PathVariable(value = "id") long id) throws IOException {
        Protocol protocol;

        protocol = Protocol.load(userFiles.protocolFile(book, id));
        model.addAttribute("protocol", protocol);
        model.addAttribute("book", library.get(book));
        model.addAttribute("library", library);
        return "protocol";
    }

    //-- exercise

    @PostMapping("/books/{book}/{selection}/comment") @ResponseStatus(value = HttpStatus.OK)
    public void comment(@PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection,
                        @RequestParam Map<String, String> body) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), body.get("e"));
        exercise.logComment(userFiles, body.get("comment"));
    }

    @GetMapping("/books/{book}/{selection}/question")
    public String question(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection,
                           @RequestParam(value = "e") String e,
                           @RequestParam(value = "question", required = false) String question) throws IOException {
        Exercise exercise;

        exercise = Exercise.forParam(library.get(book), e);
        if (question == null) {
            question = exercise.question();
        }
        model.addAttribute("selection", selection);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        return "question";
    }

    @PostMapping("/books/{book}/{selection}/answer")
    public String answer(Model model, @PathVariable(value = "book") String book, @PathVariable(value = "selection") String selection,
                         @RequestParam("e") String e,
                         @RequestParam("question") String question, @RequestParam("answer") String answer) throws IOException {
        Exercise exercise;
        String correction;

        answer = answer.trim();
        exercise = Exercise.forParam(library.get(book), e);
        correction = exercise.answer(question, answer);
        model.addAttribute("selection", selection);
        model.addAttribute("exercise", exercise);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("correction", correction);
        exercise.logAnswer(userFiles, question, answer, exercise.lookup(question) /* not correction - it might be null */);
        return "answer";
    }
}
