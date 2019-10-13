package net.mlhartme.yogi;

public class Achievement {
    public final int before;
    public final int after;

    public final String answerMin;
    public final String answerMax;
    public final String answerAvg;

    public Achievement(int before, int after, String min, String max, String avg) {
        this.before = before;
        this.after = after;

        this.answerMin = min;
        this.answerMax = max;
        this.answerAvg = avg;
    }
}
