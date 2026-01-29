package com.coma.comaroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ComaRoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComaRoomApplication.class, args);
	}

}
