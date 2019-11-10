package com.mateusz.uno.data;

import android.os.Handler;

import com.mateusz.uno.ui.singleplayer.SinglePlayerMvpView;

import java.util.ArrayList;
import java.util.Random;

import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerActivity.game;
import com.mateusz.uno.data.Card.Colour;

public class AIPlayer implements Player {

    private ArrayList<Card> cards;
    private String name;
    private int turnTime = 1000;
    private SinglePlayerMvpView mView;
    private int id;

    public AIPlayer(int id, String name, SinglePlayerMvpView singlePlayerMvpView) {
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
        mView.adjustPlayerCardViews(id, cards.size());
    }

    @Override
    public void turn(final Card c) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
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

                mView.adjustPlayerCardViews(id, cards.size());
                game.turn(playCard);
            }
        }, turnTime);
    }

    @Override
    public boolean hasUno() {
        return (cards.size() == 0);
    }

    @Override
    public void changeColour() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Colour col = null;

                Random r = new Random();
                switch (r.nextInt(4)) {
                    case 0:
                        col =  Colour.RED;
                        break;
                    case 1:
                        col =  Colour.YELLOW;
                        break;
                    case 2:
                        col =  Colour.GREEN;
                        break;
                    case 3:
                    default:
                        col =  Colour.BLUE;
                        break;
                }

                game.changeColour(col);
            }
        }, turnTime);
    }

    @Override
    public void wildCard() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Colour col = null;

                Random r = new Random();
                switch (r.nextInt(4)) {
                    case 0:
                        col =  Colour.RED;
                        break;
                    case 1:
                        col =  Colour.YELLOW;
                        break;
                    case 2:
                        col =  Colour.GREEN;
                        break;
                    case 3:
                    default:
                        col =  Colour.BLUE;
                        break;
                }

                game.wildCard(col);
            }
        }, turnTime);
    }

    @Override
    public String getName() {
        return name;
    }
}
