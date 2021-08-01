package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingActivity extends AppCompatActivity {

    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;
    private ImageButton backBtn;


    private FirebaseAuth firebaseUser;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;
    private Boolean isChecked = false;


    public static final String enableMessage = "Notification are enable";
    public static final String disableMessage = "Notification are disable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        firebaseUser = FirebaseAuth.getInstance();

        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        backBtn = findViewById(R.id.back);

        //init sharedPreferences
        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);

        //get last selected option
        isChecked = sp.getBoolean("FCM_ENABLED",false);
        fcmSwitch.setChecked(isChecked);

        if (isChecked){
            notificationStatusTv.setText(enableMessage);
        }else {
            notificationStatusTv.setText(disableMessage);
        }




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //checked ,enable notification
                    subscribeToTopic();

                }else {
                    //unChecked , disable notification
                    unSubscribeToTopic();
                }
            }
        });


    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //subscribe successfully
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();
                        Toast.makeText(SettingActivity.this, ""+enableMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enableMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed subscribing
                        Toast.makeText(SettingActivity.this, ""+e.getMessage()
                                , Toast.LENGTH_SHORT).show();


                    }
                });

    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //unsubscribe successfully
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();
                        Toast.makeText(SettingActivity.this, ""+disableMessage
                                , Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disableMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed unSubscribing

                        Toast.makeText(SettingActivity.this, ""+e.getMessage()
                                , Toast.LENGTH_SHORT).show();

                    }
                });
    }
}