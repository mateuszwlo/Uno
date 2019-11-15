package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mateusz.uno.R;
import com.mateusz.uno.data.GameListAdapter;
import com.mateusz.uno.data.InternetGame;
import com.mateusz.uno.data.InternetPlayer;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.start.StartActivity;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternetMultiplayerMenu extends AppCompatActivity implements View.OnClickListener{

    private Button createGameBtn;
    private RecyclerView availableGameRv;

    private CollectionReference gamesDb;
    private ArrayList<InternetGame> availableGames = new ArrayList<>(0);
    private GameListAdapter gameListAdapter = new GameListAdapter(availableGames);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_multiplayer_menu);

        initialiseViews();
        gamesDb = FirebaseFirestore.getInstance().collection("games");
    }

    private void initialiseViews(){
        createGameBtn = findViewById(R.id.createGameBtn);
        createGameBtn.setOnClickListener(this);

        availableGameRv = findViewById(R.id.availableGamesRv);
        availableGameRv.setAdapter(gameListAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        availableGameRv.setLayoutManager(llm);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createGameBtn:
                createGame("Test", 2);
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
                if(e != null){
                    Log.d("DATABASE", e.toString());
                    return;
                }

                for(QueryDocumentSnapshot s : queryDocumentSnapshots){
                    InternetGame game = s.toObject(InternetGame.class);
                    game.setId(s.getId());
                    int i = 0;

                    for(InternetGame games : availableGames){
                        if(games.getId().equals(game.getId())) i++;
                    }

                    if(i == 0) availableGames.add(game);
                }
                gameListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createGame(String name, int playerCount) {
        UserData data =  new SharedPrefsHelper(this).getUserData();

        InternetGame game = new InternetGame(name, 108, playerCount);
        game.addPlayer(new InternetPlayer(data.getPhotoId(), data.getName()));

        gamesDb.add(game)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("DATABASE", "Added with ID: "+ documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DATABASE", "Error adding document", e);
                    }
                });


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetMultiplayerMenu.this, StartActivity.class));
        finish();
    }
}
