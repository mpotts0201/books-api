package com.books.books_api;

import org.springframework.boot.SpringApplication;

public class TestBooksApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(BooksApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
