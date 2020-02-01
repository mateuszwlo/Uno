package com.mateusz.uno.start;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateusz.uno.R;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.internetmultiplayer.InternetMultiplayerMenu;
import com.mateusz.uno.localmultiplayer.LocalMultiplayerMenu;
import com.mateusz.uno.singleplayer.SinglePlayerGameActivity;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    private Button singlePlayerBtn, localMultiplayerBtn, internetMultiplayerBtn;
    private ImageView avatarIv;
    private TextView userNameTv;
    private AlertDialog dialog;

    private int defaultAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initialiseViews();

        //Check SharedPrefs
        getSharedPrefs();
    }

    private void initialiseViews(){
        singlePlayerBtn = findViewById(R.id.singlePlayerBtn);
        singlePlayerBtn.setOnClickListener(this);

        localMultiplayerBtn = findViewById(R.id.localMultiplayerBtn);
        localMultiplayerBtn.setOnClickListener(this);

        internetMultiplayerBtn = findViewById(R.id.internetMultiplayerBtn);
        internetMultiplayerBtn.setOnClickListener(this);

        avatarIv = findViewById(R.id.avatarIv);
        avatarIv.setOnClickListener(this);

        userNameTv = findViewById(R.id.nameTv);
        userNameTv.setOnClickListener(this);

        defaultAvatar = getResources().getIdentifier("avatar_1", "drawable", getPackageName());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.singlePlayerBtn:
                showPlayerNumberDialog();
                break;
            case R.id.localMultiplayerBtn:
                startActivity(new Intent(StartActivity.this, LocalMultiplayerMenu.class));
                finish();
                break;
            case R.id.internetMultiplayerBtn:
                startActivity(new Intent(StartActivity.this, InternetMultiplayerMenu.class));
                finish();
                break;
            case R.id.avatarIv:
            case R.id.nameTv:
                startActivity(new Intent(StartActivity.this, SetAvatarNameActivity.class));
                finish();
                break;
            case R.id.onePlayerBtn:
                dialog.dismiss();
                startGame(2);
                break;
            case R.id.twoPlayerBtn:
                dialog.dismiss();
                startGame(3);
                break;
            case R.id.threePlayerBtn:
                dialog.dismiss();
                startGame(4);
                break;
        }
    }

    private void getSharedPrefs(){
        UserData data = new SharedPrefsHelper(this).getUserData();

        if(data.getName() == null){
            startActivity(new Intent(StartActivity.this, SetAvatarNameActivity.class));
            finish();
        }

        avatarIv.setImageResource(data.getPhotoId());
        userNameTv.setText(data.getName());
    }

    private void showPlayerNumberDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.player_number_dialog, null);

        Button onePlayerBtn = view.findViewById(R.id.onePlayerBtn);
        onePlayerBtn.setOnClickListener(this);

        Button twoPlayerBtn = view.findViewById(R.id.twoPlayerBtn);
        twoPlayerBtn.setOnClickListener(this);

        Button threePlayerBtn = view.findViewById(R.id.threePlayerBtn);
        threePlayerBtn.setOnClickListener(this);

        builder.setView(view);
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }

    private void startGame(int players){
        Intent i = new Intent(StartActivity.this, SinglePlayerGameActivity.class);
        i.putExtra("playerCount", players);
        startActivity(i);
        finish();
    }
}
