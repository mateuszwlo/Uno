package com.mateusz.uno.data;

import com.mateusz.uno.ui.singleplayer.SinglePlayerMvpView;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.currentCard;

public class User implements Player {

    private String name;
    private SinglePlayerMvpView mView;

    public User(String name, SinglePlayerMvpView singlePlayerMvpView) {
        this.name = name;
        this.mView = singlePlayerMvpView;
    }

    @Override
    public void turn(Card c) {
        if (c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Card.Colour.BLACK)) {
            mView.removeCardView(0, c);
        }
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
}
