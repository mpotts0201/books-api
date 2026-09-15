package com.books.books_api.controller;

import com.books.books_api.dto.BookDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    @GetMapping
    public List<BookDto> getBooks() {
        return List.of(
                new BookDto(1L, "The Hobbit", "J.R.R. Tolkien", 1937),
                new BookDto(2L, "Dune", "Frank Herbert", 1965)
        );
    }
}
