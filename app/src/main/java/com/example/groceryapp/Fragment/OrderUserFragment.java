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

import com.example.groceryapp.Adapter.AdapterOrderUser;
import com.example.groceryapp.Model.ModelOrderUser;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class OrderUserFragment extends Fragment {

    private FirebaseAuth auth;
    private RecyclerView orderRecyclerView;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    private ProgressDialog progressDialog;


    public OrderUserFragment() {
        // Required empty public constructor
    }


    public static OrderUserFragment newInstance() {
        OrderUserFragment fragment = new OrderUserFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getContext());

        auth = FirebaseAuth.getInstance();

        orderRecyclerView = view.findViewById(R.id.ordersRv);
        loadOrders();


    }

    private void loadOrders() {
        ordersList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("orderBy").equalTo(auth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            //add to list
                                            ordersList.add(modelOrderUser);

                                        }
                                        //setupAdapter
                                        adapterOrderUser = new AdapterOrderUser(getContext(),ordersList);
                                        //set to recyclerview
                                        orderRecyclerView.setAdapter(adapterOrderUser);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}