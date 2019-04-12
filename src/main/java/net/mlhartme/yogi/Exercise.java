package net.mlhartme.yogi;

import net.oneandone.sushi.util.Separator;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
    public static Exercise forRequest(String doneParam) {
        List<Integer> done;

        done = toInt(doneParam == null ? new ArrayList<>() : Separator.COMMA.split(doneParam));
        return new Exercise(done);
    }

    public final List<Integer> done;

    public Exercise(List<Integer> done) {
        this.done = done;
    }

    public String doneParam() {
        return toString(done);
    }

    //--


    private static String toString(List<Integer> done) {
        StringBuilder result;

        result = new StringBuilder();
        for (Integer i : done) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(i);
        }
        return result.toString();
    }

    private static List<Integer> toInt(List<String> strings) {
        List<Integer> result;

        result = new ArrayList<>(strings.size());
        for (String str : strings) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

}
