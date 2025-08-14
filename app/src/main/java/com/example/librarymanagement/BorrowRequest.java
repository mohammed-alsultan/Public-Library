package com.example.librarymanagement;

public class BorrowRequest {
    private String requestId;
    private String userId;
    private String bookId;
    private long borrowDate;
    private long dueDate;
    private String status;
    private String userName;
    private double penalty;
    private String adminComment;

    public BorrowRequest() {
        // Required for Firebase
    }

    public BorrowRequest(String requestId, String userId, String bookId,
                         long borrowDate, long dueDate,
                         String userName, String status, double penalty, String adminComment) {
        this.requestId = requestId;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.userName = userName;
        this.status = status;
        this.penalty = penalty;
        this.adminComment = adminComment;
    }

    // getters & setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public long getBorrowDate() { return borrowDate; }
    public void setBorrowDate(long borrowDate) { this.borrowDate = borrowDate; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public double getPenalty() { return penalty; }
    public void setPenalty(double penalty) { this.penalty = penalty; }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}
