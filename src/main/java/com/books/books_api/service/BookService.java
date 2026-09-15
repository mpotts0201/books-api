package com.books.books_api.service;

import com.books.books_api.dto.BookDto;
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

    private BookDto toDto(BookEntity entity) {
        return new BookDto(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublishedYear()
        );
    }
}
