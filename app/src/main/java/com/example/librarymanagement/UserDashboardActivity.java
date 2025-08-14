package com.example.librarymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvDashboardTitle;
    private CardView cardProfile, cardCheckBook, cardNotifications, cardMyBooks;

    private String userId; // Unique user key from SignIn
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Get the logged-in user's ID from intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase ref for this user
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Bind views
        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        cardProfile =findViewById(R.id.cardProfile);
        cardCheckBook = findViewById(R.id.cardCheckBook);
        cardNotifications = findViewById(R.id.cardNotifications);
        cardMyBooks = findViewById(R.id.cardMyBooks);

        // Load user name in the dashboard title
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null) {
                    tvDashboardTitle.setText("Welcome, " + name);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDashboardActivity.this, "Error loading user", Toast.LENGTH_SHORT).show();
            }
        });

        // Profile click → open ProfileActivity with user ID
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserProfile.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        cardCheckBook.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserBooksActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        cardMyBooks.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserBorrowRequestsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

    }
}
