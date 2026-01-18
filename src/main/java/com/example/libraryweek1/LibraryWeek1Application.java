package com.example.libraryweek1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryWeek1Application {

    public static void main(String[] args) {
        SpringApplication.run(LibraryWeek1Application.class, args);
    }

}
