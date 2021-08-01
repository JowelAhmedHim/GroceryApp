package com.example.groceryapp.Activity.Buyer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Activity.LoginActivity;
import com.example.groceryapp.SettingActivity;
import com.example.groceryapp.Adapter.SectionPagerAdapter;
import com.example.groceryapp.Fragment.OrderUserFragment;
import com.example.groceryapp.Fragment.ShopFragment;
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

public class MainUserActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView nameTv,userEmailTv,userPhoneTv;
    private ImageButton editProfile,logout,settingBtn;
    private ImageView profileImageView;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SectionPagerAdapter viewPagerAdapter;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_user);

        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();

        checkUser();
        init();

        editProfile.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        logout.setOnClickListener(this);


        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Shops");
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
        profileImageView =  findViewById(R.id.profile_image);
        nameTv = findViewById(R.id.userName);
        userEmailTv =findViewById(R.id.userEmail);
        userPhoneTv = findViewById(R.id.userPhone);
        editProfile =  findViewById(R.id.editProfileBtn);
        logout = findViewById(R.id.logoutBtn);
        settingBtn = findViewById(R.id.settingBtn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editProfileBtn:
                startActivity(new Intent(MainUserActivity.this, EditUserActivity.class));
                break;
            case R.id.settingBtn:
                startActivity(new Intent(MainUserActivity.this, SettingActivity.class));
                break;
            case R.id.logoutBtn:
                makeMeOffline();
                break;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(ShopFragment.newInstance(),"Shops");
        viewPagerAdapter.addFragment(OrderUserFragment.newInstance(),"Orders");
        viewPager.setAdapter(viewPagerAdapter);
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
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
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

                            //get user data
                            String name  = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();


                            nameTv.setText(name + "(" +accountType +")");
                            userEmailTv.setText(email);
                            userPhoneTv.setText(phone);
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