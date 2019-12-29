package com.mateusz.uno.localmultiplayer;

import com.mateusz.uno.data.UserData;

public interface LocalGameMvpView {
    //User cards
    void addCardView(int player, int id);
    void removeCardView(int player, int id);
    int getPlayerCardCount(int player);

    //Game
    void changeCurrentCardView(int id);
    void changeTurnText(String player);
    void setupPlayerData(int player, UserData data);
    void setupPlayerName(int player, String name);
    void setupPlayerPhoto(int player, int photoId);

    //Player cards
    int getAvatarResource(int id);

    //Dialogs
    void showPlayerWinDialog(String player);
    void gameDrawDialog();
    void showColourPickerDialog();
    void showWildCardColourPickerDialog();
    void hideLoadingGameDialog();
}
