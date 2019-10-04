package com.mateusz.uno;

import java.util.ArrayList;

import static com.mateusz.uno.MainActivity.deck;

public class AIPlayer {

    private ArrayList<Card> cards;

    public AIPlayer() {
        cards = new ArrayList<>(0);

        //Draw 7 Cards
        for(int i = 0; i < 7; i++){
            drawCard();
        }
    }

    public void drawCard(){

        Card c = deck.getRandomCard();
        if(c.getId() == -2) return;

        cards.add(c);
    }

    public Card playCard(Card c){

        for(int i = 0; i < cards.size(); i++){

            Card ci = cards.get(i);

            if(ci.getColour().equals(c.getColour()) || ci.getValue().equals(c.getValue()) || ci.getColour().equals(Card.Colour.BLACK)){
                c = cards.get(i);
                cards.remove(i);
                return c;
            }
        }

        drawCard();
        return new Card(-1, null, null);
    }

    public boolean hasUno(){
        return (cards.size() == 0);
    }

}
