package com.example.groceryapp.Activity.Buyer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.groceryapp.InputValidation;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserProfile extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private TextInputLayout lay_name,lay_phone;
    private ImageView profile, imgBack, imgLocation;
    private EditText edName, edPhone, edCountry, edState, edCity, edAddress;
    private Button saveBtn;

    private String fullName,phoneNumber,country,city,state,address,email,password;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private InputValidation inputValidation;

    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;

    private Uri imageUri;
    private LocationManager locationManager;
    private double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        inputValidation = new InputValidation(this);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        initPermission();
        init();

        imgBack.setOnClickListener(this);
        imgLocation.setOnClickListener(this);
        profile.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    private void initPermission() {
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void init() {
        lay_name = findViewById(R.id.layout_fullName);
        lay_phone = findViewById(R.id.layout_phone);

        profile = findViewById(R.id.profile_image);
        edName = findViewById(R.id.fullName);
        edPhone =findViewById(R.id.phone);
        edCountry =findViewById(R.id.country);
        edState = findViewById(R.id.state);
        edCity = findViewById(R.id.city);
        edAddress =findViewById(R.id.address);

        imgBack = findViewById(R.id.back);
        imgLocation =findViewById(R.id.location);
        saveBtn = findViewById(R.id.saveData);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.saveData:
                inputValidation();

            case R.id.location:
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
                break;
            case R.id.profile_image:
                showImagePickDialog();
                break;

        }
    }

    private void inputValidation() {
        fullName = edName.getText().toString();
        phoneNumber = edPhone.getText().toString();
        country = edCountry.getText().toString();
        state = edState.getText().toString();
        city = edCity.getText().toString();
        address = edAddress.getText().toString();

        if (!inputValidation.isEmpty(edName,lay_name,"Enter Your Name"))
        {
            return;
        }
        if (!inputValidation.isEmpty(edPhone,lay_phone,"Enter Phone Number"))
        {
            return;
        }else {
            createAccount();
        }

    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account....");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                       //send verification code
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(RegisterUserProfile.this, "Verification Email has been Sent.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterUserProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        //account created
                        saveProfileIntoDatabase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUserProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });
    }



    private void saveProfileIntoDatabase() {

        progressDialog.setMessage("Saving Profile Info....");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        if(imageUri==null)
        {

            //save info without image
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+ email);
            hashMap.put("name",""+fullName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("profileImage","");
            //save data in db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserProfile.this, MainUserActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserProfile.this, LoginActivity.class));
                            finish();

                        }
                    });
        }else {

            //Save info with image
            String filePathName = "profile_images/"+ "" +firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image;
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                //setup data to save
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+ timestamp);
                                hashMap.put("accountType","User");
                                hashMap.put("online","true");
                                hashMap.put("profileImage",""+downloadImageUri);

                                //save to database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserProfile.this, MainUserActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserProfile.this,LoginActivity.class));
                                                finish();
                                            }
                                        });
                                }
                            }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }

    //Location detection

    private boolean checkLocationPermission() {
        Boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }
    private void detectLocation() {
        Toast.makeText(this, "Please wait.....", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }


    }
    @Override
    public void onLocationChanged(Location location) {

        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(this, ""+latitude, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Please turn on Location", Toast.LENGTH_SHORT).show();

    }


    //image picker
    private void showImagePickDialog() {
        //option to show in dialog
        String [] options = {"Camera","Gallery"};
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                        {
                            //camera clicked
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();

                            }else {
                                //camera permission not allowed
                                requestCameraPermission();

                            }
                        }else if(which == 1) {
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
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
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
                        Toast.makeText(this,"Location permission necessary..",Toast.LENGTH_SHORT).show();
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
                //set to imageView
                profile.setImageURI(imageUri);
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){

                //set to imageView
                profile.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}