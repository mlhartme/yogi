package net.mlhartme.yogi;

import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private final LinkedHashMap<Date, String> lines;

    public Protocol() {
        lines = new LinkedHashMap<>();
    }

    public void addRaw(String raw) throws IOException {
        int idx;
        Date date;

        idx = raw.indexOf(' ');
        if (idx == -1) {
            throw new IOException("invalid line: " + raw);
        }
        idx = raw.indexOf(' ', idx);
        if (idx == -1) {
            throw new IOException("invalid line: " + raw);
        }
        try {
            date = FMT.parse(raw.substring(0, idx));
        } catch (ParseException e) {
            throw new IOException("invalid line: " + raw, e);
        }
        lines.put(date, raw.substring(idx + 1));
    }

    public Map<String, Integer> histogram() {
        Map<String, Integer> result;
        int idx;
        String question;
        String answer;
        String correct;

        result = new HashMap<>();
        for (String line : lines.values()) {
            if (!line.startsWith("# ")) {
                idx = line.indexOf(" -> ");
                if (idx == -1) {
                    throw new IllegalStateException(line);
                }
                question = line.substring(0, idx);
                answer = line.substring(idx + 4);
                idx = line.indexOf(" -> ");
                if (idx == -1) {
                    throw new IllegalStateException(line);
                }
                correct = answer.substring(idx + 4);
                answer = answer.substring(0, idx);
            }
        }
        return result;
    }

    public List<String> comments() {
        List<String> result;

        result = new ArrayList<String>();
        for (String line : lines.values()) {
            if (line.startsWith("# ")) {
                result.add(line.substring(2));
            }
        }
        return result;
    }
}
