package com.mateusz.uno.ui.internetmultiplayer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.InternetGameData;
import com.mateusz.uno.data.InternetPlayerCards;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.mateusz.uno.ui.internetmultiplayer.InternetGameActivity.gameRef;
import static com.mateusz.uno.ui.internetmultiplayer.InternetGameActivity.usersDb;

public class InternetGame {

    private InternetGameMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private UserData[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = false;
    private String gameId;
    private String[] userIds;
    private int index;
    public static boolean ready = false;

    public InternetGame(String gameId, int playerCount, InternetGameMvpView mView) {
        this.gameId = gameId;
        this.mView = mView;
        deck = new Deck();
        players = new UserData[playerCount];
        userIds = new String[playerCount];
        currentPlayer = 0;
        order = 1;

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    setup();
                }
            });
        }
        else{
            setup();
        }
    }

    //Presenter Methods
    public void setup() {

        final UserData userData = new SharedPrefsHelper((Context) mView).getUserData();

        gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final InternetGameData gameData = documentSnapshot.toObject(InternetGameData.class);

                gameRef.collection("players").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int e = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            userIds[e] = doc.getId();
                            Log.d("PLAYER: ", doc.getId());
                            e++;
                        }

                        if(!isArrayFull(userIds)){
                            Log.d("ARRAY", "PLAYERS ARRAY IS NOT FULL");
                            return;
                        }

                        Arrays.sort(userIds);

                        for (int i = 0; i < userIds.length; i++) {
                            if (userIds[i].equals(userData.getId())) {
                                index = i;
                                break;
                            }

                        }
                        adjustUserOrder();

                        for (int i = 0; i < userIds.length; i++) {
                            final int finalI = i;
                            usersDb.document(userIds[i]).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    UserData data = documentSnapshot.toObject(UserData.class);

                                    mView.setupPlayerData(finalI + 1, data);
                                    players[finalI] = data;

                                    if (isArrayFull(players)) {
                                        playerDrawCard(0, 7);
                                        mView.hideLoadingGameDialog();
                                        updateVariables(gameData);
                                        ready = true;

                                        //if(players[getNewOrder(0)].getId().equals(userData.getId())) changeCurrentCard(deck.drawFirstCard());
                                    }
                                }
                            });

                            gameRef.collection("players").document(userIds[i]).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                    if(documentSnapshot.toObject(InternetPlayerCards.class) == null) return;

                                    List<Integer> playerCards = documentSnapshot.toObject(InternetPlayerCards.class).getCards();
                                    mView.updateCardViews(finalI, playerCards);

                                    for (int j = 0; j < playerCards.size(); j++) {
                                        if (deck.contains(deck.fetchCard(j)))
                                            deck.removeCard(deck.fetchCard(j));
                                    }
                                }
                            });

                            gameRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    InternetGameData newGameData = documentSnapshot.toObject(InternetGameData.class);

                                    if (!ready || hasWon) return;

                                    updateVariables(newGameData);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private <T> boolean isArrayFull(T[] array) {
        for(T element : array){
            if(element == null) return false;
        }

        return true;
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
        if(players[currentPlayer] == null) return;
        mView.changeTurnText(players[currentPlayer].getName());
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
        int nextPlayer = getOldOrder(getNextPlayer());
        playerDrawCard(nextPlayer, 4);
    }

    private void playerDrawCard(int player) {
        playerDrawCard(player, 1);
    }

    private void playerDrawCard(final int player, final int num) {
        for (int i = 0; i < num; i++) {
            int id = deck.getRandomCard().getId();
            mView.addCardView(player, id);

            gameRef.collection("players").document(userIds[player]).update("cards", FieldValue.arrayUnion(id));
        }
    }

    private void playerRemoveCard(final int player, final Card c) {
        mView.removeCardView(player, c.getId());

        gameRef.collection("players").document(userIds[player]).update("cards", FieldValue.arrayRemove(c.getId()));
    }
}
