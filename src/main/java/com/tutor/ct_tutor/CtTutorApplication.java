package com.tutor.ct_tutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class CtTutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CtTutorApplication.class, args);
	}

}
