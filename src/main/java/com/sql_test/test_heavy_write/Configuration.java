package com.sql_test.test_heavy_write;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    Faker createFaker(){
        return new Faker();
    }

    @Bean
    Random createRandom(){
        return new Random();
    }

//    @Bean
//    public ExecutorService createExecutor(){
//        return Executors.newVirtualThreadPerTaskExecutor();
//    }
}
