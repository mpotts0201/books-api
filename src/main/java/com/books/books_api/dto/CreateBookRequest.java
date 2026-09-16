package com.books.books_api.dto;

public record CreateBookRequest(String title, String author, int publishedYear) {
}
