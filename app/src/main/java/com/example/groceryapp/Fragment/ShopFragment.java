package com.example.groceryapp.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.groceryapp.Adapter.AdapterShop;
import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ShopFragment extends Fragment {

    private FirebaseAuth auth;
    private RecyclerView shopRecyclerView;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ProgressDialog progressDialog;


    public ShopFragment() {
        // Required empty public constructor
    }


    public static ShopFragment newInstance() {
        ShopFragment fragment = new ShopFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        progressDialog = new ProgressDialog(getContext());

        auth = FirebaseAuth.getInstance();


        shopRecyclerView = view.findViewById(R.id.shopRecyclerView);
        loadMyInfo();
    }

    private void loadShops(String city) {

        shopsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding
                        shopsList.clear();
                        for (DataSnapshot ds:  snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();

                            //show only user city shops

                            if (shopCity.equals(city)){

                                shopsList.add(modelShop);
                            }

                            //if you display all shops,skip the if statement and add this
                            //shopsList.add(modelShop);


                        }
                        //setup adapter
                        adapterShop = new AdapterShop(getContext(), shopsList);
                        //set adapter to recyclerView
                        shopRecyclerView.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String city  = ""+ds.child("city").getValue();
                            loadShops(city);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}