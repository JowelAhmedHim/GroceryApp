package com.example.groceryapp.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.groceryapp.Adapter.AdapterProductSeller;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelProduct;
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
 * Use the {@link ProductsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductsFragment extends Fragment {

    private TextView productsTv,filterProductTv;
    private ImageButton filterProduct;
    private EditText productSearch;

    private RecyclerView productRecyclerView;

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProductSeller adapterProductSeller;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    public ProductsFragment() {
        // Required empty public constructor
    }


    public static ProductsFragment newInstance() {
        ProductsFragment fragment = new ProductsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        loadAllProducts();

        filterProduct = view.findViewById(R.id.filter);
        filterProductTv = view.findViewById(R.id.filterProductTv);
        productRecyclerView = view.findViewById(R.id.recyclerView);
        productSearch = view.findViewById(R.id.searchProduct);

        filterProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });

        productSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadFilteredProducts(String selected) {
        productArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productArrayList.clear();
                        for (DataSnapshot ds :snapshot.getChildren()){
                            String productCategory = ""+ ds.child("productCategory").getValue();
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productArrayList.add(modelProduct);
                            }
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(getContext(),productArrayList);
                        productRecyclerView.setAdapter(adapterProductSeller);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


    }

    private void showCategoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Category:")
                .setItems(Constants.productOptions1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = Constants.productOptions1[which];
                        filterProductTv.setText(selected);
                        if(selected.equals("All")){
                            //load all
                            loadAllProducts();
                        }else {
                            //load filtered
                            loadFilteredProducts(selected);
                        }
                    }
                }).show();
    }

    private void loadAllProducts() {

        productArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productArrayList.clear();
                        for (DataSnapshot ds :snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productArrayList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(getContext(),productArrayList);
                        productRecyclerView.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}