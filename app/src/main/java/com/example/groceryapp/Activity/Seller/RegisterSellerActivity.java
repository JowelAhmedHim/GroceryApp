package com.example.groceryapp.Activity.Seller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.groceryapp.Activity.Buyer.RegisterUserActivity;
import com.example.groceryapp.Activity.LoginActivity;
import com.example.groceryapp.InputValidation;
import com.example.groceryapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterSellerActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputLayout lay_email,lay_pass,lay_confPass;
    private EditText edEmail, edPassword, edConfirmPassword;
    private Button btnSignUp;
    private TextView txtRegisterAsSeller, txtSignIn;

    private String email,password,confirmPass;

    private ProgressDialog progressDialog;
    private InputValidation inputValidation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        inputValidation = new InputValidation(this);

        //progress dialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        init();

        btnSignUp.setOnClickListener(this);
        txtRegisterAsSeller.setOnClickListener(this);
        txtSignIn.setOnClickListener(this);
    }


    private void init() {

        lay_email = findViewById(R.id.layout_email);
        lay_pass = findViewById(R.id.layout_password);
        lay_confPass = findViewById(R.id.layout_confirmPass);

        edEmail = (EditText) findViewById(R.id.email);
        edPassword = (EditText) findViewById(R.id.password);
        edConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        btnSignUp =findViewById(R.id.nextBtn);
        txtRegisterAsSeller = (TextView) findViewById(R.id.registerUser);
        txtSignIn = (TextView) findViewById(R.id.signIn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.nextBtn:
                inputValidation();
                break;

            case R.id.signIn:
                startActivity(new Intent(RegisterSellerActivity.this, LoginActivity.class));
                break;

            case R.id.registerUser:
                startActivity(new Intent(RegisterSellerActivity.this, RegisterUserActivity.class));
                break;

        }

    }

    private void inputValidation() {

        email = edEmail.getText().toString();
        password = edPassword.getText().toString();
        confirmPass = edConfirmPassword.getText().toString();

        if (!inputValidation.isEmailValid(edEmail,lay_email))
        {
            return;
        }

        if (!inputValidation.isPasswordValid(edPassword,lay_pass))
        {
            return;
        }

        if (!inputValidation.isPasswordMatch(edPassword,edConfirmPassword,lay_confPass)){
            return;
        }
        else {
            Intent intent  = new Intent(RegisterSellerActivity.this, RegisterSellerProfile.class);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
        }
    }



}