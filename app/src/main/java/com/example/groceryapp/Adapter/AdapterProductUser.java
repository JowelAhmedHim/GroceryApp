package com.example.groceryapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Activity.ShopDetailsActivity;
import com.example.groceryapp.FilterProduct;
import com.example.groceryapp.FilterProductUser;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProductUser filter;



    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data
        ModelProduct modelProduct = productsList.get(position);
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
        holder.discountedNote.setText(discountNote);
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
            holder.productIcon.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);

        }

        holder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //add product to cart
                showQuantityDialog(modelProduct);

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });

    }

    private double cost =0;
    private double finalCost =0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {

        //inflate layout for dialog

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);

        ImageView productTv = view.findViewById(R.id.productTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountPriceTv = view.findViewById(R.id.discountPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView finalTv = view.findViewById(R.id.finalTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton decrement = view.findViewById(R.id.decrement);
        ImageButton increment = view.findViewById(R.id.increment);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //getData from model

        String productId = modelProduct.getProductID();
        String title = modelProduct.getProductTitle();
        String description = modelProduct.getProductDes();
        String productQuantity = modelProduct.getProductQuantity();
        String discountNote = modelProduct.getDiscountNote();
        String originalPRice = modelProduct.getProductPrice();
        String discountPrice = modelProduct.getDiscountPrice();
        String image = modelProduct.getProductIcon();

        String price;

        if (modelProduct.getDiscountAvailable().equals("true")){
            price = modelProduct.getDiscountPrice();
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else {

            discountNoteTv.setVisibility(View.GONE);
            discountPriceTv.setVisibility(View.GONE);
            price = modelProduct.getProductPrice();

        }

        cost = Double.parseDouble(price.replaceAll("$",""));
        finalCost = Double.parseDouble(price.replaceAll("$",""));
        quantity = 1;


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        try{

            Picasso.get().load(image).into(productTv);

        }catch (Exception e)
        {
            productTv.setImageResource(R.drawable.ic_baseline_shopping_cart_24);
        }
        titleTv.setText(title);
        descriptionTv.setText(""+description);
        pQuantityTv.setText(""+productQuantity);
        discountNoteTv.setText(""+discountNote);
        originalPriceTv.setText(""+originalPRice);
        originalPriceTv.setTextColor(Color.RED);
        discountPriceTv.setText(""+discountPrice);
        discountPriceTv.setTextColor(Color.GREEN);
        finalTv.setText(""+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;
                finalTv.setText(""+finalCost);
                quantityTv.setText(""+quantity);
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity>1){
                    finalCost = finalCost - cost;
                    quantity--;
                    finalTv.setText(""+finalCost);
                    quantityTv.setText(""+quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalTv.getText().toString().trim().replace("$","");
                String quantity = quantityTv.getText().toString().trim();
                
                addToCart(productId,title,priceEach,totalPrice,quantity);
                dialog.dismiss();
            }
        });


    }

    private int itemId = 1;

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {

        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String []{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart...", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
        {
            filter = new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

        private ImageView productIcon;
        private TextView discountedNote,title,discountPrice,originalPrice,addToCart;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            productIcon = itemView.findViewById(R.id.product_image);
            discountedNote = itemView.findViewById(R.id.discountNoteTv);
            title = itemView.findViewById(R.id.titleTv);
            discountPrice = itemView.findViewById(R.id.discountPriceTv);
            originalPrice = itemView.findViewById(R.id.originalPriceTv);
            addToCart = itemView.findViewById(R.id.addToCartTv);
        }
    }
}
