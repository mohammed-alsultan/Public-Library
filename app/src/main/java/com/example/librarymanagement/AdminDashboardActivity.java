package com.example.librarymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView manageBooksTile, manageUsersTile, borrowRequestsTile, logoutTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Correct view binding
        manageBooksTile = findViewById(R.id.manageBooksTile);
        manageUsersTile = findViewById(R.id.manageUsersTile);
        borrowRequestsTile = findViewById(R.id.borrowRequestsTile);
        logoutTile = findViewById(R.id.logoutTile);

        // Click listeners
        manageBooksTile.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageBookActivity.class));
        });

        borrowRequestsTile.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminBorrowRequestsActivity.class));
        });

        logoutTile.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
