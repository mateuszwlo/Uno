package com.mateusz.uno;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    Button singlePlayerBtn, localMultiplayerBtn, internetMultiplayerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        singlePlayerBtn = findViewById(R.id.singlePlayerBtn);
        singlePlayerBtn.setOnClickListener(this);

        localMultiplayerBtn = findViewById(R.id.localMultiplayerBtn);
        localMultiplayerBtn.setOnClickListener(this);

        internetMultiplayerBtn = findViewById(R.id.internetMultiplayerBtn);
        internetMultiplayerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.singlePlayerBtn:
                startActivity(new Intent(this, SinglePlayerActivity.class));
                finish();
                break;
            case R.id.localMultiplayerBtn:
                startActivity(new Intent(this, LocalMultiplayerMenu.class));
                finish();
                break;
            case R.id.internetMultiplayerBtn:
                Toast.makeText(this, "Internet Multiplayer in Progress!", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
