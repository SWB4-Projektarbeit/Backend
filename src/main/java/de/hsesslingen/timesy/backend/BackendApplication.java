package de.hsesslingen.timesy.backend;

import lombok.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class BackendApplication {

	static void main(final @NonNull String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
