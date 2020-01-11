package com.mateusz.uno.data;

import java.util.ArrayList;
import java.util.Random;
import com.mateusz.uno.data.Card.Colour;

public class Deck {

    private ArrayList<Card> deck;

    public Deck() {
        this.deck = new ArrayList<>(0);
        initialiseDeck();
    }

    public Card getRandomCard(){

        if(deck.size() == 0){
            return new Card(-2, null, null);
        }

        int r = new Random().nextInt(deck.size());

        Card c  = deck.get(r);
        deck.remove(r);

        return c;
    }

    private void initialiseDeck(){

        for(int i = 0; i < 108; i++){
            deck.add(fetchCard(i));
        }
    }

    public Card fetchCard(int no){
        Colour col;
        String value;

        switch (no){
            case 108:
                return new Card(no, Colour.BLACK, "BACK");
            case 109:
                return new Card(no, Colour.RED, "SOLID");
            case 110:
                return new Card(no, Colour.YELLOW, "SOLID");
            case 111:
                return new Card(no, Colour.GREEN, "SOLID");
            case 112:
                return new Card(no, Colour.BLUE, "SOLID");
        }

        //Get Colour
        int i = no / 25;

        switch(i){
            case 0:
                col = Colour.RED;
                break;
            case 1:
                col = Colour.YELLOW;
                break;
            case 2:
                col = Colour.GREEN;
                break;
            case 3:
                col = Colour.BLUE;
                break;
            case 4:
            default:
                col = Colour.BLACK;
                break;
        }

        //Get Value
        if(i == 4){
            if(no < 104){
                value = "wild";
            }
            else{
                value = "wildcard";
            }
        }
        else{

            int j = no % 25;

            if(j < 10){
                value = String.valueOf(j);
            }
            else if(j < 19){
                value = String.valueOf(j - 9);
            }
            else if(j < 21){
                value = "skip";
            }
            else if(j < 23){
                value = "reverse";
            }
            else{
                value = "plus2";
            }
        }

        return new Card(no, col, value);
    }

    public void insertCard(Card c){
        deck.add(c);
    }

    public Card drawFirstCard(){
        Card c = getRandomCard();

        while (c.getValue().length() > 1) {
            insertCard(c);
            c = getRandomCard();
        }
        return c;
    }

    public void removeCard(Card c){
        if(deck.contains(c)) deck.remove(c);
    }

    public boolean contains(Card c){
        return deck.contains(c);
    }
}
