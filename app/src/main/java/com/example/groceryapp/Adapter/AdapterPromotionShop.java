package com.example.groceryapp.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Activity.AddPromotionCodeActivity;
import com.example.groceryapp.Model.ModelPromotion;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop> {

    private Context context;
    private ArrayList<ModelPromotion> promotionArrayList;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionArrayList) {
        this.context = context;
        this.promotionArrayList = promotionArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_shop,parent,false);

        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPromotionShop holder, int position) {

        //get data
        ModelPromotion modelPromotion = promotionArrayList.get(position);
        String id = modelPromotion.getId();
        String timestamp = modelPromotion.getTimestamp();
        String description =modelPromotion.getDescription();
        String promoCode = modelPromotion.getPromoCode();
        String promoPrice = modelPromotion.getPromoPrice();
        String minimumOrderPrice = modelPromotion.getMinimumOrderPrice();
        String expireDate = modelPromotion.getExpireDate();

        //set data

        holder.descriptionTv.setText(description);
        holder.promoCodeTv.setText("Code: "+promoCode);
        holder.promoPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.expireDateTv.setText("Expire Date: "+expireDate);

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(modelPromotion,holder);
            }
        });

    }

    private void editDeleteDialog(ModelPromotion modelPromotion, HolderPromotionShop holder) {

        String options [] = {"Edit","Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            editPromoCode(modelPromotion);
                        }else if(which==1){
                            deletePromoCode(modelPromotion);
                        }
                    }
                })
                .show();
    }

    private void deletePromoCode(ModelPromotion modelPromotion) {

        progressDialog.setMessage("Deleting Promotion Code");
        progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotion").child(modelPromotion.getId())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void editPromoCode(ModelPromotion modelPromotion) {

        Intent intent = new Intent(context, AddPromotionCodeActivity.class);
        intent.putExtra("promoId",modelPromotion.getId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return promotionArrayList.size();
    }

    class HolderPromotionShop extends RecyclerView.ViewHolder{

        private TextView promoCodeTv,promoPriceTv,minimumOrderPriceTv,expireDateTv,descriptionTv;
        private ImageButton more;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);

            promoCodeTv = itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv = itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv = itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv = itemView.findViewById(R.id.expireDateTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            more = itemView.findViewById(R.id.moreBtn);
        }
    }
}
