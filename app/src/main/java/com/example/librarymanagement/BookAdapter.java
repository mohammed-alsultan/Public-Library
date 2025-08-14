package com.example.librarymanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookActionListener {
        void onEdit(Book book);
        void onDelete(Book book);
        void onBorrow(Book book);
    }

    private final Context context;
    private final ArrayList<Book> bookList;
    private final OnBookActionListener listener;
    private final boolean isAdmin;

    public BookAdapter(Context context, ArrayList<Book> bookList,
                       OnBookActionListener listener, boolean isAdmin) {
        this.context = context;
        this.bookList = bookList;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvCategory.setText(book.getCategory());
        holder.tvCopies.setText("Copies: " + book.getCopies());
        holder.tvPenalty.setText("Penalty per day: " + book.getPenaltyPerDay());

        // Role-based visibility
        if (isAdmin) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnBorrow.setVisibility(View.GONE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnBorrow.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(book));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(book));
        holder.btnBorrow.setOnClickListener(v -> {
            if (book.getCopies() <= 0) {
                Toast.makeText(context, "No copies available", Toast.LENGTH_SHORT).show();
            } else {
                listener.onBorrow(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvCategory, tvCopies, tvPenalty;
        ImageButton btnEdit, btnDelete, btnBorrow;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvCategory = itemView.findViewById(R.id.tvBookCategory);
            tvCopies = itemView.findViewById(R.id.tvCopies);
            tvPenalty = itemView.findViewById(R.id.tvPenalty);
            btnEdit = itemView.findViewById(R.id.btnEditBook);
            btnDelete = itemView.findViewById(R.id.btnDeleteBook);
            btnBorrow = itemView.findViewById(R.id.btnBorrowBook);
        }
    }
}
