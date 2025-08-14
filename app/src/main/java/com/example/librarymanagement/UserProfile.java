package com.example.librarymanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnUpdate, btnDelete;
    private DatabaseReference userRef;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // Get the logged-in user's ID from SignInActivity
        userKey = getIntent().getStringExtra("USER_ID");

        if (userKey == null) {
            Toast.makeText(this, "Error: No user ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey);

        // Load the user's details
        loadUserDetails();

        // Update button click
        btnUpdate.setOnClickListener(v -> updateUserDetails());

        // Delete button click
        btnDelete.setOnClickListener(v -> deleteUser());
    }

    private void loadUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etEmail.setText(snapshot.child("email").getValue(String.class));
                    etPassword.setText(snapshot.child("password").getValue(String.class));
                } else {
                    Toast.makeText(UserProfile.this, "User details not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserDetails() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("name").setValue(name);
        userRef.child("email").setValue(email);
        userRef.child("password").setValue(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserProfile.this, "Details updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfile.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser() {
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserProfile.this, "Account deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserProfile.this, SignInActivity.class));
                        finish();
                    } else {
                        Toast.makeText(UserProfile.this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
