package com.books.books_api.controller;

import com.books.books_api.dto.AuthorDto;
import com.books.books_api.dto.CreateAuthorRequest;
import com.books.books_api.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorDto> getAuthors() { return authorService.getAllAuthors(); }

    @GetMapping("/{id}")
    public AuthorDto getAuthor(@PathVariable Long id) {
        return authorService.getAuthor(id);
    }

    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@RequestBody CreateAuthorRequest request) {
        AuthorDto created = authorService.createAuthor(request);
        URI location = URI.create("/authors/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable Long id, @RequestBody CreateAuthorRequest request) {
        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
