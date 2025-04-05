package com.sql_test.test_heavy_write;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableAsync
public class TestHeavyWriteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestHeavyWriteApplication.class, args);
	}

}
