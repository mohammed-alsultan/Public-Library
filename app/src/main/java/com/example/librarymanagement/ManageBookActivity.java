package com.example.librarymanagement;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class ManageBookActivity extends AppCompatActivity {

    private RecyclerView recyclerBooks;
    private FloatingActionButton fabAddBook;
    private ArrayList<Book> bookList;
    private BookAdapter adapter;
    private DatabaseReference booksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_book);

        recyclerBooks = findViewById(R.id.recyclerBooks);
        fabAddBook = findViewById(R.id.fabAddBook);
        booksRef = FirebaseDatabase.getInstance().getReference("books");

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();

        adapter = new BookAdapter(this, bookList, new BookAdapter.OnBookActionListener() {
            @Override
            public void onEdit(Book book) {
                showBookDialog(book);
            }

            @Override
            public void onDelete(Book book) {
                booksRef.child(book.getBookId()).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(ManageBookActivity.this, "Book deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ManageBookActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBorrow(Book book) {
                // Not used in admin mode
            }
        }, true); // pass true for admin


        recyclerBooks.setAdapter(adapter);

        loadBooks();

        fabAddBook.setOnClickListener(v -> showBookDialog(null));
    }

    private void loadBooks() {
        booksRef.addValueEventListener(new ValueEventListener() {
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

    private void showBookDialog(Book existingBook) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_book, null);

        EditText etTitle = dialogView.findViewById(R.id.etBookTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etBookAuthor);
        EditText etCategory = dialogView.findViewById(R.id.etBookCategory);
        EditText etCopies = dialogView.findViewById(R.id.etBookCopies);
        EditText etMaxBorrowDays = dialogView.findViewById(R.id.etMaxBorrowDays);
        EditText etPenaltyPerDay = dialogView.findViewById(R.id.etPenaltyPerDay);
        CheckBox cbAvailable = dialogView.findViewById(R.id.cbAvailable);

        if (existingBook != null) {
            etTitle.setText(existingBook.getTitle());
            etAuthor.setText(existingBook.getAuthor());
            etCategory.setText(existingBook.getCategory());
            etCopies.setText(String.valueOf(existingBook.getCopies()));
            etMaxBorrowDays.setText(String.valueOf(existingBook.getMaxBorrowDays()));
            etPenaltyPerDay.setText(String.valueOf(existingBook.getPenaltyPerDay()));
            cbAvailable.setChecked(existingBook.isAvailable());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingBook == null ? "Add Book" : "Edit Book")
                .setView(dialogView)
                .setPositiveButton("Save", null) // we'll override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String author = etAuthor.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                String copiesStr = etCopies.getText().toString().trim();
                String maxDaysStr = etMaxBorrowDays.getText().toString().trim();
                String penaltyStr = etPenaltyPerDay.getText().toString().trim();
                boolean available = cbAvailable.isChecked();

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) ||
                        TextUtils.isEmpty(category) || TextUtils.isEmpty(copiesStr) ||
                        TextUtils.isEmpty(maxDaysStr) || TextUtils.isEmpty(penaltyStr)) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return; // keep dialog open
                }

                int copies = Integer.parseInt(copiesStr);
                int maxBorrowDays = Integer.parseInt(maxDaysStr);
                double penaltyPerDay = Double.parseDouble(penaltyStr);

                if (existingBook == null) {
                    String bookId = booksRef.push().getKey();
                    Book book = new Book(bookId, title, author, category, copies, maxBorrowDays, penaltyPerDay, available);
                    booksRef.child(bookId).setValue(book);
                } else {
                    existingBook.setTitle(title);
                    existingBook.setAuthor(author);
                    existingBook.setCategory(category);
                    existingBook.setCopies(copies);
                    existingBook.setMaxBorrowDays(maxBorrowDays);
                    existingBook.setPenaltyPerDay(penaltyPerDay);
                    existingBook.setAvailable(available);
                    booksRef.child(existingBook.getBookId()).setValue(existingBook);
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

}
