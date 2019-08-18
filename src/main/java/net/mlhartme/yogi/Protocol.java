package net.mlhartme.yogi;

import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Protocol {
    @RequestMapping("/books/{book}/protocols/")
    public static List<FileNode> list(FileNode protocolBase, String book) throws IOException {
        List<FileNode> lst;

        lst = protocolBase.join(book).find("*.log");
        Collections.sort(lst, new Comparator<FileNode>() {
            @Override
            public int compare(FileNode o1, FileNode o2) {
                Integer left;
                Integer right;

                try {
                    left = Integer.parseInt(o1.getName());
                } catch (NumberFormatException e) {
                    left = null;
                }
                try {
                    right = Integer.parseInt(o2.getName());
                } catch (NumberFormatException e) {
                    right = null;
                }
                if (left != null && right != null) {
                    return left.compareTo(right);
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
        return lst;
    }

    public static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Protocol load(FileNode src) throws IOException {
        Protocol result;

        result = new Protocol();
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
    private final List<Entry> entries; // cannot use a map because of duplicte keys

    public Protocol() {
        entries = new ArrayList<>();
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
        int seconds;
        int minutes;

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

    public static class AnswerTiming {
        public final String min;
        public final String max;
        public final String avg;

        public AnswerTiming(String min, String max, String avg) {
            this.min = min;
            this.max = max;
            this.avg = avg;
        }
    }
    public AnswerTiming answerTiming() {
        long max = -1;
        long min = Long.MAX_VALUE;
        long avg = 0;
        int count;
        long diff;
        Entry prev;

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
        return new AnswerTiming(durationString(min), durationString(max), durationString(avg / count));
    }
}
