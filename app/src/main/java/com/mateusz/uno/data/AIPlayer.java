package com.mateusz.uno.data;

import android.os.Handler;

import com.mateusz.uno.ui.singleplayer.SinglePlayerMvpView;
import java.util.ArrayList;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGameActivity.game;
import com.mateusz.uno.data.Card.Colour;

public class AIPlayer implements Player {

    private ArrayList<Card> cards;
    private String name;
    private int turnTime = 1000;
    private SinglePlayerMvpView mView;
    private int id;
    private Deck deck;

    public AIPlayer(Deck deck, int id, String name, SinglePlayerMvpView singlePlayerMvpView) {
        this.deck = deck;
        this.id = id;
        this.name = name;
        this.mView = singlePlayerMvpView;
        cards = new ArrayList<>(0);
    }

    @Override
    public void drawCard() {

        Card c = deck.getRandomCard();
        if (c.getId() == -2) return;

        cards.add(c);
        mView.addCardView(id, c);
    }

    @Override
    public boolean turn(final Card c) {
        Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Card playCard = null;

                for (int i = 0; i < cards.size(); i++) {
                    Card ci = cards.get(i);

                    if (ci.getColour().equals(c.getColour()) || ci.getValue().equals(c.getValue()) || ci.getColour().equals(Colour.BLACK)) {
                        cards.remove(i);
                        playCard = ci;
                        break;
                    }
                }

                if(playCard != null) mView.removeCardView(id, playCard);
                game.turn(playCard);
            }
        };
        handler.postDelayed(r, turnTime);
        return true;
    }

    @Override
    public boolean hasUno() {
        return (cards.size() == 0);
    }

    @Override
    public void changeColour() {

        Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                Colour col = Colour.BLUE;

                for(int i = 0; i < cards.size(); i++){
                    if(cards.get(i).getColour() != Colour.BLACK){
                        col = cards.get(i).getColour();
                        break;
                    }
                }

                game.changeColour(col);
            }
        };

        handler.postDelayed(r, turnTime);
    }

    @Override
    public void wildCard() {

        Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                Colour col = Colour.BLUE;

                for(int i = 0; i < cards.size(); i++){
                    if(cards.get(i).getColour() != Colour.BLACK){
                        col = cards.get(i).getColour();
                        break;
                    }
                }

                game.wildCard(col);
            }
        };

        handler.postDelayed(r, turnTime);
    }

    @Override
    public String getName() {
        return name;
    }
}
