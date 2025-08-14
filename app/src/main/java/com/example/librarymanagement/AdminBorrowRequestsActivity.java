package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class AdminBorrowRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private BorrowRequestAdapter adapter;
    private ArrayList<BorrowRequestWithBook> requestList;
    private DatabaseReference requestsRef, booksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_borrow_requests);

        recyclerRequests = findViewById(R.id.recyclerAdminBorrowRequests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new BorrowRequestAdapter(this, requestList);
        recyclerRequests.setAdapter(adapter);

        requestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests");
        booksRef = FirebaseDatabase.getInstance().getReference("books");

        loadAllRequests();
    }

    private void loadAllRequests() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BorrowRequest request = ds.getValue(BorrowRequest.class);
                    if (request != null) {
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
                Toast.makeText(AdminBorrowRequestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Override adapter click to approve/decline
        adapter.setOnItemClickListener((rb) -> showApprovalDialog(rb));
    }

    private void showApprovalDialog(BorrowRequestWithBook rb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Request")
                .setMessage("Approve or Decline this borrow request?")
                .setPositiveButton("Approve", (dialog, which) -> updateRequestStatus(rb, "approved", null))
                .setNegativeButton("Decline", (dialog, which) -> updateRequestStatus(rb, "declined", "Not approved by admin"))
                .show();
    }

    private void updateRequestStatus(BorrowRequestWithBook rb, String status, String adminComment) {
        BorrowRequest req = rb.getRequest();
        req.setStatus(status);
        req.setAdminComment(adminComment);

        requestsRef.child(req.getRequestId())
                .setValue(req)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Request " + status, Toast.LENGTH_SHORT).show();
                        if ("approved".equalsIgnoreCase(status)) {
                            decrementBookCopies(rb.getBook().getBookId());
                        }
                    } else {
                        Toast.makeText(this, "Failed to update request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void decrementBookCopies(String bookId) {
        booksRef.child(bookId).child("copies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null || current <= 0) return Transaction.success(currentData);
                currentData.setValue(current - 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) { }
        });
    }
}
