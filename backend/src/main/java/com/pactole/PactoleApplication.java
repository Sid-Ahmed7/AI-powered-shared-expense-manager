package com.pactole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pactole.config.PactoleProperties;

@SpringBootApplication
@EnableConfigurationProperties(PactoleProperties.class)
@EnableScheduling
public class PactoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(PactoleApplication.class, args);
	}

}
