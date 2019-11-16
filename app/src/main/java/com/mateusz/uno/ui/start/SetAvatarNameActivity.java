package com.mateusz.uno.ui.start;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mateusz.uno.R;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.User;
import com.mateusz.uno.data.UserData;

import java.util.UUID;

public class SetAvatarNameActivity extends AppCompatActivity implements View.OnClickListener {

    private int avatarId = 0;
    private Button continueBtn;
    private EditText nameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_avatar_name);

        initialiseViews();

        UserData data = new SharedPrefsHelper(this).getUserData();

        if(data != null) nameEt.setText(data.getName());
    }

    private void initialiseViews(){
        continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(this);

        nameEt = findViewById(R.id.nameEt);

        for(int i = 0; i < 16; i++){
            ImageView iv =  findViewById(getResources().getIdentifier("avatar_" + (i + 1), "id", getPackageName()));
            iv.setOnClickListener(this);
            iv.setTag(i + 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.continueBtn:
                if(avatarId == 0 || nameEt.getText().toString().equals("")){
                    Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get existing data and override
                UserData data = new SharedPrefsHelper(this).getUserData();
                data.setName(nameEt.getText().toString());
                data.setPhotoId(avatarId);

                new SharedPrefsHelper(this).setUserData(data);
                startActivity(new Intent(SetAvatarNameActivity.this, StartActivity.class));
                finish();
                break;
            default:
                avatarId = getResources().getIdentifier("avatar_" + v.getTag(), "drawable", getPackageName());
                setImageTint(v.getId());
        }
    }

    private void setImageTint(int id) {
        for(int i = 0; i < 16; i++){

            ImageView v = findViewById(getResources().getIdentifier("avatar_" + (i + 1), "id", getPackageName()));
            ImageViewCompat.setImageTintList(v, ColorStateList.valueOf(getResources().getColor(R.color.tint)));
        }

        ImageViewCompat.setImageTintList((ImageView) findViewById(id), ColorStateList.valueOf(0));
    }

    @Override
    public void onBackPressed() {
    }
}
