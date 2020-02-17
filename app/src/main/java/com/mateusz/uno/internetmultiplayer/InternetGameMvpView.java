package com.mateusz.uno.internetmultiplayer;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.UserData;

import java.util.List;

public interface InternetGameMvpView {
    //User cards
    void addCardView(int player, int id);
    void removeCardView(int player, int id);
    void updateCardViews(int player, List<Integer> cards);
    int getPlayerCardCount(int player);
    void canUserStack();
    void canUserStackWild();

    //Game
    void changeCurrentCardView(int id);
    void changeTurnText(String player);
    void setupPlayerData(int player, UserData data);

    //Player cards
    int getAvatarResource(int id);
    boolean isLoaded();

    //Dialogs
    void showPlayerWinDialog(String player);
    void gameDrawDialog();
    void showColourPickerDialog();
    void showWildCardColourPickerDialog();
    void hideLoadingGameDialog();
}
