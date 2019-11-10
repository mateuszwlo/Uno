package com.mateusz.uno.ui.singleplayer;

import com.mateusz.uno.data.AIPlayer;
import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.Player;
import com.mateusz.uno.data.User;
import com.mateusz.uno.data.Colour;

public class SinglePlayerGame {

    private SinglePlayerMvpView mView;
    public static Card currentCard;
    public static Deck deck;
    private Player[] players;
    private int currentPlayer;
    private int order;

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
        for(int i = 1; i < players.length; i++){
            players[i] = new AIPlayer(i, "AI" + i, mView);
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

    public void playTurn(Card c){
        switch (c.getId()){
            case -1:
                players[currentPlayer].drawCard();
                changeTurn();
                break;
            case -2:
                //All cards in deck played
                mView.gameDrawDialog();
                break;
            default:
                changeCurrentCard(c);
                checkForWin();
                action(c);
                break;
        }
    }

    private void action(Card c) {
        switch (c.getValue()) {
            case "plus2":
                changeTurn();
                players[currentPlayer].drawCard();
                players[currentPlayer].drawCard();
               break;
            case "skip":
            case "reverse":
                if(order == 1) order = -1;
                else order = 1;
                changeTurn();
                break;
            case "wild":
                players[currentPlayer].changeColour();
                break;
            case "wildcard":
               players[currentPlayer].wildCard();
                break;
        }
        changeTurn();
        play();
    }

    private void checkForWin() {
        for(Player p : players){
            if(p.hasUno()) mView.showPlayerWinDialog(currentPlayer);
        }
    }

    public void changeCurrentCard(Card c){
        currentCard = c;
        mView.changeCurrentCardView(c.getId());
    }

    private void changeTurn(){
        currentPlayer += order;

        if(currentPlayer == -1) currentPlayer = players.length - 1;
        else if(currentPlayer == players.length) currentPlayer = 0;

        mView.changeTurnText(currentPlayer);
    }

    public void userTurn(int id){
        if(currentPlayer != 0) return;

        Card c = players[0].turn(deck.fetchCard(id));

        if(c == null) return;

        changeCurrentCard(c);
        checkForWin();
        action(c);
    }

    public void userDrawCard(){
        if(currentPlayer != 0) return;
        players[0].drawCard();
        changeTurn();
        play();
    }

    public void changeColour(Colour c){
        mView.changeColour(c);
    }

    public void wildCard(Colour c){
        mView.changeColour(c);
        changeTurn();

        for(int i = 0; i < 4; i++){
            players[currentPlayer].drawCard();
        }
    }
}
