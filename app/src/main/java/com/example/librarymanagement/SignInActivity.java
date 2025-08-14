package com.example.librarymanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout editID, editPass;
    private Button buttonLogin;
    private TextView toSignUp;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Firebase init
        FirebaseApp.initializeApp(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // UI Bindings
        editID = findViewById(R.id.editID);
        editPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.buttonLogin);
        toSignUp = findViewById(R.id.toSignUp);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Click listeners
        buttonLogin.setOnClickListener(this);
        toSignUp.setOnClickListener(this);
    }

    private boolean verifyFields() {
        String email = editID.getEditText().getText().toString().trim();
        String pass = editPass.getEditText().getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginUser() {
        if (!verifyFields()) return;

        progressDialog.setMessage("Signing In...");
        progressDialog.show();

        String id = editID.getEditText().getText().toString().trim();
        String pass = editPass.getEditText().getText().toString().trim();

        String userKey = id.replace(".", "_");

        databaseRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    // Check if password matches
                    String storedPass = snapshot.child("password").getValue(String.class);
                    if (storedPass != null && storedPass.equals(pass)) {
                        Integer type = snapshot.child("type").getValue(Integer.class);
                        if (type != null) {
                            if (type == 0) {
                                Toast.makeText(SignInActivity.this, "User Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignInActivity.this,UserDashboardActivity.class);
                                intent.putExtra("USER_ID", userKey);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignInActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignInActivity.this, AdminDashboardActivity.class));
                            }
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Account type missing in database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "Account not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(SignInActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == buttonLogin) {
            loginUser();
        } else if (v == toSignUp) {
            startActivity(new Intent(this, SignUpActivity.class));
        }
    }
}
