package com.mateusz.uno.ui.internetmultiplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mateusz.uno.R;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.InternetPlayerCards;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

public class CreateInternetGameActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText gameNameEt;
    private Button createGameBtn;
    private RadioButton twoPlayerRBtn, threePlayerRBtn, fourPlayerRBtn;

    private CollectionReference gamesDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_internet_game);

        initialiseViews();
        gamesDb = FirebaseFirestore.getInstance().collection("games");
    }

    private void initialiseViews() {
        gameNameEt = findViewById(R.id.gameNameEt);

        createGameBtn = findViewById(R.id.hostGameBtn);
        createGameBtn.setOnClickListener(this);

        twoPlayerRBtn = findViewById(R.id.twoPlayerRBtn);
        threePlayerRBtn = findViewById(R.id.threePlayerRBtn);
        fourPlayerRBtn = findViewById(R.id.fourPlayerRBtn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hostGameBtn:

                if(gameNameEt.getText().toString().equals("")){
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                int playerCount = 2;
                if(threePlayerRBtn.isChecked()) playerCount = 3;
                if(fourPlayerRBtn.isChecked()) playerCount = 4;

                createGame(gameNameEt.getText().toString(), playerCount);
                break;
        }
    }

    private void createGame(final String name, final int playerCount) {

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    addGame(name, playerCount);
                }
            });
        }
        else{
            addGame(name, playerCount);
        }
    }

    private void addGame(String name, int playerCount) {
        final UserData userData =  new SharedPrefsHelper(this).getUserData();

        final InternetGameData game = new InternetGameData(name, 0, playerCount);


        gamesDb.add(game)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        final String gameId = documentReference.getId();

                        gamesDb.document(gameId)
                                .collection("players")
                                .document(userData.getId())
                                .set(new InternetPlayerCards())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        openGame(gameId);
                                    }
                                });


                    }
                });
    }

    private void openGame(String id) {
        Intent i = new Intent(CreateInternetGameActivity.this, InternetGameLoadingActivity.class);
        i.putExtra("id", id);
        startActivity(i);
        finish();
    }
}
