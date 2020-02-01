package com.mateusz.uno.singleplayer;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;

import static com.mateusz.uno.singleplayer.SinglePlayerGame.currentCard;
import static com.mateusz.uno.singleplayer.SinglePlayerGameActivity.game;

public class User implements Player {

    private String name;
    private SinglePlayerMvpView mView;
    private Deck deck;

    public User(Deck deck, String name, SinglePlayerMvpView singlePlayerMvpView) {
        this.deck = deck;
        this.name = name;
        this.mView = singlePlayerMvpView;
    }

    @Override
    public boolean turn(Card c) {
        if (c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Card.Colour.BLACK)) {
            mView.removeCardView(0, c);
            return true;
        }

        return false;
    }

    @Override
    public boolean hasUno() {
        return (mView.getPlayer1CardCount() == 1);
    }

    @Override
    public void drawCard() {
        Card c = deck.getRandomCard();

        if (c.getId() == -2) {
            mView.gameDrawDialog();
            return;
        }
        mView.addCardView(0, c);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void changeColour() {
        mView.showColourPickerDialog();
    }

    @Override
    public void wildCard() {
        mView.showWildCardColourPickerDialog();
    }

    @Override
    public void willStackWild() {
        mView.canUserStackWild();
    }

    @Override
    public void willStack(){
        mView.canUserStack();
    }

    @Override
    public void removeCard(Card c){
        mView.removeCardView(0, c);
    }
}
