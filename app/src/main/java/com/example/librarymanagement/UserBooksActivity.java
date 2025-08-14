package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class UserBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerBooks;
    private ArrayList<Book> bookList;
    private BookAdapter adapter;
    private DatabaseReference booksRef, borrowRequestsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();

        booksRef = FirebaseDatabase.getInstance().getReference("books");
        borrowRequestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests");

        adapter = new BookAdapter(this, bookList, new BookAdapter.OnBookActionListener() {
            @Override
            public void onEdit(Book book) {
                // Not needed for user
            }

            @Override
            public void onDelete(Book book) {
                // Not needed for user
            }

            @Override
            public void onBorrow(Book book) {
                borrowBook(book);
            }
        }, false); // false → user mode

        recyclerBooks.setAdapter(adapter);

        loadBooks();
    }

    private void loadBooks() {
        booksRef.orderByChild("available").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Book book = ds.getValue(Book.class);
                            if (book != null) {
                                bookList.add(book);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void borrowBook(Book book) {
        // Check if user already has a pending request for this book
        borrowRequestsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyRequested = false;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BorrowRequest req = ds.getValue(BorrowRequest.class);
                            if (req != null
                                    && req.getBookId().equals(book.getBookId())
                                    && req.getStatus().equals("pending")) {
                                alreadyRequested = true;
                                break;
                            }
                        }

                        if (alreadyRequested) {
                            Toast.makeText(UserBooksActivity.this,
                                    "You already have a pending request for this book",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Create new borrow request
                        String requestId = borrowRequestsRef.push().getKey(); // unique key
                        if (requestId == null) return;

                        long borrowDate = System.currentTimeMillis();
                        long dueDate = borrowDate + 14L*24*60*60*1000; // 14 days

                        BorrowRequest request = new BorrowRequest(
                                requestId,
                                userId,
                                book.getBookId(),
                                borrowDate,
                                dueDate,
                                null, // optional: userName
                                "pending",
                                0.0,
                                "" // adminComment initially empty
                        );

                        borrowRequestsRef.child(requestId)
                                .setValue(request)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(UserBooksActivity.this,
                                                "Borrow request sent for approval",
                                                Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(UserBooksActivity.this,
                                                "Failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }


}
