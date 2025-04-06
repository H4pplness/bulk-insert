package com.sql_test.test_heavy_write;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@org.springframework.context.annotation.Configuration
@EnableAsync
public class Configuration {

    @Bean
    Faker createFaker(){
        return new Faker();
    }

    @Bean
    Random createRandom(){
        return new Random();
    }

    @Bean(name = "VirtualThreadExecutor")
    public Executor createExecutor(){
        return Thread.ofVirtual().name("vthread-", 0).factory()::newThread;
    }
}
