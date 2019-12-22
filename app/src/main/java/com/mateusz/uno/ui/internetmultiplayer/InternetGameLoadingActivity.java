package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mateusz.uno.R;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

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
    private InternetGameData gameData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_game_loading);

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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    setUpListener();
                }
            });
        } else {
            setUpListener();
        }
    }

    private void setUpListener() {
        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gameData = documentSnapshot.toObject(InternetGameData.class);

                gameNameTv.setText(gameData.getName());

                gameRef.collection("players").addSnapshotListener(InternetGameLoadingActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                        if (gameData.getPlayerCount() == queryDocumentSnapshots.size())
                            startGame(gameData.getPlayerCount());

                        //Update Player List
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            usersDb.document(doc.getId())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    UserData data = documentSnapshot.toObject(UserData.class);
                                    if (!connectedPlayers.contains(data.getName())) {
                                        connectedPlayers.add(data.getName());
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
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
        gameRef.collection("players").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots == null || queryDocumentSnapshots.size() == 0) {
                    gameRef.delete();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (!hasJoinedGame) {
            gameRef.collection("players").document(userData.getId()).delete();
            checkIfPlayersLeft();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetGameLoadingActivity.this, InternetMultiplayerMenu.class));
        finish();
    }

    private void startGame(int playerCount) {
        hasJoinedGame = true;

        Intent i = new Intent(InternetGameLoadingActivity.this, InternetGameActivity.class);
        i.putExtra("gameId", gameId);
        i.putExtra("playerCount", playerCount);
        startActivity(i);
        finish();
    }
}
