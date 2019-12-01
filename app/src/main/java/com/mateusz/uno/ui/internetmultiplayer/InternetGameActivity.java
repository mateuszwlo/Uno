package com.mateusz.uno.ui.internetmultiplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.j2objc.annotations.ObjectiveCName;
import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.singleplayer.PlayerCardView.AIPlayerCardView;

import java.util.ArrayList;
import java.util.List;

import static com.mateusz.uno.ui.internetmultiplayer.InternetGame.deck;
import static com.mateusz.uno.ui.internetmultiplayer.InternetGame.ready;

public class InternetGameActivity extends AppCompatActivity implements View.OnClickListener, InternetGameMvpView {

    private InternetGame game;
    private int playerCount;
    private String gameId;
    private UserData userData;
    private boolean hasLeft = false;

    public static DocumentReference gameRef;
    public static CollectionReference usersDb;

    private LinearLayout userCards;
    private ImageView deckIv;
    private ImageView pileIv;
    private TextView playerTurnTv;
    private AlertDialog colourPickerDialog;
    private HorizontalScrollView.LayoutParams scrollViewParams;
    private ProgressDialog loadingGameDialog;
    private LinearLayout.LayoutParams cardParams;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameId = getIntent().getStringExtra("gameId");
        playerCount = getIntent().getIntExtra("playerCount", 2);
        userData = new SharedPrefsHelper(this).getUserData();

        setupBoard();
        initialiseViews();

        gameRef = FirebaseFirestore.getInstance().collection("games").document(gameId);
        usersDb = FirebaseFirestore.getInstance().collection("users");

        game = new InternetGame(gameId, playerCount, this);
        game.setup();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!ready){
            loadingGameDialog = new ProgressDialog(this);
            loadingGameDialog.setMessage("Loading Game");
            loadingGameDialog.show();
        }

        registration = FirebaseFirestore.getInstance().collection("games").document(gameId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                InternetGameData data = documentSnapshot.toObject(InternetGameData.class);

                if(data.getPlayerList().size() < data.getPlayerCount()) leaveGame();
            }
        });
    }

    public void setupBoard() {
        //If 1v1, Choose layout portrait, else horizontal
        if (playerCount == 2) {
            setContentView(R.layout.activity_singleplayer_vertical);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setContentView(R.layout.activity_singleplayer_horizontal);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //Showing AI Cards
        switch (playerCount) {
            case 4:
                findViewById(R.id.player4Cards).setVisibility(View.VISIBLE);
            case 3:
                findViewById(R.id.player3Cards).setVisibility(View.VISIBLE);
            case 2:
            default:
                findViewById(R.id.player2Cards).setVisibility(View.VISIBLE);
                break;
        }
    }

    public void initialiseViews() {
        userCards = findViewById(R.id.userCards).findViewById(R.id.userCardsLayout);

        deckIv = findViewById(R.id.deckIv);
        deckIv.setOnClickListener(this);

        pileIv = findViewById(R.id.pileIv);
        playerTurnTv = findViewById(R.id.playerTurnTv);

        scrollViewParams = new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.MATCH_PARENT
        );

        cardParams = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.MATCH_PARENT);

        cardParams.leftMargin = (int) (-60 * getResources().getDisplayMetrics().density);
        cardParams.weight = 0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deckIv:
                game.userDrawCard();
            case R.id.pileIv:
                break;
            default:
                game.userTurn(deck.fetchCard(view.getId()));
                break;
        }
    }

    //User Cards
    @Override
    public void addCardView(int player, Card c) {

        if (player == 0) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(getResources().getIdentifier("c" + c.getId(), "drawable", getPackageName()));
            iv.setId(c.getId());
            iv.setOnClickListener(this);

            userCards.addView(iv, cardParams);
        } else {
            AIPlayerCardView cardView = findViewById(getResources()
                    .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                    .findViewById(R.id.playerCardsLayout);

            cardView.addCard(c);
        }
    }

    @Override
    public void removeCardView(int player, Card c) {
        if (player == 0) {
            userCards.removeView(findViewById(c.getId()));
        } else {
            AIPlayerCardView cardView = findViewById(getResources()
                    .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                    .findViewById(R.id.playerCardsLayout);
            cardView.removeCard(c);
        }
    }

    @Override
    public void updateCardViews(int player, List<Integer> cards) {
        //User Cards
        if (player == 0) return;

        //Other players' cards
        AIPlayerCardView cardView = findViewById(getResources()
            .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
            .findViewById(R.id.playerCardsLayout);

        for(int card : cards){
            if(cardView.findViewById(card) == null) cardView.addCard(deck.fetchCard(card));
        }

        for(int i = 0; i < cardView.getChildCount(); i++){
            if(!cards.contains(cardView.getChildAt(i).getId())){
                cardView.removeView(cardView.getChildAt(i));
            }
        }
    }

    @Override
    public int getPlayerCardCount(int player) {
        if (player == 0) {
            return userCards.getChildCount() - 1;
        }

        AIPlayerCardView cardView = findViewById(getResources()
                .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                .findViewById(R.id.playerCardsLayout);

        return cardView.getChildCount() - 1;
    }

    //Game
    @Override
    public void changeCurrentCardView(int id) {
        pileIv.setImageResource(getResources().getIdentifier("c" + id, "drawable", getPackageName()));
    }

    @Override
    public void changeTurnText(String player) {
        playerTurnTv.setText(player + "'s Turn");
    }

    @Override
    public void setupPlayerData(int player, UserData data) {

        String tag = "player" + player + "Cards";

        if (player == 1) {
            tag = "userCards";
        }

        ImageView iv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.avatarIv);
        iv.setImageResource(data.getPhotoId());

        TextView tv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.nameTv);
        tv.setText(data.getName());
    }

    @Override
    public int getAvatarResource(int id) {
        return getResources().getIdentifier("avatar_" + id, "drawable", getPackageName());
    }

    //Dialogs
    @Override
    public void showPlayerWinDialog(String player) {
        showGameEndDialog(player + " Wins!");
    }

    @Override
    public void gameDrawDialog() {
        showGameEndDialog("Game ended in a draw.");
    }

    @Override
    public void showColourPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.colour_picker_dialog, null);

        ImageView redIv = view.findViewById(R.id.redIv);
        redIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(109));
            }
        });

        ImageView yellowIv = view.findViewById(R.id.yellowIv);
        yellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(110));
            }
        });

        ImageView greenIv = view.findViewById(R.id.greenIv);
        greenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(111));
            }
        });

        ImageView blueIv = view.findViewById(R.id.blueIv);
        blueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(112));
            }
        });

        builder.setView(view);
        builder.setCancelable(false);
        colourPickerDialog = builder.create();
        colourPickerDialog.show();
    }

    @Override
    public void showWildCardColourPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.colour_picker_dialog, null);

        ImageView redIv = view.findViewById(R.id.redIv);
        redIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard();
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(109));
            }
        });

        ImageView yellowIv = view.findViewById(R.id.yellowIv);
        yellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard();
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(110));
            }
        });

        ImageView greenIv = view.findViewById(R.id.greenIv);
        greenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard();
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(111));
            }
        });

        ImageView blueIv = view.findViewById(R.id.blueIv);
        blueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard();
                colourPickerDialog.dismiss();
                game.changeCurrentCard(deck.fetchCard(112));
            }
        });

        builder.setView(view);
        builder.setCancelable(false);
        colourPickerDialog = builder.create();
        colourPickerDialog.show();
    }

    @Override
    public void hideLoadingGameDialog() {
        loadingGameDialog.dismiss();
    }

    public void showGameEndDialog(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(msg)
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveGame();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    public void onBackPressed() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hasLeft = true;
                        leaveGame();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Resume game
                        dialog.dismiss();
                    }
                })
                .create();

        dialog.show();

    }

    @Override
    protected void onDestroy() {
        if(!hasLeft){
            leaveGame();
            startActivity(new Intent(InternetGameActivity.this, InternetMultiplayerMenu.class));
            finish();
        }

        super.onDestroy();
    }

    private void leaveGame(){
        registration.remove();

        FirebaseFirestore.getInstance().collection("games").document(gameId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);

                if(gameData.getPlayers().size() == 1){
                    gameRef.delete();
                }
                else{
                    gameData.getPlayers().remove(userData.getId());
                    gameRef.update("players", gameData.getPlayers());
                }
            }
        });

        Intent i = new Intent(InternetGameActivity.this, InternetMultiplayerMenu.class);
        startActivity(i);
        finish();
    }
}
