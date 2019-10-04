package com.mateusz.uno;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static Deck deck = new Deck();
    private Card currentCard;
    private boolean player1Turn = true;
    private AIPlayer player2 = new AIPlayer();

    private LinearLayout playerCards;
    private LinearLayout.LayoutParams cardLayoutParams;
    private ImageView deckIv;
    private ImageView pileIv;
    private ImageView playerTurnIv;
    private AlertDialog colourPickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseViews();
        setupGame();
    }

    private void initialiseViews() {
        cardLayoutParams = new LinearLayout.LayoutParams(
                (int) (110 * getResources().getDisplayMetrics().density),
                (int) (140 * getResources().getDisplayMetrics().density)
        );

        playerCards = findViewById(R.id.playerCards);
        deckIv = findViewById(R.id.deckIv);
        deckIv.setOnClickListener(this);

        pileIv = findViewById(R.id.pileIv);

        playerTurnIv = findViewById(R.id.playerTurnIv);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deckIv:
                if (player1Turn) {
                    addCard();
                    player1Turn = false;
                    player2Turn();
                }
                break;
            case R.id.redIv:
                changeCard(new Card(0, Card.Colour.RED, "0"));
                colourPickerDialog.dismiss();

                break;
            case R.id.blueIv:
                changeCard(new Card(75, Card.Colour.BLUE, "0"));
                colourPickerDialog.dismiss();
                break;
            case R.id.greenIv:
                changeCard(new Card(50, Card.Colour.GREEN, "0"));
                colourPickerDialog.dismiss();
                break;
            case R.id.yellowIv:
                changeCard(new Card(25, Card.Colour.YELLOW, "0"));
                colourPickerDialog.dismiss();
                break;
        }
    }

    private void setupGame() {
        //Picking first card
        Card c = deck.getRandomCard();

        while (c.getValue().length() > 1) {
            deck.insertCard(c);
            c = deck.getRandomCard();
        }
        changeCard(c);

        //Picking 7 cards for player
        for (int i = 0; i < 7; i++) {
            addCard();
        }
    }

    private void addCard() {

        final Card c = deck.getRandomCard();
        if (c.getId() == -2) Toast.makeText(this, "No More Cards. ", Toast.LENGTH_LONG).show();

        ImageView iv = new ImageView(this);
        iv.setImageResource(getResources().getIdentifier("c" + c.getId(), "drawable", getPackageName()));
        iv.setId(c.getId());

        playerCards.addView(iv, cardLayoutParams);

        //Card is pressed
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!player1Turn) return;
                player1Turn(c.getId());
            }
        });

    }

    private void changeCard(Card c) {
        currentCard = c;
        pileIv.setImageResource(getResources().getIdentifier("c" + currentCard.getId(), "drawable", getPackageName()));
    }

    private void player1Turn(int id) {

        Card c = deck.fetchCard(id);

        if (c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Card.Colour.BLACK)) {
            changeCard(c);
            playerCards.removeView(findViewById(id));

            checkForWin();
            action(c);
        }
    }

    private void action(Card c) {
        switch (c.getValue()) {
            case "plus2":
                if (player1Turn) {
                    player2.drawCard();
                    player2.drawCard();
                    return;
                } else {
                    addCard();
                    addCard();
                }
                break;
            case "skip":
            case "reverse":
                if (!player1Turn) player2Turn();
                return;
            case "wild":
                if (player1Turn) {
                    ColourPickerDialog();
                } else {
                    wildcard();
                    player2Turn();
                }
                return;
            case "wildcard":
                if (player1Turn) {
                    ColourPickerDialog();
                    player2.drawCard();
                    player2.drawCard();
                    player2.drawCard();
                    player2.drawCard();
                } else {
                    wildcard();
                    addCard();
                    addCard();
                    addCard();
                    addCard();
                    player2Turn();
                }
                return;
        }

        if (player1Turn) {
            player2Turn();
        } else {
            player1Turn = true;
            playerTurnIv.setImageResource(R.drawable.player1);
        }
    }

    private void checkForWin() {
        if (!player1Turn) {
            if (player2.hasUno()) {
                Toast.makeText(this, "Player 2 Wins!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (playerCards.getChildCount() == 0) {
                Toast.makeText(this, "Player 1 Wins!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void player2Turn() {

        player1Turn = false;
        playerTurnIv.setImageResource(R.drawable.player2);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Card c = player2.playCard(currentCard);

                //If player 2 has no fitting cards
                if (c.getId() == -1) {
                    player1Turn = true;
                    playerTurnIv.setImageResource(R.drawable.player1);
                    return;
                }

                changeCard(c);
                checkForWin();
                action(c);

            }
        }, 1000);
    }

    private void ColourPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.colour_picker_dialog, null);

        ImageView redIv = view.findViewById(R.id.redIv);
        redIv.setOnClickListener(this);

        ImageView blueIv = view.findViewById(R.id.blueIv);
        blueIv.setOnClickListener(this);

        ImageView greenIv = view.findViewById(R.id.greenIv);
        greenIv.setOnClickListener(this);

        ImageView yellowIv = view.findViewById(R.id.yellowIv);
        yellowIv.setOnClickListener(this);

        builder.setView(view);
        colourPickerDialog = builder.create();
        colourPickerDialog.show();
    }

    private void wildcard() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Random r = new Random();
                switch (r.nextInt(4)) {
                    case 0:
                        changeCard(new Card(0, Card.Colour.RED, "0"));
                        break;
                    case 1:
                        changeCard(new Card(25, Card.Colour.YELLOW, "0"));
                        break;
                    case 2:
                        changeCard(new Card(50, Card.Colour.GREEN, "0"));
                        break;
                    case 3:
                        changeCard(new Card(75, Card.Colour.BLUE, "0"));
                        break;
                }
            }
        }, 1000);

    }
}
