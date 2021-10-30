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
package net.schmizzolin.yogi;

import net.oneandone.sushi.fs.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Library implements Iterable<Book> {
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
