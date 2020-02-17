package com.mateusz.uno.internetmultiplayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static com.mateusz.uno.internetmultiplayer.InternetGameActivity.gameRef;
import static com.mateusz.uno.internetmultiplayer.InternetGameActivity.usersDb;

public class InternetGame {

    private String gameId;
    private InternetGameMvpView mView;

    public static Card currentCard;
    public static Deck deck;
    private UserData[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = true;
    private String[] userIds;
    private int index;
    public static boolean ready = false;
    public static int pickUpAmount = 0;
    private int lastPlayedCard = 0;


    public InternetGame(String gameId, int playerCount, InternetGameMvpView mView) {
        this.gameId = gameId;
        this.mView = mView;
        deck = new Deck();
        players = new UserData[playerCount];
        userIds = new String[playerCount];
        currentPlayer = 0;
        order = 1;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    setup();
                }
            });
        } else {
            setup();
        }
    }

    //Presenter Methods
    public void setup() {
        //if(ready) return;
        Log.d("SETUP", "STARTING Setup()");

        //Setting user's avatar and name
        final UserData userData = new SharedPrefsHelper((Context) mView).getUserData();
        mView.setupPlayerData(1, userData);
        players[0] = userData;

        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);

                gameRef.collection("players").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //Getting ids's of all the players
                        int e = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            userIds[e] = doc.getId();
                            e++;
                        }
                        //Sort to make sure each user has same order (Firestore has random order)
                        Arrays.sort(userIds);

                        //Makes user the have index 0 to simplify other functions
                        for (int i = 0; i < userIds.length; i++) {
                            if (userIds[i].equals(userData.getId())) {
                                index = i;
                                adjustUserOrder();
                                break;
                            }
                        }

                        for (int i = 1; i < userIds.length; i++) {
                            final int finalI = i;
                            usersDb.document(userIds[i]).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    UserData data = documentSnapshot.toObject(UserData.class);
                                    mView.setupPlayerData(finalI + 1, data);
                                    players[finalI] = data;
                                }
                            });

                            gameRef.collection("players").document(userIds[finalI]).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    InternetPlayerCards cards = documentSnapshot.toObject(InternetPlayerCards.class);

                                    if(!ready) return;
                                    if (cards == null) return;

                                    List<Integer> playerCards = cards.getCards();
                                    if(playerCards.size() == 0){
                                        mView.showPlayerWinDialog(players[finalI].getName());
                                        hasWon = true;
                                    }

                                    mView.updateCardViews(finalI, playerCards);

                                    for (int c : playerCards) {
                                        if (deck.contains(deck.fetchCard(c)))
                                            deck.removeCard(deck.fetchCard(c));
                                    }
                                }
                            });

                            if (mView.isLoaded()) {
                                playerDrawCard(0, 7);
                                mView.hideLoadingGameDialog();
                                updateVariables(gameData);
                                hasWon = false;

                                if (getOldOrder(0) == 0) changeCurrentCard(deck.drawFirstCard());
                                break;
                            }
                        }
                    }
                });
            }
        });

        gameRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                InternetGameData newGameData = documentSnapshot.toObject(InternetGameData.class);

                if (hasWon) return;
                if (newGameData == null) return;
                ready = true;

                updateVariables(newGameData);
            }
        });
    }

    private int getNewOrder(int i) {
        int position = i - index;
        if (position < 0) position += userIds.length;

        return position;
    }

    private int getOldOrder(int i) {
        int position = i + index;
        if (position > userIds.length - 1) position -= userIds.length;

        return position;
    }

    private void adjustUserOrder() {
        String temp;
        int position;

        for (int i = 0; i < Math.ceil(userIds.length / 2.0); i++) {
            position = getNewOrder(i);

            temp = userIds[position];
            userIds[position] = userIds[i];
            userIds[i] = temp;
        }

        currentPlayer = getNewOrder(0);
    }

    private void updateVariables(InternetGameData gameData) {
        //Change Card
        mView.changeCurrentCardView(gameData.getCurrentCard());
        currentCard = deck.fetchCard(gameData.getCurrentCard());
        deck.removeCard(currentCard);

        //Change player
        currentPlayer = getNewOrder(gameData.getCurrentPlayer());
        if (players[currentPlayer] == null) return;
        mView.changeTurnText(players[currentPlayer].getName());

        pickUpAmount = gameData.getPickUpAmount();

        if(pickUpAmount == 0) return;
        if(currentPlayer != 0) return;
        if(lastPlayedCard == currentCard.getId()) return;

        if (currentCard.getValue().equals("plus2")) {
            mView.canUserStack();
        } else if (currentCard.getValue().equals("wildcard")) {
            mView.canUserStackWild();
        }
    }

    private int getNextPlayer() {
        int next = currentPlayer + order;

        if (next == -1) return players.length - 1;
        else if (next == players.length) return 0;
        else return next;
    }

    private void action(Card c) {
        if (hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                updateStack(2);
                changeTurn();
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
                updateStack(4);
                mView.showWildCardColourPickerDialog();
                changeTurn();
                return;
            default:
                changeTurn();
                break;
        }

    }

    private void updateStack(int num){
        if(num == 0){
            gameRef.update("pickUpAmount", 0);
            pickUpAmount = 0;
        }
        else{
            gameRef.update("pickUpAmount", FieldValue.increment(num));
            pickUpAmount += num;
        }
    }

    public void stack(Card c) {
        lastPlayedCard = c.getId();
        playerRemoveCard(0, c);
        changeCurrentCard(c);
        checkForWin();
        action(c);
    }

    public void pickUpStack() {
        Toast.makeText((Context) mView, players[0].getName() + " picks up " + pickUpAmount + " cards.", Toast.LENGTH_SHORT).show();
        playerDrawCard(0, pickUpAmount);
        updateStack(0);
        changeTurn();
    }

    private void checkForWin(){
        if(mView.getPlayerCardCount(0) == 0){
            mView.showPlayerWinDialog(players[0].getName());
            hasWon = true;
        }
    }

    public void changeCurrentCard(Card c) {
        currentCard = c;
        mView.changeCurrentCardView(c.getId());
        gameRef.update("currentCard", c.getId());
    }

    private void changeTurn() {
        changeTurn(1);
    }

    private void changeTurn(int turns) {

        for (int i = 0; i < turns; i++) {
            currentPlayer = getNextPlayer();
        }

        mView.changeTurnText(players[currentPlayer].getName());
        gameRef.update("currentPlayer", getOldOrder(currentPlayer));
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
            lastPlayedCard = c.getId();
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

    private void playerDrawCard(int player) {
        playerDrawCard(player, 1);
    }

    private void playerDrawCard(final int player, final int num) {
        if (num == 0) return;

        int id = deck.getRandomCard().getId();
        mView.addCardView(player, id);

        gameRef.collection("players")
                .document(userIds[player])
                .update("cards", FieldValue.arrayUnion(id))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        playerDrawCard(player, num - 1);
                    }
                });
    }

    private void playerRemoveCard(final int player, final Card c) {
        mView.removeCardView(player, c.getId());

        gameRef.collection("players").document(userIds[player]).update("cards", FieldValue.arrayRemove(c.getId()));
    }
}