package com.example.groceryapp.Activity.Seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Adapter.AdapterOrderedItem;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelOrderedItem;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private ImageButton backBtn, mapBtn, editBtn;
    private TextView orderIdTv, dateTv,orderStatusTv,emailTv,phoneTv,totalItemsTv,amountTv,addressTv;
    private RecyclerView itemsRv;

    private String orderId, orderBy;

    private String sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        firebaseAuth = FirebaseAuth.getInstance();

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        init();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderInfo();
        loadOrderItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();

            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderStatusDialog();
            }
        });

    }

    private void editOrderStatusDialog() {

        String[] options = {"In Progress","Completed","Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();
    }

    private void editOrderStatus(String selectedOption) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+selectedOption);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String message = " Order Is Now "+selectedOption;
                        Toast.makeText(OrderDetailsSellerActivity.this,""+message , Toast.LENGTH_SHORT).show();

                        preparedNotificationMessage(orderId,message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderItems() {

        orderedItemArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // before adding data clear lsit
                        orderedItemArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);

                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);

                        }

                        //setup Adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this,orderedItemArrayList);
                        //setup recyclerview
                        itemsRv.setAdapter(adapterOrderedItem);

                        totalItemsTv.setText(""+snapshot.getChildrenCount());





                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get order info
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormat = DateFormat.format("dd/MM/yyy",calendar).toString();
                        if (orderStatus.equals("In progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.design_default_color_primary_dark));

                        }else if(orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.Green));

                        }else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.Red));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$ "+orderCost + "[Including Delivery Fee $ "+deliveryFee + "]");
                        dateTv.setText(dateFormat);

                        //findAddress(latitude,longitude);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {

        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        Geocoder geocoder;

        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat,lon,1);
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    private void openMap(){

        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + sourceLatitude + "," +sourceLongitude + "&daddr=" + destinationLatitude +","+ destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        sourceLatitude = ""+snapshot.child("Latitude").getValue();
                        sourceLongitude = ""+snapshot.child("Longitude").getValue();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadBuyerInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get buyer information
                        destinationLatitude = ""+snapshot.child("Latitude").getValue();
                        destinationLongitude = ""+snapshot.child("Longitude").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String phone = ""+snapshot.child("phone").getValue();
                        //set buyer info
                        emailTv.setText(email);
                        phoneTv.setText(phone);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {

        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemTv);
        amountTv = findViewById(R.id.amountTV);
        addressTv = findViewById(R.id.addressTv);
        backBtn = findViewById(R.id.back);
        mapBtn = findViewById(R.id.mapBtn);
        editBtn = findViewById(R.id.editBn);
        itemsRv = findViewById(R.id.itemsRv);

    }

    private void preparedNotificationMessage(String orderId, String message){

        //when seller changes  order status , send Notification to buyer

        // prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribe by user
        String NOTIFICATION_TITLE = "Your Order"+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        //prepare json(What to send and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo =  new JSONObject();;
        try{

            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy);
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid());
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC); // to all who subscribe to this topic;
            notificationJo.put("data",notificationBodyJo);



        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        sendFcmNotification(notificationJo);




    }

    private void sendFcmNotification(JSONObject notificationJo) {

        // send Volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Notification sent




            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed



            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers;

                Map<String,String> headers = new HashMap<>();
                headers.put("Content-type", "application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request

        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }
}