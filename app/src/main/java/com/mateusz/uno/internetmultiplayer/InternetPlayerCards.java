package com.mateusz.uno.internetmultiplayer;

import java.util.ArrayList;

public class InternetPlayerCards {
    private ArrayList<Integer> cards;

    public InternetPlayerCards(){
        cards = new ArrayList<>(0);
    }

    public ArrayList<Integer> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Integer> cards) {
        this.cards = cards;
    }
}
