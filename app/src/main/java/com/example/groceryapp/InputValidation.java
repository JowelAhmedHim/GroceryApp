package com.example.groceryapp;

import android.content.Context;
import android.util.Patterns;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputValidation {

    private Context context;

    public InputValidation(Context context) {
        this.context = context;
    }

    public boolean isEmailValid(EditText editText , TextInputLayout textInputLayout){

        String value = editText.getText().toString();

        if (value.isEmpty()){
            textInputLayout.setError("Enter an email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            textInputLayout.setError("Try a valid email address");
            return false;
        }else {
            textInputLayout.setError(null);
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }
    public boolean isPasswordValid(EditText editText, TextInputLayout textInputLayout){
        String value = editText.getText().toString();
        if (value.isEmpty()){
            textInputLayout.setError("Enter a password");
            return false;
        }else if(editText.length()<6)
        {
            textInputLayout.setError("Enter 6 digit password");
            return false;
        }
        else {
            textInputLayout.setError(null);
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }
    public boolean isPasswordMatch(EditText editText1, EditText editText2, TextInputLayout textInputLayout){
        String value1 = editText1.getText().toString().trim();
        String value2 = editText2.getText().toString().trim();

        if (!value1.contentEquals(value2))
        {
            textInputLayout.setError("Password Did not match");
            return false;
        }else {
            textInputLayout.setError(null);
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }

    public boolean isEmpty(EditText editText,TextInputLayout textInputLayout,String message){
        String value = editText.getText().toString().trim();
        if (value.isEmpty()){
            textInputLayout.setError(message);
            return false;
        }else {
            textInputLayout.setError(null);
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }
}
