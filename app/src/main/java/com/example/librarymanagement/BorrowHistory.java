package com.example.librarymanagement;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BorrowHistory extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private final ArrayList<BorrowRequest> requests = new ArrayList<>();
    private RequestsAdapter adapter;

    private DatabaseReference booksRef;
    private DatabaseReference requestsRef;

    private String currentUserId = "USER_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_history);

        if (getIntent().hasExtra("USER_ID")) currentUserId = getIntent().getStringExtra("USER_ID");

        recyclerRequests = findViewById(R.id.recyclerRequests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RequestsAdapter(requests, this::returnBook);
        recyclerRequests.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        booksRef = db.getReference("Books");
        requestsRef = db.getReference("BorrowRequests");

        loadMyRequests();
    }

    private void loadMyRequests() {
        requestsRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requests.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BorrowRequest br = ds.getValue(BorrowRequest.class);
                            if (br != null) requests.add(br);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BorrowHistory.this, "Load failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void returnBook(BorrowRequest req) {
        // Fetch the book for penalty rate
        booksRef.child(req.getBookId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book b = snapshot.getValue(Book.class);
                if (b == null) { Toast.makeText(BorrowHistory.this, "Book not found", Toast.LENGTH_SHORT).show(); return; }

                long now = System.currentTimeMillis();
                long lateDays = Math.max(0, (now - req.getDueDate()) / (1000L*60*60*24));
                double penalty = lateDays * b.getPenaltyPerDay();

                requestsRef.child(req.getRequestId()).child("returnDate").setValue(now);
                requestsRef.child(req.getRequestId()).child("status").setValue(lateDays > 0 ? "returned_late" : "returned");
                requestsRef.child(req.getRequestId()).child("penaltyAmount").setValue(penalty)
                        .addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                // increment copies
                                booksRef.child(b.getBookId()).child("copies").runTransaction(new Transaction.Handler() {
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

                                String msg = lateDays > 0 ? ("Returned late. Penalty: " + penalty) : "Returned on time.";
                                Toast.makeText(BorrowHistory.this, msg, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(BorrowHistory.this, "Return failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Simple inner adapter for requests list
    static class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RVH> {
        interface OnReturnClick { void onClick(BorrowRequest req); }

        private final ArrayList<BorrowRequest> list;
        private final OnReturnClick onReturnClick;

        RequestsAdapter(ArrayList<BorrowRequest> list, OnReturnClick onReturnClick) {
            this.list = list;
            this.onReturnClick = onReturnClick;
        }

        @NonNull @Override public RVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_borrow, parent, false);
            return new RVH(v);
        }

        @Override public void onBindViewHolder(@NonNull RVH h, int pos) {
            BorrowRequest r = list.get(pos);
            h.tvTitle.setText("Request: " + r.getRequestId());
            h.tvStatus.setText("Status: " + r.getStatus());
            String due = DateFormat.getDateInstance().format(new Date(r.getDueDate()));
            h.tvDue.setText("Due: " + due);
            h.btnReturn.setOnClickListener(v -> onReturnClick.onClick(r));
        }

        @Override public int getItemCount() { return list.size(); }

        static class RVH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatus, tvDue;
            android.widget.Button btnReturn;
            RVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvReqTitle);
                tvStatus = itemView.findViewById(R.id.tvReqStatus);
                tvDue = itemView.findViewById(R.id.tvReqDue);
                btnReturn = itemView.findViewById(R.id.btnReturn);
            }
        }
    }
}
