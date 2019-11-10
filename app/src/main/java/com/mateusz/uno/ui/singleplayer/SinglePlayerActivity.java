package com.mateusz.uno.ui.singleplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Colour;

import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;

public class SinglePlayerActivity extends AppCompatActivity implements MvpView,View.OnClickListener {

    private LinearLayout userCards;
    private LinearLayout.LayoutParams cardLayoutParams;
    private ImageView deckIv;
    private ImageView pileIv;
    private TextView playerTurnTv;
    private AlertDialog colourPickerDialog;
    public static SinglePlayerGame game;
    private int playerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleplayer_vertical);

        playerCount = getIntent().getIntExtra("playerCount", 2);
        game = new SinglePlayerGame(playerCount,this);

        initialiseViews();
        game.setup();
        game.play();
    }

    private void initialiseViews() {
        cardLayoutParams = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                (int) (130 * getResources().getDisplayMetrics().density)
        );
        cardLayoutParams.leftMargin = (int) (-50 * getResources().getDisplayMetrics().density);

        userCards = findViewById(R.id.userCards).findViewById(R.id.userCardsLayout);

        deckIv = findViewById(R.id.deckIv);
        deckIv.setOnClickListener(this);

        pileIv = findViewById(R.id.pileIv);
        playerTurnTv = findViewById(R.id.playerTurnTv);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deckIv:
                game.userDrawCard();
            default:
                game.userTurn(view.getId());
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

        userCards.addView(iv, cardLayoutParams);
    }

    @Override
    public void gameDrawDialog() {
        showGameEndDialog("Game ended in a draw.");
    }

    @Override
    public void removeCardView(int id) {
        userCards.removeView(findViewById(id));
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

        LinearLayout cardsLayout = findViewById(getResources().getIdentifier("player" + (id + 1) + "Cards", "id", getPackageName()));

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
                        finish();
                        System.exit(0);
                    }
                })
                .setTitle(msg);

        builder.create();
        builder.show();
    }
}
