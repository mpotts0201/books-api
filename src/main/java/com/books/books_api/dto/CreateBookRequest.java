package com.books.books_api.dto;

public record CreateBookRequest(String title, Long authorId, int publishedYear) {
}
