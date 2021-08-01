package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Activity.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPasswordActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout lay_email;
    private EditText edEmail;
    private ImageView back;
    private Button recoverBtn;

    private String email;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private InputValidation inputValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        auth = FirebaseAuth.getInstance();
        inputValidation = new InputValidation(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        lay_email = findViewById(R.id.layout_email);
        edEmail = findViewById(R.id.email);
        back =  findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recoverBtn = findViewById(R.id.recover);
        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
        
    }

    private void recoverPassword() {

        email = edEmail.getText().toString();
        if (!inputValidation.isEmailValid(edEmail,lay_email))
        {
            return;
        }

        progressDialog.setMessage("Sending email to reset email");
        progressDialog.show();

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this, "Check your email..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}