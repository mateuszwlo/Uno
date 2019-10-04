package com.mateusz.uno;

import java.util.ArrayList;
import java.util.Random;

public class Deck {

    private ArrayList<Card> deck;
    private Random random;

    public Deck() {
        this.deck = new ArrayList<>(0);
        initialiseDeck();

        random = new Random();
    }

    public Card getRandomCard(){

        if(deck.size() == 0){
            return new Card(-2, null, null);
        }

        //RED = 25/108 -> 1-9 = 2/25, 0 = 1/25
        //BLUE = 25/108
        //GREEN = 25/108
        //YELLOW = 25/108
        //BLACK = 8/108 -> Wild = 1/2, WildCard = 1/2

        int r = random.nextInt(deck.size());

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

        Card.Colour col;
        String value;

        //Get Colour
        int i = no / 25;

        switch(i){
            case 0:
                col = Card.Colour.RED;
                break;
            case 1:
                col = Card.Colour.YELLOW;
                break;
            case 2:
                col = Card.Colour.GREEN;
                break;
            case 3:
                col = Card.Colour.BLUE;
                break;
            case 4:
                col = Card.Colour.BLACK;
                break;

            default:
                col = Card.Colour.BLACK;
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

}
