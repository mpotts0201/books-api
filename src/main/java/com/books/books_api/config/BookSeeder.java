package com.books.books_api.config;

import com.books.books_api.entity.AuthorEntity;
import com.books.books_api.entity.BookEntity;
import com.books.books_api.repository.AuthorRepository;
import com.books.books_api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        AuthorEntity tolkien = new AuthorEntity();
        tolkien.setName("J.R.R. Tolkien");

        AuthorEntity herbert = new AuthorEntity();
        herbert.setName("Frank Herbert");

        AuthorEntity gibson = new AuthorEntity();
        gibson.setName("William Gibson");

        BookEntity hobbit = new BookEntity();
        hobbit.setTitle("The Hobbit");
        hobbit.setAuthor(tolkien);
        hobbit.setPublishedYear(1937);

        BookEntity dune = new BookEntity();
        dune.setTitle("Dune");
        dune.setAuthor(herbert);
        dune.setPublishedYear(1965);

        BookEntity neuromancer = new BookEntity();
        neuromancer.setTitle("Neuromancer");
        neuromancer.setAuthor(gibson);
        neuromancer.setPublishedYear(1984);

        authorRepository.saveAll(List.of(tolkien, herbert, gibson));
        bookRepository.saveAll(List.of(hobbit, dune, neuromancer));
    }
}
