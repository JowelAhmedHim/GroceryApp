package com.example.groceryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Activity.Buyer.MainUserActivity;
import com.example.groceryapp.Activity.Seller.MainSellerActivity;
import com.example.groceryapp.Activity.Seller.RegisterSellerActivity;
import com.example.groceryapp.ForgetPasswordActivity;
import com.example.groceryapp.InputValidation;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {



    private TextInputLayout lay_email,lay_password;
    private TextInputEditText emailEt,passwordEt;
    private Button loginBtn;
    private TextView forgetPass,btnSignUp;

    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private InputValidation inputValidation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputValidation = new InputValidation(this);
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        init();

        loginBtn.setOnClickListener(this);
        forgetPass.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    private void init() {
        lay_email = findViewById(R.id.layout_email);
        lay_password = findViewById(R.id.layout_password);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        loginBtn = findViewById(R.id.signIn);
        forgetPass = findViewById(R.id.resetPassword);
        btnSignUp = findViewById(R.id.signUp);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.signIn:
                verificationUser();
                break;

            case R.id.signUp:
                startActivity(new Intent(LoginActivity.this, RegisterSellerActivity.class));
                break;

            case R.id.resetPassword:
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                break;
        }
    }

    private void verificationUser() {
        if(!inputValidation.isEmailValid(emailEt,lay_email)){
            return;
        }
        if(!inputValidation.isPasswordValid(passwordEt,lay_password)) {
            return;
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {

        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        progressDialog.setMessage("Logging In....");
        progressDialog.show();

        auth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //set user online info as true
    private void makeMeOnline() {
        progressDialog.setMessage("Checking User.....");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //checking user type
    private void checkUserType() {

        //check if user is seller or buyer,start activity accordingly
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                //user is seller start seller activity
                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            }else {
                                //user is buyer ,start buyer activity
                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}