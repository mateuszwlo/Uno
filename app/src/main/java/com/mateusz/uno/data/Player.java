package com.mateusz.uno.data;

public interface Player {

    boolean turn(Card c);
    boolean hasUno();
    void drawCard();
    String getName();
    void changeColour();
    void wildCard();
}
