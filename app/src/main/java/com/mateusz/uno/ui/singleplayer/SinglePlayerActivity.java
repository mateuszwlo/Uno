package com.mateusz.uno.ui.singleplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Card.Colour;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.start.StartActivity;

import java.util.Random;

import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;

public class SinglePlayerActivity extends AppCompatActivity implements SinglePlayerMvpView,View.OnClickListener {

    private LinearLayout userCards;
    private ImageView deckIv;
    private ImageView pileIv;
    private TextView playerTurnTv;
    private AlertDialog colourPickerDialog;
    public static SinglePlayerGame game;
    private int playerCount;
    private LinearLayout.LayoutParams cardParams;
    private HorizontalScrollView.LayoutParams scrollViewParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBoard();
        initialiseViews();

        game = new SinglePlayerGame(playerCount,this);
        game.play();
    }

    private void initialiseViews() {
        userCards = findViewById(R.id.userCards).findViewById(R.id.userCardsLayout);

        deckIv = findViewById(R.id.deckIv);
        deckIv.setOnClickListener(this);

        pileIv = findViewById(R.id.pileIv);
        playerTurnTv = findViewById(R.id.playerTurnTv);

        cardParams = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.MATCH_PARENT);

        scrollViewParams = new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.MATCH_PARENT
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deckIv:
                game.userDrawCard();
            case R.id.pileIv:
                break;
            default:
                game.turn(deck.fetchCard(view.getId()));
                break;
        }
    }

    public void setupBoard(){

        playerCount = getIntent().getIntExtra("playerCount", 2);

        //If 1v1, Choose layout portrait, else horizontal
        if(playerCount == 2){
            setContentView(R.layout.activity_singleplayer_vertical);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            setContentView(R.layout.activity_singleplayer_horizontal);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //Showing AI Cards
        switch (playerCount){
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

    //View Methods
    @Override
    public void player1AddCardView(Card c) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(getResources().getIdentifier("c" + c.getId(), "drawable", getPackageName()));
        iv.setId(c.getId());
        iv.setOnClickListener(this);

        userCards.addView(iv, getCardParams());
    }

    @Override
    public void gameDrawDialog() {
        showGameEndDialog("Game ended in a draw.");
    }

    @Override
    public void removeCardView(int id) {
        userCards.removeView(findViewById(id));
        getCardParams();
    }

    @Override
    public void changeCurrentCardView(int id) { pileIv.setImageResource(getResources().getIdentifier("c" + id, "drawable", getPackageName())); }

    @Override
    public void showPlayerWinDialog(int player) { showGameEndDialog("Player " + (player + 1) + " Wins!"); }

    @Override
    public int getPlayer1CardCount() {
        return userCards.getChildCount();
    }

    @Override
    public void changeTurnText(int to) {
        playerTurnTv.setText("Player " + (to + 1));
    }

    @Override
    public void changeColour(Colour col) {
        Card c;

        switch(col){
            case RED:
                c = new Card(109, Colour.RED, "SOLID");
                break;
            case YELLOW:
                c = new Card(110, Colour.YELLOW, "SOLID");
                break;
             case GREEN:
                 c = new Card(111, Colour.GREEN, "SOLID");
                break;
             case BLUE:
             default:
                 c = new Card(112, Colour.BLUE, "SOLID");
                break;
        }
        changeCurrentCardView(c.getId());
        game.changeCurrentCard(c);
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
                game.changeColour(Colour.RED);
                colourPickerDialog.dismiss();
            }
        });

        ImageView blueIv = view.findViewById(R.id.blueIv);
        blueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.changeColour(Colour.BLUE);
                colourPickerDialog.dismiss();
            }
        });

        ImageView greenIv = view.findViewById(R.id.greenIv);
        greenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.changeColour(Colour.GREEN);
                colourPickerDialog.dismiss();
            }
        });

        ImageView yellowIv = view.findViewById(R.id.yellowIv);
        yellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.changeColour(Colour.YELLOW);
                colourPickerDialog.dismiss();
            }
        });

        builder.setView(view);
        builder.setCancelable(false);
        colourPickerDialog = builder.create();
        colourPickerDialog.show();
    }

    @Override
    public void adjustPlayerCardViews(int id, int size) {

        ConstraintLayout cardsLayout = findViewById(getResources().getIdentifier("player" + (id + 1) + "Cards", "id", getPackageName()));

        switch (size){
            case 3:
            default:
                cardsLayout.findViewById(R.id.c3).setVisibility(View.VISIBLE);
                cardsLayout.findViewById(R.id.c2).setVisibility(View.VISIBLE);
                cardsLayout.findViewById(R.id.c1).setVisibility(View.VISIBLE);
                break;
            case 2:
                cardsLayout.findViewById(R.id.c3).setVisibility(View.VISIBLE);
                cardsLayout.findViewById(R.id.c2).setVisibility(View.VISIBLE);
                cardsLayout.findViewById(R.id.c1).setVisibility(View.INVISIBLE);
                break;
            case 1:
                cardsLayout.findViewById(R.id.c3).setVisibility(View.INVISIBLE);
                cardsLayout.findViewById(R.id.c2).setVisibility(View.INVISIBLE);
                cardsLayout.findViewById(R.id.c1).setVisibility(View.VISIBLE);
                break;
            case 0:
                cardsLayout.findViewById(R.id.c3).setVisibility(View.INVISIBLE);
                cardsLayout.findViewById(R.id.c2).setVisibility(View.INVISIBLE);
                cardsLayout.findViewById(R.id.c1).setVisibility(View.INVISIBLE);
                break;

            }
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
                game.wildCard(Colour.RED);
                colourPickerDialog.dismiss();
            }
        });

        ImageView blueIv = view.findViewById(R.id.blueIv);
        blueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard(Colour.BLUE);
                colourPickerDialog.dismiss();
            }
        });

        ImageView greenIv = view.findViewById(R.id.greenIv);
        greenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard(Colour.GREEN);
                colourPickerDialog.dismiss();
            }
        });

        ImageView yellowIv = view.findViewById(R.id.yellowIv);
        yellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.wildCard(Colour.YELLOW);
                colourPickerDialog.dismiss();
            }
        });

        builder.setView(view);
        builder.setCancelable(false);
        colourPickerDialog = builder.create();
        colourPickerDialog.show();
    }

    @Override
    public void setupPlayerData(int player, UserData data) {

        String tag = "player" + player + "Cards";

        if(player == 1){
            data = new SharedPrefsHelper(this).getUserData();
            tag = "userCards";
        }

        ImageView iv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.avatarIv);
        iv.setImageResource(data.getId());

        TextView tv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.nameTv);
        tv.setText(data.getName());
    }

    @Override
    public int getAvatarResource(int id) {
        return getResources().getIdentifier("avatar_" + id, "drawable", getPackageName());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SinglePlayerActivity.this, StartActivity.class));
    }

    private LinearLayout.LayoutParams getCardParams(){

        LinearLayout l = userCards.findViewById(R.id.userCardsLayout);

        //Decreasing or increasing left margins for all cards
        int leftMargin = (int) (-60 * getResources().getDisplayMetrics().density);

        cardParams.leftMargin = leftMargin;
        cardParams.weight = 0;

        //Setting margins for each card in the layout
        for(int i = 0; i < l.getChildCount(); i++){
            if(l.getChildAt(i).getId() != R.id.placeholderCard) l.getChildAt(i).setLayoutParams(cardParams);
        }

        //Adjusting width of placeholder for accommodate new left margin
        LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                leftMargin * -1,
                LinearLayout.LayoutParams.MATCH_PARENT);

        findViewById(R.id.placeholderCard).setLayoutParams(placeholderParams);

        if(l.getChildCount() < 7) scrollViewParams.gravity = Gravity.CENTER_HORIZONTAL;
        else scrollViewParams.gravity = Gravity.LEFT;

        l.setLayoutParams(scrollViewParams);

        return cardParams;
    }

    public void showGameEndDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(SinglePlayerActivity.this, SinglePlayerActivity.class));
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setTitle(msg);

        builder.create();
        builder.show();
    }
}
