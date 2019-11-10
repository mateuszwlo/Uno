package com.mateusz.uno.ui.singleplayer;

import com.mateusz.uno.data.AIPlayer;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Card.Colour;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.Player;
import com.mateusz.uno.data.User;
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
        players[0] = new User("Mateusz", mView);
        mView.setupPlayerData(1, null);

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
    }

    public void play(){
        if(currentPlayer == 0) return;
        players[currentPlayer].turn(currentCard);
     }

    private void action(Card c) {
        if(hasWon) return;

        switch (c.getValue()) {
            case "plus2":
                players[getNextPlayer()].drawCard();
                players[getNextPlayer()].drawCard();
                changeTurn();
               break;
            case "skip":
            case "reverse":
                if(order == 1) order = -1;
                else order = 1;
                if(players.length == 2) changeTurn();
                break;
            case "wild":
                players[currentPlayer].changeColour();
                return;
            case "wildcard":
                players[currentPlayer].wildCard();
                return;
        }
        changeTurn();
        play();
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
                mView.showPlayerWinDialog(currentPlayer);
                hasWon = true;
            }
        }
    }

    public void changeCurrentCard(Card c){
        currentCard = c;
        mView.changeCurrentCardView(c.getId());
    }

    private void changeTurn(){
        currentPlayer = getNextPlayer();
        mView.changeTurnText(currentPlayer);
    }

    public void turn(Card c){

        if(currentPlayer == 0){
            players[0].turn(c);
        }

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

    public void userDrawCard(){
        if(currentPlayer != 0) return;
        players[0].drawCard();
        changeTurn();
        play();
    }

    public void wildCard(Colour c){
        mView.changeColour(c);

        for(int i = 0; i < 4; i++){
            players[getNextPlayer()].drawCard();
        }

        changeTurn();
        changeTurn();
        play();
    }

    public void changeColour(Colour c){
        mView.changeColour(c);
        changeTurn();
        play();
    }

    private UserData getRandomData(){
        int i = new Random().nextInt(names.length - 1) + 1;
        int avatar = mView.getAvatarResource(i + 1);

        return new UserData(avatar, names[i]);

    }
}
