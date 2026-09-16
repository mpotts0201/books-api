package com.books.books_api.service;

import com.books.books_api.dto.BookDto;
import com.books.books_api.dto.CreateBookRequest;
import com.books.books_api.entity.BookEntity;
import com.books.books_api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BookDto createBook(CreateBookRequest request) {
        BookEntity saved = bookRepository.save(toEntity(request));
        return toDto(saved);
    }

    private BookDto toDto(BookEntity entity) {
        return new BookDto(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublishedYear()
        );
    }

    private BookEntity toEntity(CreateBookRequest request) {
        BookEntity entity = new BookEntity();
        entity.setTitle(request.title());
        entity.setAuthor(request.author());
        entity.setPublishedYear(request.publishedYear());
        return entity;
    }

}
