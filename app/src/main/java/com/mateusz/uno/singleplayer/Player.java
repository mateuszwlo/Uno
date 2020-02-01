package com.mateusz.uno.singleplayer;

import com.mateusz.uno.data.Card;

public interface Player {

    boolean turn(Card c);
    boolean hasUno();
    void drawCard();
    String getName();
    void changeColour();
    void wildCard();
    void willStackWild();
    void willStack();
    void removeCard(Card c);
}
