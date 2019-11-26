package com.mateusz.uno.ui.internetmultiplayer;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.Deck;
import com.mateusz.uno.data.Player;

import java.util.ArrayList;

import static com.mateusz.uno.ui.internetmultiplayer.InternetGame.currentCard;
import static com.mateusz.uno.ui.internetmultiplayer.InternetGame.deck;

public class InternetPlayer implements Player {

    private ArrayList<Card> cards;
    private String name;
    private InternetGameMvpView mView;
    private String id;
    int index;

    public InternetPlayer(int index, String name, String id, InternetGameMvpView mView) {
        this.index = index;
        this.name = name;
        this.id = id;
        this.mView = mView;

        cards = new ArrayList<>(0);
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
        return false;
    }

    @Override
    public void drawCard() {
        Card c =  deck.getRandomCard();
        cards.add(c);
        mView.addCardView(index, c);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void changeColour() {

    }

    @Override
    public void wildCard() {

    }
}
