package com.example.groceryapp.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Activity.Buyer.OrderDetailsUserActivity;
import com.example.groceryapp.Adapter.AdapterCartItem;
import com.example.groceryapp.Adapter.AdapterProductUser;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIv;
    private TextView shopNameTv,phoneTv,emailTv,openCloseTv,addressTv,deliveryFeeTv,cartCountTv;
    private ImageButton callBtn,mapBtn,cartItemBtn,backBTn,reviewBtn;
    private RecyclerView productRv;
    private RatingBar ratingBar;

    private String shopUid;
    private String myLatitude,myLongitude,myPhone;
    private String shopLatitude,shopLongitude;
    public String shopName,shopEmail,shopPhone,shopAddress,deliveryFee,shopOpen,profileImage;

    private ArrayList<ModelProduct> productsLst;
    private AdapterProductUser adapterProductUser;

    private ArrayList<ModelCartItem> cartItems;
    private AdapterCartItem adapterCartItem;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private  EasyDB easyDB;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //get uid of shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        auth = FirebaseAuth.getInstance();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        init();
        loadMyInfo();
        loadShopDetails();
        loadProducts();
        loadReviews();


         easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String []{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //each shop have its own products and orders
        //if user add items to cart ,go back, open card from different shop , then cart should be different
        //so delete cart data whenever user open this activity

        deleteCartData();
        cartCount();
        
        

       
        backBTn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();

            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();

            }
        });
        cartItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCartDialog();

            }
        });

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid",shopUid);
                startActivity(intent);

            }
        });


    }

    private float ratingSum= 0;

    private void loadReviews() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data in it

                        ratingSum = 0;

                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum +rating;

                        }



                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        cartItemBtn = findViewById(R.id.cartItem);
        backBTn = findViewById(R.id.backBtn);
        mapBtn = findViewById(R.id.mapBtn);
        callBtn = findViewById(R.id.callBtn);
        productRv = findViewById(R.id.productRecyclerView);
        cartCountTv = findViewById(R.id.cartCount);
        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);

    }


    private void deleteCartData() {

        easyDB.deleteAllDataFromTable();
    }

    public void cartCount(){
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){

            //no item in cart, hide cart count textView
            cartCountTv.setVisibility(View.GONE);

        }else {
            //have item in cart , show cart count textView and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);

        }
    }

    public double allTotalPrice = 0.00;

    public TextView sTotalTv,dFeeTv,allTotalPriceTv,promoDescriptionTv,discountTv;
    public EditText promoCodeEt;
    public Button applyBtn;


    private void showCartDialog(){
        cartItems = new ArrayList<>();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);

        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);

        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        applyBtn = view.findViewById(R.id.applyBtn);


        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);

        sTotalTv =  view.findViewById(R.id.sTotalTv);
        dFeeTv =  view.findViewById(R.id.dTotalTv);
        allTotalPriceTv =  view.findViewById(R.id.totalTv);

        Button checkoutBtn = view.findViewById(R.id.confirmBtn);

        if (isPromoCodeApplied){
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);

        }else {
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Apply");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String []{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()){
            String id = cursor.getString(1);
            String pId = cursor.getString(2);
            String name = cursor.getString(3);
            String price = cursor.getString(4);
            String cost = cursor.getString(5);
            String quantity = cursor.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItems.add(modelCartItem);

        }

        //setupAdapter
        adapterCartItem = new AdapterCartItem(this,cartItems);
        //setup recyclerView
        cartItemRv.setAdapter(adapterCartItem);

        if(isPromoCodeApplied){
            priceWithDiscount();
        }else {
            priceWithoutDiscount();
        }

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLatitude.equals("" )|| myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null"))
                {
                    // user did not enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address before placing order", Toast.LENGTH_SHORT).show();
                    return; // don't processed further
                }
                if (myPhone.equals("")||myPhone.equals("null")){
                    //user did not enter phone number
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartItems.size()==0){
                    //cart list empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                submitOrder();
            }
        });

        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(promoCode)){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promoCode", Toast.LENGTH_SHORT).show();
                }else {
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });
    }

    private void priceWithDiscount() {
        discountTv.setText("$ "+promoPrice);
        dFeeTv.setText("$ "+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("$","")) - Double.parseDouble(promoPrice)));

    }

    private void priceWithoutDiscount(){
        discountTv.setText("$ 0");
        dFeeTv.setText("$ "+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$" +(allTotalPrice +Double.parseDouble(deliveryFee.replace("$",""))));

    }


    public boolean isPromoCodeApplied = false;
    public String promoId, promoTimestamp,promoCode,promoDescription,promoExpDate,promoMinimumPrice,promoPrice;
    
    private void checkCodeAvailability(String promotionCode){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking Promo Code");
        progressDialog.setCancelable(false);
        
        isPromoCodeApplied = false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        // check promo code availability
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Promotion").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            progressDialog.dismiss();
                            for (DataSnapshot ds: snapshot.getChildren()){
                                promoId = ""+ds.child("id").getValue();
                                promoTimestamp = ""+ds.child("timestamp").getValue();
                                promoCode = ""+ds.child("promoCode").getValue();
                                promoDescription = ""+ds.child("description").getValue();
                                promoExpDate = ""+ds.child("expireDate").getValue();
                                promoMinimumPrice = ""+ds.child("minimumOrderPrice").getValue();
                                promoPrice = ""+ds.child("promoPrice").getValue();
                                
                                //check expiryDate
                                checkCodeExpireDate();
                            }
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this,"Invalid Promo Code",Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void checkCodeExpireDate() {
        Calendar calendar  =  Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String todayDate = day + "/" +month+ "/"+year;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = simpleDateFormat.parse(todayDate);
            Date expireDate = simpleDateFormat.parse(promoExpDate);
            if (expireDate.compareTo(currentDate) > 0){
                checkMinimumOrderPrice();
            }else if (expireDate.compareTo(currentDate) < 0){
                Toast.makeText(this, "The promotion Code expired on"+promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }else if (expireDate.compareTo(currentDate) == 0){
                checkMinimumOrderPrice();
            }

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void checkMinimumOrderPrice() {

        if (Double.parseDouble(String.format("%.2f",allTotalPrice))< Double.parseDouble(promoMinimumPrice)){
            Toast.makeText(this, "This code is valid for order with minimum amount: $"+promoMinimumPrice, Toast.LENGTH_SHORT).show();
        }else {
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }


    private void submitOrder() {

        //show progress dialog
        progressDialog.setMessage("Placing order....");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$",""); // remove $ if contains


        //setup order data

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");// in progress/completed/cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+auth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",myLatitude);
        hashMap.put("longitude",myLongitude);
        hashMap.put("deliveryFee",""+deliveryFee);
        if (isPromoCodeApplied){
            hashMap.put("discount",""+promoPrice);
        }else {
            hashMap.put("discount","0");
        }


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        for (int i=0;i<cartItems.size();i++)
                        {
                            String pId = cartItems.get(i).getPid();
                            String id = cartItems.get(i).getId();
                            String name = cartItems.get(i).getName();
                            String cost = cartItems.get(i).getCost();
                            String price = cartItems.get(i).getPrice();
                            String quantity = cartItems.get(i).getQuantity();

                            HashMap<String,String> hashMap1 =  new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            reference.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order place successfully ", Toast.LENGTH_SHORT).show();
                        preparedNotificationMessage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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

                            //get user data
                            String name  = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String city  = ""+ds.child("city").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            myLatitude  = ""+ds.child("latitude").getValue();
                            myLongitude  = ""+ds.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadShopDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //getShop data
                String name = ""+snapshot.child("name").getValue();
                shopName  = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress =  ""+snapshot.child("address").getValue();

                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();


                deliveryFee =""+snapshot.child("deliveryFee").getValue();
                profileImage = ""+snapshot.child("profileImage").getValue();
                shopOpen =   ""+snapshot.child("shopOpen").getValue();

                //setShopData
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                phoneTv.setText(shopPhone);
                addressTv.setText(shopAddress);
                deliveryFeeTv.setText("DFee:"+deliveryFee);
                if (shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                    openCloseTv.setTextColor(Color.GREEN);
                }else {
                    openCloseTv.setText("Close");
                    openCloseTv.setTextColor(Color.RED);
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);

                }catch (Exception e){
                    shopIv.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadProducts() {

        productsLst = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsLst.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsLst.add(modelProduct);

                        }

                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productsLst);
                        productRv.setAdapter(adapterProductUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void dialPhone(){
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }
    private void openMap(){
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr="+shopLatitude +","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void preparedNotificationMessage(String orderId){

        //when user place order , send Notification to seller
        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribe by user
        String NOTIFICATION_TITLE = "New Order "+orderId;
        String NOTIFICATION_MESSAGE = "Congratulation..! You Have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json(What to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo =  new JSONObject();;
        try{

            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",auth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC); // to all who subscribe to this topic;
            notificationJo.put("data",notificationBodyJo);



        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        sendFcmNotification(notificationJo,orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {

        // send Volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //if failed sending fcm, still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsUserActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers;

                Map<String,String> headers = new HashMap<>();
                headers.put("Content-type", "application/json");
                headers.put("Authorization", "key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }
}
