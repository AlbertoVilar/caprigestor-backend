package com.devmaster.GoatFarm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GoatFarmApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GoatFarmApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Hello World!");
	}
}
