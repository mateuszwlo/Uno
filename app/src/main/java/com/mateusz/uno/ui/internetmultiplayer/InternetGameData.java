package com.mateusz.uno.ui.internetmultiplayer;

import com.google.firebase.firestore.Exclude;
import com.mateusz.uno.data.UserData;

import java.util.ArrayList;

public class InternetGameData {

    private String id;
    private String name;
    private int currentCard;
    private int playerCount;
    private ArrayList<String> players;

    public InternetGameData(){
        //No Argument Constructor
    }

    public InternetGameData(String name, int currentCard, int playerCount) {
        this.name = name;
        this.currentCard = currentCard;
        this.playerCount = playerCount;
        this.players = new ArrayList<>(0);
    }

    public void setCurrentCard(int currentCard) {
        this.currentCard = currentCard;
    }

    public void addPlayer(String player){
        if(players.size() != playerCount) players.add(player);
    }

    public int getCurrentCard() {
        return currentCard;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public ArrayList<String> getPlayers() {
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
