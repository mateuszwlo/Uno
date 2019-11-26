package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mateusz.uno.R;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.singleplayer.SinglePlayerGameActivity;

import java.util.ArrayList;

public class InternetGameLoadingActivity extends AppCompatActivity {

    private ListView connectedPlayersListView;
    private TextView gameNameTv;

    private DocumentReference gameRef;
    private CollectionReference usersDb;
    private String gameId;
    private ArrayList<String> connectedPlayers = new ArrayList<>(0);
    private ArrayAdapter<String> adapter;
    private UserData userData;
    private boolean hasJoinedGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_game_loading);
        Log.d("LOADING", "onCreate: LOADING SCREEN");
        initialiseViews();
        getGameId();

        gameRef = FirebaseFirestore.getInstance().collection("games").document(gameId);
        usersDb = FirebaseFirestore.getInstance().collection("users");

        userData = new SharedPrefsHelper(this).getUserData();
    }

    private void initialiseViews() {
        connectedPlayersListView = findViewById(R.id.connectedPlayersListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connectedPlayers);
        connectedPlayersListView.setAdapter(adapter);

        gameNameTv = findViewById(R.id.gameNameTv);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (gameId == null) getGameId();

        FirebaseFirestore.getInstance()
                .collection("games")
                .document(gameId)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        final InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);

                        gameNameTv.setText(gameData.getName());

                        if (gameData.getPlayerCount() == gameData.getPlayers().size()) {
                            Log.d("GAME", "startGame: STARTING GAME");
                            hasJoinedGame = true;

                            Intent i = new Intent(InternetGameLoadingActivity.this, InternetGameActivity.class);
                            i.putExtra("gameId", gameId);
                            i.putExtra("playerCount", gameData.getPlayerCount());
                            startActivity(i);
                            finish();
                            return;
                        } else {
                            //Update Player List
                            for (String id : gameData.getPlayers()) {
                                usersDb.document(id)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                UserData data = documentSnapshot.toObject(UserData.class);

                                                if(!connectedPlayers.contains(data.getName())) connectedPlayers.add(data.getName());
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void getGameId() {
        gameId = getIntent().getStringExtra("id");

        if (gameId == null) {
            startActivity(new Intent(InternetGameLoadingActivity.this, InternetMultiplayerMenu.class));
            finish();
        }
    }

    private void checkIfPlayersLeft() {
        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                InternetGameData data = documentSnapshot.toObject(InternetGameData.class);
                if (data.getPlayers() == null || data.getPlayers().size() == 0) {
                    gameRef.delete();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (!hasJoinedGame) {
            gameRef.update("players", FieldValue.arrayRemove(userData.getId()));
            checkIfPlayersLeft();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetGameLoadingActivity.this, InternetMultiplayerMenu.class));
        finish();
    }


}
