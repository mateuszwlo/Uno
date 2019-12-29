package com.mateusz.uno.localmultiplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mateusz.uno.R;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.internetmultiplayer.InternetGame;
import com.mateusz.uno.singleplayer.PlayerCardView.*;

import static com.mateusz.uno.localmultiplayer.LocalGame.deck;

public class LocalGameActivity extends AppCompatActivity implements View.OnClickListener, LocalGameMvpView{

    private LocalGame game;
    private UserData userData;

    private LinearLayout userCards;
    private ImageView deckIv;
    private ImageView pileIv;
    private TextView playerTurnTv;
    private AlertDialog colourPickerDialog;
    private HorizontalScrollView.LayoutParams scrollViewParams;
    private ProgressDialog loadingGameDialog;
    private LinearLayout.LayoutParams cardParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleplayer_vertical);

        userData = new SharedPrefsHelper(this).getUserData();
        initialiseViews();

        game = new LocalGame(this);

    }

    private void initialiseViews(){
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deckIv:
                game.userDrawCard();
            case R.id.pileIv:
                break;
            default:
                game.userTurn(deck.fetchCard(v.getId()));
                break;
        }
    }

    @Override
    public void addCardView(int player, int id) {
        if (player == 0) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(getResources().getIdentifier("c" + id, "drawable", getPackageName()));
            iv.setId(id);
            iv.setOnClickListener(this);

            userCards.addView(iv, getUserCardParams());
        } else {
            AIPlayerCardView cardView = findViewById(getResources()
                    .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                    .findViewById(R.id.playerCardsLayout);

            cardView.addCard(id);
        }
    }

    @Override
    public void removeCardView(int player, int id) {
        if (player == 0) {
            userCards.removeView(findViewById(id));
            getUserCardParams();
        } else {
            AIPlayerCardView cardView = findViewById(getResources()
                    .getIdentifier("player" + (player + 1) + "Cards", "id", getPackageName()))
                    .findViewById(R.id.playerCardsLayout);
            cardView.removeCard(id);
        }
    }

    public LinearLayout.LayoutParams getUserCardParams() {

        LinearLayout l = userCards.findViewById(R.id.userCardsLayout);

        if (l.getChildCount() < 7) scrollViewParams.gravity = Gravity.CENTER_HORIZONTAL;
        else scrollViewParams.gravity = Gravity.START;

        l.setLayoutParams(scrollViewParams);

        return cardParams;
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
        String tag = "player" + (player + 1) + "Cards";

        if (player == 0) {
            tag = "userCards";
        }

        ImageView iv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.avatarIv);
        iv.setImageResource(data.getPhotoId());

        TextView tv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.nameTv);
        tv.setText(data.getName());
    }

    @Override
    public void setupPlayerName(int player, String name) {
        String tag = "player" + (player + 1) + "Cards";

        if (player == 0) {
            tag = "userCards";
        }

        TextView tv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.nameTv);
        tv.setText(name);
    }

    @Override
    public void setupPlayerPhoto(int player, int photoId) {
        String tag = "player" + (player + 1) + "Cards";

        if (player == 0) {
            tag = "userCards";
        }

        ImageView iv = findViewById(getResources().getIdentifier(tag, "id", getPackageName())).findViewById(R.id.avatarIv);
        iv.setImageResource(photoId);
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
        if(isFinishing() || loadingGameDialog == null) return;
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

    private void leaveGame(){
        game.leaveGame();
        startActivity(new Intent(LocalGameActivity.this, LocalMultiplayerMenu.class));
        finish();
    }

    @Override
    public void onBackPressed() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
        super.onDestroy();
    }

}
