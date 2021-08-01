package com.example.groceryapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.Model.ModelOrderedItem;
import com.example.groceryapp.R;

import java.util.ArrayList;

public class AdapterOrderedItem  extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context context;
    private ArrayList<ModelOrderedItem> orderItemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> modelOrderedItems) {
        this.context = context;
        this.orderItemArrayList = modelOrderedItems;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordered_item,parent,false);

        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //get data at position

        ModelOrderedItem modelOrderedItem = orderItemArrayList.get(position);

        String getPId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();


        //set data at position

        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("$"+price);
        holder.itemPriceTv.setText("$"+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");



    }

    @Override
    public int getItemCount() {
        return orderItemArrayList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder{

        private TextView itemPriceTv,itemTitleTv,itemPriceEachTv,itemQuantityTv;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);

        }
    }
}
