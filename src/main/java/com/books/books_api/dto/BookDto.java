package com.books.books_api.dto;

public record BookDto(Long id, String title, AuthorDto author, int publishedYear) {
}
