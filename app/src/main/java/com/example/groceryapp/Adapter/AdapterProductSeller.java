package com.example.groceryapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Activity.EditProductActivity;
import com.example.groceryapp.FilterProduct;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller  extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {

        //get data
        ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductID();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDes();
        String productPrice = modelProduct.getProductPrice();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();

        //set data
        holder.title.setText(title);
        holder.quantity.setText(quantity);
        holder.discountedNote.setText(discountNote+" %");
        holder.originalPrice.setText("$"+productPrice);
        holder.discountPrice.setText("$"+discountPrice);
        if (discountAvailable.equals("true")){
            holder.discountPrice.setVisibility(View.VISIBLE);
            holder.discountedNote.setVisibility(View.VISIBLE);
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        }else {
            holder.discountPrice.setVisibility(View.GONE);
            holder.discountedNote.setVisibility(View.GONE);

        }
        try {
            Picasso.get().load(icon).into(holder.productIcon);

        }catch (Exception e)
        {
            holder.productIcon.setImageResource(R.drawable.ic_baseline_local_grocery_store_24);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsBottomSheet(modelProduct);
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {

        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);


        // inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        bottomSheetDialog.setContentView(view);

        //init view of bottom sheet
        ImageButton backBtn = view.findViewById(R.id.backbtn);
        ImageButton delete = view.findViewById(R.id.deleteBtn);
        ImageButton edit = view.findViewById(R.id.editBn);
        ImageView productIcon = view.findViewById(R.id.productIconTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView productTitleTv = view.findViewById(R.id.titleTv);
        TextView productDesTv = view.findViewById(R.id.descriptionTv);
        TextView productCategoryTv = view.findViewById(R.id.categoryTv);
        TextView productQuantityTv = view.findViewById(R.id.quantityTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView discountPriceTv = view.findViewById(R.id.discountPriceTv);

        //get data from modelView
        String id = modelProduct.getProductID();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String title = modelProduct.getProductTitle();
        String description = modelProduct.getProductDes();
        String category = modelProduct.getProductCategory();
        String quantity = modelProduct.getProductQuantity();
        String price = modelProduct.getProductPrice();
        String timestamp = modelProduct.getTimestamp();
        String icon = modelProduct.getProductIcon();

        //set data
        productTitleTv.setText(title);
        productDesTv.setText(description);
        productCategoryTv.setText(category);
        productQuantityTv.setText(quantity);

        discountNoteTv.setText(discountNote+"%");
        discountPriceTv.setText("$"+discountPrice);
        originalPriceTv.setText("$"+price);

        if (discountAvailable.equals("true")){
            discountNoteTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }else {
            discountPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon).into(productIcon);
        }catch (Exception e)
        {
           productIcon.setImageResource(R.drawable.ic_baseline_local_grocery_store_24);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete product"+title+"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProduct(id);
                                bottomSheetDialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        bottomSheetDialog.show();
    }

    private void deleteProduct(String id) {

        //delete product using product id
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Product").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
        {
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productIcon;
        private TextView discountedNote,title,quantity,discountPrice,originalPrice;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIcon = itemView.findViewById(R.id.product_image);
            discountedNote = itemView.findViewById(R.id.discountNoteTv);
            title = itemView.findViewById(R.id.titleTv);
            discountPrice = itemView.findViewById(R.id.discountPriceTv);
            originalPrice = itemView.findViewById(R.id.originalPriceTv);
            quantity = itemView.findViewById(R.id.quantityTv);
        }
    }
}
