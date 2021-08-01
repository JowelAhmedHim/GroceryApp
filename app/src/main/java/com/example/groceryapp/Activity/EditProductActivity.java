package com.example.groceryapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Constants;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditProductActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView backBtn,productIcon;
    private Button addProductBtn;
    private EditText edTitle,edDescription,edPrice,edDiscountPrice,edDiscountNote,edQuantity;
    private TextView categoryTv;
    private SwitchCompat discountSwitch;

    private String productTitle,productDes,productPrice,productCategory,
            productQuantity,discountPrice,discountNote;
    private boolean discountAvailable;

    private FirebaseAuth firebaseAuth;

    private String productId;
    

    //permission constants
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;

    //image pic constants
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission Array
    private String [] cameraPermission;
    private String [] storagePermission;

    //image pick uri
    private Uri image_uri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productId = getIntent().getStringExtra("productId");


        //firebase instance
        firebaseAuth = FirebaseAuth.getInstance();


        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        init();
        loadProductDetails(productId);
        edDiscountPrice.setVisibility(View.GONE);
        edDiscountNote.setVisibility(View.GONE);
        backBtn.setOnClickListener(this);
        productIcon.setOnClickListener(this);
        categoryTv.setOnClickListener(this);
        discountSwitch.setOnClickListener(this);
        addProductBtn.setOnClickListener(this);

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    edDiscountPrice.setVisibility(View.VISIBLE);
                    edDiscountNote.setVisibility(View.VISIBLE);
                }else {
                    edDiscountPrice.setVisibility(View.GONE);
                    edDiscountNote.setVisibility(View.GONE);
                }
            }
        });


    }

    private void loadProductDetails(String productId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productID = ""+snapshot.child("productID").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDes = ""+snapshot.child("productDes").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productPrice = ""+snapshot.child("productPrice").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String Icon = ""+snapshot.child("productIcon").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();

                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);
                            edDiscountPrice.setVisibility(View.VISIBLE);
                            edDiscountNote.setVisibility(View.VISIBLE);
                        }else {
                            discountSwitch.setChecked(false);
                            edDiscountPrice.setVisibility(View.GONE);
                            edDiscountNote.setVisibility(View.GONE);

                        }

                        edTitle.setText(productTitle);
                        edDescription.setText(productDes);
                        categoryTv.setText(productCategory);
                        edQuantity.setText(productQuantity);
                        edPrice.setText(productPrice);
                        edDiscountPrice.setText(discountPrice);
                        edDiscountNote.setText(discountNote);
                        try {
                            Picasso.get().load(Icon).into(productIcon);
                        }catch (Exception e){
                            
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {
        backBtn = (ImageView) findViewById(R.id.back);
        productIcon = (ImageView) findViewById(R.id.product_image);
        addProductBtn = (Button) findViewById(R.id.addProduct);
        edTitle = (EditText) findViewById(R.id.productTitle);
        edDescription =(EditText) findViewById(R.id.productDescription);
        edQuantity = (EditText) findViewById(R.id.productQuantity);
        edPrice = (EditText) findViewById(R.id.productPrice);
        edDiscountPrice = (EditText) findViewById(R.id.discountPrice);
        edDiscountNote = (EditText) findViewById(R.id.discountNote);
        categoryTv = (TextView)findViewById(R.id.category);
        discountSwitch = findViewById(R.id.discountSwitch);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.addProduct:
                inputValidation();
                break;
            case R.id.product_image:
                showImagePickDialog();
                break;
            case R.id.category:
                categoryDialog();
                break;

        }

    }
    private void inputValidation() {
        productTitle = edTitle.getText().toString().trim();
        productDes = edDescription.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productPrice = edPrice.getText().toString().trim();
        productQuantity = edQuantity.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        if (TextUtils.isEmpty(productTitle))
        {
            edTitle.setError("Title is required..");
            return;
        }
        if (TextUtils.isEmpty(productDes))
        {
            edDescription.setError("Description  is required..");
            return;
        }
        if (TextUtils.isEmpty(productCategory))
        {
            categoryTv.setError("Category  is required..");
            return;
        }
        if (TextUtils.isEmpty(productPrice))
        {
            edPrice.setError("Price is required..");
            return;
        }
        if (discountAvailable){
            discountPrice = edDiscountPrice.getText().toString().trim();
            discountNote = edDiscountNote.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)){
                edDiscountPrice.setError("Discount price is required..");
                return;//don't proceed further
            }

        }else {

            //without discount
            discountPrice = "0";
            discountNote = "";

        }
        
        updateProduct();


    }

    private void updateProduct() {

        progressDialog.setMessage("updating product.....");
        progressDialog.show();

        if (image_uri == null)
        {
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", ""+ productTitle);
            hashMap.put("productDes", ""+ productDes);
            hashMap.put("productCategory", ""+ productCategory);
            hashMap.put("productPrice", ""+ productPrice);
            hashMap.put("productQuantity", ""+ productQuantity);
            hashMap.put("discountPrice", ""+ discountPrice);
            hashMap.put("discountNote", ""+ discountNote);
            hashMap.put("discountAvailable", ""+ discountAvailable);

            //update to db

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated....", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


        }else {
            String filePathName = "product_images/" + ""+productId;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //image uploaded
                            //get uploaded images

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()){
                                Uri downloadUri = uriTask.getResult();
                                if (uriTask.isSuccessful()){

                                    //uri of images received
                                    //setup data
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("productTitle", ""+ productTitle);
                                    hashMap.put("productDes", ""+ productDes);
                                    hashMap.put("productCategory", ""+ productCategory);
                                    hashMap.put("productPrice", ""+ productPrice);
                                    hashMap.put("productQuantity", ""+ productQuantity);
                                    hashMap.put("productIcon", ""+downloadUri);
                                    hashMap.put("discountPrice", ""+ discountPrice);
                                    hashMap.put("discountNote", ""+ discountNote);
                                    hashMap.put("discountAvailable", ""+ discountAvailable);



                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    progressDialog.dismiss();
                                                    Toast.makeText(EditProductActivity.this, "Updated....", Toast.LENGTH_SHORT).show();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                }
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    private void clearData() {

        edTitle.setText("");
        edDescription.setText("");
        categoryTv.setText("");
        edQuantity.setText("");
        edPrice.setText("");
        edDiscountPrice.setText("");
        edDiscountNote.setText("");
        productIcon.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        image_uri = null;
    }

    private void categoryDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = Constants.productOptions[which];
                        categoryTv.setText(category);

                    }
                })
                .show();

    }
    private void showImagePickDialog() {

        //option for dialog
        String [ ] option = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        if (which==0)
                        {
                            //camera click
                            if (checkCameraPermission()){
                                //permission granted
                                pickFromCamera();

                            }else {
                                //permission not granted
                                requestCameraPermission();
                            }

                        }else {
                            //gallery click
                            if(checkStoragePermission()){
                                //permission granted
                                pickFromGallery();
                            }else {
                                //permission not granted
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
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){
            case CAMERA_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & storage permission required..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if ( storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "storage permission required..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                productIcon.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                productIcon.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}