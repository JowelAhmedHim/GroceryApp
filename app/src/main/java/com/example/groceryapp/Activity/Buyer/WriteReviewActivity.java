package com.example.groceryapp.Activity.Buyer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;

    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);


        profileIv = findViewById(R.id.profile_image);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        auth = FirebaseAuth.getInstance();

        shopUid = getIntent().getStringExtra("shopUid");
        
        //load shop info:shop name, shop Image
        loadShopInfo();

        //if user has written review to this shop ,load it
        loadMyReview();


        backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void loadShopInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("profileImage").getValue();

                shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_baseline_maps_home_work_24).into(profileIv);
                }catch (Exception e){
                    profileIv.setImageResource(R.drawable.ic_baseline_maps_home_work_24);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            //my review is available in this shop
                            //get review details

                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            //set review details in our UI
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();

        String timestamp = ""+System.currentTimeMillis();

        //setup data in hash map

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",auth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);

        //put data in db>users>shopUid>Rating
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(auth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WriteReviewActivity.this, "Review published successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }
}