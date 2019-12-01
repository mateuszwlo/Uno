package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Multimap;
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
import com.mateusz.uno.ui.start.StartActivity;

import java.util.ArrayList;

public class InternetMultiplayerMenu extends AppCompatActivity implements View.OnClickListener {

    private Button createGameBtn;
    private RecyclerView availableGameRv;

    private CollectionReference gamesDb;
    private CollectionReference usersDb;
    private ArrayList<InternetGameData> availableGames = new ArrayList<>(0);
    private GameListAdapter gameListAdapter = new GameListAdapter(availableGames);
    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_multiplayer_menu);

        initialiseViews();
        gamesDb = FirebaseFirestore.getInstance().collection("games");
        usersDb = FirebaseFirestore.getInstance().collection("users");

        getSharedPrefs();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        availableGameRv.setAdapter(gameListAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        availableGameRv.setLayoutManager(llm);

        availableGameRv.addOnItemTouchListener(new RecyclerItemListener(this, availableGameRv, new RecyclerItemListener.RecyclerTouchListener() {
            @Override
            public void onClickItem(View v, int position) {
                openGame(availableGames.get(position).getId());
            }

            @Override
            public void onLongClickItem(View v, int position) {

            }
        }));
    }

    private void initialiseViews() {
        createGameBtn = findViewById(R.id.createGameBtn);
        createGameBtn.setOnClickListener(this);

        availableGameRv = findViewById(R.id.availableGamesRv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createGameBtn:
                startActivity(new Intent(InternetMultiplayerMenu.this, CreateInternetGameActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get available games
        gamesDb.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("DATABASE", e.toString());
                    return;
                }

                availableGames.clear();

                if (queryDocumentSnapshots.getMetadata().isFromCache()) {
                    gamesDb.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            addToList(queryDocumentSnapshots);
                        }
                    });
                }
                else{
                    addToList(queryDocumentSnapshots);
                }
            }
        });
    }

    public void getSharedPrefs() {
        userData = new SharedPrefsHelper(this).getUserData();

        if (userData.getId() != null) {
            usersDb.document(userData.getId()).set(userData);
        } else {
            usersDb.add(userData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    userData.setId(documentReference.getId());
                    new SharedPrefsHelper(InternetMultiplayerMenu.this).setUserData(userData);
                }
            });
        }
    }

    private void openGame(final String gameId) {

        //Check if game is full
        final DocumentReference gameRef = gamesDb.document(gameId);

        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                InternetGameData data = documentSnapshot.toObject(InternetGameData.class);

                if (data.getPlayerList() == null) return;

                if (data.getPlayerList().size() == data.getPlayerCount()) {
                    Toast.makeText(InternetMultiplayerMenu.this, "Game is full.", Toast.LENGTH_LONG).show();
                    return;
                }

                data.addPlayer(userData.getId());
                gameRef.set(data);

                Intent i = new Intent(InternetMultiplayerMenu.this, InternetGameLoadingActivity.class);
                i.putExtra("id", gameId);
                startActivity(i);
                finish();
            }
        });
    }

    private void addToList(QuerySnapshot queryDocumentSnapshots){
        for (QueryDocumentSnapshot s : queryDocumentSnapshots) {

            InternetGameData game = s.toObject(InternetGameData.class);
            game.setId(s.getId());
            int i = 0;

            for (InternetGameData games : availableGames) {
                if (games.getId().equals(game.getId())) {
                    i++;
                    if (games.getPlayerList().size() != game.getPlayerList().size())
                        games.setPlayerList(game.getPlayerList());
                }
            }

            if (i == 0) availableGames.add(game);
        }
        gameListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetMultiplayerMenu.this, StartActivity.class));
        finish();
    }
}
