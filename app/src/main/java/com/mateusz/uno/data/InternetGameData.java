package com.mateusz.uno.data;

import com.google.firebase.firestore.Exclude;


public class InternetGameData {

    private String id;
    private String name;
    private int currentCard;
    private int playerCount;
    private int currentPlayer;

    public InternetGameData(){
        //No Argument Constructor
    }

    public InternetGameData(String name, int currentCard, int playerCount) {
        this.name = name;
        this.currentCard = currentCard;
        this.playerCount = playerCount;
        currentPlayer = 0;
    }

    public void setCurrentCard(int currentCard) {
        this.currentCard = currentCard;
    }

    public int getCurrentCard() {
        return currentCard;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
