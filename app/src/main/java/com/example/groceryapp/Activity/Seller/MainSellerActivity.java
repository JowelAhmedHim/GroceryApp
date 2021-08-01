package com.example.groceryapp.Activity.Seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Activity.AddProductActivity;
import com.example.groceryapp.Activity.LoginActivity;
import com.example.groceryapp.Activity.PromotionCodesActivity;
import com.example.groceryapp.SettingActivity;
import com.example.groceryapp.Activity.ShopReviewsActivity;
import com.example.groceryapp.Adapter.SectionPagerAdapter;
import com.example.groceryapp.Fragment.OrdersFragment;
import com.example.groceryapp.Fragment.ProductsFragment;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView nameTv,shopNameTv,shopEmailTv,verificationTv;
    private ImageButton editProfile,addProduct,moreBtn;
    private ImageView profileImageView;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SectionPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);



        auth = FirebaseAuth.getInstance();

        checkUser();

        init();

        FirebaseUser user = auth.getCurrentUser();
        if (!user.isEmailVerified()){
            verificationTv.setVisibility(View.VISIBLE);
            verificationTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //send verification code
                    FirebaseUser user = auth.getCurrentUser();
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainSellerActivity.this, "Verification Email has been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        editProfile.setOnClickListener(this);
        addProduct.setOnClickListener(this);


        //popup menu
        PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this,moreBtn);
        //add menu items to our menu

        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Promotion Codes");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("LogOut");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getTitle() == "Settings"){
                    startActivity(new Intent(MainSellerActivity.this, SettingActivity.class));

                }else if (item.getTitle() == "Reviews")
                {
                    showShopReview();
                }
                else if (item.getTitle() == "Promotion Codes")
                {
                    startActivity(new Intent(MainSellerActivity.this, PromotionCodesActivity.class));

                }else if (item.getTitle() == "LogOut"){
                    makeMeOffline();
                }
                return true;
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });


        //setup viewPager and TabLayout
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Products");
        tabLayout.getTabAt(1).setText("Orders");
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void init() {
        profileImageView = findViewById(R.id.profile_image);
        nameTv = findViewById(R.id.sellerName);
        shopNameTv = findViewById(R.id.shopName);
        shopEmailTv = findViewById(R.id.shopEmail);
        editProfile = findViewById(R.id.editProfile);
        addProduct = findViewById(R.id.addProductBtn);
        moreBtn = findViewById(R.id.moreBtn);
        verificationTv = findViewById(R.id.verifyTv);
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(ProductsFragment.newInstance(),"Products");
        viewPagerAdapter.addFragment(OrdersFragment.newInstance(),"Orders");
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editProfile:
                startActivity(new Intent(MainSellerActivity.this, EditSellerAcitivity.class));
                finish();
                break;
            case R.id.addProductBtn:
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
                break;
        }
    }

    private void makeMeOffline(){
        progressDialog.setMessage("Checking User.....");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Update successfully
                        auth.signOut();
                        checkUser();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showShopReview() {

        Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
        intent.putExtra("shopUid",auth.getUid());
        startActivity(intent);
    }

    private void checkUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            fileList();
        }else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();


                            nameTv.setText(name + "(" +accountType +")");
                            shopNameTv.setText(shopName);
                            shopEmailTv.setText(email);
                            try {
                                Picasso.get().load(profileImage).into(profileImageView);

                            }catch (Exception e)
                            {
                                profileImageView.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}