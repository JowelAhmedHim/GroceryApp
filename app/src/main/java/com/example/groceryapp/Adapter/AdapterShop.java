package com.example.groceryapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.R;
import com.example.groceryapp.Activity.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop  extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    private ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {

        //get Data
        ModelShop modelShop = shopList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String state = modelShop.getState();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String phone = modelShop.getPhone();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String uid = modelShop.getUid();
        String shopOpen = modelShop.getShopOpen();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();
        String online = modelShop.getOnline();
        
        loadReviews(modelShop,holder);

        //set Data
        holder.shopNameTv.setText(shopName);
        holder.addressTv.setText(address);
        holder.phoneTv.setText(phone);
        if (online.equals("true")){
            holder.onlineIV.setVisibility(View.VISIBLE);
        }else {
            holder.onlineIV.setVisibility(View.GONE);
        }

        if (shopOpen.equals("true")){
            holder.shopClosedTv.setVisibility(View.GONE);
        }else {
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).into(holder.shopImageIv);
        }catch (Exception e){
            holder.shopImageIv.setImageResource(R.drawable.ic_baseline_store_24);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });


    }

    private float ratingSum= 0;

    private void loadReviews(ModelShop modelShop, HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data in it
                        ratingSum = 0;

                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum +rating;

                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return shopList.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{

        //ui view of row shop
        private ImageView shopImageIv,onlineIV;
        private TextView shopClosedTv,phoneTv,shopNameTv,addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);
            shopImageIv = itemView.findViewById(R.id.shopImage);
            onlineIV = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopCloseTv);
            phoneTv = itemView.findViewById(R.id.shopPhoneTv);
            addressTv = itemView.findViewById(R.id.shopAddressTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
