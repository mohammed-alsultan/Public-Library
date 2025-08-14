package com.example.librarymanagement;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BorrowRequestAdapter extends RecyclerView.Adapter<BorrowRequestAdapter.RequestViewHolder> {

    private final Context context;
    private final ArrayList<BorrowRequestWithBook> requests;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BorrowRequestWithBook rb);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public BorrowRequestAdapter(Context context, ArrayList<BorrowRequestWithBook> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_borrow_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BorrowRequestWithBook rb = requests.get(position);
        BorrowRequest req = rb.getRequest();
        Book book = rb.getBook();

        holder.tvBookTitle.setText("Title: " + book.getTitle());
        holder.tvBookAuthor.setText("Author: " + book.getAuthor());
        holder.tvBookCategory.setText("Category: " + book.getCategory());
        holder.tvStatus.setText("Status: " + req.getStatus());

        // Color coding for status
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvBorrowDate.setText("Borrowed: " + sdf.format(new Date(req.getBorrowDate())));
        holder.tvDueDate.setText("Due: " + sdf.format(new Date(req.getDueDate())));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserBorrowRequestDetailActivity.class);
            intent.putExtra("REQUEST_DETAIL", rb); // pass combined object
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(rb);
        });

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookAuthor, tvBookCategory, tvStatus, tvBorrowDate, tvDueDate;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvRequestBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvRequestBookAuthor);
            tvBookCategory = itemView.findViewById(R.id.tvRequestBookCategory);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);
            tvBorrowDate = itemView.findViewById(R.id.tvRequestBorrowDate);
            tvDueDate = itemView.findViewById(R.id.tvRequestDueDate);
        }
    }
}
