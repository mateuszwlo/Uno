package com.mateusz.uno.data;

import android.util.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InternetGameData {

    private String id;
    private String name;
    private int currentCard;
    private int playerCount;
    private Map<String, List<Integer>> players = new HashMap<>(0);
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

    public void addPlayer(String player){
        if(players == null) players = new HashMap<>(0);

        if(players.size() != playerCount) players.put(player, new ArrayList<Integer>());
    }

    public void removePlayer(String player){
        players.remove(player);
    }

    public int getCurrentCard() {
        return currentCard;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    @Exclude
    public ArrayList<String> getPlayerList() {
        ArrayList<String> list = new ArrayList<>(0);

        if(players == null) return list;

        list.addAll(players.keySet());
        return list;
    }

    public void setPlayerList(ArrayList<String> playerList){
        players = new HashMap<>(0);

        for(int i = 0; i < playerList.size(); i++){
            players.put(playerList.get(i), null);
        }
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

    public Map<String, List<Integer>> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, List<Integer>> players) {
        this.players = players;
    }
}
