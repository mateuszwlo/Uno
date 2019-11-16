package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mateusz.uno.R;
import com.mateusz.uno.data.RecylerItemListener;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.start.SetAvatarNameActivity;
import com.mateusz.uno.ui.start.StartActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InternetMultiplayerMenu extends AppCompatActivity implements View.OnClickListener {

    private Button createGameBtn;
    private RecyclerView availableGameRv;

    private CollectionReference gamesDb;
    private CollectionReference usersDb;
    private ArrayList<InternetGameData> availableGames = new ArrayList<>(0);
    private GameListAdapter gameListAdapter = new GameListAdapter(availableGames);
    private UserData data;

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

        availableGameRv.addOnItemTouchListener(new RecylerItemListener(this, availableGameRv, new RecylerItemListener.RecyclerTouchListener() {
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

                for (QueryDocumentSnapshot s : queryDocumentSnapshots) {
                    InternetGameData game = s.toObject(InternetGameData.class);
                    game.setId(s.getId());
                    int i = 0;

                    for (InternetGameData games : availableGames) {
                        if (games.getId().equals(game.getId())) i++;
                    }

                    if (i == 0) availableGames.add(game);
                }
                gameListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getSharedPrefs() {
        data = new SharedPrefsHelper(this).getUserData();

        if (data.getId() != null) {
            usersDb.document(data.getId())
                    .update("name", data.getName(), "photoId", data.getPhotoId());
        } else {
            usersDb.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    data.setId(documentReference.getId());
                    new SharedPrefsHelper(InternetMultiplayerMenu.this).setUserData(data);

                }
            });
        }
    }

    private void openGame(String id) {

        DocumentReference playersRef = FirebaseFirestore.getInstance().document("games/" + id);

        playersRef.update("players", FieldValue.arrayUnion(data.getId()));

        Intent i = new Intent(InternetMultiplayerMenu.this, InternetGameLoadingActivity.class);
        i.putExtra("id", id);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InternetMultiplayerMenu.this, StartActivity.class));
        finish();
    }
}
