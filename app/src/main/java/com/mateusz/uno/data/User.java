package com.mateusz.uno.data;

import com.mateusz.uno.ui.singleplayer.MvpView;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.currentCard;

public class User implements Player {

    private String name;
    private MvpView mView;

    public User(String name, MvpView mvpView) {
        this.name = name;
        this.mView = mvpView;
    }

    @Override
    public Card turn(Card c) {
        if (c.getColour().equals(currentCard.getColour()) || c.getValue().equals(currentCard.getValue()) || c.getColour().equals(Colour.BLACK)) {
            mView.removeCardView(c.getId());
            return c;
        }
        return null;
    }

    @Override
    public boolean hasUno() {
        return (mView.getPlayer1CardCount() == 0);
    }

    @Override
    public void drawCard() {
        Card c = deck.getRandomCard();

        if (c.getId() == -2) {
            mView.gameDrawDialog();
            return;
        }
        mView.player1AddCardView(c);
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
        mView.showColourPickerDialog();
    }
}
