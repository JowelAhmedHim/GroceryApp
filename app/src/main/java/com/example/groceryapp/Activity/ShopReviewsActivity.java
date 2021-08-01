package com.example.groceryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.groceryapp.Adapter.AdapterReview;
import com.example.groceryapp.Model.ModelReview;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private String shopUid;

    private ImageView product_image;
    private ImageButton backBtn;
    private TextView shopNameTv,ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");
        backBtn = findViewById(R.id.backBtn);
        product_image = findViewById(R.id.product_image);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        ratingsTv = findViewById(R.id.ratingsTv);
        recyclerView = findViewById(R.id.reviewRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails();
        loadReviews();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        

    }

    private float ratingSum= 0;

    private void loadReviews() {

        reviewArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data in it
                        reviewArrayList.clear();
                        ratingSum = 0;

                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum +rating;

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }

                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this,reviewArrayList);
                        //set up recyclerview
                        recyclerView.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        ratingsTv.setText(String.format("%.2f",avgRating) + "[" + numberOfReviews + "]");
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_24).into(product_image);
                        }catch (Exception e){
                            product_image.setImageResource(R.drawable.ic_baseline_maps_home_work_24);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}