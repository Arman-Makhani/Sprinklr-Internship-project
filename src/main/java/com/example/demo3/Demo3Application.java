package com.example.demo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Demo3Application {

	public static void main(String[] args) {

		SpringApplication.run(Demo3Application.class, args);
	}

}
