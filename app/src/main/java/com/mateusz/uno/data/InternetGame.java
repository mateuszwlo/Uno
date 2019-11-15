package com.mateusz.uno.data;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class InternetGame {

    private String id;
    private String name;
    private int currentCard;
    private int playerCount;
    private ArrayList<InternetPlayer> players;

    public InternetGame(){
        //No Argument Constructor
    }

    public InternetGame(String name, int currentCard, int playerCount) {
        this.name = name;
        this.currentCard = currentCard;
        this.playerCount = playerCount;
        this.players = new ArrayList<>(0);
    }

    public void setCurrentCard(int currentCard) {
        this.currentCard = currentCard;
    }

    public void addPlayer(InternetPlayer player){
        if(players.size() != playerCount) players.add(player);
    }

    public int getCurrentCard() {
        return currentCard;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public ArrayList<InternetPlayer> getPlayers() {
        return players;
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
}
