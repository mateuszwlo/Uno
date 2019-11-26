package com.mateusz.uno.ui.internetmultiplayer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mateusz.uno.data.AIPlayer;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.Player;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.User;
import com.mateusz.uno.data.UserData;

import java.util.ArrayList;
import java.util.Random;

import static com.mateusz.uno.ui.internetmultiplayer.InternetGameActivity.gameRef;
import static com.mateusz.uno.ui.internetmultiplayer.InternetGameActivity.usersDb;

public class InternetGame {

    private InternetGameMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private Player[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = false;
    private String gameId;
    private String[] userIds;
    private int index;

    public InternetGame(String gameId, int playerCount, InternetGameMvpView mView) {
        this.gameId = gameId;
        this.mView = mView;
        deck = new Deck();
        players = new Player[playerCount];
        userIds = new String[playerCount];
        currentPlayer = 0;
        order = 1;
    }

    //Presenter Methods
    public void setup() {
        Log.d("SETUP", "setup: SETUP");
        final UserData userData = new SharedPrefsHelper((Context) mView).getUserData();

        //Setting first card
        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);
                final ArrayList<String> playerList = gameData.getPlayers();

                for(int i = 0; i < playerList.size(); i++){
                    userIds[i] = playerList.get(i);

                    if(playerList.get(i).equals(userData.getId())) index = i;

                }

                adjustUserOrder();

                //Setting user Data
                for(int i = 0; i < userIds.length; i++){
                    final int finalI = i;
                    usersDb.document(userIds[i]).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserData data = documentSnapshot.toObject(UserData.class);
                            mView.setupPlayerData(finalI + 1, data);

                            for(int i = 0; i < userIds.length; i++){
                                players[i] = new InternetPlayer(i, data.getName(), playerList.get(i), mView);
                            }

                            //Each player draws 7 cards
                            for(int j = 0; j < 7; j++) players[finalI].drawCard();
                        }
                    });
                }
            }
        });


        //Changes card as database changes
        gameRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);

                mView.changeCurrentCardView(gameData.getCurrentCard());
                currentCard = deck.fetchCard(gameData.getCurrentCard());
                deck.removeCard(currentCard);
            }
        });

        //mView.changeTurnText(players[currentPlayer].getName());
        play();
    }

    private void adjustUserOrder() {
        String temp;
        int position;

        for(int i = 0; i < Math.ceil(userIds.length / 2.0); i++){
            position = i - index;
            if(position < 0) position += userIds.length;

            temp = userIds[position];
            userIds[position] = userIds[i];
            userIds[i] = temp;
        }

        currentPlayer = index;
    }

    public void play(){
        if(currentPlayer == 0) return;
    }

    private void action(Card c) {
        if(hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                players[getNextPlayer()].drawCard();
                players[getNextPlayer()].drawCard();
                changeTurn(2);
                play();
                return;
            case "skip":
                changeTurn(2);
                play();
                return;
            case "reverse":
                if(players.length == 2) changeTurn();
                if(order == 1) order = -1;
                else order = 1;
                changeTurn();
                play();
                return;
            case "wild":
                players[currentPlayer].changeColour();
                return;
            case "wildcard":
                players[currentPlayer].wildCard();
                return;
            default:
                changeTurn();
                play();
                break;
        }

    }

    private int getNextPlayer(){
        int next = currentPlayer + order;

        if(next == -1) return players.length - 1;
        else if(next  == players.length) return 0;
        else return next;
    }

    private void checkForWin() {
        for(Player p : players){
            if(p.hasUno()) {
                mView.showPlayerWinDialog(players[currentPlayer].getName());
                hasWon = true;
            }
        }
    }

    public void changeCurrentCard(Card c){
        currentCard = c;
        mView.changeCurrentCardView(c.getId());
        gameRef.update("currentCard", c.getId());
    }

    private void changeTurn(){
        changeTurn(1);
    }

    private void changeTurn(int turns){

        for(int i = 0; i < turns; i++){
            currentPlayer = getNextPlayer();
        }

        mView.changeTurnText(players[currentPlayer].getName());
    }

    public void turn(Card c){
        if(c == null) {
            players[currentPlayer].drawCard();
            changeTurn();
            play();
        }
        else if(c.getId() == -2){
            //All cards in deck played
            mView.gameDrawDialog();
        }
        else{
            changeCurrentCard(c);
            checkForWin();
            action(c);
        }
    }

    public void userTurn(Card c){
        if(currentPlayer != 0) return;
        if(players[0].turn(c)) turn(c);
    }

    public void userDrawCard(){
        if(currentPlayer != 0) return;
        players[0].drawCard();
        changeTurn();
        play();
    }

    public void wildCard(Card.Colour c){
        mView.changeColour(c);

        int nextPlayer = getNextPlayer();
        for(int i = 0; i < 4; i++){
            players[nextPlayer].drawCard();
        }

        changeTurn(2);
        play();
    }

    public void changeColour(Card.Colour c){
        mView.changeColour(c);
        changeTurn();
        play();
    }
}
