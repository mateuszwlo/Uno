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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mateusz.uno.R;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.InternetPlayerCards;
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
    private boolean ready = false;

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
        createGameBtn = findViewById(R.id.hostGameBtn);
        createGameBtn.setOnClickListener(this);

        availableGameRv = findViewById(R.id.availableGamesRv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hostGameBtn:
                startActivity(new Intent(InternetMultiplayerMenu.this, CreateInternetGameActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get available games
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
                } else {
                    addToList(queryDocumentSnapshots);
                }
            }
        });
    }

    public void getSharedPrefs() {
        userData = new SharedPrefsHelper(this).getUserData();

        if (userData.getId() != null) {
            usersDb.document(userData.getId()).set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    ready = true;
                }
            });
        } else {
            usersDb.add(userData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    userData.setId(documentReference.getId());
                    new SharedPrefsHelper(InternetMultiplayerMenu.this).setUserData(userData);
                    ready = true;
                }
            });
        }
    }

    private void openGame(final String gameId) {
        if(!ready) return;

        //Check if game is full
        final DocumentReference gameRef = gamesDb.document(gameId);

        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final InternetGameData data = documentSnapshot.toObject(InternetGameData.class);

                gameRef.collection("players").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots == null) return;

                        if (data.getPlayerCount() == queryDocumentSnapshots.size()) {
                            Toast.makeText(InternetMultiplayerMenu.this, "Game is full.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        gameRef.collection("players").document(userData.getId()).set(new InternetPlayerCards()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent i = new Intent(InternetMultiplayerMenu.this, InternetGameLoadingActivity.class);
                                i.putExtra("id", gameId);
                                startActivity(i);
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    private void addToList(QuerySnapshot queryDocumentSnapshots) {
        for (QueryDocumentSnapshot s : queryDocumentSnapshots) {

            InternetGameData gameData = s.toObject(InternetGameData.class);
            gameData.setId(s.getId());

            availableGames.add(gameData);
            gameListAdapter.notifyDataSetChanged();
        }
        gameListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetMultiplayerMenu.this, StartActivity.class));
        finish();
    }
}
