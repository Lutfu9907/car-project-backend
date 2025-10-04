package com.lutfudolay.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lutfudolay"})
@EntityScan(basePackages = {"com.lutfudolay.model"})
@EnableJpaRepositories(basePackages = {"com.lutfudolay.repository"})

public class CarProjectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarProjectBackendApplication.class, args);
	}

}
