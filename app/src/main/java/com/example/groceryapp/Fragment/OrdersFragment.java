package com.example.groceryapp.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.groceryapp.Adapter.AdapterOrderShop;
import com.example.groceryapp.Model.ModelOrderShop;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrdersFragment extends Fragment {


    private TextView ordersTv,filterOrderTv;
    private ImageButton filterOrderBtn;
    private RecyclerView ordersRv;


    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    public OrdersFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static OrdersFragment newInstance() {
        OrdersFragment fragment = new OrdersFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        loadAllOrders();

        ordersRv = view.findViewById(R.id.ordersRv);
        filterOrderTv = view.findViewById(R.id.filterOrdersTv);
        filterOrderBtn = view.findViewById(R.id.filterOrderBtn);

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterOrder();
            }
        });
    }

    private void showFilterOrder() {
        //option for displaying on filterOrders button click
        String[] options = {"All","In Progress","Completed","Cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filter Orders")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Handle option item click
                        if (which == 0){
                            filterOrderTv.setText("Showing All Orders");
                            adapterOrderShop.getFilter().filter("");
                        }else {
                            String optionClicked = options[which];
                            filterOrderTv.setText("Showing "+optionClicked + "Orders");
                            adapterOrderShop.getFilter().filter(optionClicked);
                        }

                    }
                })
                .show();
    }
    private void loadAllOrders() {

        orderShopArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            // add to list
                            orderShopArrayList.add(modelOrderShop);
                        }

                        //setup Adapter
                        adapterOrderShop = new AdapterOrderShop(getContext(),orderShopArrayList);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}