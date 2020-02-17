package com.mateusz.uno.singleplayer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Card.Colour;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.SharedPrefsHelper;
import com.mateusz.uno.data.UserData;

import java.util.Random;

public class SinglePlayerGame {

    private SinglePlayerMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private Player[] players;
    private int currentPlayer;
    private int order;
    private boolean hasWon = false;
    private String[] names = {"Matt", "Ben", "Tim", "Amy", "John", "Sara", "Maisie", "Sophie", "Jess", "Pam", "Alan", "Romeo", "Alexa", "Robert", "Tracy", "Bill"};
    public static int pickUpAmount = 0;
    private Colour pendingColour;

    public SinglePlayerGame(int playerCount, SinglePlayerMvpView mView) {
        this.mView = mView;
        deck = new Deck();
        players = new Player[playerCount];
        currentPlayer = 0;
        order = 1;

        setup();
    }

    //Presenter Methods
    public void setup() {

        //Creating players
        UserData d = new SharedPrefsHelper((Context) mView).getUserData();

        players[0] = new User(deck, d.getName(), mView);
        mView.setupPlayerData(1, d);

        for(int i = 1; i < players.length; i++){
            UserData data = getRandomData();
            players[i] = new AIPlayer(i, data.getName(), mView);

            //Setting player data
            mView.setupPlayerData(i + 1, data);
        }

        //Picking first card
         changeCurrentCard(deck.drawFirstCard());

         //Picking 7 cards for each player
          for(Player p : players) {
              for (int i = 0; i < 7; i++) {
                  p.drawCard();
              }
          }

     mView.changeTurnText(players[currentPlayer].getName());
    }

    public void play(){
        if(currentPlayer == 0) return;
        players[currentPlayer].turn(currentCard);
     }

     public void stack(Card c){
         players[currentPlayer].removeCard(c);
         changeCurrentCard(c);
         checkForWin();
         action(c);
     }

     public void pickUpStack(){
        Toast.makeText((Context) mView, players[currentPlayer].getName() + " picks up " + pickUpAmount + " cards.", Toast.LENGTH_SHORT).show();

         for(int i = 0; i < pickUpAmount; i++) players[currentPlayer].drawCard();

         pickUpAmount = 0;
         changeTurn();
         play();
     }

     public void pickUpStackWild(){
        mView.changeColour(pendingColour);
        pickUpStack();
     }

    private void action(Card c) {
        if(hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                pickUpAmount += 2;
                changeTurn();
                players[currentPlayer].willStack();
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
                pickUpAmount += 4;
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
        if(currentPlayer == 0) {
            if(players[0].turn(c)){
                changeCurrentCard(c);
                checkForWin();
                action(c);
            }
        }
    }

    public void userDrawCard(){
        if(currentPlayer != 0) return;
        players[0].drawCard();
        changeTurn();
        play();
    }

    public void wildCard(Colour c){
        pendingColour = c;
        changeTurn();
        players[currentPlayer].willStackWild();
    }

    public void changeColour(Colour c){
        mView.changeColour(c);
        changeTurn();
        play();
    }

    private UserData getRandomData(){
        int i = new Random().nextInt(names.length - 1) + 1;
        int avatar = mView.getAvatarResource(i + 1);

        return new UserData("", avatar, names[i]);

    }

    public Deck getDeck() {
        return deck;
    }
}
