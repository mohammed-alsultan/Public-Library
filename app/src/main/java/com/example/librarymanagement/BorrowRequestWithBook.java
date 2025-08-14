package com.example.librarymanagement;

import java.io.Serializable;

public class BorrowRequestWithBook implements Serializable {
    private BorrowRequest request;
    private Book book;

    public BorrowRequestWithBook(BorrowRequest request, Book book) {
        this.request = request;
        this.book = book;
    }

    public BorrowRequest getRequest() { return request; }
    public Book getBook() { return book; }
}
