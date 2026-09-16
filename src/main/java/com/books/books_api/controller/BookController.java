package com.books.books_api.controller;

import com.books.books_api.dto.BookDto;
import com.books.books_api.dto.CreateBookRequest;
import com.books.books_api.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookDto> getBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody CreateBookRequest request) {
        BookDto created = bookService.createBook(request);
        URI location = URI.create("/books/" + created.id());
        return ResponseEntity.created(location).body(created);
    }
}
