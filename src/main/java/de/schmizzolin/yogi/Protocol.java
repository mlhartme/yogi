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
package de.schmizzolin.yogi;

import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Protocol {
    public static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Protocol load(FileNode src) throws IOException {
        Protocol result;

        result = new Protocol(src);
        for (String line : src.readLines()) {
            result.addRaw(line);
        }
        return result;
    }

    private static class Entry {
        public final Date date;
        public final String text;

        private Entry(Date date, String text) {
            this.date = date;
            this.text = text;
        }
    }

    private final FileNode file;
    private final List<Entry> entries; // cannot use a map because of duplicate keys

    public Protocol(FileNode file) {
        this.file = file;
        this.entries = new ArrayList<>();
    }

    public void addRaw(String raw) throws IOException {
        int idx;
        Date date;

        idx = raw.indexOf(' ');
        if (idx == -1) {
            throw new IOException("invalid line: " + raw);
        }
        idx = raw.indexOf(' ', idx + 1);
        if (idx == -1) {
            throw new IOException("invalid line: " + raw);
        }
        try {
            date = FMT.parse(raw.substring(0, idx));
        } catch (ParseException e) {
            throw new IOException("invalid line: " + raw, e);
        }
        entries.add(new Entry(date, raw.substring(idx + 1)));
    }

    public int words() {
        int result;
        Map<Integer, List<String>> raw;

        raw = histogramRaw();
        result = 0;
        for (List<String> lst : raw.values()) {
            result += lst.size();
        }
        return result;
    }

    public String date() {
        return FMT.format(entries.get(0).date);
    }

    public String duration() {
        long millis;

        millis = entries.get(entries.size() - 1).date.getTime() - entries.get(0).date.getTime();
        return durationString(millis);
    }

    private static String durationString(long millis) {
        int seconds;
        int minutes;

        seconds = (int) (millis / 1000);
        minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + ":" + Strings.padLeft(Integer.toString(seconds), 2, '0');
    }

    public Map<Integer, String> histogram() {
        int words;
        Map<Integer, List<String>> raw;
        Map<Integer, String> result;

        words = words();
        raw = histogramRaw();
        result = new TreeMap<>();
        for (Map.Entry<Integer, List<String>> entry : raw.entrySet()) {
            result.put(entry.getKey(), Integer.toString(entry.getValue().size() * 100 / words));
        }
        return result;
    }

    public static final int NOT_ANSWERED = 999;

    /** @return maps number of tries to questions with that number */
    public Map<Integer, List<String>> histogramRaw() {
        Map<Integer, List<String>> result;
        int count;
        String question;
        List<String> questions;

        result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : questionCount().entrySet()) {
            count = entry.getValue();
            question = entry.getKey();
            if (count < 0) {
                count = NOT_ANSWERED;
            }
            questions = result.get(count);
            if (questions == null) {
                questions = new ArrayList<>();
                result.put(count, questions);
            }
            questions.add(question);
        }
        return result;
    }

    /** @return map question to number of +/- tries; positive, if the last try was correct */
    public Map<String, Integer> questionCount() {
        String again;
        Map<String, Integer> result;
        int idx;
        String question;
        String answer;
        String correct;
        Integer count;
        String text;

        again = null;
        result = new HashMap<>();
        for (Entry entry : entries) {
            text = entry.text;
            if (text.startsWith("# ")) {
                // skip
            } else if (text.startsWith("! ")) {
                // skip
            } else {
                idx = text.indexOf(" -> ");
                if (idx == -1) {
                    throw new IllegalStateException(text);
                }
                question = text.substring(0, idx);
                answer = text.substring(idx + 4);
                idx = answer.indexOf(" -> ");
                if (idx == -1) {
                    throw new IllegalStateException(text);
                }
                correct = answer.substring(idx + 4);
                answer = answer.substring(0, idx);

                if (again == null) {
                    count = result.get(question);
                    if (count == null) {
                        count = -1;
                    } else {
                        count--;
                    }
                    result.put(question, count);
                    if (answer.equals(correct)) {
                        result.put(question, -result.get(question));
                    }
                }
                if (answer.equals(correct)) {
                    again = null;
                } else {
                    again = question;
                }
            }
        }
        return result;
    }

    public String title() {
        String result;
        String text;

        result = "";
        for (Entry entry : entries) {
            text = entry.text;
            if (text.startsWith("! ")) {
                text = text.substring(2);
                if (result.isEmpty()) {
                    result = text;
                } else {
                    result = result + "\n" + text;
                }
            }
        }
        return result;
    }
    public List<String> comments() {
        List<String> result;
        String text;

        result = new ArrayList<>();
        for (Entry entry : entries) {
            text = entry.text;
            if (text.startsWith("# ")) {
                result.add(text.substring(2));
            }
        }
        return result;
    }

    public Achievement achievement(UserFiles context, Book book) throws IOException {
        long max = -1;
        long min = Long.MAX_VALUE;
        long avg = 0;
        int count;
        long diff;
        Entry prev;
        int[] beforeAfter;

        prev = null;
        count = 0;
        for (Entry entry : entries) {
            if (prev != null) {
                count++;
                diff = entry.date.getTime() - prev.date.getTime();
                avg += diff;
                max = Math.max(max, diff);
                min = Math.min(min, diff);
            }
            prev = entry;
        }
        beforeAfter =  Statistics.beforeAfter(context, book, this.file, book.selection(this));

        return count == 0 ? new Achievement(beforeAfter[0], beforeAfter[1], "-", "-", "-")
                : new Achievement(beforeAfter[0], beforeAfter[1],
                durationString(min), durationString(max), durationString(avg / count));
    }
}
