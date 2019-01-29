package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class YogiController {
    private World world;

    public YogiController(World world) {
        this.world = world;
    }

    @GetMapping({"/", "/{name}"})
    public String hello(Model model, @RequestParam(value="name", required=false, defaultValue="test") String name) throws IOException {
        List<String> lines;

        lines = world.resource("BOOT-INF/classes/data/english/test.yogi").readLines();
        model.addAttribute("lines", lines);
        return "yogi";
    }
}