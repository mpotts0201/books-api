package com.books.books_api.service;

import com.books.books_api.dto.AuthorDto;
import com.books.books_api.dto.BookDto;
import com.books.books_api.dto.CreateBookRequest;
import com.books.books_api.entity.AuthorEntity;
import com.books.books_api.entity.BookEntity;
import com.books.books_api.exception.AuthorNotFoundException;
import com.books.books_api.exception.BookNotFoundException;
import com.books.books_api.repository.AuthorRepository;
import com.books.books_api.repository.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public List<BookDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BookDto createBook(CreateBookRequest request) {
        AuthorEntity author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + request.authorId()));

        BookEntity saved = bookRepository.save(toEntity(request, author));
        return toDto(saved);
    }

    public BookDto getBook(Long id) {
        return bookRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));
    }

    @Transactional
    public BookDto updateBook(Long id, CreateBookRequest request) {
        AuthorEntity author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + request.authorId()));

        BookEntity entity = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));

        entity.setTitle(request.title());
        entity.setAuthor(author);
        entity.setPublishedYear(request.publishedYear());

        return toDto(entity);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        bookRepository.deleteById(id);
    }

    private BookDto toDto(BookEntity entity) {
        return new BookDto(
                entity.getId(),
                entity.getTitle(),
                new AuthorDto(entity.getAuthor().getId(),entity.getAuthor().getName()),
                entity.getPublishedYear()
        );
    }

    private BookEntity toEntity(CreateBookRequest request, AuthorEntity author) {
        BookEntity entity = new BookEntity();
        entity.setTitle(request.title());
        entity.setAuthor(author);
        entity.setPublishedYear(request.publishedYear());
        return entity;
    }

}
