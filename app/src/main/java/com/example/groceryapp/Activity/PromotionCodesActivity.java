package com.example.groceryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterPromotionShop;
import com.example.groceryapp.Model.ModelPromotion;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    ImageButton backBrn,addBtn,filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        firebaseAuth = FirebaseAuth.getInstance();

        backBrn = findViewById(R.id.backBtn);
        addBtn = findViewById(R.id.addbtn);

        filteredTv = findViewById(R.id.filteredTv);
        filterBtn = findViewById(R.id.filterBtn);
        promoRv = findViewById(R.id.promoRv);


        loadAllPromoCodes();


        backBrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PromotionCodesActivity.this,AddPromotionCodeActivity.class));

            }
        });
        
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });

    }

    private void filterDialog() {

        String[] options = {"All","Expired","Not Expired"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Code")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which==0){
                            filteredTv.setText("All Promotion Code");
                            loadAllPromoCodes();
                        } else if (which==1) {
                            filteredTv.setText("Expired Promotion Code");
                            loadExpiredPromoCode();

                        }else  if (which==2){
                            filteredTv.setText("Not expired Promotion Code");
                            loadNotExpiredPromoCode();
                        }

                    }
                })
                .show();
    }

    private void loadAllPromoCodes(){

        //init list
        promotionArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            promotionArrayList.add(modelPromotion);
                        }

                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadExpiredPromoCode(){

        //get current date;

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String todayDate = day + "/"+ month+ "/"+ year;


        //init list
        promotionArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            try {
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = format.parse(todayDate);
                                Date expireDate = format.parse(expDate);

                                if (expireDate.compareTo(currentDate) > 0){

                                }else  if (expireDate.compareTo(currentDate) < 0){
                                    //expired
                                    promotionArrayList.add(modelPromotion);

                                }else if (expireDate.compareTo(currentDate) == 0){

                                }

                            }catch (Exception e){

                            }
                        }

                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadNotExpiredPromoCode(){
        //get current date;
        DecimalFormat decimalFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String todayDate = day+"/"+month+"/"+year;


        //init list
        promotionArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            try {
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = format.parse(todayDate);
                                Date expireDate = format.parse(expDate);

                                if (expireDate.compareTo(currentDate) >0){
                                    //not expired
                                    promotionArrayList.add(modelPromotion);

                                }else  if (expireDate.compareTo(currentDate)<0){



                                }else if (expireDate.compareTo(currentDate)==0){
                                    promotionArrayList.add(modelPromotion);

                                }


                            }catch (Exception e){

                            }

                        }

                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}