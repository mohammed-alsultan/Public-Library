package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserBorrowRequestDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvAuthor, tvCategory, tvStatus, tvBorrowDate, tvDueDate, tvPenalty, tvAdminComment;

    private DatabaseReference booksRef;
    private DatabaseReference requestsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_borrow_request_detail);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvBorrowDate = findViewById(R.id.tvDetailBorrowDate);
        tvDueDate = findViewById(R.id.tvDetailDueDate);
        tvPenalty = findViewById(R.id.tvDetailPenalty);
        tvAdminComment = findViewById(R.id.tvDetailAdminComment);

        String requestId = getIntent().getStringExtra("REQUEST_ID");
        String bookId = getIntent().getStringExtra("BOOK_ID");

        if (requestId == null || bookId == null) {
            Toast.makeText(this, "Invalid request or book ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        booksRef = FirebaseDatabase.getInstance().getReference("books").child(bookId);
        requestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests").child(requestId);

        // First, fetch the BorrowRequest
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot requestSnapshot) {
                BorrowRequest request = requestSnapshot.getValue(BorrowRequest.class);
                if (request == null) {
                    Toast.makeText(UserBorrowRequestDetailActivity.this, "Request not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Then fetch the book details
                booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot bookSnapshot) {
                        Book book = bookSnapshot.getValue(Book.class);
                        if (book == null) {
                            Toast.makeText(UserBorrowRequestDetailActivity.this, "Book not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Populate the UI
                        tvTitle.setText(book.getTitle());
                        tvAuthor.setText(book.getAuthor());
                        tvCategory.setText(book.getCategory());
                        tvStatus.setText(request.getStatus());

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvBorrowDate.setText(sdf.format(new Date(request.getBorrowDate())));
                        tvDueDate.setText(sdf.format(new Date(request.getDueDate())));
                        tvPenalty.setText(String.valueOf(request.getPenalty()));

                        if ("declined".equalsIgnoreCase(request.getStatus())) {
                            tvAdminComment.setText("Admin Comment: " + (request.getUserName() != null ? request.getUserName() : "No comment"));
                        } else {
                            tvAdminComment.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserBorrowRequestDetailActivity.this, "Error loading book", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserBorrowRequestDetailActivity.this, "Error loading request", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
