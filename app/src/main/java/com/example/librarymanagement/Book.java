package com.example.librarymanagement;
import java.io.Serializable;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String category;
    private int copies;
    private int maxBorrowDays;
    private double penaltyPerDay;
    private boolean available;

    public Book() { }

    public Book(String bookId, String title, String author, String category,
                int copies, int maxBorrowDays, double penaltyPerDay, boolean available) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.copies = copies;
        this.maxBorrowDays = maxBorrowDays;
        this.penaltyPerDay = penaltyPerDay;
        this.available = available;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getCopies() { return copies; }
    public int getMaxBorrowDays() { return maxBorrowDays; }
    public double getPenaltyPerDay() { return penaltyPerDay; }
    public boolean isAvailable() { return available; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setCopies(int copies) { this.copies = copies; }
    public void setMaxBorrowDays(int maxBorrowDays) { this.maxBorrowDays = maxBorrowDays; }
    public void setPenaltyPerDay(double penaltyPerDay) { this.penaltyPerDay = penaltyPerDay; }
    public void setAvailable(boolean available) { this.available = available; }
}
