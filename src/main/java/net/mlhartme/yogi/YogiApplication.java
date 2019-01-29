package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class YogiApplication {
	public static void main(String[] args) {
		SpringApplication.run(YogiApplication.class, args);
	}

	@Bean
	public World world() throws IOException {
		return World.create();
	}
}

