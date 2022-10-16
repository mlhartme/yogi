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

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.NodeInstantiationException;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.fs.http.HttpNode;
import net.oneandone.sushi.fs.http.MovedTemporarilyException;
import net.oneandone.sushi.fs.http.model.HeaderList;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Library implements Iterable<Book> {
    public static void main(String[] args) throws Exception {
        World world;
        String token;

        world = World.create();
        token = world.getHome().join(".github").readProperties().getProperty("oauth");
        run(world, "mlhartme/yogi-etc", token);
    }
    public static void run(World world, String repository, String token) throws IOException {
        GitHub github = GitHub.connect();
        Library library;

        GHAsset asset = latest(github, repository);
        library = download(world, asset.getUrl().toString(), token);
        for (Book b : library.books) {
            System.out.println("book " + b.name);
        }
    }

    public static Library download(World world, String url, String token) throws IOException {
        HttpNode http;
        FileNode tmp;

        try {
            http = (HttpNode) world.node(url);
        } catch (NodeInstantiationException|URISyntaxException e) {
            throw new IOException(url + ": " + e.getMessage());
        }
        tmp = world.getTemp().createTempFile();
        try {
            HeaderList headers = HeaderList.of("Accept", "application/octet-stream");
            if (token != null) {
                headers.add("Authorization", "Bearer " + token);
            }
            http = http.withHeaders(headers);
            try {
                http.copyFile(tmp);
            } catch (IOException e) {
                if (rootCause(e) instanceof MovedTemporarilyException rc) {
                    return download(world, rc.location, token);
                } else {
                    throw e;
                }
            }
            Node<?> zip = tmp.openZip();
            return Library.load(zip);
        } finally {
            tmp.deleteFile();
        }
    }

    private static Throwable rootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }
    public static GHAsset latest(GitHub github, String repository) throws IOException {
        GHRepository repo;
        GHAsset result;
        Date date;

        repo = github.getRepository(repository);
        date = null;
        result = null;
        for (GHRelease release : repo.listReleases().toList()) {
            var asset = getBookOpt(release);
            if (asset != null) {
                var d = asset.getCreatedAt();
                if (date == null || d.compareTo(date) > 0) {
                    date = d;
                    result = asset;
                }
            }
        }
        return result;
    }

    public static GHAsset getBookOpt(GHRelease release) throws IOException {
        for (GHAsset a : release.listAssets().toList()) {
            if (a.getName().equals("books.jar")) {
                return a;
            }
        }
        return null;
    }
    public static Library load(Node<?> base) throws IOException {
        Library result;
        byte[] jpg;

        result = new Library();
        for (Node<?> book : base.find("*.yogi")) {
            jpg = base.join(book.getBasename() + ".jpg").readBytes();
            result.books.add(Book.load(book, jpg));
        }
        Collections.sort(result.books);
        return result;
    }

    private final List<Book> books;

    public Library() {
        this.books = new ArrayList<>();
    }

    public int size() {
        return books.size();
    }

    public Book get(String name) throws IOException {
        for (Book book : books) {
            if (book.name.equals(name)) {
                return book;
            }
        }
        throw new IOException("book not found: " + name);
    }

    @Override
    public Iterator<Book> iterator() {
        return books.iterator();
    }
}
