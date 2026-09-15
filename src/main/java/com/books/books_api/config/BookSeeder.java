package com.books.books_api.config;

import com.books.books_api.entity.BookEntity;
import com.books.books_api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        BookEntity hobbit = new BookEntity();
        hobbit.setTitle("The Hobbit");
        hobbit.setAuthor("J.R.R. Tolkien");
        hobbit.setPublishedYear(1937);

        BookEntity dune = new BookEntity();
        dune.setTitle("Dune");
        dune.setAuthor("Frank Herbert");
        dune.setPublishedYear(1965);

        bookRepository.saveAll(List.of(hobbit, dune));
    }
}
