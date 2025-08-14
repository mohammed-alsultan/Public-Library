package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class UserBorrowRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private BorrowRequestAdapter adapter;
    private ArrayList<BorrowRequestWithBook> requestList; // new combined model
    private DatabaseReference requestsRef, booksRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_borrow_requests);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerRequests = findViewById(R.id.recyclerBorrowRequests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new BorrowRequestAdapter(this, requestList);
        recyclerRequests.setAdapter(adapter);

        requestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests");
        booksRef = FirebaseDatabase.getInstance().getReference("books");

        loadUserRequests();
    }

    private void loadUserRequests() {
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests");
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");

        requestsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requestList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BorrowRequest request = ds.getValue(BorrowRequest.class);
                            if (request != null) {
                                // Fetch book details
                                booksRef.child(request.getBookId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot bookSnap) {
                                                Book book = bookSnap.getValue(Book.class);
                                                if (book != null) {
                                                    BorrowRequestWithBook combined = new BorrowRequestWithBook(request, book);
                                                    requestList.add(combined);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) { }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserBorrowRequestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
