package com.mateusz.uno.data;

public interface Player {

    void turn(Card c);
    boolean hasUno();
    void drawCard();
    String getName();
    void changeColour();
    void wildCard();
}
