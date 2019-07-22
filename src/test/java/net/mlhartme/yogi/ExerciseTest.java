package net.mlhartme.yogi;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.util.Strings;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class ExerciseTest {
    @Test
    public void bug() throws IOException {
        World world;
        String urlstr = "http://localhost:8080/question.html?e=16c1871a09b%3Afranzösisch%2F1-1%3A1%3A0%3A46%2C34%2C8%2C3%2C45%2C7%2C11%2C35%2C29%2C1%2C44%2C25%2C33%2C47%3A5%2C55%2C40%2C9%2C53%2C37%2C32%2C23%2C31%2C56%2C0%2C2%2C42%2C30%2C19%2C18%2C10%2C6%2C28%2C54%2C15%2C51%2C48%2C14%2C22%2C38%2C16%2C17%2C26%2C39%2C49%2C27%2C41%2C50%2C52%2C24%2C12%2C36%2C43%2C4%2C13";
        URL url = new URL(urlstr);
        String e = Strings.removeLeft(url.getQuery(), "e=");
        world = World.create();
        Exercise ex = Exercise.forParam(world.resource("data"), URLDecoder.decode(e));
        System.out.println(ex);

    }
}
