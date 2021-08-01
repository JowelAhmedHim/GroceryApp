package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.groceryapp.Activity.LoginActivity;
import com.example.groceryapp.Activity.Seller.MainSellerActivity;
import com.example.groceryapp.Activity.Buyer.MainUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private Animation animBlink;
    private TextView title;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       //         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        animBlink = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bounce);
        title = findViewById(R.id.titleApp);
        title.startAnimation(animBlink);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                if (user== null){
                    //user not logged in, start loginActivity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }else {
                    //user is logged in,check user type;
                    checkUserType();
                }

            }
        },2000);
    }

    private void checkUserType() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();

                        if (accountType.equals("User")){
                            progressDialog.dismiss();

                            startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                            finish();
                        }else {
                            progressDialog.dismiss();

                            startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



}