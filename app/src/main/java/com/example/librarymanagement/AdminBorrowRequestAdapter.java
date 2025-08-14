package com.example.librarymanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminBorrowRequestAdapter extends RecyclerView.Adapter<AdminBorrowRequestAdapter.AdminRequestViewHolder> {

    private final Context context;
    private final ArrayList<BorrowRequestWithBook> requests;
    private final DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("BorrowRequests");
    private final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");

    public AdminBorrowRequestAdapter(Context context, ArrayList<BorrowRequestWithBook> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public AdminRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_borrow_request, parent, false);
        return new AdminRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminRequestViewHolder holder, int position) {
        BorrowRequestWithBook rb = requests.get(position);
        BorrowRequest req = rb.getRequest();
        Book book = rb.getBook();

        holder.tvTitle.setText("Title: " + book.getTitle());
        holder.tvStatus.setText("Status: " + req.getStatus());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvBorrowDate.setText("Borrowed: " + sdf.format(new Date(req.getBorrowDate())));
        holder.tvDueDate.setText("Due: " + sdf.format(new Date(req.getDueDate())));

        // Real-time color coding
        switch (req.getStatus().toLowerCase()) {
            case "pending":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "approved":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "declined":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        holder.btnApprove.setOnClickListener(v -> {
            requestsRef.child(req.getRequestId()).child("status").setValue("approved");
            booksRef.child(book.getBookId()).child("copies").setValue(book.getCopies() - 1);
            Toast.makeText(context, "Approved!", Toast.LENGTH_SHORT).show();
        });

        holder.btnDecline.setOnClickListener(v -> {
            requestsRef.child(req.getRequestId()).child("status").setValue("declined");
            Toast.makeText(context, "Declined!", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class AdminRequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvBorrowDate, tvDueDate;
        Button btnApprove, btnDecline;

        public AdminRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminRequestTitle);
            tvStatus = itemView.findViewById(R.id.tvAdminRequestStatus);
            tvBorrowDate = itemView.findViewById(R.id.tvAdminRequestBorrowDate);
            tvDueDate = itemView.findViewById(R.id.tvAdminRequestDueDate);
            btnApprove = itemView.findViewById(R.id.btnApproveRequest);
            btnDecline = itemView.findViewById(R.id.btnDeclineRequest);
        }
    }
}
