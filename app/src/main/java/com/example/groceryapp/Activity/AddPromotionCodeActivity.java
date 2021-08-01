package com.example.groceryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText promoCodeEt, promoDescriptionEt, promoPriceEt, minimumPriceEt;
    private TextView expireDateTv,titleTv;
    private Button addBtn;

    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String promoId;
    private boolean isUpdating = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        

        backBtn = findViewById(R.id.backBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        titleTv =  findViewById(R.id.titleTv);

        Intent intent =  getIntent();
        if (intent.getStringExtra("promoId") != null){

            promoId = intent.getStringExtra("promoId");
            titleTv.setText("Update Promotion Code");
            addBtn.setText("Update");
            isUpdating = true;
            loadPromoCodeInfo();

        }else {
            titleTv.setText("Add Promotion Code");
            addBtn.setText("Add");
            isUpdating = false;
            Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();

        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputData();
            }
        });
    }

    private void loadPromoCodeInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get info of promo code
                        String id = ""+snapshot.child("id").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String promoCode = ""+snapshot.child("promoCode").getValue();
                        String promoPrice = ""+snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = ""+snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = ""+snapshot.child("expireDate").getValue();

                        //set data
                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        //get input data as String
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        //validate from data
        if (TextUtils.isEmpty(promoCode)) {
            Toast.makeText(this, "Enter Promo Code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Promo Description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(promoPrice)) {
            Toast.makeText(this, "Enter Promo Price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter Minimum Order price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter Minimum Order price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(expireDate)) {
            Toast.makeText(this, "Enter Expire Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating){
            //update data
            updateDataDb();

        }else {
            //add data
            addDataToDb();

        }



    }

    private void updateDataDb() {

        progressDialog.setMessage("Updating Promotion Code");
        progressDialog.show();

        //setup data to add in firebase database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("description",""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice",""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Updating Successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

    }

    private void addDataToDb() {
        progressDialog.setMessage("Adding Promotion Code");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in firebase database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("description",""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice",""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion").child(timestamp)
              .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Promotion code added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void datePickDialog() {

        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                DecimalFormat format = new DecimalFormat("00");
                String pDay = format.format(dayOfMonth);
                String pMonth = format.format(month);
                String pYear = format.format(year);
                String pDate = pDay + "/" + pMonth + "/" + pYear;

                expireDateTv.setText(pDate);
            }
            }, mYear, mMonth, mDay);

        dialog.show();

        //disable past date selection on calender
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }
}