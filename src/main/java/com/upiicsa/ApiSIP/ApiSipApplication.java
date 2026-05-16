package com.upiicsa.ApiSIP;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApiSipApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiSipApplication.class, args);
	}

}
