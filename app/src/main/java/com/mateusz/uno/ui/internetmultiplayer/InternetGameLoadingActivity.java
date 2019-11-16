package com.mateusz.uno.ui.internetmultiplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mateusz.uno.R;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class InternetGameLoadingActivity extends AppCompatActivity{

    private ListView connectedPlayersListView;
    private TextView gameNameTv;

    private DocumentReference game;
    private String gameId;
    private ArrayList<String> connectedPlayers = new ArrayList<>(0);
    private boolean gameReady = false;
    private ArrayAdapter<String> adapter;
    private UserData userData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_game_loading);

        initialiseViews();
        getGameId();

        game = FirebaseFirestore.getInstance().document("games/" + gameId);
        getData();

        userData = new SharedPrefsHelper(this).getUserData();

//        while(!gameReady){
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    game.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            InternetGameData data = documentSnapshot.toObject(InternetGameData.class);
//
//                            if(data.getPlayers().size() == data.getPlayerCount()){
//                                gameReady = true;
//                            }
//                        }
//                    });
//                }
//            }, 1000);
//        }

        Toast.makeText(this, "Game is Ready!", Toast.LENGTH_LONG).show();

    }

    private void initialiseViews(){
        connectedPlayersListView = findViewById(R.id.connectedPlayersListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connectedPlayers);
        connectedPlayersListView.setAdapter(adapter);

        gameNameTv = findViewById(R.id.gameNameTv);
    }

    private void getGameId() {
        gameId = getIntent().getStringExtra("id");

        if(gameId == null) {
            startActivity(new Intent(InternetGameLoadingActivity.this, InternetMultiplayerMenu.class));
            finish();
        }
    }

    private void getData(){
        game.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                InternetGameData data = documentSnapshot.toObject(InternetGameData.class);

                gameNameTv.setText(data.getName());

                for(String id : data.getPlayers()){
                    addToListView(id);
                    adapter.notifyDataSetChanged();

                }
            }
        });
    }

    private void addToListView(String id){
        FirebaseFirestore.getInstance().document("users/" + id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserData data  = documentSnapshot.toObject(UserData.class);
                        connectedPlayers.add(data.getName());
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        game.update("players", FieldValue.arrayRemove(userData.getId()));
        super.onDestroy();
    }
}
