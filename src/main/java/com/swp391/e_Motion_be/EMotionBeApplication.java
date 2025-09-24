package com.swp391.e_Motion_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EMotionBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(EMotionBeApplication.class, args);
	}

}
