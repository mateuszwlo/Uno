package com.mateusz.uno.localmultiplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;
import com.mateusz.uno.singleplayer.User;

import static com.mateusz.uno.localmultiplayer.BluetoothConnectionService.isHosting;

public class LocalGame {

    private LocalGameMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private UserData[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = false;
    private BluetoothConnectionService bluetoothConnectionService;
    private UserData userData;

    public LocalGame(LocalGameMvpView mView) {
        this.mView = mView;
        deck = new Deck();
        currentPlayer = 0;
        order = 1;
        players = new UserData[2];

        setup();
    }

    //Presenter Methods
    public void setup() {
        bluetoothConnectionService = BluetoothConnectionService.getInstance((Context) mView);

        LocalBroadcastManager.getInstance((Context) mView).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        userData = new SharedPrefsHelper((Context) mView).getUserData();
        bluetoothConnectionService.write("name", userData.getName());
        bluetoothConnectionService.write("photoId", String.valueOf(userData.getPhotoId()));

        players[0] = userData;
        players[1] = new UserData();

        mView.setupPlayerData(0, userData);

        if(isHosting){
            changeCurrentCard(deck.drawFirstCard());
            bluetoothConnectionService.write("firstCard", String.valueOf(currentCard.getId()));
            mView.changeTurnText(userData.getName());
        }
        else{
            currentPlayer = 1;
        }

        drawFirstCards();

        mView.hideLoadingGameDialog();
    }

    private int getNextPlayer() {
        if(currentPlayer == 0) return 1;
        return 0;
    }

    private void action(Card c) {
        if (hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                playerDrawCard(1);
                playerDrawCard(1);
                changeTurn(2);
                break;
            case "skip":
            case "reverse":
                changeTurn(2);
                break;
            case "wild":
                mView.showColourPickerDialog();
                changeTurn();
                break;
            case "wildcard":
                mView.showWildCardColourPickerDialog();
                changeTurn(2);
                break;
            default:
                changeTurn();
                break;
        }
    }

    private void onReceiveAction(Card c){
        changeTurn();
        changeCurrentCard(c);
        deck.removeCard(c);
        mView.removeCardView(1, c.getId());

        switch (c.getValue()){
            case "skip":
            case "reverse":
            case "plus2":
            case "wildcard":
                changeTurn();
                break;
        }
    }

    private void checkForWin() {
        if(mView.getPlayerCardCount(0) == 0){
            bluetoothConnectionService.write("won", players[0].getName());
            hasWon = true;
            mView.showPlayerWinDialog(players[0].getName());
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
        if (!(c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Card.Colour.BLACK))) return;

        //Actions
        if (c.getId() == -2) {
            //All cards in deck played
            mView.gameDrawDialog();
        } else {
            bluetoothConnectionService.write("playedCard", String.valueOf(c.getId()));
            changeCurrentCard(c);
            playerRemoveCard(0, c);
            checkForWin();
            action(c);
        }
    }

    public void userDrawCard() {
        if (currentPlayer != 0) return;

        int id = deck.getRandomCard().getId();
        mView.addCardView(0, id);

        bluetoothConnectionService.write("drawCard", String.valueOf(id));

        changeTurn();
    }

    private void drawFirstCards(){
        for(int i = 0; i < 7; i ++){
            int id = deck.getRandomCard().getId();
            mView.addCardView(0, id);

            bluetoothConnectionService.write("drawCard", String.valueOf(id));
        }
    }

    public void wildCard() {
        playerDrawCard(4);
    }

    public void changeColour(Card c){
        changeCurrentCard(c);
        bluetoothConnectionService.write("colour", c.getColour().toString());
    }

    private void playerDrawCard(int num) {
        for (int i = 0; i < num; i++) {
            int id = deck.getRandomCard().getId();
            mView.addCardView(1, id);

            bluetoothConnectionService.write("draw", String.valueOf(id));
        }
    }

    private void playerRemoveCard(int player, final Card c) {
        mView.removeCardView(player, c.getId());
    }

    public void leaveGame(){
        bluetoothConnectionService.disconnect();
    }

    public final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
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
                players[1].setName(msg);
                if(!isHosting) mView.changeTurnText(msg);
                break;
            case "photoId":
                mView.setupPlayerPhoto(1, Integer.parseInt(msg));
                players[1].setPhotoId(Integer.parseInt(msg));
                break;
            case "playedCard":
                Card c = deck.fetchCard(Integer.parseInt(msg));
                onReceiveAction(c);
                break;
            case "draw":
                mView.addCardView(0, Integer.parseInt(msg));
                break;
            case "firstCard":
                changeCurrentCard(deck.fetchCard(Integer.parseInt(msg)));
                break;
            case "drawCard":
                Card card = deck.fetchCard(Integer.parseInt(msg));
                mView.addCardView(1, card.getId());
                changeTurn();
                break;
            case "won":
                hasWon = true;
                mView.showPlayerWinDialog(msg);
                break;
            case "colour":
                switch(msg){
                    case "RED":
                        changeCurrentCard(deck.fetchCard(109));
                        break;
                    case "YELLOW":
                        changeCurrentCard(deck.fetchCard(110));
                        break;
                    case "GREEN":
                        changeCurrentCard(deck.fetchCard(111));
                        break;
                    case "BLUE":
                        changeCurrentCard(deck.fetchCard(112));
                        break;
                }
                break;
        }
    }
}

