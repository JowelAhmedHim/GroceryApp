package com.example.groceryapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.R;
import com.example.groceryapp.Activity.ShopDetailsActivity;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_item,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {

        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String pId= modelCartItem.getPid();
        String name = modelCartItem.getName();
        String price  = modelCartItem.getPrice();
        String cost = modelCartItem.getCost();
        String quantity = modelCartItem.getQuantity();


        holder.itemTitleTv.setText(""+name);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");
        holder.itemPriceEachTv.setText(""+price);
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //will create table if not exits, but in that case will must exits;
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String []{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart..", Toast.LENGTH_SHORT).show();
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double subTotalWithoutDiscount = ((ShopDetailsActivity)context).allTotalPrice;
                double subTotalAfterProductRemove = subTotalWithoutDiscount - Double.parseDouble(cost.replace("$",""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterProductRemove;
                ((ShopDetailsActivity)context).sTotalTv.setText("$"+ String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));

                double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliveryFee  = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("$",""));

                if (((ShopDetailsActivity)context).isPromoCodeApplied){

                    if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity)context).promoMinimumPrice)){
                        Toast.makeText(context, "This code is valid for order with minimum amount: $ "+((ShopDetailsActivity)context).promoMinimumPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity)context).discountTv.setText("$0");
                        ((ShopDetailsActivity)context).isPromoCodeApplied = false;

                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" +String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee))));
                    }else{
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);

                        ((ShopDetailsActivity)context).isPromoCodeApplied = true;
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" +String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee - promoPrice))));

                    }

                }else {
                    //not applied
                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" +String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee))));

                }

                //after removing item from cart, update cart count
                ((ShopDetailsActivity)context).cartCount();
            }
        });


    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv,itemPriceTv,itemQuantityTv,itemRemoveTv,itemPriceEachTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoved);
        }
    }
}
