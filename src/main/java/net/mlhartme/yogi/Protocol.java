package net.mlhartme.yogi;

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
        Map<Integer, Integer> raw;

        raw = histogramRaw();
        result = 0;
        for (Integer i : raw.values()) {
            result += i;
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
        Map<Integer, Integer> raw;
        Map<Integer, String> result;

        words = words();
        raw = histogramRaw();
        result = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : raw.entrySet()) {
            result.put(entry.getKey(), Integer.toString(entry.getValue() * 100 / words));
        }
        return result;
    }

    public Map<Integer, Integer> histogramRaw() {
        Map<Integer, Integer> result;
        Integer n;

        result = new HashMap<>();
        for (Integer count : questionCount().values()) {
            n = result.get(count);
            n = n == null ? 1 : n + 1;
            result.put(count, n);
        }
        return result;
    }

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
                        count = 1;
                    } else {
                        count++;
                    }
                    result.put(question, count);
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

    public String longestBreak() {
        long max = -1;
        long diff;
        Entry prev;

        prev = null;
        for (Entry entry : entries) {
            if (prev != null) {
                diff = entry.date.getTime() - prev.date.getTime();
                max = Math.max(max, diff);
            }
            prev = entry;
        }
        return durationString(max);
    }
}
