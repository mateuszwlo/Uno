package com.mateusz.uno.localmultiplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.internetmultiplayer.InternetGameData;
import com.mateusz.uno.internetmultiplayer.InternetGameMvpView;
import com.mateusz.uno.internetmultiplayer.InternetPlayerCards;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

import java.util.Arrays;
import java.util.List;

import static com.mateusz.uno.internetmultiplayer.InternetGameActivity.gameRef;
import static com.mateusz.uno.internetmultiplayer.InternetGameActivity.usersDb;

public class LocalGame {

    private LocalGameMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private UserData[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = false;
    private BluetoothConnectionService bluetoothConnectionService;

    public LocalGame(LocalGameMvpView mView) {
        this.mView = mView;
        deck = new Deck();
        currentPlayer = 0;
        order = 1;

        setup();
    }

    //Presenter Methods
    public void setup() {
        bluetoothConnectionService = BluetoothConnectionService.getInstance((Context) mView);

        LocalBroadcastManager.getInstance((Context) mView).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        UserData userData = new SharedPrefsHelper((Context) mView).getUserData();
        bluetoothConnectionService.write("name", userData.getName());
        bluetoothConnectionService.write("photoId", String.valueOf(userData.getPhotoId()));

        playerDrawCard(0, 7);

        mView.hideLoadingGameDialog();
    }

    private int getNextPlayer() {
        int next = currentPlayer + order;

        if (next == -1) return players.length - 1;
        else if (next == players.length) return 0;
        else return next;
    }

    public void action(Card c) {
        if (hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                playerDrawCard(getNextPlayer());
                playerDrawCard(getNextPlayer());
                changeTurn(2);
                return;
            case "skip":
                changeTurn(2);
                return;
            case "reverse":
                if (players.length == 2) changeTurn();
                if (order == 1) order = -1;
                else order = 1;
                changeTurn();
                return;
            case "wild":
                mView.showColourPickerDialog();
                changeTurn();
                return;
            case "wildcard":
                mView.showWildCardColourPickerDialog();
                changeTurn(2);
                return;
            default:
                changeTurn();
                break;
        }

    }

    private void checkForWin() {
        for (int i = 0; i < players.length; i++) {
            if (mView.getPlayerCardCount(i) == 0) {
                mView.showPlayerWinDialog(players[i].getName());
                hasWon = true;
            }
        }
    }

    public void changeCurrentCard(Card c) {
        currentCard = c;
        mView.changeCurrentCardView(c.getId());
        deck.removeCard(c);
    }

    private void changeTurn() {
        changeTurn(1);
    }

    private void changeTurn(int turns) {

        for (int i = 0; i < turns; i++) {
            currentPlayer = getNextPlayer();
        }

        mView.changeTurnText(players[currentPlayer].getName());
    }

    public void userTurn(Card c) {
        if (currentPlayer != 0) return;
        if (!(c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Card.Colour.BLACK)))
            return;

        //Actions
        if (c.getId() == -2) {
            //All cards in deck played
            mView.gameDrawDialog();
        } else {
            changeCurrentCard(c);
            playerRemoveCard(0, c);
            checkForWin();
            action(c);
        }
    }

    public void userDrawCard() {
        if (currentPlayer != 0) return;
        playerDrawCard(0);
        changeTurn();
    }

    public void wildCard() {
        int nextPlayer = getNextPlayer();
        playerDrawCard(nextPlayer, 4);
    }

    private void playerDrawCard(int player) {
        playerDrawCard(player, 1);
    }

    private void playerDrawCard(final int player, final int num) {
        for (int i = 0; i < num; i++) {
            int id = deck.getRandomCard().getId();
            mView.addCardView(player, id);

            bluetoothConnectionService.write("draw", String.valueOf(id));
        }
    }

    private void playerRemoveCard(int player, final Card c) {
        mView.removeCardView(player, c.getId());

    }

    public void leaveGame(){
        bluetoothConnectionService.disconnect();
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            for (String key : bundle.keySet()) {
                String msg = bundle.getString(key);

                if(msg == null) return;
                onAction(key, bundle.getString(key));
            }
        }
    };

    private void onAction(String key, String msg){
        switch (key) {
            case "name":
                mView.setupPlayerName(1, msg);
                break;
            case "photoId":
                mView.setupPlayerPhoto(1, Integer.parseInt(msg));
                break;
            case "playedCard":
                changeCurrentCard(deck.fetchCard(Integer.parseInt(msg)));
                action(deck.fetchCard(Integer.parseInt(msg)));
                break;
            case "draw":
                mView.addCardView(1, Integer.parseInt(msg));
                break;
        }
    }
}

