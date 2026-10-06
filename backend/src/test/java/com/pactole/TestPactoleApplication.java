package com.pactole;

import org.springframework.boot.SpringApplication;

public class TestPactoleApplication {

	public static void main(String[] args) {
		SpringApplication.from(PactoleApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
