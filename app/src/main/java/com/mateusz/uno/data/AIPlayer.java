package com.mateusz.uno.data;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.mateusz.uno.ui.singleplayer.MvpView;

import java.util.ArrayList;
import java.util.Random;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerGame.deck;
import static com.mateusz.uno.ui.singleplayer.SinglePlayerActivity.game;

public class AIPlayer implements Player {

    private ArrayList<Card> cards;
    private String name;
    private int turnTime = 1000;
    private MvpView mView;
    private int id;

    public AIPlayer(int id, String name, MvpView mvpView) {
        this.id = id;
        this.name = name;
        this.mView = mvpView;
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
    public Card turn(final Card c) {
        GetCard task = new GetCard();
        task.execute(c);

        return null;
    }

    @Override
    public boolean hasUno() {
        return (cards.size() == 0);
    }

    @Override
    public void changeColour() {
        ChooseColour chooseColour = new ChooseColour();
        chooseColour.execute();
    }

    @Override
    public void wildCard() {
        WildCard wildCard = new WildCard();
        wildCard.execute();
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCard extends AsyncTask<Card, Void, Card> {

        @Override
        protected Card doInBackground(Card... ccards) {
            Card playCard = null;

            for (int i = 0; i < cards.size(); i++) {
                Card ci = cards.get(i);

                if (ci.getColour().equals(ccards[0].getColour()) || ci.getValue().equals(ccards[0].getValue()) || ci.getColour().equals(Colour.BLACK)) {
                    cards.remove(i);
                    playCard = ci;
                    break;
                }
            }

            try {
                Thread.sleep(turnTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (playCard == null) {
                return new Card(-1, Colour.BLACK, "");
            } else {
                return playCard;
            }
        }

        @Override
        protected void onPostExecute(Card c) {
            mView.adjustPlayerCardViews(id, cards.size());
            game.playTurn(c);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ChooseColour extends AsyncTask<Colour, Void, Colour> {

        @Override
        protected Colour doInBackground(Colour... cols) {

            try {
                Thread.sleep(turnTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Random r = new Random();
            switch (r.nextInt(4)) {
                case 0:
                    return Colour.RED;
                case 1:
                    return Colour.YELLOW;
                case 2:
                    return Colour.GREEN;
                case 3:
                default:
                    return Colour.BLUE;
            }
        }

        @Override
        protected void onPostExecute(Colour c) {
            mView.adjustPlayerCardViews(id, cards.size());
            game.changeColour(c);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class WildCard extends AsyncTask<Colour, Void, Colour> {

        @Override
        protected Colour doInBackground(Colour... cols) {

            try {
                Thread.sleep(turnTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Random r = new Random();
            switch (r.nextInt(4)) {
                case 0:
                    return Colour.RED;
                case 1:
                    return Colour.YELLOW;
                case 2:
                    return Colour.GREEN;
                case 3:
                default:
                    return Colour.BLUE;
            }
        }

        @Override
        protected void onPostExecute(Colour c) {
            mView.adjustPlayerCardViews(id, cards.size());
            game.wildCard(c);
        }
    }
}
