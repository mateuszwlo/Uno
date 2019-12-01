package com.mateusz.uno.ui.singleplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Card.Colour;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.ui.singleplayer.PlayerCardView.AIPlayerCardView;
import com.mateusz.uno.ui.start.StartActivity;

public class SinglePlayerGameActivity extends AppCompatActivity implements SinglePlayerMvpView, View.OnClickListener {

    private LinearLayout userCards;
    private ImageView deckIv;
    private ImageView pileIv;
    private TextView playerTurnTv;
    private AlertDialog colourPickerDialog;
    public static SinglePlayerGame game;
    private int playerCount;
    private HorizontalScrollView.LayoutParams scrollViewParams;
    LinearLayout.LayoutParams cardParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerCount = getIntent().getIntExtra("playerCount", 2);

        setupBoard();
        initialiseViews();

        game = new SinglePlayerGame(playerCount, this);
        game.play();

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
                game.userTurn(game.getDeck().fetchCard(view.getId()));
                break;
        }
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

    //View Methods

    //User Cards
    @Override
    public void addCardView(int player, Card c) {

        if (player == 0) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(getResources().getIdentifier("c" + c.getId(), "drawable", getPackageName()));
            iv.setId(c.getId());
            iv.setOnClickListener(this);

            userCards.addView(iv, getUserCardParams());
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
            getUserCardParams();
        } else {
            AIPlayerCardView cardView = findViewById(getResources()
                    .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                    .findViewById(R.id.playerCardsLayout);
            cardView.removeCard(c);
        }
    }

    @Override
    public int getPlayer1CardCount() {
        return userCards.getChildCount();
    }

    public LinearLayout.LayoutParams getUserCardParams() {

        LinearLayout l = userCards.findViewById(R.id.userCardsLayout);

        if (l.getChildCount() < 7) scrollViewParams.gravity = Gravity.CENTER_HORIZONTAL;
        else scrollViewParams.gravity = Gravity.START;

        l.setLayoutParams(scrollViewParams);

        return cardParams;
    }

    //Game
    @Override
    public void changeCurrentCardView(int id) {
        pileIv.setImageResource(getResources().getIdentifier("c" + id, "drawable", getPackageName()));
    }

    @Override
    public void changeColour(Colour col) {
        Card c;

        switch (col) {
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

    public void showGameEndDialog(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(msg)
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(SinglePlayerGameActivity.this, SinglePlayerGameActivity.class);
                        i.putExtra("playerCount", playerCount);
                        startActivity(i);
                        finish();
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

                        startActivity(new Intent(SinglePlayerGameActivity.this, StartActivity.class));
                        finish();
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
}
