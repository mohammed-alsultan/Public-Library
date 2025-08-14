package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Books extends AppCompatActivity implements BookAdapter.OnBookActionListener {

    private RecyclerView recyclerBooks;
    private BookAdapter adapter;
    private final ArrayList<Book> books = new ArrayList<>();
    private final ArrayList<String> bookIds = new ArrayList<>();

    // CONFIG: change paths if you use "Library/Books"
    private DatabaseReference booksRef;
    private DatabaseReference requestsRef;

    // Replace with your signed-in user id and role
    private String currentUserId = "USER_ID";   // pass this via Intent from SignInActivity
    private boolean isAdmin = false;             // pass via Intent or fetch from DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        // If provided by SignInActivity:
        if (getIntent().hasExtra("USER_ID")) currentUserId = getIntent().getStringExtra("USER_ID");
        if (getIntent().hasExtra("IS_ADMIN")) isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookAdapter(this, books, this, isAdmin);
        recyclerBooks.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        booksRef = db.getReference("Books");              // or db.getReference("Library/Books")
        requestsRef = db.getReference("BorrowRequests");  // or db.getReference("Library/BorrowRequests")

        loadBooks();
    }

    private void loadBooks() {
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                books.clear();
                bookIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book b = ds.getValue(Book.class);
                    if (b != null) {
                        // Ensure bookId exists for downstream ops
                        if (b.getBookId() == null) b.setBookId(ds.getKey());
                        books.add(b);
                        bookIds.add(ds.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Books.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== OnBookActionListener ==========
    @Override
    public void onEdit(Book book) {
        if (!isAdmin) {
            Toast.makeText(this, "Admins only", Toast.LENGTH_SHORT).show();
            return;
        }
        // Example: open an EditBookActivity (not included here)
        // Intent i = new Intent(this, EditBookActivity.class);
        // i.putExtra("BOOK_ID", book.getBookId());
        // startActivity(i);
        Toast.makeText(this, "Edit flow here for " + book.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(Book book) {
        if (!isAdmin) {
            Toast.makeText(this, "Admins only", Toast.LENGTH_SHORT).show();
            return;
        }
        if (book.getBookId() == null) {
            Toast.makeText(this, "Missing bookId", Toast.LENGTH_SHORT).show();
            return;
        }
        booksRef.child(book.getBookId()).removeValue()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Toast.makeText(this, "Book deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBorrow(Book book) {
        if (isAdmin) {
            Toast.makeText(this, "Admins cannot borrow here (demo)", Toast.LENGTH_SHORT).show();
            return;
        }
        placeBorrowRequest(book);
    }

    // ========== Borrow / Return logic ==========
    private void placeBorrowRequest(Book book) {
        if (book.getCopies() <= 0) {
            Toast.makeText(this, "No copies available", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestId = requestsRef.push().getKey();
        if (requestId == null) {
            Toast.makeText(this, "Request key error", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        long due = now + (long) book.getMaxBorrowDays() * 24L * 60L * 60L * 1000L;

        BorrowRequest req = new BorrowRequest(
                requestId,
                currentUserId,
                book.getBookId(),
                now,
                due,
                null,
                "pending",
                0.0,
                null        // comment / admin note
        );

        requestsRef.child(requestId).setValue(req)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Borrow request sent for approval", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        requestsRef.child(requestId).setValue(req).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                decrementCopies(book.getBookId());
                Toast.makeText(this,
                        "Borrowed! Return within " + book.getMaxBorrowDays() + " days.\nPenalty: " +
                                book.getPenaltyPerDay() + " per late day.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Borrow failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void decrementCopies(String bookId) {
        booksRef.child(bookId).child("copies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) return Transaction.success(currentData);
                if (current <= 0) return Transaction.abort();
                currentData.setValue(current - 1);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) { }
        });
    }

    private void incrementCopies(String bookId) {
        booksRef.child(bookId).child("copies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) current = 0;
                currentData.setValue(current + 1);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) { }
        });
    }

    // Call this when returning a book (e.g., from a "My Borrows" screen)
    private void returnBook(String requestId) {
        requestsRef.child(requestId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                BorrowRequest req = snapshot.getValue(BorrowRequest.class);
                if (req == null) { Toast.makeText(Books.this, "Request not found", Toast.LENGTH_SHORT).show(); return; }

                String bookId = req.getBookId();
                // Find the book to get penaltyPerDay
                booksRef.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        Book b = snap.getValue(Book.class);
                        if (b == null) { Toast.makeText(Books.this, "Book not found", Toast.LENGTH_SHORT).show(); return; }

                        long now = System.currentTimeMillis();
                        long diffDays = Math.max(0, (now - req.getDueDate()) / (1000L * 60L * 60L * 24L));
                        double penalty = diffDays * b.getPenaltyPerDay();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("returnDate", now);
                        updates.put("status", diffDays > 0 ? "returned_late" : "returned");
                        updates.put("penaltyAmount", penalty);

                        requestsRef.child(requestId).updateChildren(updates).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                incrementCopies(bookId);
                                String msg = diffDays > 0
                                        ? "Returned. Late by " + diffDays + " day(s). Penalty: " + penalty
                                        : "Returned on time. No penalty.";
                                Toast.makeText(Books.this, msg, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Books.this, "Return update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
