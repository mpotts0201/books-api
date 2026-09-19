package com.books.books_api.service;


import com.books.books_api.dto.AuthorDto;
import com.books.books_api.dto.CreateAuthorRequest;
import com.books.books_api.entity.AuthorEntity;
import com.books.books_api.exception.AuthorNotFoundException;
import com.books.books_api.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public AuthorDto getAuthor(Long id) {
        return authorRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + id));
    }

    public AuthorDto createAuthor(CreateAuthorRequest request) {
        AuthorEntity author = authorRepository.save(toEntity(request));
        return toDto(author);
    }

    @Transactional
    public AuthorDto updateAuthor(Long id, CreateAuthorRequest request) {
        AuthorEntity author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + id));

        author.setName(request.name());

        return toDto(author);
    }

    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException("Author not found: " + id);
        }

        authorRepository.deleteById(id);
    }

    private AuthorDto toDto(AuthorEntity entity) {
        return new AuthorDto(
                entity.getId(),
                entity.getName()
        );
    }

    private AuthorEntity toEntity(CreateAuthorRequest request) {
        AuthorEntity entity = new AuthorEntity();
        entity.setName(request.name());

        return entity;
    }
}
