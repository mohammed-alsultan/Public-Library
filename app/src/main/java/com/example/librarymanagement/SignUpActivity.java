package com.example.librarymanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout editName, editEnrollNo, editCardNo, editID, editPass, editPass1;
    private Button buttonRegister;
    private TextView toSignIn;
    private CheckBox check1;
    private Spinner userType;
    private String type;
    private int type1;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase init
        FirebaseApp.initializeApp(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // UI Bindings
        editName = findViewById(R.id.editName);
        editEnrollNo = findViewById(R.id.editEnrollNo);
        editCardNo = findViewById(R.id.editCardNo);
        editID = findViewById(R.id.editID);
        editPass = findViewById(R.id.editPass);
        editPass1 = findViewById(R.id.editPass1);
        buttonRegister = findViewById(R.id.buttonRegister);
        toSignIn = findViewById(R.id.toSignIn);
        check1 = findViewById(R.id.check1);
        userType = findViewById(R.id.userType);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Spinner setup
        List<String> list = new ArrayList<>();
        list.add("Select Account Type");
        list.add("User");
        list.add("Admin");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userType.setAdapter(adapter);

        userType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = parent.getItemAtPosition(position).toString();
                if (type.equals("Select Account Type")) {
                    setFieldsEnabled(false, false);
                } else if (type.equals("User")) {
                    setFieldsEnabled(true, true);
                } else { // Admin
                    setFieldsEnabled(true, false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Click listeners
        buttonRegister.setOnClickListener(this);
        toSignIn.setOnClickListener(this);
        check1.setOnClickListener(this);
    }

    private void setFieldsEnabled(boolean enableNameEmailPass, boolean enableEnrollCard) {
        editName.setEnabled(enableNameEmailPass);
        editID.setEnabled(enableNameEmailPass);
        editPass.setEnabled(enableNameEmailPass);
        editPass1.setEnabled(enableNameEmailPass);
        editEnrollNo.setEnabled(enableEnrollCard);
        editCardNo.setEnabled(enableEnrollCard);
    }

    private boolean verifyFields() {
        if (type.equals("Select Account Type")) {
            Toast.makeText(this, "Please select account type!", Toast.LENGTH_SHORT).show();
            return false;
        }

        String name = editName.getEditText().getText().toString().trim();
        String email = editID.getEditText().getText().toString().trim();
        String pass = editPass.getEditText().getText().toString().trim();
        String pass1 = editPass1.getEditText().getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || pass1.isEmpty()) {
            Toast.makeText(this, "All required fields must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pass.equals(pass1)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (type.equals("User")) {
            String enroll = editEnrollNo.getEditText().getText().toString().trim();
            String card = editCardNo.getEditText().getText().toString().trim();
            if (enroll.isEmpty() || card.isEmpty()) {
                Toast.makeText(this, "Enrollment No. and Card No. are required for User!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void registerUser() {
        if (!verifyFields()) return;

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        String id = editID.getEditText().getText().toString().trim();
        String name = editName.getEditText().getText().toString().trim();
        String pass = editPass.getEditText().getText().toString().trim();

        if (type.equals("User")) {
            type1 = 0;
            int enroll = Integer.parseInt(editEnrollNo.getEditText().getText().toString().trim());
            int card = Integer.parseInt(editCardNo.getEditText().getText().toString().trim());

            User user = new User(name, id, enroll, card, type1, pass);
            databaseRef.child(id.replace(".", "_")).setValue(user)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this,SignInActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            type1 = 1;
            Admin admin = new Admin(type1, name, id, pass);
            databaseRef.child(id.replace(".", "_")).setValue(admin)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Admin Registered Successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == check1) {
            buttonRegister.setEnabled(check1.isChecked());
        } else if (v == buttonRegister) {
            registerUser();
        } else if (v == toSignIn) {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(new Intent(intent));
        }
    }
}
