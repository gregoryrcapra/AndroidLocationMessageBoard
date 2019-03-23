package com.example.cs160_sp18.prog3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    Toolbar mActionBarToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mActionBarToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle("Greg's Landmark App");
    }

    public void dispatchEnterUser(View view){
        Intent usernameIntent = new Intent(LoginActivity.this, LandmarkActivity.class);
        EditText username = (EditText) findViewById(R.id.usernameField);
        usernameIntent.putExtra("username", username.getText().toString());
        startActivity(usernameIntent);
    }
}
