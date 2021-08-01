package com.example.groceryapp.Activity.Seller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.groceryapp.Activity.LoginActivity;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditSellerAcitivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private ImageView profile, imgBack, imgLocation;
    private EditText edName,edShop,edDeliveryFee, edPhone, edCountry, edState, edCity, edAddress;
    private Button update;
    private SwitchCompat shopOpenSwitch;

    private String fullName,shopName,phoneNumber,deliveryFee,country,city,state,address;
    private boolean shopOpen;

    private Uri imageUri;
    private LocationManager locationManager;
    private double latitude,longitude;

    //dialog
    private ProgressDialog progressDialog;

    //firebase
    private FirebaseAuth firebaseAuth;

   //permission
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;


    //request_code
    private static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_seller);

        firebaseAuth = FirebaseAuth.getInstance();

        //progress dialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        init();
        initPermission();
        checkUser();

        imgBack.setOnClickListener(this);
        imgLocation.setOnClickListener(this);
        profile.setOnClickListener(this);
        update.setOnClickListener(this);
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user== null){
            startActivity(new Intent(EditSellerAcitivity.this, LoginActivity.class));
            finish();
        }else {
            LoadMyInfo();
        }

    }

    private void LoadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            String address = ""+ds.child("address").getValue();
                            String city = ""+ds.child("city").getValue();
                            String state = ""+ds.child("state").getValue();
                            String country = ""+ds.child("country").getValue();
                            String deliveryFee = ""+ds.child("deliveryFee").getValue();
                            String email = ""+ds.child("email").getValue();
                            String name = ""+ds.child("name").getValue();
                            String latitude = ""+ds.child("latitude").getValue();
                            String longitude = ""+ds.child("longitude").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String online = ""+ds.child("online").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String shopOpen = ""+ds.child("shopOpen").getValue();
                            edName.setText(name);
                            edPhone.setText(phone);
                            edCountry.setText(country);
                            edState.setText(state);
                            edCity.setText(city);
                            edAddress.setText(address);
                            edShop.setText(shopName);
                            edDeliveryFee.setText(deliveryFee);

                            if (shopOpen.equals("true"))
                            {
                                shopOpenSwitch.setChecked(true);
                            }else {
                                shopOpenSwitch.setChecked(false);
                            }
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_launcher_background).into(profile);

                            }catch (Exception e){
                                profile.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {
        imgBack = findViewById(R.id.back);
        imgLocation =  findViewById(R.id.location);
        profile = findViewById(R.id.profile_image);
        edName = findViewById(R.id.fullName);
        edShop = findViewById(R.id.shopName) ;
        edDeliveryFee= findViewById(R.id.deliverFee) ;
        edPhone =  findViewById(R.id.phone);
        edCountry =  findViewById(R.id.country);
        edState =  findViewById(R.id.state);
        edCity =  findViewById(R.id.city);
        edAddress = findViewById(R.id.address);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);
        update = findViewById(R.id.updateProfile);
    }
    private void initPermission() {
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                startActivity(new Intent(EditSellerAcitivity.this, MainSellerActivity.class));
                finish();
                break;
            case R.id.location:
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
                break;
            case R.id.updateProfile:
                inputData();
                break;
            case R.id.profile_image:
                showImagePickDialog();
                break;
        }
    }

    private void inputData() {
        fullName = edName.getText().toString();
        shopName = edShop.getText().toString();
        phoneNumber = edPhone.getText().toString();
        deliveryFee = edDeliveryFee.getText().toString();
        country = edCountry.getText().toString();
        state = edState.getText().toString();
        city = edCity.getText().toString();
        address = edAddress.getText().toString();

        shopOpen = shopOpenSwitch.isChecked();
        updateProfile();
    }

    private void updateProfile() {
        progressDialog.setMessage("Saving Account Info....");
        progressDialog.show();

        if(imageUri==null) {
            HashMap<String, Object> hashMap = new HashMap<>();

            hashMap.put("name", "" + fullName);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("deliveryFee", "" + deliveryFee);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("shopOpen", "true");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditSellerAcitivity.this, "Profile updated..", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditSellerAcitivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


        }else {
            String filePAthName = "profile_image/"+""+firebaseAuth.getUid();
            StorageReference reference = FirebaseStorage.getInstance().getReference(filePAthName);
            reference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()){
                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("name", "" + fullName);
                                hashMap.put("shopName", "" + shopName);
                                hashMap.put("phone", "" + phoneNumber);
                                hashMap.put("deliveryFee", "" + deliveryFee);
                                hashMap.put("country", "" + country);
                                hashMap.put("state", "" + state);
                                hashMap.put("city", "" + city);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("shopOpen", "true");
                                hashMap.put("profileImage", "" +downloadImageUri );


                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditSellerAcitivity.this, "Profile updated..", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditSellerAcitivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                }
                            }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditSellerAcitivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {
        //option to show in dialog
        String [] options = {"Camera","Gallery"};
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0)
                        {
                            //camera clicked
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();
                            }else {
                                //camera permission not allowed
                                requestCameraPermission();
                            }
                        }else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                //storage permission allowed
                                pickFromGallery();
                            }else {
                                //storage permission not allowed
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }


    private void detectLocation() {

        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private boolean checkLocationPermission(){

        Boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        return result;

    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            edCountry.setText(country);
            edState.setText(state);
            edCity.setText(city);
            edAddress.setText(address);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //disable location
        Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0 ){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission granted
                        detectLocation();
                    }else {
                        //permission denied
                        Toast.makeText(this,"Location permission necessery..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0 ){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission granted
                        pickFromCamera();
                    }else {
                        //permission denied
                        Toast.makeText(this,"Camera permission necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0 ){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(this,"Storage permission necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();
                profile.setImageURI(imageUri);
            }else if (requestCode == IMAGE_PICK_GALLERY_CODE){
                profile.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}