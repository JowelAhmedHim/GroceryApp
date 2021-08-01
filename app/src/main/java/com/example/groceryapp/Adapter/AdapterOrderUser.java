package com.example.groceryapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Activity.Buyer.OrderDetailsUserActivity;
import com.example.groceryapp.Model.ModelOrderUser;
import com.example.groceryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {

    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);

        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {

        ModelOrderUser modelOrderUser = orderUserList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderBy = modelOrderUser.getOrderBy();
        String orderTo = modelOrderUser.getOrderTo();
        String orderTime = modelOrderUser.getOrderTime();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderCost = modelOrderUser.getOrderCost();
        
        //getShopInfo
        loadShopInfo(modelOrderUser,holder);

        holder.amountTv.setText("Amount S:"+orderCost);
        holder.orderIdTv.setText("OrderId:"+orderId);
        holder.statusTv.setText(orderStatus);

        //change order status color;
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.design_default_color_primary));
        }else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.Green));
        }else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.Red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.dateTv.setText(formatDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open order details, we need to keys there, orderId,orderTo

                Intent intent = new Intent(context,OrderDetailsUserActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent);


            }
        });

    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HolderOrderUser extends RecyclerView.ViewHolder{

        private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            amountTv = itemView.findViewById(R.id.amountTv);
        }
    }
}
